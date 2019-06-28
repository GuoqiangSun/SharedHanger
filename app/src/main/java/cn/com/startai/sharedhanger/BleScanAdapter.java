package cn.com.startai.sharedhanger;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cn.com.swain.baselib.util.StrUtil;
import cn.com.swain.support.ble.scan.ScanBle;


public class BleScanAdapter extends BaseAdapter {

    private Context mContext;
    private final ArrayList<ScanBle> data = new ArrayList<ScanBle>();
    private LeSort mBleSort = new LeSort();
    private Handler mUIHandler;

    public BleScanAdapter(Context mContext) {

        this.mContext = mContext;
        this.mUIHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (msg.what == MSG_CLEAR_DATA) {
                    data.clear();
                    BleScanAdapter.this.notifyDataSetChanged();
                } else {
                    if (msg.obj == null) {
                        return;
                    }
                    ScanBle mScanBle = (ScanBle) msg.obj;
                    boolean add = true;
                    if (data.size() > 0) {
                        for (ScanBle ble : data) {
                            if (ble.address.equalsIgnoreCase(mScanBle.address)) {
                                add = false;
                                break;
                            }
                        }
                    }
                    if (add) {
                        data.add(mScanBle);
                    }
                    Collections.sort(data, mBleSort);
                    BleScanAdapter.this.notifyDataSetChanged();
                }

            }
        };

    }

    private static final int MSG_CLEAR_DATA = 0x00;

    public void clearData() {

        if (mUIHandler != null) {
            mUIHandler.sendEmptyMessage(MSG_CLEAR_DATA);
        }
    }

    private static final int MSG_OBTAIN_DATA = 0x01;

    public void onBleScan(ScanBle mScanBle) {
        if (mUIHandler != null) {
            mUIHandler.obtainMessage(MSG_OBTAIN_DATA, mScanBle).sendToTarget();
        }
    }

    private class LeSort implements Comparator<ScanBle> {

        @Override
        public int compare(ScanBle lhs, ScanBle rhs) {
            // TODO Auto-generated method stub

            if (lhs.rssi > rhs.rssi) {
                return -1;
            } else if (lhs.rssi < rhs.rssi) {
                return 1;
            }

            return 0;
        }
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return data.size();
    }

    @Override
    public ScanBle getItem(int position) {
        // TODO Auto-generated method stub
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub

        ViewHolder mHolder = null;

        if (convertView == null) {
            mHolder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_ble, null);
            mHolder.mNameTxt = (TextView) convertView.findViewById(R.id.name);
            mHolder.mAddressTxt = (TextView) convertView.findViewById(R.id.address);
            mHolder.mRssiTxt = (TextView) convertView.findViewById(R.id.rssi);
            mHolder.scanRecordTxt = (TextView) convertView.findViewById(R.id.scanRecord);
            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }

        ScanBle scanBle = data.get(position);

        if (scanBle != null) {
            mHolder.mNameTxt.setText(scanBle.name);
            mHolder.mAddressTxt.setText(scanBle.address);
            mHolder.mRssiTxt.setText(String.valueOf(scanBle.rssi));
            mHolder.scanRecordTxt.setText(StrUtil.toHexString(scanBle.getScanRecord()));
        }

        return convertView;
    }

    public static class ViewHolder {
        public TextView mNameTxt;
        public TextView mAddressTxt;
        public TextView mRssiTxt;
        public TextView scanRecordTxt;
    }
}
