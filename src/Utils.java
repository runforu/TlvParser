

import java.util.Arrays;

public class Utils {

    // "2PAY.SYS.DDF01" --> {0x32,0x50,0x41,0x59,0x2e,0x53,0x59,0x53,0x2e,0x44,0x44,0x46,0x30,0x31}
    public static byte[] asciiToByteArray(String s) {
        byte[] result = new byte[s.length()];
        for (int i = 0; i < s.length(); i++) {
            result[i] = (byte) (s.charAt(i) & 0x0FF);
        }
        return result;
    }

    // "2PAY.SYS.DDF01" --> "325041592e5359532e4444463031"
    public static String asciiToHexString(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            sb.append(String.format("%02x", (int) s.charAt(i)));
        }
        return sb.toString();
    }

    // convert bytes to Hex string {0x5f,0x8f,0xef} --> "5F8FEF"
    public static String byteArrayToHexString(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return byteArrayToHexString(bytes, 0, bytes.length);
    }

    // convert bytes to Hex string {0x5f,0x8f,0xef} --> "5F8FEF"
    public static String byteArrayToHexString(byte[] bytes, int start, int count) {
        final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        char[] hexChars = new char[count * 2];
        int v;
        for (int j = 0; j < count; j++) {
            v = bytes[start + j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    // {0x32,0x50,0x41,0x59}, {0x2e,0x53,0x59,0x53,0x2e,0x44,0x44,0x46,0x30,0x31} --> {0x32,0x50,0x41,0x59,0x2e,0x53,0x59,0x53,0x2e,0x44,0x44,0x46,0x30,0x31}
    public static byte[] concatArrays(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            totalLength += array.length;
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    // "5F8FEF" --> {0x5f,0x8f,0xef}
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
}
