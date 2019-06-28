package cn.com.startai.sharedhanger;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;

import cn.com.startai.sharedhanger.fragment.BleConFragment;
import cn.com.startai.sharedhanger.fragment.ControlFragment;
import cn.com.startai.sharedhanger.fragment.FragmentPagerAdapter;
import cn.com.swain.baselib.jsInterface.IotContent.BusinessContent;
import cn.com.swain.baselib.jsInterface.IotContent.ControlContent;
import cn.com.swain.baselib.log.Tlog;
import cn.com.swain.baselib.util.StrUtil;
import cn.com.swain.support.ble.scan.ScanBle;

/**
 * author: Guoqiang_Sun
 * date: 2018-03-13
 * description:
 */

public class BleConActivity extends AppCompatActivity implements IBleData {
    private ScanBle item;

    public ScanBle getScanBle() {
        return item;
    }

    BleConFragment mBleConFram;

    public static String TAG = "BleConActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ble_con_t);

        item = getIntent().getParcelableExtra("ble");

        ViewPager mFrameVp = findViewById(R.id.frame_viewpager);

        ArrayList<Fragment> mFragments = new ArrayList<>();

        mBleConFram = new BleConFragment();
        final ControlFragment controlFragment = new ControlFragment();

        mFragments.add(mBleConFram);
        mFragments.add(controlFragment);


        FragmentPagerAdapter mAdapter = new FragmentPagerAdapter(getSupportFragmentManager(), mFragments);
        mFrameVp.setAdapter(mAdapter);
        mFrameVp.setCurrentItem(0);
        mFrameVp.setOffscreenPageLimit(mFragments.size());

        final Handler mWorkHandler = new Handler(LooperManager.getInstance().getWorkLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                byte[] buf = (byte[]) msg.obj;
                Tlog.d(TAG, " handleWorkMessage: " + StrUtil.toHexString(buf));

                if (buf.length < 37) {
                    controlFragment.receiveOnePkgError(buf, " length not enough");
                    return;
                }

                ControlContent mControlContent = new ControlContent();
                mControlContent.setVer(((buf[0] & 0xFF) << 8) | (buf[1] & 0xFF));

                long tsHigh = ((buf[2] & 0xFF) << 8) | (buf[3] & 0xFF);
                long ts = ((buf[4] & 0xFF) << 24) | ((buf[5] & 0xFF) << 16) | ((buf[6] & 0xFF) << 8) | (buf[7] & 0xFF);
                ts = ((tsHigh & 0x0000FFFF) << 32) | ts;
                mControlContent.setTs(ts);

                long fromHigh = ((buf[8] & 0xFF) << 8) | (buf[9] & 0xFF);
                long from = ((buf[10] & 0xFF) << 24) | ((buf[11] & 0xFF) << 16) | ((buf[12] & 0xFF) << 8) | (buf[13] & 0xFF);
                from = ((fromHigh & 0x0000FFFF) << 32) | from;
                mControlContent.setFrom(from);

                long toHigh = ((buf[14] & 0xFF) << 8) | (buf[15] & 0xFF);
                long to = ((buf[16] & 0xFF) << 24) | ((buf[17] & 0xFF) << 16) | ((buf[18] & 0xFF) << 8) | (buf[19] & 0xFF);
                to = ((toHigh & 0x0000FFFF) << 32) | to;
                mControlContent.setTo(to);

                mControlContent.setSession(((buf[20] & 0xFF) << 8) | (buf[21] & 0xFF));

                long appid = ((buf[22] & 0xFF) << 24) | ((buf[23] & 0xFF) << 16) | ((buf[24] & 0xFF) << 8) | (buf[25] & 0xFF);
                mControlContent.setAppid(appid);

                mControlContent.setMsgtw(((buf[26] & 0xFF) << 8) | (buf[27] & 0xFF));

                BusinessContent mBusinessContent = new BusinessContent();
//                content  =buf[28] buf[29]
                mBusinessContent.setCustom(((buf[30] & 0xFF) << 8) | (buf[31] & 0xFF));
                mBusinessContent.setProduct(((buf[32] & 0xFF) << 8) | (buf[33] & 0xFF));
                mBusinessContent.setCmd(((buf[34] & 0xFF) << 24) | ((buf[35] & 0xFF) << 16) | ((buf[36] & 0xFF) << 8) | (buf[37] & 0xFF));

                String remainStr = "";
                byte[] remainBuf = null;
                if (buf.length > 38) {
                    int remainLength = buf.length - 38;
                    remainBuf = new byte[remainLength];
                    System.arraycopy(buf, 38, remainBuf, 0, remainLength);
                    remainStr = " remain:" + StrUtil.toHexString(remainBuf);
                }

                controlFragment.receiveOnePkg(mControlContent, mBusinessContent, remainBuf, remainStr);
            }
        };


        mProtocolHandler = new Handler(LooperManager.getInstance().getProtocolLooper()) {

            private ParseUtils mParseUtils = new ParseUtils(new ParseUtils.ICallBackParse() {
                @Override
                public void parsePkg(byte[] pkg) {
                    mWorkHandler.obtainMessage(0, pkg).sendToTarget();
                }
            });

            //0x53 0x41 0x21 0x23
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (msg.what == 0) {
                    Tlog.v(TAG, " parseMsg start ");
                    mParseUtils.parseMsg(msg);
                    Tlog.v(TAG, " parseMsg end ");
                }

            }
        };
    }

    private Handler mProtocolHandler;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void sendData(byte[] buf) {
        if (mBleConFram != null) {
            mBleConFram.sendData(buf);
        }

        // 测试用，自己解析自己发送的数据包，看是否能解析
//        receiveData(buf);
    }

    @Override
    public void receiveData(byte[] buf) {
        mProtocolHandler.obtainMessage(0, buf).sendToTarget();
    }


}
