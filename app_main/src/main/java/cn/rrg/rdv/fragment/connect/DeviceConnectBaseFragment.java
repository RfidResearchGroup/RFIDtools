package cn.rrg.rdv.fragment.connect;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Arrays;

import cn.rrg.rdv.R;
import cn.rrg.rdv.adapter.DevArrayAdapter;
import cn.rrg.rdv.fragment.base.BaseFragment;
import cn.rrg.rdv.javabean.DevBean;
import cn.rrg.rdv.view.DeviceAttachView;
import cn.rrg.rdv.view.DeviceExistsView;
import cn.rrg.rdv.util.Commons;
import cn.dxl.common.util.RestartUtils;
import cn.dxl.common.util.ViewUtil;
import cn.dxl.common.util.AppUtil;

public abstract class DeviceConnectBaseFragment
        extends BaseFragment
        implements DeviceAttachView, DeviceExistsView {

    //展示数据的列表视图!
    protected ListView listViewShowDevs;
    //搜索到的设备的数列
    protected ArrayList<DevBean> devicesList = new ArrayList<>();
    //集合适配器
    protected ArrayAdapter<DevBean> arrayAdapter;
    //下拉刷新控件!
    protected SwipeRefreshLayout srDiscovery;
    //无数据时的内容填充!
    protected RelativeLayout layout_404_device;
    //连接过程提示框
    protected AlertDialog dialogConnectTips = null;
    //是否是自带NFC的标志位!
    protected boolean isDefaultNfc = false;
    //缓存连接的bean实例!
    protected DevBean mDevBean;
    //显示消息的视图
    protected View msgView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dev_connect, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //初始化适配器!
        arrayAdapter = new DevArrayAdapter(getContext(), R.layout.dev_info, devicesList);
        initViews();
        initActions();
        initDialogs();
        //子类实现初始化子类的资源!
        initResource();
    }

    private void initViews() {
        View view = getView();
        if (view != null) {
            listViewShowDevs = view.findViewById(R.id.lstvShowDev);
            //设置适配器进入列表视图中!
            listViewShowDevs.setAdapter(arrayAdapter);
            listViewShowDevs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(LOG_TAG, "点击了设备，将会开始进行连接!");
                    //开始尝试连接
                    DevBean devBean = arrayAdapter.getItem(position);
                    if (devBean != null) {
                        Log.d(LOG_TAG, "devBean不为空，将会进入下一步!");
                        //缓存到全局!
                        mDevBean = devBean;
                        //取出信息，进行判断处理!
                        String address = devBean.getMacAddress();
                        if (address != null) {
                            //此处调用子类实现的连接!
                            Log.d(LOG_TAG, "设备地址不为空，将会调用子类的实现进行连接设备!");
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    onConnectDev(address);
                                }
                            }).start();
                            //标记当前驱动信息
                            //FIXME 变量调用错误，导致自带NFC判断出错：isConnected = false;
                            isDefaultNfc = address.equals("00:00:00:00:00:02");
                            //弹窗显示连接提示
                            dialogConnectTips.show();
                        } else {
                            showToast("addr为空!");
                        }
                    } else {
                        showToast("bean为空!");
                    }
                }
            });
            layout_404_device = view.findViewById(R.id.layout_404_device);
            srDiscovery = view.findViewById(R.id.srDiscovery);
        }
    }

    private void initActions() {
        srDiscovery.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //监听到刷新事件!
                onDiscovery();
                srDiscovery.setRefreshing(false);
            }
        });
    }

    private void initDialogs() {
        //初始化连接过程弹窗提示对象
        dialogConnectTips = new AlertDialog.Builder(getContext()).create();
        dialogConnectTips.setTitle(getString(R.string.title_connecting));
        msgView = ViewUtil.inflate(getContext(), R.layout.dialog_working_msg);
        dialogConnectTips.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.title_connect_cancel), (dialog, which) -> {
            dialogConnectTips.dismiss();
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.warning).setMessage(getString(R.string.cancel_tips))
                    .setCancelable(false).show();
            //断开链接
            onDisconnect();
            //销毁当前栈中所有的实例!
            AppUtil.getInstance().finishAll();
            //重启APP
            RestartUtils.restartAPP(getContext());
        });
        //设置对话框视图
        dialogConnectTips.setView(msgView);
        //不可点击外部视图隐藏dialog
        dialogConnectTips.setCancelable(false);
    }

    //TODO 由父类控制初始化!
    protected abstract void initResource();

    //留一个抽象方法让子类实现，实现下拉刷新时的相关设备!
    protected abstract void onDiscovery();

    //在点击了连接的时候回调!
    protected abstract void onConnectDev(String address);

    //在断开连接的时候的操作!
    protected abstract void onDisconnect();

    //在初始化NFC设备的回调!
    protected abstract void onInitNfcDev();

    //在设备初始化成功时候的回调!
    protected abstract void onInitSuccess();

    //显示empty视图!
    protected void showEmptyView() {
        listViewShowDevs.setVisibility(View.GONE);
        layout_404_device.setVisibility(View.VISIBLE);
    }

    //隐藏empty视图!
    protected void dismissEmptyView() {
        listViewShowDevs.setVisibility(View.VISIBLE);
        layout_404_device.setVisibility(View.GONE);
    }

    @Override
    public void onRegisterError(String name) {
        if (name == null) {
            return;
        }
        //这里做一下控制，设备驱动没有注册时，应当怎么做？
        switch (name) {
            case "spp":
                //TODO 待实现的spp注册异常处理
                showToast(name);
                break;

            case "usb":
                //TODO 待实现的usb注册异常处理
                showToast(name);
                break;

            case "not":
                //TODO 待实现的非spp和usb类型异常处理!
                showToast(name);
                break;
        }
        //showToast("在注册驱动: " + name + " 时出现异常，请重启程序!");
    }

    @Override
    public void showExistsDev(DevBean[] devList) {
        if (devList != null) {
            Log.d(LOG_TAG, "showExistsDev() 设备列表不为空!");
            if (getActivity() != null)
                getActivity().runOnUiThread(() -> {
                    //清除旧数据和添加新的数据 TODO 不清除
                    //devicesList.clear();
                    //此时才可以添加新的数据!
                    ArrayList<DevBean> tmpList = new ArrayList<>();
                    for (int i = 0; i < devList.length; i++) {
                        //取出适配器中传输过来的设备实体!
                        DevBean devBean = devList[i];
                        //是否存在!
                        boolean isExists = false;
                        if (devicesList.size() == 0) {
                            Log.d(LOG_TAG, "长度为零，直接添加!");
                            //TODO 如果缓存列表的长度为零，会导致进不去循环，导致历史设备无法显示!
                            // 此时可以直接添加，而无需判断是否重复，因为数列本身就是空的!
                            devicesList.addAll(Arrays.asList(devList));
                            //通知数据刷新
                            arrayAdapter.notifyDataSetChanged();
                            return;
                        }
                        //判断重复不重复!
                        for (int j = 0; j < devicesList.size(); j++) {
                            //取出全局缓存的设备实体!
                            DevBean devBean1 = devicesList.get(j);
                            //直接进行判断!
                            if (Commons.equalDebBean(devBean, devBean1)) {
                                isExists = true;
                                break;
                            }
                        }
                        if (!isExists) {
                            tmpList.add(devBean);
                        }
                    }
                    devicesList.addAll(tmpList);
                });
        }
    }

    @Override
    public void devAttach(DevBean dev) {
        if (dev == null)
            return;
        if (getActivity() != null)
            getActivity().runOnUiThread(() -> {
                boolean isExists = false;
                for (DevBean devs : devicesList) {
                    if (Commons.equalDebBean(dev, devs)) {
                        //已经存在在列表中
                        isExists = true;
                    }
                }
                if (!isExists) {
                    devicesList.add(dev);
                    //更新列表
                    arrayAdapter.notifyDataSetChanged();
                    //有设备接入了肯定要处理empty视图!
                    dismissEmptyView();
                }
            });
    }

    @Override
    public void devDetach(DevBean bean) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Commons.removeDevByList(bean, devicesList);
                    //判断需不需要显示empty视图!
                    if (arrayAdapter.getCount() == 0) {
                        showEmptyView();
                    }
                }
            });
        }
    }

    @Override
    public void onConnectFail() {
        if (getActivity() != null)
            getActivity().runOnUiThread(() -> {
                showToast(getString(R.string.com_err_tips));
                dialogConnectTips.dismiss();
            });
    }

    @Override
    public void onConnectSuccess() {
        if (getActivity() != null)
            getActivity().runOnUiThread(() -> {
                showToast(getString(R.string.com_normal_tips));
                onInitNfcDev();
            });
    }

    @Override
    public void onInitNfcAdapterSuccess() {
        if (getActivity() != null)
            getActivity().runOnUiThread(() -> {
                        dialogConnectTips.dismiss();
                        onInitSuccess();
                    }
            );
    }

    @Override
    public void onInitNfcAdapterFail() {
        if (getActivity() != null)
            getActivity().runOnUiThread(() -> {
                        showToast(getString(R.string.device_init_err));
                        dialogConnectTips.dismiss();
                    }
            );
    }
}
