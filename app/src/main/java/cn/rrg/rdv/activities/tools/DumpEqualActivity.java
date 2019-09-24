package cn.rrg.rdv.activities.tools;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import cn.dxl.common.widget.FilesSelectorDialog;
import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.main.BaseActivity;
import cn.rrg.rdv.adapter.DumpEqualAdapter;
import cn.rrg.rdv.javabean.M1Bean;
import cn.rrg.rdv.presenter.FileReadLinePresenter;
import cn.rrg.rdv.view.FileReadLineView;
import cn.rrg.rdv.util.Commons;
import cn.rrg.rdv.util.LineEqualUtil;
import cn.rrg.rdv.util.Paths;
import cn.dxl.common.util.MyArrays;
import cn.dxl.common.widget.ToastUtil;

public class DumpEqualActivity extends BaseActivity implements FileReadLineView {

    private RadioGroup radioGroupPathSelect = null;
    private TextView txtSelectData1 = null;
    private TextView txtSelectData2 = null;
    private TextView txtShowEqualMsg = null;
    private DumpEqualAdapter adapter = null;
    private ArrayList<String> mData1 = new ArrayList<>();
    private ArrayList<String> mData2 = new ArrayList<>();
    private FilesSelectorDialog mSelector = null;
    private int fileId = 1;
    private FileReadLinePresenter presenter = null;
    //初始化给两个必须要的参数！
    private boolean[] states = {false, false};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.act_dump_equal);
        super.onCreate(savedInstanceState);

        presenter = new FileReadLinePresenter();
        presenter.attachView(this);

        //初始化视图
        initViews();
        //初始化动作!
        initActions();

        //看看能否取得传递过来的数据!
        Intent intent = getIntent();
        //取出数据进行转换!
        ArrayList<M1Bean> dataList = (ArrayList<M1Bean>) intent.getSerializableExtra("dataList");
        //进行判断!
        if (dataList != null) {
            //取出数据名字!
            String name = intent.getStringExtra("dataName");
            if (name != null) {
                txtSelectData1.setText(name);
            } else {
                txtSelectData1.setText(R.string.data_name_dafault);
            }
            ArrayList<String> data = new ArrayList<>(128);
            //进行拼接!
            for (M1Bean bean : dataList) {
                //进行拼接!
                data.addAll(Arrays.asList(bean.getDatas()));
            }
            //设置ID
            fileId = 1;
            onReadFinish(MyArrays.list2Arr(data));
        }
    }

    private void initViews() {
        radioGroupPathSelect = findViewById(R.id.rdoGroupDataPath);
        txtSelectData1 = findViewById(R.id.txtDumpEqualData1);
        txtSelectData2 = findViewById(R.id.txtDumpEqualData2);
        txtShowEqualMsg = findViewById(R.id.txtShowEqualMsg);
        ListView lvShowEqualResult = findViewById(R.id.lvShowEqualResult);
        adapter = new DumpEqualAdapter(this, R.layout.dump_equal_result_item);
        lvShowEqualResult.setAdapter(adapter);
        mSelector = new FilesSelectorDialog.Builder(this).setPathOnLoad(Paths.DUMP_DIRECTORY).create();
    }

    private void initActions() {
        txtSelectData1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = Commons.getCustomerPath(radioGroupPathSelect);
                mSelector.setPathOnLoad(path);
                mSelector.setOnSelectListener(new FilesSelectorDialog.OnSelectListener() {
                    @Override
                    public void selected(File file) {
                        fileId = 1;
                        txtSelectData1.setText(file.getName());
                        presenter.load(file);
                    }
                });
                mSelector.show();
            }
        });
        txtSelectData2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = Commons.getCustomerPath(radioGroupPathSelect);
                mSelector.setPathOnLoad(path);
                mSelector.setOnSelectListener(new FilesSelectorDialog.OnSelectListener() {
                    @Override
                    public void selected(File file) {
                        fileId = 2;
                        txtSelectData2.setText(file.getName());
                        presenter.load(file);
                    }
                });
                mSelector.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        //清除数据，帮助回收!
        mData1.clear();
        mData2.clear();
        adapter.clear();
        super.onDestroy();
    }

    @Override
    public void onReadFinish(String[] line) {
        //加载成功了,需要进行判断当前是哪个文件加载出来的!
        switch (fileId) {
            //进行处理!
            case 1:
                //第一个数据!
                mData1.clear();
                mData1.addAll(Arrays.asList(line));
                states[0] = true;
                break;

            case 2:
                //第二个数据!
                mData2.clear();
                mData2.addAll(Arrays.asList(line));
                states[1] = true;
                break;
            default:
                break;
        }
        //进行判断，如果两个数据皆正常，则直接开始计算判断!
        boolean canStart = true;
        for (boolean state : states) {
            if (!state) {
                canStart = false;
                break;
            }
        }
        if (canStart) {
            //已经可以进行对比!
            LineEqualUtil leu = new LineEqualUtil();
            leu.putData(MyArrays.list2Arr(mData1), MyArrays.list2Arr(mData2));
            String[] result = leu.finalResult();
            int blockDiffCount = leu.getBlockDiffCount();
            int allDiffCount = leu.getAllDiffCount();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String msg = getString(R.string.data_block_diff_count) + blockDiffCount + getString(R.string.data_char_diff_count) + allDiffCount;
                    txtShowEqualMsg.setText(msg);
                    //显示一些必要的弹窗
                    if (blockDiffCount == 0 && allDiffCount == 0) {
                        showToast(getString(R.string.data_identical));
                    }
                    //必须先清除之前的数据
                    adapter.clear();
                    adapter.addAll(result);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void onReadFail(String msg) {
        showToast(msg);
    }

    @Override
    public void showToast(String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtil.show(DumpEqualActivity.this, msg, false);
            }
        });
    }

    @Override
    public void showDialog(String title, String msg) {

    }

    @Override
    public void hideDialog() {

    }
}
