package cn.com.startai.sharedhanger;

import android.os.Message;

import java.util.Arrays;

import cn.com.swain.baselib.log.Tlog;
import cn.com.swain.baselib.util.StrUtil;

/**
 * author Guoqiang_Sun
 * date 2019/5/28
 * desc
 */
public class ParseUtils {

    private String TAG = BleConActivity.TAG;

    private ICallBackParse mCallBack;

    public ParseUtils(ICallBackParse mCallBack) {
        this.mCallBack = mCallBack;
    }

    private final int LENGTH = 1024;

    // 一包完整的数据
    private byte[] PKG = new byte[LENGTH];
    private int pkgPoint = 0;

    private void addPkg(byte b) {
        PKG[pkgPoint] = b;
        pkgPoint++;
    }

    private byte[] getCompletePkg() {
        byte[] bytes = new byte[pkgPoint];
        System.arraycopy(PKG, 0, bytes, 0, pkgPoint);
        return bytes;
    }

    private void addPkg(byte[] buf) {
        System.arraycopy(buf, 0, PKG, pkgPoint, buf.length);
        pkgPoint += buf.length;
    }

    private void addPkg(byte[] buf, int start, int length) {
        System.arraycopy(buf, start, PKG, pkgPoint, length);
        pkgPoint += length;
    }

    // 一包分包数据
    private byte[] DATA = new byte[LENGTH];
    private int dataPoint = 0;

    private void addData(byte b) {
        DATA[dataPoint] = b;
        dataPoint++;
    }

    private void fillData() {
        Arrays.fill(DATA, (byte) 0x00);
    }

    private void addDATA(byte[] buf) {
        System.arraycopy(buf, 0, DATA, dataPoint, buf.length);
        dataPoint += buf.length;
    }

    public void parseMsg(Message msg) {

        byte[] buf = (byte[]) msg.obj;
        Tlog.d(TAG, " handleProtocolMessage: " + StrUtil.toHexString(buf));

        addDATA(buf);

        if (dataPoint >= 4) {

            if (DATA[0] == 0x53 && DATA[1] == 0x41
                    && DATA[2] == 0x21 && DATA[3] == 0x23) {

                parse();

            } else {

                for (int i = 4; i < dataPoint; i++) {

                    int start = i;
                    final int s = start;

                    if (i > (100 - 4)) {

                        Tlog.e(TAG, "copy remain i > 96");

                        break;
                    }

                    if (DATA[start] == 0x53 && DATA[++start] == 0x41
                            && DATA[++start] == 0x21 && DATA[++start] == 0x23) {

                        Tlog.e(TAG, " old data:" + StrUtil.toHexString(DATA));

                        Tlog.e(TAG, "copy remain; receive heard point=" + s + " dataPoint:" + dataPoint);

                        int length = dataPoint - s;
                        System.arraycopy(DATA, s, DATA, 0, length);
                        dataPoint = length;

                        Tlog.e(TAG, " new data:" + StrUtil.toHexString(DATA));

                        parse();

                        break;

                    }

                }

            }


        }

    }

    private void parse() {

        if (dataPoint < 9) {
            Tlog.e(TAG, " receive heard dataPoint < 9;");
            return;
        }
        int curSize = (DATA[8] & 0xFF) << 8 | (DATA[9] & 0xFF);
        int totalSize = (DATA[6] & 0xFF) << 8 | (DATA[7] & 0xFF);

        Tlog.e(TAG, " receive heard ; totalSize:" + totalSize
                + " curSize:" + curSize
                + " dataPoint:" + dataPoint
                + " pkgPoint:" + pkgPoint);

        if ((curSize + 13) == dataPoint) {
            // 刚好一包
            addPkg(DATA, 13, curSize);

            dataPoint = 0;

            if (pkgPoint == totalSize) {

                byte[] completePkg = getCompletePkg();
                Tlog.e(TAG, " receive complete pkg1 " + StrUtil.toHexString(completePkg));
                pkgPoint = 0;
                mCallBack.parsePkg(completePkg);
            }

        } else if ((curSize + 13) < dataPoint) {
            // 可能两包在一起
            addPkg(DATA, 13, curSize);

            if (pkgPoint == totalSize) {

                byte[] completePkg = getCompletePkg();
                Tlog.e(TAG, " receive complete pkg2 " + StrUtil.toHexString(completePkg));
                pkgPoint = 0;
                mCallBack.parsePkg(completePkg);
            }

            int s = 13 + curSize;
            int length = dataPoint - s;
            System.arraycopy(DATA, s, DATA, 0, length);
            dataPoint = length;
        }

    }


    public interface ICallBackParse {
        void parsePkg(byte[] pkg);
    }


}
