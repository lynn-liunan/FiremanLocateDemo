package com.honeywell.firemanlocate.network;

import com.honeywell.firemanlocate.util.ByteUtil;

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
public class UDPClient {
    private static final int SERVER_PORT = 12300;

    private String mAddress;
    private DatagramSocket dSocket = null;
    private Object[] mMessage;
    private int mDataLength;

    /**
     * @param message
     */
    public UDPClient(String address, Object[] message, int dataLength) {
        mAddress = address;
        mMessage = message;
        mDataLength = dataLength;
    }

    /**
     * 发送信息到服务器
     */
    public String send() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(mDataLength).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < mMessage.length; i++) {
            byteBuffer.put(ByteUtil.getLittleByte(mMessage[i]));
        }
        StringBuilder stringBuilder = new StringBuilder();
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(mAddress);
            stringBuilder.append("address: " + mAddress).append("\n");
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
        byte[] bytes = byteBuffer.array();
        if (bytes == null || bytes.length ==0) {
            stringBuilder.append("data empty").append("\n");
            return stringBuilder.toString();
        }
        int msg_len = byteBuffer == null ? 0 : bytes.length;
        DatagramPacket dPacket = new DatagramPacket(bytes, msg_len, inetAddress, SERVER_PORT);
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
