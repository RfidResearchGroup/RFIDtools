package cn.rrg.rdv.activities.tools;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;

import cn.dxl.common.util.FileUtils;
import cn.rrg.rdv.R;
import cn.rrg.rdv.activities.main.BaseActivity;
import cn.rrg.rdv.binder.FileInfoBinder;
import cn.rrg.rdv.javabean.FileBean;
import cn.rrg.rdv.util.DumpUtils;
import me.drakeet.multitype.Items;
import me.drakeet.multitype.MultiTypeAdapter;

public abstract class FileListActivity extends BaseActivity {

    public enum MODE {
        EDIT,
        SELECT
    }

    protected MultiTypeAdapter multiTypeAdapter;
    protected Items items = new Items();
    protected AlertDialog workDialog;
    protected ImageButton btnAdd;

    protected MODE mode;

    protected FileFilter fileFilter;
    protected String initPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_file_list);

        multiTypeAdapter = new MultiTypeAdapter(items);

        initViews();
        initActions();

        Intent intent = getIntent();
        // parse mode!
        mode = (MODE) intent.getSerializableExtra("mode");
        if (mode == null) {
            throw new RuntimeException("The mode param is null");
        }

        fileFilter = onInitFilter();
        initPath = onInitPath();

        initDatas();
    }

    private void initViews() {
        RecyclerView rvDumpList = findViewById(R.id.rvDumpList);
        rvDumpList.setAdapter(multiTypeAdapter);
        rvDumpList.setLayoutManager(new LinearLayoutManager(context));

        multiTypeAdapter.register(FileBean.class, new FileInfoBinder());

        workDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.woring)
                .setView(View.inflate(context, R.layout.dialog_working_msg, null))
                .create();

        btnAdd = findViewById(R.id.btnAdd);
    }

    private void initActions() {
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAdd();
            }
        });
    }

    protected void initDatas() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Uri uri = Uri.fromFile(new File(initPath));
                traverFile2UseRawFile(uri);
            }
        }).start();
    }

    protected void traverFile2UseRawFile(Uri path) {
        items.clear();
        showOrDismissDialog(true);
        LinkedList<File> list = new LinkedList<>();
        if (path != null) {
            String prePath = FileUtils.getFilePathByUri(path);
            if (prePath != null) {
                list.add(new File(prePath));
            } else {
                return;
            }
        }
        // 版本不同可以导致迭代思路不同!
        while (!list.isEmpty()) {
            File f = list.poll();
            if (f != null) {
                // 处理迭代逻辑!
                if (f.isFile()) { // 如果是文件，并且后缀是我们需要的，则进行保存!
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // TODO 是文件!
                            StringBuilder info = new StringBuilder();
                            Date date = new Date(f.lastModified());
                            String dateStr = SimpleDateFormat.getDateTimeInstance().format(date);
                            // 是文件，则拼接大小信息 + 最后修改日期!
                            info.append(FileUtils.getFileLengthIfFile(f));
                            // 拼接最后的修改日期!
                            info.append(" | ").append(dateStr);
                            items.add(new FileBean(
                                    f.isFile(),
                                    f.getName(),
                                    f.getPath(),
                                    info.toString(),
                                    false) {
                                @Override
                                public void onClick() {
                                    if (mode == MODE.EDIT) {
                                        onEdit(this);
                                    } else {
                                        Intent intent = new Intent().putExtra("file", getPath());
                                        setResult(Activity.RESULT_OK, intent);
                                        finish();
                                    }
                                }

                                @Override
                                public boolean onLongClick() {
                                    // TODO 实现文件删除或者共享（发送）
                                    return true;
                                }
                            });
                            updateViews();
                        }
                    }).start();
                } else {
                    // 加入队列，以便进入接下来的迭代!
                    File[] files = f.listFiles(fileFilter);
                    // 进行条件循环!
                    if (files != null) {
                        list.addAll(Arrays.asList(files));
                    }
                }
            }
        }
        showOrDismissDialog(false);
    }

    protected void showOrDismissDialog(boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (show && !workDialog.isShowing()) {
                    workDialog.show();
                } else if (!show && workDialog.isShowing()) {
                    workDialog.dismiss();
                }
            }
        });
    }

    protected void updateViews() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                multiTypeAdapter.notifyDataSetChanged();
            }
        });
    }

    protected abstract FileFilter onInitFilter();

    protected abstract String onInitPath();

    protected abstract void onEdit(FileBean fileBean);

    protected abstract void onAdd();
}
