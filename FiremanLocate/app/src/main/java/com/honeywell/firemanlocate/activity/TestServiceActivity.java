package com.honeywell.firemanlocate.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import com.honeywell.firemanlocate.R;
import com.honeywell.firemanlocate.model.FiremanPosition;
import com.honeywell.firemanlocate.model.Report;
import com.honeywell.firemanlocate.service.CalculatePositionService;
import com.honeywell.firemanlocate.util.MatrixUtil;
import com.honeywell.firemanlocate.util.MatrixUtil2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestServiceActivity extends Activity {
    private static final int SEND_RESULT = 11;
    private CalculatePositionService msgService;
    public static final String UPDATE_DATA = "update_data";
    public static final String UPDATE_DRAWVIEW_ACTION = "draw_position_received_action";
    private BroadcastReceiver mUpdateReceiver;
    private ArrayList<FiremanPosition> mFiremanPositionArrayList = new ArrayList<>();
    private ArrayList<FiremanPosition> mLastFiremanPositionArrayList = null;
    private Map mDistanceMap = new HashMap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_service);

        //绑定Service
        Intent intent = new Intent(TestServiceActivity.this, CalculatePositionService.class);
        startService(intent);
        mUpdateReceiver = new UpdateViewReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UPDATE_DRAWVIEW_ACTION);
        registerReceiver(mUpdateReceiver, intentFilter);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEND_RESULT:
                    mFiremanPositionArrayList = MatrixUtil2.calculatePointsPosition(mDistanceMap,mLastFiremanPositionArrayList);
                    mLastFiremanPositionArrayList = MatrixUtil2.saveFiremanPositionHistory(mFiremanPositionArrayList);
                    break;
                default:
                    break;
            }
        }
    };

    private class UpdateViewReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            List<Report> mReportList = (ArrayList<Report>) intent.getSerializableExtra(UPDATE_DATA);
            if (mReportList != null) {
                Message msg = new Message();
                msg.what = SEND_RESULT;
                msg.obj = MatrixUtil.parseList(mReportList,mDistanceMap);
                mHandler.sendMessage(msg);
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUpdateReceiver);
    }
}
