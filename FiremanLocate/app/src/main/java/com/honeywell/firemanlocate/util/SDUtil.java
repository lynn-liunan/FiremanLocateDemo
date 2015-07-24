package com.honeywell.firemanlocate.util;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by lynn.liu on 6/19/15.
 */
public class SDUtil {
    /**
     * 判断SDCard是否存在（当没有外挂SD卡时，内置ROM也被识别为存在sd卡）
     *
     * @return
     */
    public static boolean isSdCardExist() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SD卡根目录路径
     *
     * @return
     */
    public static String getSdCardPath() {
        boolean exist = isSdCardExist();
        String sdpath = "";
        if (exist) {
            sdpath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath();
        } else {
            sdpath = "error";
        }
        return sdpath;

    }

    /**
     * 获取默认的文件路径
     *
     * @return
     */
    public static String getDefaultFilePath(String fileName) {
        String filepath = "";
        File file = new File(Environment.getExternalStorageDirectory(), fileName);
        if (file.exists()) {
            filepath = file.getAbsolutePath();
        } else {
            filepath = "error";
        }
        return filepath;
    }


    public static String readFileUseBufferReader(String fileName) {
        String result = "";
        try {
            File file = new File(Environment.getExternalStorageDirectory(), fileName);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String readLine = "";
            StringBuffer sb = new StringBuffer();
            while ((readLine = br.readLine()) != null) {
                sb.append(readLine);
            }
            br.close();
            result += sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
