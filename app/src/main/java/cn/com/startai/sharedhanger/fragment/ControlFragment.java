package cn.com.startai.sharedhanger.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import cn.com.startai.sharedhanger.BleConActivity;
import cn.com.startai.sharedhanger.IBleData;
import cn.com.startai.sharedhanger.R;
import cn.com.swain.baselib.jsInterface.IotContent.BusinessContent;
import cn.com.swain.baselib.jsInterface.IotContent.ControlContent;
import cn.com.swain.baselib.log.Tlog;
import cn.com.swain.baselib.util.CrcUtil;
import cn.com.swain.baselib.util.StrUtil;
import cn.com.swain.support.ble.scan.ScanBle;

/**
 * author Guoqiang_Sun
 * date 2019/5/24
 * desc
 */
public class ControlFragment extends BaseFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Tlog.v(TAG, " ControlFragment onCreate() ");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Tlog.v(TAG, " ControlFragment onCreate() ");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Tlog.v(TAG, " ControlFragment onActivityCreated() ");
        super.onActivityCreated(savedInstanceState);


        BleConActivity activity = (BleConActivity) getActivity();
        if (activity != null) {
            item = activity.getScanBle();
            mSendData = activity;
        } else {
            Tlog.e(TAG, " onActivityCreated activity is null ");
        }
    }

    private ScanBle item;
    private IBleData mSendData;

    private byte model;
    Handler mUIHandler;
    private ToggleButton mToggleButton;

    @Override
    protected View inflateView() {
        View view = View.inflate(getActivity(), R.layout.framgment_ble_send,
                null);

        mToggleButton = view.findViewById(R.id.toggleButton);
        final TextView mTxtView = view.findViewById(R.id.txtNotify);

        final TextView mModelTxt = view.findViewById(R.id.model_txt);

        final TextView mCountdownTxt = view.findViewById(R.id.countdown_txt);

        final TextView mCountdownConfirm = view.findViewById(R.id.countdown_confirm);

        mUIHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (msg.what == 0) {
                    mTxtView.setText((String) msg.obj);
                } else if (msg.what == 1) {
                    mCountdownTxt.setText(String.valueOf(msg.obj));
                    mCountdownConfirm.setText(msg.arg1 == 1 ? "启动" : "结束");
                } else if (msg.what == 2) {
                    mModelTxt.setText(String.valueOf(model));
                }

            }
        };

        BleConActivity activity = (BleConActivity) getActivity();
        if (activity != null) {
            item = activity.getScanBle();
            mSendData = activity;
        } else {
            Toast.makeText(getContext(), " activity is null", Toast.LENGTH_SHORT).show();
        }

        final Button mSendModelBtn = view.findViewById(R.id.send_model_btn);
        mSendModelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "send model", Toast.LENGTH_SHORT).show();


