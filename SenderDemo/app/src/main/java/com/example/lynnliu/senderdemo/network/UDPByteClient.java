package com.example.lynnliu.senderdemo.network;

import com.example.lynnliu.senderdemo.util.ByteUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by lynn.liu on 7/6/15.
 */
public class UDPByteClient {
    private static final int SERVER_PORT = 12300;

    private String mAddress;
    private DatagramSocket dSocket = null;
    private byte[] mMessage;

    /**
     * @param message
     */
    public UDPByteClient(String address, byte[] message) {
        mAddress = address;
        mMessage = message;
    }

    /**
     * 发送信息到服务器
     */
    public String send() {
        StringBuilder stringBuilder = new StringBuilder();
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(mAddress);
            stringBuilder.append("server found, connecting").append("\n");
        } catch (UnknownHostException e) {
            stringBuilder.append("server not found").append("\n");
            e.printStackTrace();
        }
        try {
            dSocket = new DatagramSocket();
            stringBuilder.append("server connected").append("\n");
        } catch (SocketException e) {
            e.printStackTrace();
            stringBuilder.append("connect failed").append("\n");
        }
        if (mMessage == null || mMessage.length == 0) {
            stringBuilder.append("data empty").append("\n");
            return stringBuilder.toString();
        }
        int msg_len = mMessage == null ? 0 : mMessage.length;
        DatagramPacket dPacket = new DatagramPacket(mMessage, msg_len, inetAddress, SERVER_PORT);
        try {
            dSocket.send(dPacket);
            stringBuilder.append("send succeed").append("\n");
        } catch (IOException e) {
            e.printStackTrace();
            stringBuilder.append("send failed").append("\n");
        }
        dSocket.close();
        return stringBuilder.toString();
    }
}
