package com.honeywell.firemanlocate.util;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

/**
 * Created by lynnliu on 7/17/15.
 */
public class NetworkUtil {

    public static String getIPAddress(Context ctx) {
        WifiManager wifi_service = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifi_service.getDhcpInfo();
        Log.d("DHCP info gateway----->", Formatter.formatIpAddress(dhcpInfo.gateway));
        return Formatter.formatIpAddress(dhcpInfo.gateway);
    }
}
