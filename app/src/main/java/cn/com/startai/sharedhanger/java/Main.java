package cn.com.startai.sharedhanger.java;

/**
 * author Guoqiang_Sun
 * date 2019/5/24
 * desc
 */
public class Main {
    public static void main(String[] args) {

        long num = 0x0000569821122363L;
        byte[] buf = long2Bytes(num);
        System.out.println("num:" + num);

        long fromHigh = ((buf[0] & 0xFF) << 24) | ((buf[1] & 0xFF) << 16) | ((buf[2] & 0xFF) << 8) | (buf[3] & 0xFF);
        System.out.println("fromHigh Hex:" + Long.toHexString(fromHigh));

        long from = ((buf[4] & 0xFF) << 24) | ((buf[5] & 0xFF) << 16) | ((buf[6] & 0xFF) << 8) | (buf[7] & 0xFF);
        System.out.println("fromHigh Hex:" + Long.toHexString(from));

        from = ((fromHigh) << 32) | from;
        System.out.println("from Hex:" + Long.toHexString(from));
        System.out.println("from:" + from);
    }


    public static byte[] long2Bytes(long num) {
        byte[] byteNum = new byte[8];
        for (int ix = 0; ix < 8; ++ix) {
            int offset = 64 - (ix + 1) * 8;
            byteNum[ix] = (byte) ((num >> offset) & 0xff);
        }
        return byteNum;
    }
}
