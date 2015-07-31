package com.honeywell.firemanlocate.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.honeywell.firemanlocate.R;
import com.honeywell.firemanlocate.model.DataType;
import com.honeywell.firemanlocate.model.IPackage;
import com.honeywell.firemanlocate.model.Report;
import com.honeywell.firemanlocate.model.Report2;
import com.honeywell.firemanlocate.model.TimeACK;
import com.honeywell.firemanlocate.model.TimeSync;
import com.honeywell.firemanlocate.util.NetworkUtil;
import com.honeywell.firemanlocate.network.UDPClient;
import com.honeywell.firemanlocate.network.UDPServer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lynnliu on 7/17/15.
 */
public class ShowActivity extends Activity {

    private static final int SEND_RESULT = 11;

    private Button mStartButton;
    private ScrollView mScrollView;
    private TextView mLogTextView;
    private EditText mAddressEditText;

    private TimeSync mTimeSync;
    private PackageGotReceiver mMessageReceiver;
    private List<IPackage> mReportList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        mStartButton = (Button) findViewById(R.id.start_button);
        mScrollView = (ScrollView) findViewById(R.id.scroll_view);
        mLogTextView = (TextView) findViewById(R.id.msg_log_text);
        mAddressEditText = (EditText) findViewById(R.id.ip_edittext);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mMessageReceiver = new PackageGotReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PackageGotReceiver.MSG_RECEIVED_ACTION);
        registerReceiver(mMessageReceiver, intentFilter);

        ExecutorService exec = Executors.newCachedThreadPool();
        UDPServer server = new UDPServer(this);
        exec.execute(server);

        mStartButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mAddressEditText.getText().toString().trim())) {
                    Toast.makeText(ShowActivity.this, R.string.input_address, Toast.LENGTH_SHORT).show();
                    return;
                }
                final String ipAddressString = mAddressEditText.getText().toString().trim();
                new Thread() {

                    @Override
                    public void run() {
                        mTimeSync = new TimeSync();
                        UDPClient sender = new UDPClient(ipAddressString/*NetworkUtil.getIPAddress
                                (ShowActivity.this)*/, mTimeSync.getDataArray(), TimeSync.DATA_LENGTH);
                        if (!mReportList.isEmpty()) mReportList.clear();

                        Message msg = new Message();
                        msg.what = SEND_RESULT;
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
                case SEND_RESULT:
                    mLogTextView.setText(msg.obj.toString() + mTimeSync.getPrintableString());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mMessageReceiver);
    }
}
