

import java.util.ArrayList;
import java.util.List;

public class TagLengthValue {
    int mLen;
    String mTag;
    String mValue;

    public static TagLengthValue build(String tag, List<TagLengthValue> tlv) {
        return new TagLengthValue(tag, tlv);
    }

    public static TagLengthValue build(String tag, String value) {
        return new TagLengthValue(tag, value);
    }

    public static TagLengthValue find(List<TagLengthValue> tlvs, String tag) {
        for (TagLengthValue tlv : tlvs) {
            if (tag.equalsIgnoreCase(tlv.mTag)) {
                return tlv;
            }
        }
        return null;
    }

    public static TagLengthValue getTlv(byte[] tlv, byte[] tag) {
        String tagString = Utils.byteArrayToHexString(tag);
        return getTlv(tlv, tagString);
    }

    public static TagLengthValue getTlv(byte[] tlv, String tag) {
        List<TagLengthValue> list = parseTlv(tlv, 0, tlv.length, true);
        for (TagLengthValue e : list) {
            if (tag.equalsIgnoreCase(e.mTag)) {
                return e;
            }
        }
        return null;
    }

    public static TagLengthValue getTlv(String tlv, String tag) {
        byte[] tlvByte = Utils.hexStringToByteArray(tlv);
        return getTlv(tlvByte, tag);
    }

    public static void main(String[] args) {
        for (int i = 1; i < 0xFFFFFF; i <<= 1) {
            System.out.println("" + i + "   " + decodeLength(i));
        }
        String tlvString = "A539500A50424F432044454249548701019F38099F7A019F02065F2A025F2D027A689F1101019F120A50424F43204445424954BF0C059F4D020B0A";
        List<TagLengthValue> lengthValues = parseTlv(tlvString, true);
        for (TagLengthValue t : lengthValues) {
            System.out.println(t.mTag + "  " + t.mLen + " " + t.mValue);
        }
        tlvString = "9F7A81FF9F02065F2A02";
        lengthValues = parseTlv(tlvString, false);
        for (TagLengthValue t : lengthValues) {
            System.out.println(t.mTag + "  " + t.mLen + " " + t.mValue);
        }
    }

    public static List<TagLengthValue> parseTlv(byte[] tlv, boolean hasValue) {
        return parseTlv(tlv, 0, tlv.length, hasValue);
    }

    public static List<TagLengthValue> parseTlv(String tlv, boolean hasValue) {
        byte[] tlv_bytes = Utils.hexStringToByteArray(tlv);
        return parseTlv(tlv_bytes, 0, tlv_bytes.length, hasValue);
    }

    // tlv uses big enddian
    private static int bytesToInt(byte[] bytes, int off, int len) {
        int rt = 0;
        for (int i = 0; i < len; i++) {
            rt += (bytes[off + len - i - 1] & 0X00FF) << (8 * i);
        }
        return rt;
    }

    // tlv uses big enddian
    private static String decodeLength(int len) {
        if (len < 128) {
            return String.format("%02X", len);
        }
        StringBuilder sb = new StringBuilder();
        int c = 0;
        for (int l = len; l > 0; l >>= 8) {
            sb.insert(0, String.format("%02X", l & 0x0FF));
            c++;
        }
        sb.insert(0, String.format("%02X", c + 0x80));
        return sb.toString();
    }

    private static int extractTlv(byte[] tlv, int offset, boolean hasValue, TagLengthValue tagLengthValue) {
        int pos = offset;
        int tag_len = (tlv[offset] & 0x1f) != 0x1f ? 1 : 2;
        tagLengthValue.mTag = Utils.byteArrayToHexString(tlv, pos, tag_len);
        pos += tag_len;
        int len = tlv[pos] & 0X00FF;
        if (len < 128) {
            pos++;
        } else if (len >= 128) {
            int c = (len & 0x7F) & 0X00FF;
            pos++;
            len = bytesToInt(tlv, pos, c);
            pos += c;
        }
        tagLengthValue.mLen = len;
        if (hasValue) {
            tagLengthValue.mValue = Utils.byteArrayToHexString(tlv, pos, len);
            pos += len;
        }
        return pos - offset;
    }

    private static List<TagLengthValue> parseTlv(byte[] tlv, int offset, int length, boolean hasValue) {
        int pos = offset;
        List<TagLengthValue> list = new ArrayList<TagLengthValue>();
        while (pos < offset + length) {
            if ((tlv[pos] & 0x20) != 0x20) {
                TagLengthValue tagLengthValue = new TagLengthValue();
                int len = extractTlv(tlv, pos, hasValue, tagLengthValue);
                list.add(tagLengthValue);
                pos += len;
            } else {
                TagLengthValue tagLengthValue = new TagLengthValue();
                int len = extractTlv(tlv, pos, hasValue, tagLengthValue);
                list.add(tagLengthValue);
                List<TagLengthValue> subList = parseTlv(tlv, pos + len - tagLengthValue.mLen, tagLengthValue.mLen,
                        hasValue);
                list.addAll(subList);
                pos += len;
            }
        }
        return list;
    }

    private TagLengthValue() {
    }

    private TagLengthValue(String tag, List<TagLengthValue> values) throws IllegalArgumentException {
        if (tag.length() != 2 && tag.length() != 4) {
            throw new IllegalArgumentException("tag or value not correct.");
        }
        mTag = tag;
        StringBuilder sb = new StringBuilder();
        for (TagLengthValue tlv : values) {
            sb.append(tlv);
        }
        mValue = sb.toString();
        mLen = sb.length() / 2;
    }

    private TagLengthValue(String tag, String value) throws IllegalArgumentException {
        if (value.length() % 2 != 0 || (tag.length() != 2 && tag.length() != 4)) {
            throw new IllegalArgumentException("tag or value not correct.");
        }
        mTag = tag;
        mValue = value;
        mLen = value.length() / 2;
    }

    @Override
    public String toString() {
        return mTag + decodeLength(mLen) + mValue;
    }
}
