package com.honeywell.firemanlocate.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.RecoverySystem;
import android.widget.ScrollView;
import android.widget.TextView;

import com.honeywell.firemanlocate.activity.ShowActivity;
import com.honeywell.firemanlocate.activity.TestServiceActivity;
import com.honeywell.firemanlocate.model.DataType;
import com.honeywell.firemanlocate.model.IPackage;
import com.honeywell.firemanlocate.model.Report;
import com.honeywell.firemanlocate.model.TimeACK;
import com.honeywell.firemanlocate.network.UDPServer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CalculatePositionService extends Service {
    private List<IPackage> mReportList = new ArrayList<>();
    private Timer timer = new Timer();
    private PackagetReceiver mMessageReceiver;

    public CalculatePositionService() {
    }

    private Context mContext;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new MsgBinder();
    }

    public class MsgBinder extends Binder {
        /**
         * 获取当前Servic的实例
         *
         * @return
         */
        public CalculatePositionService getService() {
            return CalculatePositionService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PackagetReceiver.MSG_RECEIVED_ACTION);
        registerReceiver(mMessageReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mReportList != null && mReportList.size() != 0) {
                    Intent intent = new Intent();
                    intent.putExtra(TestServiceActivity.UPDATE_DATA, (Serializable) mReportList);  //告诉activity
                    mReportList.clear();
                    intent.setAction(TestServiceActivity.UPDATE_DRAWVIEW_ACTION);
                    mContext.getApplicationContext().sendBroadcast(intent);
                }
            }
        }, 10 * 1000, 2000);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mMessageReceiver);
    }

    private class PackagetReceiver extends BroadcastReceiver {
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

        }
    }
}
