package com.honeywell.firemanlocate.network;

import android.content.Context;
import android.content.Intent;

import com.honeywell.firemanlocate.activity.ShowActivity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by lynn.liu on 7/6/15.
 */
public class UDPServer implements Runnable {

    public static final String UDP_MSG_RECEIVED = "udp_msg_received";

    private static final int PORT = 12300;
    private byte[] mMessage = new byte[1024];
    private boolean mLife = true;
    private Context mContext;

    public UDPServer(Context context) {
        mContext = context;
    }

    /**
     * @return the mLife
     */
    public boolean isLife() {
        return mLife;
    }

    /**
     * @param life the mLife to set
     */
    public void setLife(boolean life) {
        this.mLife = life;
    }

    @Override
    public void run() {

        DatagramSocket dSocket = null;
        DatagramPacket dPacket = new DatagramPacket(mMessage, mMessage.length);
        try {
            dSocket = new DatagramSocket(PORT);
            while (mLife) {
                try {
                    dSocket.receive(dPacket);
                    if (dPacket.getData() != null && dPacket.getData().length > 0) {
                        Intent intent = new Intent();
                        intent.putExtra(UDP_MSG_RECEIVED, dPacket.getData());
                        intent.setAction(ShowActivity.PackageGotReceiver.MSG_RECEIVED_ACTION);
                        mContext.getApplicationContext().sendBroadcast(intent);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
