package com.honeywell.firemanlocate.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.honeywell.firemanlocate.R;
import com.honeywell.firemanlocate.model.DataType;
import com.honeywell.firemanlocate.model.DistanceMap;
import com.honeywell.firemanlocate.model.FiremanPosition;
import com.honeywell.firemanlocate.model.IPackage;
import com.honeywell.firemanlocate.model.Report;
import com.honeywell.firemanlocate.model.TimeACK;
import com.honeywell.firemanlocate.model.TimeSync;
import com.honeywell.firemanlocate.service.CalculatePositionService;
import com.honeywell.firemanlocate.util.MatrixUtil2;
import com.honeywell.firemanlocate.util.NetworkUtil;
import com.honeywell.firemanlocate.network.UDPClient;
import com.honeywell.firemanlocate.network.UDPServer;
import com.honeywell.firemanlocate.view.chart.ScatterChart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lynnliu on 7/17/15.
 */
public class ShowActivity extends Activity {

    private static final int SEND_RESULT = 11;
    private static final int SERVICE_RESULT = 12;
    public static final String send_parameter = "draw_pramater";
    public static final String UPDATE_DATA = "update_data";
    public static final String UPDATE_DRAWVIEW_ACTION = "draw_position_received_action";
    private Button mStartButton;
    private ScrollView mScrollView;
    private TextView mLogTextView;

    private TimeSync mTimeSync;
    private PackageGotReceiver mMessageReceiver;
    private List<IPackage> mReportList;
    private Button mDrawButton;

    private BroadcastReceiver mUpdateReceiver;

    private TreeMap mDistanceMap = new TreeMap(); //存位置关系和距离
    private ArrayList<FiremanPosition> mFiremanPositionArrayList = new ArrayList<>();
    private ArrayList<FiremanPosition> mLastFiremanPositionArrayList = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        mStartButton = (Button) findViewById(R.id.start_button);
        mScrollView = (ScrollView) findViewById(R.id.scroll_view);
        mLogTextView = (TextView) findViewById(R.id.msg_log_text);
        mReportList = new ArrayList<IPackage>();
        mDrawButton = (Button) findViewById(R.id.draw_position);
        Intent intent = new Intent(ShowActivity.this, CalculatePositionService.class);
        startService(intent);  //主入口启动数据接受service

    }

    @Override
    protected void onStart() {
        super.onStart();

//        //服务器给到的广播
        mMessageReceiver = new PackageGotReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PackageGotReceiver.MSG_RECEIVED_ACTION);
        registerReceiver(mMessageReceiver, intentFilter);
        //service 给到的广播
//        mUpdateReceiver = new UpdateViewReceiver();
//        IntentFilter intentFilter2 = new IntentFilter();
//        intentFilter2.addAction(UPDATE_DRAWVIEW_ACTION);
//        registerReceiver(mUpdateReceiver, intentFilter2);

        ExecutorService exec = Executors.newCachedThreadPool();
        UDPServer server = new UDPServer(this);
        exec.execute(server);

//        mStartButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
        new Thread() {

            @Override
            public void run() {
                mTimeSync = new TimeSync();
                UDPClient sender = new UDPClient(NetworkUtil.getIPAddress(ShowActivity
                        .this), mTimeSync.getDataArray(), TimeSync.DATA_LENGTH);
                if (!mReportList.isEmpty()) mReportList.clear();

                Message msg = new Message();
                msg.what = SEND_RESULT;
                msg.obj = sender.send();
                mHandler.sendMessage(msg);
            }

        }.start();
//            }
//        });
        mDrawButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DistanceMap.setDistanceMap(mDistanceMap);
                Intent intent = (new ScatterChart(mFiremanPositionArrayList, mLastFiremanPositionArrayList,mDistanceMap)).execute(ShowActivity.this);
                startActivity(intent);
                finish();
            }
        });


    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEND_RESULT:
                    mLogTextView.setText(msg.obj.toString() + mTimeSync.getPrintableString());
                    break;
                case SERVICE_RESULT:
                    new MyThread().start();
                    break;
                default:
                    break;
            }
        }
    };

    public class MyThread extends Thread {
        public void run() {
            mFiremanPositionArrayList = MatrixUtil2.calculatePointsPosition(mDistanceMap, mLastFiremanPositionArrayList);
            mLastFiremanPositionArrayList = MatrixUtil2.saveFiremanPositionHistory(mFiremanPositionArrayList);
        }
    }


    //服务器给到的广播
    public class PackageGotReceiver extends BroadcastReceiver {
        public static final String MSG_RECEIVED_ACTION = "msg_received_action";

        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] msgReceived = intent.getByteArrayExtra(UDPServer.UDP_MSG_RECEIVED);
            if (msgReceived == null || msgReceived.length == 0)
                return;
            IPackage iPackage = null;
            switch (DataType.values()[msgReceived[0] - 1]) {
                case TIME_ACK:
                    iPackage = new TimeACK(msgReceived);
                    break;
                case REPORT:
                    iPackage = new Report(msgReceived);
                    mReportList.add(iPackage);
                    break;
                default:
                    break;
            }
            if (iPackage != null) {
                mLogTextView.setText(mLogTextView.getText() + iPackage.getPrintableString());
            }
            mScrollView.post(new Runnable() {
                public void run() {
                    mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        }
    }

    //service给到的广播
    private class UpdateViewReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("hellowlrd", "UpdateReceiver");
            String action = intent.getAction();

            Log.i("hellowlrd", "action:" + action);
            if (UPDATE_DRAWVIEW_ACTION.equals(action)) {
                List<Report> mReportList = (ArrayList<Report>) intent.getSerializableExtra(UPDATE_DATA);
                Log.i("hellowlrd", "mReportList.size():" + mReportList.size());
                if (mReportList != null) {
                    Message msg = new Message();
                    msg.what = SERVICE_RESULT;
                    msg.obj = MatrixUtil2.parseList(mReportList, mDistanceMap);
                    mHandler.sendMessage(msg);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mMessageReceiver);
        unregisterReceiver(mUpdateReceiver);
    }
}