//                0x01 0x01   // ver
//                0x00 0x00 0x5C 0xBF 0xFF 0xCF // ts
//                0x00 0x00 0xE5 0x96 0x24 0x79 // from
//                0x00 0x00 0xD7 0x82 0xCE 0x56 // to
//                0x29 0x63 // session
//                0x00 0x00 0x56 0xD9  //appid
//                0x80 0x18 // msgtw
//                0x00 0x09  // content length
//                0x00 0x03   // custom
//                0x00 0x03   // product
//                0x00 0x00 0x00 0x03   // cmd
//                0x00 // model


                byte[] buf = new byte[39];
                buf[0] = 0x00;
                buf[1] = 0x02;

                long l = System.currentTimeMillis();
                buf[2] = (byte) ((l >> 40) & 0xFF);
                buf[3] = (byte) ((l >> 32) & 0xFF);
                buf[4] = (byte) ((l >> 24) & 0xFF);
                buf[5] = (byte) ((l >> 16) & 0xFF);
                buf[6] = (byte) ((l >> 8) & 0xFF);
                buf[7] = (byte) ((l >> 0) & 0xFF);

                buf[8] = 0x00;  // from
                buf[9] = 0x01;
                buf[10] = 0x02;
                buf[11] = 0x03;
                buf[12] = 0x04;
                buf[13] = 0x05;

                String address = null;
                if (item != null) {
                    address = item.address;
                }
                if (address != null) {
                    String[] split = address.split(":");
                    for (int i = 0; i < split.length; i++) {
                        String s = split[i];
                        try {
                            buf[14 + i] = (byte) Integer.parseInt(s, 16);
                        } catch (Exception e) {
                            Tlog.e(TAG, " parseByte " + s, e);
                        }
                    }
                } else {
                    buf[14] = 0x00;  // to
                    buf[15] = 0x01;
                    buf[16] = 0x02;
                    buf[17] = 0x03;
                    buf[18] = 0x04;
                    buf[19] = 0x05;
                }

                buf[20] = 0x00; // session
                buf[21] = 0x00;

                buf[22] = 0x00; //appid
                buf[23] = 0x01;
                buf[24] = 0x02;
                buf[25] = 0x03;

                buf[26] = 0x00; // msgtw
                buf[27] = 0x00;

                buf[28] = 0x00; // length
                buf[29] = 0x09; // length

                buf[30] = 0x00; // custom
                buf[31] = 0x08;

                buf[32] = 0x00; // product
                buf[33] = 0x08;

                buf[34] = 0x11; // cmd
                buf[35] = 0x00;
                buf[36] = 0x00;
                buf[37] = 0x03;

                buf[38] = (byte) ((++model) % 0x03);

                sendBuf(buf);

            }
        });

        Button mReadModelBtn = view.findViewById(R.id.read_model_btn);
        mReadModelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "read model", Toast.LENGTH_SHORT).show();
                byte[] buf = new byte[38];
                buf[0] = 0x00;
                buf[1] = 0x02;

                long l = System.currentTimeMillis();
                buf[2] = (byte) ((l >> 40) & 0xFF);
                buf[3] = (byte) ((l >> 32) & 0xFF);
                buf[4] = (byte) ((l >> 24) & 0xFF);
                buf[5] = (byte) ((l >> 16) & 0xFF);
                buf[6] = (byte) ((l >> 8) & 0xFF);
                buf[7] = (byte) ((l >> 0) & 0xFF);

                buf[8] = 0x00;  // from
                buf[9] = 0x01;
                buf[10] = 0x02;
                buf[11] = 0x03;
                buf[12] = 0x04;
                buf[13] = 0x05;

                String address = null;
                if (item != null) {
                    address = item.address;
                }
                if (address != null) {
                    String[] split = address.split(":");
                    for (int i = 0; i < split.length; i++) {
                        String s = split[i];
                        try {
                            buf[14 + i] = (byte) Integer.parseInt(s, 16);
                        } catch (Exception e) {
                            Tlog.e(TAG, " parseByte " + s, e);
                        }
                    }
                } else {
                    buf[14] = 0x00;  // to
                    buf[15] = 0x01;
                    buf[16] = 0x02;
                    buf[17] = 0x03;
                    buf[18] = 0x04;
                    buf[19] = 0x05;
                }

                buf[20] = 0x00; // session
                buf[21] = 0x00;

                buf[22] = 0x00; //appid
                buf[23] = 0x01;
                buf[24] = 0x02;
                buf[25] = 0x03;

                buf[26] = 0x00; // msgtw
                buf[27] = 0x00;

                buf[28] = 0x00; // length
                buf[29] = 0x09; // length

                buf[30] = 0x00; // custom
                buf[31] = 0x08;

                buf[32] = 0x00; // product
                buf[33] = 0x08;

                buf[34] = 0x11; // cmd
                buf[35] = 0x00;
                buf[36] = 0x00;
                buf[37] = 0x04;

                sendBuf(buf);

            }
        });

        final EditText mCountdownEdt = view.findViewById(R.id.countdown_edt);

        Button mSendCountdownBtn = view.findViewById(R.id.send_countdown_btn);
        mSendCountdownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String s1 = mCountdownEdt.getText().toString();
                if ("".equals(s1)) {
                    Toast.makeText(getContext(), "please input", Toast.LENGTH_SHORT).show();
                    return;
                }
                int countdown;
                try {
                    countdown = Integer.parseInt(s1);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "please input integer", Toast.LENGTH_SHORT).show();
                    return;
                }

                byte[] buf = new byte[43];
                buf[0] = 0x00;
                buf[1] = 0x02;

                long l = System.currentTimeMillis();
                buf[2] = (byte) ((l >> 40) & 0xFF);
                buf[3] = (byte) ((l >> 32) & 0xFF);
                buf[4] = (byte) ((l >> 24) & 0xFF);
                buf[5] = (byte) ((l >> 16) & 0xFF);
                buf[6] = (byte) ((l >> 8) & 0xFF);
                buf[7] = (byte) ((l >> 0) & 0xFF);

                buf[8] = 0x00;  // from
                buf[9] = 0x01;
                buf[10] = 0x02;
                buf[11] = 0x03;
                buf[12] = 0x04;
                buf[13] = 0x05;

                String address = null;
                if (item != null) {
                    address = item.address;
                }
                if (address != null) {
                    String[] split = address.split(":");
                    for (int i = 0; i < split.length; i++) {
                        String s = split[i];
                        try {
                            buf[14 + i] = (byte) Integer.parseInt(s, 16);
                        } catch (Exception e) {
                            Tlog.e(TAG, " parseByte " + s, e);
                        }
                    }
                } else {
                    buf[14] = 0x00;  // to
                    buf[15] = 0x01;
                    buf[16] = 0x02;
                    buf[17] = 0x03;
                    buf[18] = 0x04;
                    buf[19] = 0x05;
                }

                buf[20] = 0x00; // session
                buf[21] = 0x00;

                buf[22] = 0x00; //appid
                buf[23] = 0x01;
                buf[24] = 0x02;
                buf[25] = 0x03;

                buf[26] = 0x00; // msgtw
                buf[27] = 0x00;

                buf[28] = 0x00; // length
                buf[29] = 0x09;

                buf[30] = 0x00; // custom
                buf[31] = 0x08;

                buf[32] = 0x00; // product
                buf[33] = 0x08;

                buf[34] = 0x11; // cmd
                buf[35] = 0x00;
                buf[36] = 0x00;
                buf[37] = 0x01;

                buf[38] = 0x01; // 启动

                buf[39] = (byte) ((countdown >> 24) & 0xFF); // 倒计时
                buf[40] = (byte) ((countdown >> 16) & 0xFF);
                buf[41] = (byte) ((countdown >> 8) & 0xFF);
                buf[42] = (byte) ((countdown >> 0) & 0xFF);

                sendBuf(buf);

                Toast.makeText(getContext(), "send countdown", Toast.LENGTH_SHORT).show();

            }
        });

        Button mReadCountdownBtn = view.findViewById(R.id.read_countdown_btn);
        mReadCountdownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                byte[] buf = new byte[38];
                buf[0] = 0x00;
                buf[1] = 0x02;

                long l = System.currentTimeMillis();
                buf[2] = (byte) ((l >> 40) & 0xFF);
                buf[3] = (byte) ((l >> 32) & 0xFF);
                buf[4] = (byte) ((l >> 24) & 0xFF);
                buf[5] = (byte) ((l >> 16) & 0xFF);
                buf[6] = (byte) ((l >> 8) & 0xFF);
                buf[7] = (byte) ((l >> 0) & 0xFF);

                buf[8] = 0x00;  // from
                buf[9] = 0x01;
                buf[10] = 0x02;
                buf[11] = 0x03;
                buf[12] = 0x04;
                buf[13] = 0x05;

                String address = null;
                if (item != null) {
                    address = item.address;
                }
                if (address != null) {
                    String[] split = address.split(":");
                    for (int i = 0; i < split.length; i++) {
                        String s = split[i];
                        try {
                            buf[14 + i] = (byte) Integer.parseInt(s, 16);
                        } catch (Exception e) {
                            Tlog.e(TAG, " parseByte " + s, e);
                        }
                    }
                } else {
                    buf[14] = 0x00;  // to
                    buf[15] = 0x01;
                    buf[16] = 0x02;
                    buf[17] = 0x03;
                    buf[18] = 0x04;
                    buf[19] = 0x05;
                }

                buf[20] = 0x00; // session
                buf[21] = 0x00;

                buf[22] = 0x00; //appid
                buf[23] = 0x01;
                buf[24] = 0x02;
                buf[25] = 0x03;

                buf[26] = 0x00; // msgtw
                buf[27] = 0x00;

                buf[28] = 0x00; // length
                buf[29] = 0x09; // length

                buf[30] = 0x00; // custom
                buf[31] = 0x08;

                buf[32] = 0x00; // product
                buf[33] = 0x08;

                buf[34] = 0x11; // cmd
                buf[35] = 0x00;
                buf[36] = 0x00;
                buf[37] = 0x02;

                sendBuf(buf);

                Toast.makeText(getContext(), "read countdown", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }


    private void sendBuf(byte[] buf) {
        if (mToggleButton.isChecked()) {
            sendBuf2(buf);
        } else {
            sendBuf1(buf);
        }
    }

    private void sendBuf2(byte[] buf) {
        int length = buf.length;
        byte crc8 = CrcUtil.CRC8(buf);

        byte[] onePkgBuf = new byte[length + HEAD_LENGTH];
        onePkgBuf[0] = 0x53;
        onePkgBuf[1] = 0x41;
        onePkgBuf[2] = 0x21;
        onePkgBuf[3] = 0x23;

        onePkgBuf[4] = (byte) (((++seq) >> 8) & 0xFF);
        onePkgBuf[5] = (byte) (seq & 0xFF);

        onePkgBuf[6] = (byte) ((length >> 8) & 0xFf);
        onePkgBuf[7] = (byte) (length & 0xFf);

        onePkgBuf[8] = (byte) ((length << 8) & 0xFf);
        onePkgBuf[9] = (byte) (length & 0xFf);

        onePkgBuf[10] = (byte) 1;
        onePkgBuf[11] = (byte) 0;

        onePkgBuf[12] = crc8;

        System.arraycopy(buf, 0, onePkgBuf, 13, length);
        Tlog.v(TAG, " sendBuf :" + StrUtil.toHexString(onePkgBuf));

        int maxL = MAX_SEND_BYTE;
        int onePkgLen = onePkgBuf.length;
        int i;
        int mo = onePkgLen % maxL;
        if (mo == 0) {
            i = onePkgLen / maxL;
        } else {
            i = onePkgLen / maxL + 1;
        }

        Tlog.v(TAG, " sendBuf,  i:" + i + " mo:" + mo + " onePkgLen:" + onePkgLen);

        for (int k = 0; k < i; k++) {
            byte[] sendBuf;
            if (k == i - 1) {
                int remainLength = onePkgLen - maxL * k;
                int p = onePkgLen - remainLength;
                sendBuf = new byte[remainLength];
                System.arraycopy(onePkgBuf, p, sendBuf, 0, remainLength);
            } else {
                sendBuf = new byte[maxL];
                System.arraycopy(onePkgBuf, k * maxL, sendBuf, 0, maxL);
            }
            if (mSendData != null) {
                Tlog.v(TAG, " ControlFragment sendData :" + StrUtil.toHexString(sendBuf));
                mSendData.sendData(sendBuf);
            }
        }


    }

    private final int MAX_SEND_BYTE = 17;

    private final int HEAD_LENGTH = 13;

    private int seq = 0;

    private void sendBuf1(byte[] buf) {

//        0x53 0x41 0x21 0x23
//// 包头   固定, 四个字节
//        0x00 0x01
//// 数据结构序号，两个字节，无符号
//        0x00 0x27
//// totalSize 数据结构包字节个数 ,两个字节 ,无符号
//        0x00 0x27
//// curSize 当前数据结构包字节个数,两个字节, 无符号
//        0x01
//// TotalPage  有些数据结构过长，分包发送的总页数,一个字节，无符号
//        0x01
//// curPage 有些数据结构过长，分包发送的当前页数,一个字节，无符号
//        0x01
//// CRC校验

        Tlog.v(TAG, " sendBuf :" + StrUtil.toHexString(buf));

        int length = buf.length;
        int maxL = (MAX_SEND_BYTE - HEAD_LENGTH);

        int i;
        int onePkgLen;

        if (length % maxL == 0) {
            i = length / maxL;
            onePkgLen = length / i;
        } else {
            i = length / maxL + 1;
            onePkgLen = length / i + 1;
        }


        Tlog.v(TAG, " sendBuf, length:" + length + " i:" + i + " onePkgLen:" + onePkgLen);

        byte crc8 = CrcUtil.CRC8(buf);

        for (int k = 0; k < i; k++) {
            if (k == i - 1) {
                onePkgLen = length - k * onePkgLen;
                Tlog.v(TAG, " end, onePkgLen:" + onePkgLen);
            }
            byte[] onePkgBuf = new byte[onePkgLen + HEAD_LENGTH];
            onePkgBuf[0] = 0x53;
            onePkgBuf[1] = 0x41;
            onePkgBuf[2] = 0x21;
            onePkgBuf[3] = 0x23;

            onePkgBuf[4] = (byte) (((++seq) << 8) & 0xFF);
            onePkgBuf[5] = (byte) (seq & 0xFF);

            onePkgBuf[6] = (byte) ((length << 8) & 0xFf);
            onePkgBuf[7] = (byte) (length & 0xFf);

            onePkgBuf[8] = (byte) ((onePkgLen << 8) & 0xFf);
            onePkgBuf[9] = (byte) (onePkgLen & 0xFf);

            onePkgBuf[10] = (byte) i;
            onePkgBuf[11] = (byte) k;

            onePkgBuf[12] = crc8;

            if (k == i - 1) {
                int p = length - onePkgLen;
                System.arraycopy(buf, p, onePkgBuf, 13, onePkgLen);
            } else {
                System.arraycopy(buf, k * onePkgLen, onePkgBuf, 13, onePkgLen);
            }

            if (mSendData != null) {
                Tlog.v(TAG, " sendData :" + StrUtil.toHexString(onePkgBuf));
                mSendData.sendData(onePkgBuf);
            }
        }

    }


    @Override
    public void onDestroyView() {
        Tlog.v(TAG, " ControlFragment onDestroyView() ");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Tlog.v(TAG, " ControlFragment onDestroy() ");
        super.onDestroy();
    }

    public void receiveOnePkgError(byte[] data, String remain) {
        if (mUIHandler != null) {
            mUIHandler.obtainMessage(0, remain + " " + StrUtil.toHexString(data)).sendToTarget();
        }

    }

    public void receiveOnePkg(ControlContent mControlContent, BusinessContent mBusinessContent, byte[] remainBuf, String remain) {
        if (mUIHandler != null) {
            mUIHandler.obtainMessage(0, String.valueOf(mControlContent) + String.valueOf(mBusinessContent) + remain).sendToTarget();
        }

        long cmd = mBusinessContent.getCmd();
        if (cmd == 0x11000004 || cmd == 0x11000003) {
            if (remainBuf != null && remainBuf.length >= 3) {
                model = remainBuf[2];
                mUIHandler.sendEmptyMessage(2);
            }
        } else if (cmd == 0x11000002 || cmd == 0x11000001) {
            if (remainBuf != null && remainBuf.length >= 7) {
                byte confirm = remainBuf[2];
                long coutdownTime = ((remainBuf[3] & 0xFF) << 24) | ((remainBuf[4] & 0xFF) << 16) |
                        ((remainBuf[5] & 0xFF) << 8) | (remainBuf[6] & 0xFF);
                if (mUIHandler != null) {
                    mUIHandler.obtainMessage(1, confirm, confirm, coutdownTime).sendToTarget();
                }
            }
        }

    }
}
