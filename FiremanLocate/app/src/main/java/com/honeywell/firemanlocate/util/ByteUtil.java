package com.honeywell.firemanlocate.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by lynnliu on 7/8/15.
 */
public class ByteUtil {
    private static final String TAG = "ByteUtil";

    public static boolean isBigEndian() {
        return (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN);
//        short i = 0x1;
//        boolean bRet = ((i >> 8) == 0x1);
//        Log.d(TAG, "bRet = " + bRet);
//        return bRet;
    }

    public static ByteBuffer getLittleByteFromByte(byte value) {
        ByteBuffer buffer = ByteBuffer.allocate(1).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(value);
        return buffer;
    }

    public static ByteBuffer getLittleByteFromShort(short value) {
        ByteBuffer buffer = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort(value);
        return buffer;
    }

    public static ByteBuffer getLittleByteFromInt(int value) {
        ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(value);
        return buffer;
    }

    public static ByteBuffer getLittleByteFromByteArray(byte[] value) {
        ByteBuffer buffer = ByteBuffer.allocate(value.length).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(value);
        return buffer;
    }

    public static byte[] getLittleByte(Object value) {
        int dataLength = 0;
        if (value instanceof Byte) {
            dataLength = 1;
        } else if (value instanceof Short) {
            dataLength = 2;
        } else if (value instanceof Integer) {
            dataLength = 4;
        } else if (value instanceof Byte[]) {
            dataLength = ((byte[]) value).length;
        }
        byte[] bytes = new byte[dataLength];
        if (dataLength > 0) {
            ByteBuffer buffer = null;
            if (value instanceof Byte) {
                buffer = getLittleByteFromByte((Byte) value);
            } else if (value instanceof Short) {
                buffer = getLittleByteFromShort((Short) value);
            } else if (value instanceof Integer) {
                buffer = getLittleByteFromInt((Integer) value);
            } else if (value instanceof Byte[]) {
                buffer = getLittleByteFromByteArray((byte[]) value);
            }
            if (buffer != null) {
                bytes = buffer.array();
            }
        }
        return bytes;
    }

    public static short bytesToShort(byte[] bytes) {
        return (short) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
    }

    public static int bytesToInt(byte[] bytes) {
        return (0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)) | (0xff0000 & (bytes[2] << 16)) |
                (0xff000000 & (bytes[3] << 24));
    }
}
