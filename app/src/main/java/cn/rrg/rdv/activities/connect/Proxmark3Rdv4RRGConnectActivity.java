package cn.rrg.rdv.activities.connect;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import android.text.Html;
import android.widget.TextView;

import cn.dxl.common.util.DisplayUtil;
import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.main.Proxmark3Rdv4RRGMain;
import cn.rrg.rdv.activities.tools.DeviceConnectActivity;
import cn.rrg.rdv.application.RuntimeProperties;
import cn.rrg.rdv.callback.ConnectFailedCtxCallback;
import cn.rrg.rdv.models.AbstractDeviceModel;
import cn.rrg.rdv.models.Proxmark3Rdv4SppModel;
import cn.rrg.rdv.models.Proxmark3Rdv4UsbModel;
import cn.dxl.common.util.PermissionUtil;

/**
 * 专供RDV4连接设备
 * 可以使用USB 与 SPP两种方式连接设备
 * v
 *
 * @author DXL
 */
public class Proxmark3Rdv4RRGConnectActivity
        extends DeviceConnectActivity {

    private static ConnectFailedCtxCallback connectFailedCtxCallback = new ConnectFailedCtxCallback() {
        @Override
        public void onFailed(Activity activity) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int paddingValue = DisplayUtil.dip2px(activity, 16);
                    TextView msg = new TextView(activity);
                    msg.setTextIsSelectable(true);
                    msg.setPadding(paddingValue, paddingValue, paddingValue, paddingValue);
                    msg.setText(Html.fromHtml(activity.getString(R.string.connect_errr_msg_1)));
                    new AlertDialog.Builder(activity)
                            .setTitle(R.string.connect_faild)
                            .setView(msg).show();
                }
            });
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //这里我们申请一下权限，因为RDV4需要蓝牙!
        PermissionUtil pu = new PermissionUtil(this);
        pu.setRequestCode(0x666);
        pu.request(Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    @Override
    public AbstractDeviceModel[] getModels() {
        return new AbstractDeviceModel[]{
                //RDV4支持SPP数据源，因此提供SPP的通信!
                new Proxmark3Rdv4SppModel(),
                //RDV4还支持USB的数据源，同样提供USB的通信!
                new Proxmark3Rdv4UsbModel()
        };
    }

    @Override
    public Class getTarget() {
        //连接成功后将跳转到该界面!
        return Proxmark3Rdv4RRGMain.class;
    }

    @Override
    public String getConnectingMsg() {
        String msg = getString(R.string.progress_msg);
        msg += "\n\nFirmware: ";
        String version = RuntimeProperties.PM3_CLIENT_VERSION;
        msg += version + "\n\n";
        String tips = getString(R.string.tips_pm3_version);
        return msg + tips;
    }

    @Override
    public ConnectFailedCtxCallback getCallback() {
        return connectFailedCtxCallback;
    }
}
