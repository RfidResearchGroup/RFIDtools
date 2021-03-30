package cn.proxgrind.com;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import cn.proxgrind.utils.ContextContentProvider;

/**
 * Created by DXL on 2019/8/1.
 * 抽象SPP相关属性与动作，降低代码冗余!
 * Abstract SPP related attributes and actions, reduce code redundancy!
 *
 * @author DXL
 */
public abstract class AbsBluetoothSpp implements DriverInterface<BluetoothDevice, BluetoothAdapter> {

    private Context context = ContextContentProvider.mContext;
    //广播对象实例
    private static BroadcastReceiver btBroadcastRecv = null;
    //蓝牙设备套接字
    private static BluetoothSocket btSocket = null;
    //蓝牙设备适配器
    private BluetoothAdapter btAdapter;
    //蓝牙SPP的IO接口
    protected InputStream inputStream = null;
    protected OutputStream outputStream = null;
    //广播注册状态
    private static boolean registerState = false;
    // SPP UUID
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public AbsBluetoothSpp() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public void register(final DevCallback<BluetoothDevice> callback) {
        if (!registerState) {
            //注册广播
            btBroadcastRecv = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action == null) return;
                    switch (action) {
                        case BluetoothDevice.ACTION_FOUND:
                            //设备发现广播
                            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                            //避免重复添加已经绑定过的设备
                            if (device != null) {
                                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                                    if (callback != null)
                                        callback.onAttach(device);
                                }
                            }
                            break;
                        case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                            //开始搜索广播
                            break;
                        case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                            //搜索完成广播
                            break;
                        case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                            //蓝牙断开连接广播
                            device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                            if (callback != null)
                                callback.onDetach(device);
                            break;
                        case BluetoothAdapter.ACTION_STATE_CHANGED:
                            //蓝牙状态改变广播
                            /*if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                                    == BluetoothAdapter.STATE_OFF) {
                            }*/
                            break;

                        default:
                            //不匹配广播
                            break;
                    }
                }
            };
            try {
                IntentFilter intent = new IntentFilter();
                intent.addAction(BluetoothDevice.ACTION_FOUND);
                intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
                intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
                intent.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                context.registerReceiver(btBroadcastRecv, intent);
            } catch (Exception ignored) {
            }
            registerState = true;
        }
    }

    @Override
    public boolean connect(BluetoothDevice t) {
        try {
            // can't connect at scanning.
            if (btAdapter.isDiscovering()) btAdapter.cancelDiscovery();
            // close prev connect!
            if (btSocket != null && btSocket.isConnected()) {
                close();
                Thread.sleep(1500);
            }
            btSocket = t.createRfcommSocketToServiceRecord(SPP_UUID);
            // auto retry connect.
            for (int i = 0; ; i++) {
                try {
                    btSocket.connect();
                    break;
                } catch (IOException ex) {
                    if (i < 5) {
                        continue;
                    }
                    break;
                }
            }
            inputStream = btSocket.getInputStream();
            outputStream = btSocket.getOutputStream();
            return true;
        } catch (IOException | InterruptedException ioe) {
            ioe.printStackTrace();
        }
        return false;
    }

    @Override
    public BluetoothAdapter getAdapter() {
        return btAdapter;
    }

    @Override
    public BluetoothDevice getDevice() {
        return null;
    }

    @Override
    public void disconect() {
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void close() throws IOException {
        if (outputStream != null && inputStream != null) {
            outputStream.close();
            inputStream.close();
        }
        if (btSocket != null && btSocket.isConnected()) {
            btSocket.close();
            btSocket = null;
        }
    }

    @Override
    public void unregister() {
        if (registerState) {
            try {
                context.unregisterReceiver(btBroadcastRecv);
                registerState = false;
            } catch (Exception ignored) {
            }
        }
    }
}