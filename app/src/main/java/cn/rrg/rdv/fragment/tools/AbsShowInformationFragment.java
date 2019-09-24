package cn.rrg.rdv.fragment.tools;

import android.app.AlertDialog;
import android.nfc.Tag;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import cn.dxl.common.util.FragmentUtil;
import cn.dxl.mifare.GlobalTag;
import cn.rrg.rdv.R;
import cn.rrg.rdv.fragment.base.BaseFragment;
import cn.rrg.rdv.presenter.AbsTagInformationsPresenter;
import cn.rrg.rdv.view.TagInformationsView;

/*
 * 封装了信息显示的相关操作!
 * */
public abstract class AbsShowInformationFragment
        extends BaseFragment implements
        TagInformationsView<CharSequence>,
        GlobalTag.OnNewTagListener {

    private AbsTagInformationsPresenter presenter;

    protected abstract AbsTagInformationsPresenter getPresenter();

    private WebView webViewShowInfo;

    private AlertDialog workingMsgDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlobalTag.addListener(this);
        workingMsgDialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.tips)
                .setCancelable(false)
                .setMessage(R.string.progress_msg).create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_std_tag_information, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter = getPresenter();
        presenter.attachView(this);

        FloatingActionButton fabShowInfo = view.findViewById(R.id.fabShowTagInformation);
        webViewShowInfo = view.findViewById(R.id.webViewShowInfo);
        //TODO 不能让webview使用缓存，会导致信息无法及时刷新!
        WebSettings webSettings = webViewShowInfo.getSettings();
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        fabShowInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workingMsgDialog.show();
                presenter.show();
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (isVisible()) {
            workingMsgDialog.show();
            // 在用户切换到这个fragment时才进行信息请求!
            presenter.show();
        }
    }

    @Override
    public void onNewTag(Tag tag) {
        Log.d(LOG_TAG, "onNewTag() 被触发，将会更新信息!");
        //收到标签发生变动的通知，此处只需要做出刷新标签信息的动作!
        if (presenter != null) {
            workingMsgDialog.show();
            presenter.show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GlobalTag.removeListener(this);
        presenter.detachView();
        workingMsgDialog.dismiss();
        workingMsgDialog = null;
    }

    @Override
    public void onInformationsShow(CharSequence info) {
        FragmentUtil.runOnUiThread(getActivity(), new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, "onInformationsShow() 被触发，将会更新信息!");
                webViewShowInfo.loadDataWithBaseURL(null, info.toString(), "text/html", "UTF-8", null);
                workingMsgDialog.dismiss();
            }
        });
    }
}
