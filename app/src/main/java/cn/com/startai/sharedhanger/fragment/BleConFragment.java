package cn.com.startai.sharedhanger.fragment;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import cn.com.startai.sharedhanger.BleConActivity;
import cn.com.startai.sharedhanger.IBleData;
import cn.com.startai.sharedhanger.LooperManager;
import cn.com.startai.sharedhanger.R;
import cn.com.swain.baselib.log.Tlog;
import cn.com.swain.baselib.util.StrUtil;
import cn.com.swain.support.ble.connect.AbsBleConnect;
import cn.com.swain.support.ble.connect.BleConnectEngine;
import cn.com.swain.support.ble.connect.IBleConCallBack;
import cn.com.swain.support.ble.scan.ScanBle;
import cn.com.swain.support.ble.send.AbsBleSend;
import cn.com.swain.support.ble.send.BleDataSendProduce;
import cn.com.swain.support.ble.send.SendDataQueue;

/**
 * author: Guoqiang_Sun
 * date : 2018/4/13 0013
 * desc :
 */

public class BleConFragment extends BaseFragment {

    @SuppressLint("HandlerLeak")
    private Handler h = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == 0) {
                String msgStr = (String) msg.obj;
                mTxtNotify.append(msgStr);
            } else if (msg.what == 1) {
                mTxtBleState.setText("连接成功");
            } else if (msg.what == 2) {
                mTxtBleState.setText("订阅成功");
            } else if (msg.what == 3) {
                mTxtBleState.setText("断开连接");
            } else if (msg.what == 4) {
                mTxtBleState.setText("未连接");
            } else if (msg.what == 5) {
                mTxtBleState.setText("连接失败");
            } else if (msg.what == 6) {
                mTxtBleState.setText("订阅失败");
            }

        }
    };

    private Context getApplicationContext() {
        if (mContext != null) {
            return mContext;
        }
        Context context = getContext();
        if (context != null) {
            mContext = context;
        }
        return mContext;

    }


    /**
     * {@link BleConnectEngine}
     */
    private AbsBleConnect mBleCon;
    private ArrayList<AbsBleSend> absBleSends;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tlog.v(TAG, " BleConFragment onCreate() ");
    }

    private IBleData mBleData;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Tlog.v(TAG, " BleConFragment onActivityCreated() ");
        BleConActivity activity = (BleConActivity) getActivity();
        if (activity != null) {
            item = activity.getScanBle();
            mBleData = activity;
        } else {
            Tlog.e(TAG, " onActivityCreated activity is null ");
        }
    }

    private ScanBle item;

    private View findViewById(int id) {
        return vw.findViewById(id);
    }

    private View vw;

    private Context mContext;

    @Override
    protected View inflateView() {
        Tlog.v(TAG, " BleConFragment inflateView() ");
        View view = View.inflate(getActivity(), R.layout.framgment_ble_con,
                null);
        vw = view;
        mContext = getContext();
        BleConActivity activity = (BleConActivity) getActivity();
        if (activity != null) {
            item = activity.getScanBle();
            mBleData = activity;
        } else {
            Toast.makeText(getApplicationContext(), " activity is null", Toast.LENGTH_SHORT).show();
        }

        initView();


        mBleCon = new BleConnectEngine(getApplicationContext(), LooperManager.getInstance().getWorkLooper(), new IBleConCallBack() {
            @Override
            public void onResultConnect(boolean result, ScanBle mItem) {

                if (result) {
                    Toast.makeText(getApplicationContext(), "connect success " + mItem.address, Toast.LENGTH_SHORT).show();
                    h.sendEmptyMessage(1);
                } else {
                    Toast.makeText(getApplicationContext(), "connect fail " + mItem.address, Toast.LENGTH_SHORT).show();
                    h.sendEmptyMessage(5);
                }

            }

            @Override
            public void onResultAlreadyConnected(ScanBle mItem) {
                Toast.makeText(getApplicationContext(), "already connected " + mItem.address, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResultDisconnectPassively(boolean result, ScanBle mItem) {
                style = null;
                if (result) {
                    h.sendEmptyMessage(3);
                    Toast.makeText(getApplicationContext(), "passively disconnect success " + mItem.address, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "passively disconnect fail " + mItem.address, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onResultDisconnectActively(ScanBle mItem) {
                style = null;
                h.sendEmptyMessage(3);
                Toast.makeText(getApplicationContext(), "actively disconnect success " + mItem.address, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResultServiceOrder(boolean result, ScanBle mItem, BluetoothGatt mConGatt) {

                if (result) {
                    h.sendEmptyMessage(2);
                    Toast.makeText(getApplicationContext(), "service order success " + mItem.address, Toast.LENGTH_SHORT).show();

                    style = BleDataSendProduce.showService(mConGatt);
                    absBleSends = BleDataSendProduce.produceBleSend(mConGatt);
                } else {
                    h.sendEmptyMessage(6);
                    Toast.makeText(getApplicationContext(), "service order fail " + mItem.address, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onPeripheralNotify(final String mac, String uuidStr, byte[] data) {

                String s = StrUtil.toString(data);
                Tlog.v(TAG, "onPeripheralNotify " + mac + ": " + uuidStr + ": " + s);
                h.obtainMessage(0, s).sendToTarget();

                if (mBleData != null) {
                    mBleData.receiveData(data);
                }

            }

            @Override
            public void onWriteDataFail(ScanBle mItem) {
                Toast.makeText(getApplicationContext(), "msg send fail " + ": " + mItem.address, Toast.LENGTH_SHORT).show();

            }
        }
        );

        if (item != null) {
            mBleCon.connect(item);

        } else {
            Toast.makeText(getApplicationContext(), "no ble device ", Toast.LENGTH_SHORT).show();
        }


        return view;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Tlog.v(TAG, " BleConFragment onCreateView() ");
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onDestroyView() {
        Tlog.v(TAG, " BleConFragment onDestroyView() ");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Tlog.v(TAG, " BleConFragment onDestroy() ");
        if (mBleCon != null && item != null) {
            mBleCon.disconnect(item);
        }
    }


    private TextView mTxtBleState;
    private EditText mEdtData;
    private TextView mTxtNotify;
    private TextView mTxtSend;


    private void initView() {

        Button mConBleBtn = (Button) findViewById(R.id.conBleBtn);
        mConBleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conBle(v);
            }
        });

        Button mDisonBleBtn = (Button) findViewById(R.id.disconBleBtn);
        mDisonBleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconBle(v);
            }
        });

        Button lookServiceBtn = (Button) findViewById(R.id.lookServiceBtn);
        lookServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lookService(v);
            }
        });

        Button mselectWriteServiceBtn = (Button) findViewById(R.id.selectWriteServiceBtn);
        mselectWriteServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectWriteService(v);
            }
        });

        Button msendDataBtn = (Button) findViewById(R.id.sendDataBtn);
        msendDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData(v);
            }
        });

        Button clearSendDataBtn = (Button) findViewById(R.id.clearSendDataBtn);
        clearSendDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSendData(v);
            }
        });

        Button mclearRecDataBtn = (Button) findViewById(R.id.clearRecDataBtn);
        mclearRecDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearRecData(v);
            }
        });

        TextView mTxtName = (TextView) findViewById(R.id.txtName);
        TextView mTxtAdd = (TextView) findViewById(R.id.txtAddress);
        TextView mTxtRssi = (TextView) findViewById(R.id.txtRssi);
        if (item != null) {
            mTxtName.setText(item.name);
            mTxtAdd.setText(item.address);
            mTxtRssi.setText("rssi:" + item.rssi);
        }

        mTxtBleState = (TextView) findViewById(R.id.txtBleState);
        mEdtData = (EditText) findViewById(R.id.edtData);
        mTxtNotify = (TextView) findViewById(R.id.txtNotify);
        mTxtSend = (TextView) findViewById(R.id.txtSend);

    }


    public void conBle(View v) {
        if (mBleCon != null) {
            if (item != null) {
                mBleCon.connect(item);
            } else {
                Toast.makeText(getApplicationContext(), "no ble device ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void disconBle(View v) {
        if (mBleCon != null) {
            mBleCon.disconnect(item);
        }
        mTxtBleState.setText("未连接");
        if (absBleSends != null) {
            absBleSends.clear();
            absBleSends = null;
        }
        if (mBleSend != null) {
            mBleSend.removeMsg();
            mBleSend.closeGatt();
            mBleSend = null;
        }
    }

    SpannableStringBuilder style = null;

    public void lookService(View v) {

        new AlertDialog.Builder(getApplicationContext())
                .setTitle("服务")// 设置对话框标题
                .setMessage(style != null ? style : "null").show();// 设置显示的内容

    }

    public void clearRecData(View v) {
        mTxtNotify.setText("");
    }

    public void clearSendData(View v) {
        mTxtSend.setText("");
    }

    private AbsBleSend mBleSend;

    public void selectWriteService(View v) {

        ArrayList<AbsBleSend> mAbsBleSends = absBleSends;

        if (mAbsBleSends == null) {
            Toast.makeText(getApplicationContext(), " service is not order ", Toast.LENGTH_SHORT).show();
            return;
        }

        final Object[] objects = mAbsBleSends.toArray();

        int size = mAbsBleSends.size();
        final String[] items = new String[size];
        for (int i = 0; i < size; i++) {
            AbsBleSend mAbsBleSend = mAbsBleSends.get(i);
            items[i] = mAbsBleSend.getUuidStr();
        }

//        final String items[] = {"刘德华", "张柏芝", "蔡依林", "张学友"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setTitle("写特征值:");
        // builder.setMessage("是否确认退出?"); //设置内容
        builder.setIcon(R.mipmap.ic_launcher);
        // 设置列表显示，注意设置了列表显示就不要设置builder.setMessage()了，否则列表不起作用。
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if (mBleSend != null) {
                    mBleSend.removeMsg();
                    mBleSend = null;
                }
                if (objects == null) {
                    Toast.makeText(getApplicationContext(), "objects==null",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                AbsBleSend tBleSend = (AbsBleSend) objects[which];
                mBleSend = new SendDataQueue(LooperManager.getInstance().getRepeatLooper(), tBleSend);

                Toast.makeText(getApplicationContext(), mBleSend.getUuidStr(),
                        Toast.LENGTH_SHORT).show();

            }
        });
        builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }


    public void sendData(View v) {

        String s = mEdtData.getText().toString();
        if ("".equals(s)) {
            Toast.makeText(getApplicationContext(), "没输入数据", Toast.LENGTH_LONG).show();
            return;
        }

//            byte[] bytes = s.getBytes();
        byte[] bytes;

        try {
            bytes = StrUtil.toHex(s);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "输入有误", Toast.LENGTH_LONG).show();
            return;
        }

        sendData(bytes);

    }

    public void sendData(byte[] bytes) {

        if (mBleSend != null) {
            if (bytes == null) {
                Toast.makeText(getApplicationContext(), "数据不能为空", Toast.LENGTH_LONG).show();
                return;
            }
            if (bytes.length > 17) {
                Toast.makeText(getApplicationContext(), "发送长度不可以超过17个字节", Toast.LENGTH_LONG).show();
                return;
            }
            mTxtSend.append(StrUtil.toString(bytes));
            mBleSend.sendData(bytes);
        } else {
            Toast.makeText(getApplicationContext(), "没选择要发送的特征值", Toast.LENGTH_LONG).show();
        }

    }


}
