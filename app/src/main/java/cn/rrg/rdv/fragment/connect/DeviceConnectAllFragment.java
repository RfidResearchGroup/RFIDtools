package cn.rrg.rdv.fragment.connect;

import java.util.ArrayList;

import cn.rrg.rdv.R;
import cn.rrg.rdv.javabean.DevBean;
import cn.rrg.rdv.models.AbstractDeviceModel;
import cn.rrg.rdv.presenter.DeviceExistsPresenter;
import cn.rrg.rdv.view.DeviceExistsView;

public class DeviceConnectAllFragment
        extends DeviceConnectFragment
        implements DeviceExistsView {

    //重新定义类型!
    protected ArrayList<DeviceExistsPresenter> presenters = new ArrayList<>();

    @Override
    protected void initResource() {
        super.initResource();
        //初始化toolbar
        getToolbar().setTitle(R.string.history_device);
        if (models != null) {
            for (AbstractDeviceModel model : models) {
                DeviceExistsPresenter presenter = new DeviceExistsPresenter(model);
                presenter.attachSubView(this);
                presenter.attachView(this);
                presenters.add(presenter);
                //直接取出相关的已存在设备即可!
                presenter.existsDevList();
            }
        } else {
            throw new RuntimeException("models not init exception!");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (DeviceExistsPresenter presenter : presenters) {
            //销毁视图绑定!
            presenter.detachSubView();
            presenter.detachView();
        }
    }

    @Override
    protected void onDiscovery() {
        for (DeviceExistsPresenter presenter : presenters)
            presenter.existsDevList();
    }

    @Override
    public void showExistsDev(DevBean[] devList) {
        super.showExistsDev(devList);
        //判断需不需要显示或者隐藏填充视图!
        showOrDismissEmptyView();
    }
}
