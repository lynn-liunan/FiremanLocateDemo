package com.example.lynnliu.senderdemo.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.lynnliu.senderdemo.R;
import com.example.lynnliu.senderdemo.model.DataType;
import com.example.lynnliu.senderdemo.model.IPackage;
import com.example.lynnliu.senderdemo.model.Report;
import com.example.lynnliu.senderdemo.model.TimeACK;
import com.example.lynnliu.senderdemo.model.TimeSync;
import com.example.lynnliu.senderdemo.network.UDPByteClient;
import com.example.lynnliu.senderdemo.network.UDPClient;
import com.example.lynnliu.senderdemo.network.UDPServer;
import com.example.lynnliu.senderdemo.util.NetworkUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lynnliu on 7/17/15.
 */
public class ShowActivity extends Activity {

    private static final int SEND_ACK = 11;
    private static final int SEND_REPORT = 12;

    private Button mSendACKButton;
    private Button mSendReportButton;
    private ScrollView mScrollView;
    private TextView mLogTextView;

    private TimeSync mTimeSync;
    private TimeACK mTimeACK;
    private Report mReport;
    private PackageGotReceiver mMessageReceiver;
    private byte[] mTimeACKBytes = {0x02, 0x00, 0x01, 0x10, 0x28, 0x7B, 0x0D, 0x01, (byte) 0x89, 0x02};
    private byte[] mReportBytes = {0x03, 0x01, 0x3B, 0x0B, 0x28, 0x7B, 0x0D, 0x01, 0x50, 0x03,
            0x02, 0x10, 0x03, 0x10, 0x02, 0x00, 0x00, 0x00};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        mSendACKButton = (Button) findViewById(R.id.send_ack_button);
        mSendReportButton = (Button) findViewById(R.id.send_report_button);
        mScrollView = (ScrollView) findViewById(R.id.scroll_view);
        mLogTextView = (TextView) findViewById(R.id.msg_log_text);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mMessageReceiver = new PackageGotReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PackageGotReceiver.MSG_RECEIVED_ACTION);
        registerReceiver(mMessageReceiver, intentFilter);

//        ExecutorService exec = Executors.newCachedThreadPool();
//        UDPServer server = new UDPServer(this);
//        exec.execute(server);

        mSendACKButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {

                    @Override
                    public void run() {
                        mTimeACK = new TimeACK(mTimeACKBytes);
                        UDPByteClient sender = new UDPByteClient("192.168.2.183", mTimeACKBytes);
                        Message msg = new Message();
                        msg.what = SEND_ACK;
                        msg.obj = sender.send();
                        mHandler.sendMessage(msg);
                    }

                }.start();
            }
        });
        mSendReportButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {

                    @Override
                    public void run() {
                        mReport = new Report(mReportBytes);
                        UDPByteClient sender = new UDPByteClient("192.168.2.183", mReportBytes);
                        Message msg = new Message();
                        msg.what = SEND_REPORT;
                        msg.obj = sender.send();
                        mHandler.sendMessage(msg);
                    }

                }.start();
            }
        });
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEND_ACK:
                    mLogTextView.setText(msg.obj.toString() + mTimeACK.getPrintableString());
                    break;
                case SEND_REPORT:
                    mLogTextView.setText(msg.obj.toString() + mReport.getPrintableString());
                    break;
                default:
                    break;
            }
        }
    };

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mMessageReceiver);
    }
}
