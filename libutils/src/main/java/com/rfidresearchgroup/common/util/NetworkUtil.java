package com.rfidresearchgroup.common.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;

public class NetworkUtil {
    /**
     * 是否使用代理(WiFi状态下的,避免被抓包)
     */
    public static boolean isWifiProxy() {
        String proxyAddress;
        int proxyPort;
        proxyAddress = System.getProperty("http.proxyHost");
        String portstr = System.getProperty("http.proxyPort");
        proxyPort = Integer.parseInt((portstr != null ? portstr : "-1"));
        System.out.println(proxyAddress + "~");
        System.out.println("port = " + proxyPort);
        return (!TextUtils.isEmpty(proxyAddress)) && (proxyPort != -1);
    }

    /**
     * 是否正在使用VPN
     */
    public static boolean isVpnUsed() {
        try {
            Enumeration niList = NetworkInterface.getNetworkInterfaces();
            if (niList != null) {
                for (Object intf : Collections.list(niList)) {
                    NetworkInterface ni = (NetworkInterface) intf;
                    if (!ni.isUp() || ni.getInterfaceAddresses().size() == 0) {
                        continue;
                    }
                    Log.d("-----", "isVpnUsed() NetworkInterface Name: " + ni.getName());
                    if ("tun0".equals(ni.getName()) || "ppp0".equals(ni.getName())) {
                        return true; // The VPN is up
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 网络是否连接!
     *
     * @param context 上下文
     * @return 有网时返回true，没网时返回false
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * wifi是否连接
     *
     * @param context 上下文
     * @return 是WIFI网络返回true，不是WIFI返回false
     */
    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 移动网络是否连接
     *
     * @param context 上下文
     * @return 是数据流量时返回true，不是返回false
     */
    public static boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mMobileNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null) {
                return mMobileNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 获取当前网络连接的类型信息
     *
     * @param context 上下文
     * @return 判断结果，
     */
    public static int getConnectedType(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
                return mNetworkInfo.getType();
            }
        }
        return -1;
    }

    /**
     * 判断主机是否存在!
     *
     * @param host    主机
     * @param timeout 超时值!
     */
    public static boolean isHostReachable(String host, int timeout) {
        try {
            return InetAddress.getByName(host).isReachable(timeout);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断主机是否可以连接!
     *
     * @param host    主机
     * @param port    端口
     * @param timeout 超时值
     */
    public static boolean isHostConnectable(String host, int port, int timeout) {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port), timeout);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 判断主机是否可以连接!
     *
     * @param host 主机
     * @param port 端口
     */
    public static boolean isHostConnectable(String host, int port) {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

}