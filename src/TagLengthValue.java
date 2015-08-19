import java.util.ArrayList;
import java.util.List;

public class TagLengthValue {
    String mTag;
    String mValue;
    int mLen;

    private static String bytesToHex(byte[] bytes, int off, int len) {
        if (bytes == null) {
            return "";
        }
        StringBuffer buff = new StringBuffer();
        for (int j = off; j < off + len; j++) {
            if ((bytes[j] & 0xff) < 16) {
                buff.append('0');
            }
            buff.append(Integer.toHexString(bytes[j] & 0xff));
        }
        return buff.toString().toUpperCase();
    }

    // tlv uses big enddian
    private static int bytesToInt(byte[] bytes, int off, int len) {
        int rt = 0;
        for (int i = 0; i < len; i++) {
            rt += (bytes[off + len - i - 1] & 0X00FF) << (8 * i);
        }
        return rt;
    }

    public static byte[] hexStringToByteArray(String s) throws IllegalArgumentException {
        int len = s.length();
        if (len % 2 == 1) {
            throw new IllegalArgumentException("Hex string must have even number of characters");
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private static List<TagLengthValue> parseTlv(String tlv, boolean hasValue) {
        byte[] tlv_bytes = hexStringToByteArray(tlv);
        return parseTlv(tlv_bytes, 0, tlv_bytes.length, hasValue);
    }

    private static List<TagLengthValue> parseTlv(byte[] tlv, boolean hasValue) {
        return parseTlv(tlv, 0, tlv.length, hasValue);
    }

    private static int extractTlv(byte[] tlv, int offset, boolean hasValue, TagLengthValue tagLengthValue) {
        int pos = offset;
        int tag_len = (tlv[offset] & 0x1f) != 0x1f ? 1 : 2;
        tagLengthValue.mTag = bytesToHex(tlv, pos, tag_len);
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
            tagLengthValue.mValue = bytesToHex(tlv, pos, len);
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

    public static void main(String[] args) {
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
}
