package cn.rrg.com;

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

import cn.dxl.utils.ContextContentProvider;

/**
 * Created by DXL on 2019/8/1.
 * 抽象SPP相关属性与动作，降低代码冗余!
 * Abstract SPP related attributes and actions, reduce code redundancy!
 *
 * @author DXL
 */
public abstract class AbsBluetoothSpp implements DriverInterface<BluetoothDevice, BluetoothAdapter> {

    private Context context = ContextContentProvider.mContext;
    //允许失败的次数上限，事不过三...
    private static final int FAILD_MAX = 3;
    //优化，超过多少次就判断失败，不自动连接!
    private int faildCount = 0;
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
            //设备搜寻与设备链接有冲突
            if (btAdapter.isDiscovering()) btAdapter.cancelDiscovery();
            //在重新连接前先尝试关闭旧的连接!
            if (btSocket != null && btSocket.isConnected()) {
                close();
                //1秒半延迟。防止蓝牙出现射频迟缓问题!
                Thread.sleep(1500);
            }
            //根据UUID连接SPP
            btSocket = t.createRfcommSocketToServiceRecord(SPP_UUID);
            //链接设备
            btSocket.connect();
            inputStream = btSocket.getInputStream();
            outputStream = btSocket.getOutputStream();
            //连接成功后清除异常次数!
            faildCount = 0;
            return true;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            //异常次数自增!
            ++faildCount;
            //判断是否超出了异常上限!
            if (faildCount >= FAILD_MAX) {
                return false;
            }
            //进行重试连接!
            connect(t);
        } catch (InterruptedException e) {
            e.printStackTrace();
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