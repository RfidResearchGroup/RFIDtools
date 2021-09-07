package com.rfidresearchgroup.common.widget;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rfidresearchgroup.common.R;
import com.rfidresearchgroup.common.util.ViewUtil;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class FilesSelectorDialog extends FillParentWidthDialog {
    private static String LOG_TAG = FilesSelectorDialog.class.getName();

    /*
     * 回调接口
     */
    private OnSelectListener mOnSelectListener;
    private OnSelectsListener mOnSelectsListener;
    private OnNoSelectListener mOnNoSelectListener;
    private OnUnSelectListener mOnUnSelectListener;
    /*
     * 控制标志
     * */
    private boolean mIsCanMultiple = false;
    private boolean mIsCanGetPath = false;
    private boolean mIsCanBack = true;
    private boolean mIsCanDissmiss = true;
    /*
     * 视图
     * */
    private View mContentView = null;
    private TextView txtTitle = null;
    private ImageView imgIcon = null;
    private Button btnNegative = null;
    private Button btnPositive = null;
    private Button btnNeutral = null;
    private ProgressBar progOnLoading = null;
    private ListView lstvFileLIst = null;
    private SelectorAdapter selectorAdapter = null;
    private TextView txtShowPathAndMsg = null;
    /*
     * 内容
     * */
    private ArrayList<File> fileViewsList = new ArrayList<>(512);
    private ArrayList<File> fileSelectedList = new ArrayList<>(512);
    private File[] fileSelectedHistory = null;
    private File defaultPath = Environment.getExternalStorageDirectory();
    private FileFilter mFileFilter = null;
    private Comparator<File> comparator = new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {
            return o2.compareTo(o1);
        }
    };
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        //处理消息
        @Override
        public void handleMessage(Message msg) {
            //如果列表没有数据则需要提醒用户
            if (msg.what == -3) {
                fileViewsList.clear();
                selectorAdapter.notifyDataSetChanged();
                txtShowPathAndMsg.setText(String.format("%s：" + getContext().getString(R.string.empty_dir), msg.obj));
                return;
            }
            //如果传输开始则需要做出视图上的一些改变!
            if (msg.what == -2) {
                //先清除旧的数据缓存
                fileViewsList.clear();
                selectorAdapter.notifyDataSetChanged();
                //隐藏数据列表
                lstvFileLIst.setVisibility(View.INVISIBLE);
                //显示进度框
                progOnLoading.setVisibility(View.VISIBLE);
                return;
            }
            Bundle data = msg.getData();
            Object obj = data.getSerializable("f");
            //确认数据类型
            if (obj instanceof File) {
                // 添加数据!
                fileViewsList.add((File) obj);
                // 通知更新内容!
                selectorAdapter.notifyDataSetChanged();
            }
            //设置数据个数
            if (msg.arg1 != 0) {
                //设置上限值!
                progOnLoading.setMax(msg.arg1);
                //刷新进度条
                progOnLoading.incrementProgressBy(msg.arg2);
            }
            //如果传输完成则开始更新UID（-1标志位传输完成）
            if (msg.what == -1) {
                //通知更新数据显示
                selectorAdapter.notifyDataSetChanged();
                //显示数据列表
                lstvFileLIst.setVisibility(View.VISIBLE);
                //隐藏进度框
                progOnLoading.setVisibility(View.GONE);
                //设置当前的路径提示消息!
                txtShowPathAndMsg.setText((String) msg.obj);
            }
        }
    };
    private int preChioceItemIndex = -1;
    private boolean isMultipled = false;

    private FilesSelectorDialog(@NonNull Context context) {
        super(context);
        initViews();
        initActions();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //隐藏上面的工具栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.setView(mContentView);
        super.onCreate(savedInstanceState);
    }

    private void initViews() {
        mContentView = ViewUtil.inflate(getContext(), R.layout.file_selector);

        txtTitle = findViewById(R.id.txtView_FileSelect_Dialog_title);
        txtShowPathAndMsg = findViewById(R.id.txtShowPathAndMsg);

        imgIcon = findViewById(R.id.imgView_FileSelect_Dialog_icon);
        progOnLoading = findViewById(R.id.progressBar_onLoading);
        btnNegative = findViewById(R.id.btnFileSelector_negative);
        btnPositive = findViewById(R.id.btnFileSelector_positive);
        btnNeutral = findViewById(R.id.btnFileSelector_neutral);

        lstvFileLIst = findViewById(R.id.lstvFileSelectorList);
    }

    private void initActions() {
        //初始化适配器
        selectorAdapter = new SelectorAdapter(getContext(), R.layout.file_selector_info, fileViewsList);
        //设置适配器到listview
        lstvFileLIst.setAdapter(selectorAdapter);
        //需要设置事件，在选中项目的时候根据当前的模式做出相应的处理
        lstvFileLIst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO 这里做出处理
                File tmpFile = selectorAdapter.getItem(position);
                if (tmpFile != null) {
                    if (tmpFile.exists() && tmpFile.isDirectory()) {
                        //当前是文件夹，可以进入里面
                        defaultPath = tmpFile;
                        //清除ckBox的标志状态
                        selectorAdapter.clearSelecetedOnBack();
                        new LoadThread(tmpFile).start();
                    }
                }
            }
        });
        //设置取消事件
        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsCanDissmiss) {
                    //隐藏自己
                    FilesSelectorDialog.this.dismiss();
                    //尝试通知底层垃圾回收器回收资源!
                    System.gc();
                }
            }
        });
        //设置确认事件，根据模式回回调结果
        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnUnSelectListener != null) {
                    if (fileSelectedHistory != null && fileSelectedHistory.length > 0) {
                        ArrayList<File> tmp = new ArrayList<>();
                        for (File fileArrElement : fileSelectedHistory) {
                            // 已经选择的文件的列表里不包含我们设置过得历史选择，则是取消操作!
                            if (!fileSelectedList.contains(fileArrElement)) {
                                tmp.add(fileArrElement);
                                Log.d(LOG_TAG, "已经选择的文件的列表里不包含我们设置过得历史选择，则是取消操作: " + fileArrElement.getAbsolutePath());
                            }
                        }
                        Log.d(LOG_TAG, "取消选择的文件个数: " + tmp.size());
                        mOnUnSelectListener.onUnSelect(tmp.toArray(new File[0]));
                    }
                }
                //多选模式
                if (mIsCanMultiple) {
                    if (mOnSelectsListener != null) {
                        File[] files = fileSelectedList.toArray(new File[0]);
                        if (files.length > 0) {
                            mOnSelectsListener.selected(files);
                        } else {
                            //没有选择时回调
                            if (mIsCanGetPath) {
                                mOnSelectsListener.selected(new File[]{defaultPath});
                            } else {
                                if (mOnNoSelectListener != null) {
                                    mOnNoSelectListener.noSelect();
                                }
                            }
                        }
                    }

                } else {
                    //单选模式!
                    if (mOnSelectListener != null) {
                        File file = fileSelectedList.size() > 0 ? fileSelectedList.get(0) : null;
                        if (file != null) {
                            mOnSelectListener.selected(file);
                        } else {
                            //没有选择时回调
                            if (mIsCanGetPath) {
                                mOnSelectListener.selected(defaultPath);
                            } else {
                                if (mOnNoSelectListener != null) {
                                    mOnNoSelectListener.noSelect();
                                }
                            }
                        }
                    }
                }
                //销毁自身,回收资源
                FilesSelectorDialog.this.cancel();
                System.gc();
            }
        });
        //设置中立事件，实际是返回上级操作
        btnNeutral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //模拟按键点击。。
                FilesSelectorDialog.this.onKeyDown(KeyEvent.KEYCODE_BACK, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
            }
        });
    }

    @Override
    public void dismiss() {
        //在这两个地方清空被选中的文件集合
        fileSelectedList.clear();
        super.dismiss();
    }

    public void clear() {
        selectorAdapter.clearSelecetedOnBack();
        fileSelectedHistory = null;
        fileSelectedList.clear();
        fileViewsList.clear();
    }

    @Override
    public void cancel() {
        fileSelectedList.clear();
        super.cancel();
    }

    @Override
    public void show() {
        //清除可能存在的被选择过的历史记录
        selectorAdapter.clearSelecetedOnBack();
        fileSelectedList.clear();
        //调用父类显示
        super.show();
        //在这里重新加载
        new LoadThread(defaultPath).start();
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        /*处理事件，在点击了返回键时是后退而不是隐藏*/
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!mIsCanBack) return false;
            //在点击返回键的时候回退到上一级,直到无法回退
            String pathUp = defaultPath.getParent();
            if (pathUp != null) {
                File dir = new File(pathUp);
                defaultPath = dir;
                selectorAdapter.clearSelecetedOnBack();
                new LoadThread(dir).start();
            } else {
                //重置目录
                defaultPath = Environment.getExternalStorageDirectory();
                selectorAdapter.clearSelecetedOnBack();
                new LoadThread(defaultPath).start();
            }
        }
        return false;
    }

    @Override
    public <T extends View> T findViewById(int id) {
        //重写映射函数，使其从指定的view树种中寻找控件
        return (T) (mContentView.findViewById(id) != null ? mContentView.findViewById(id) : super.findViewById(id));
    }

    @Override
    public void setTitle(int titleId) {
        txtTitle.setText(titleId);
    }

    @Override
    public void setTitle(@Nullable CharSequence title) {
        txtTitle.setText(title);
    }

    public void setIcon(int resId) {
        imgIcon.setImageResource(resId);
    }

    public void setPathOnLoad(String path) {
        //需要判断一下传入的参数是否是一个路径，是的话再做替换
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            //做出替换!
            defaultPath = file;
            selectorAdapter.clear();
        }
    }

    public void addFileSelected(File[] files) {
        fileSelectedHistory = files;
    }

    public void setCanGetPath(boolean isCan) {
        mIsCanGetPath = isCan;
    }

    public void setCanBack(boolean isCan) {
        mIsCanBack = isCan;
    }

    public void setCanDissmiss(boolean isCan) {
        mIsCanDissmiss = isCan;
    }

    public void setCanMultiple(boolean isCan) {
        mIsCanMultiple = isCan;
    }

    public void setFileFilter(FileFilter filter) {
        mFileFilter = filter;
    }

    public void setOnSelectListener(OnSelectListener listener) {

        mOnSelectListener = listener;
    }

    public void setOnSelectsListener(OnSelectsListener listener) {

        mOnSelectsListener = listener;
    }

    public void setOnNoSelectListener(OnNoSelectListener listener) {

        mOnNoSelectListener = listener;
    }

    public void setOnUnSelectListener(OnUnSelectListener listener) {

        mOnUnSelectListener = listener;
    }

    public static class Builder {

        private FilesSelectorDialog mDialog;
        private FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                //过滤文件夹，只选择文件
                return pathname.isFile();
            }
        };
        private FileFilter dirFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                //过滤文件，只选择文件夹
                return pathname.isDirectory();
            }
        };

        public Builder(Context context) {
            mDialog = new FilesSelectorDialog(context);
        }

        public Builder setTitle(int resId) {
            mDialog.setTitle(resId);
            return this;
        }

        public Builder setTitle(CharSequence cs) {
            mDialog.setTitle(cs);
            return this;
        }

        public Builder setCanMultiple(boolean isCan) {
            mDialog.setCanMultiple(isCan);
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            mDialog.setCancelable(cancelable);
            return this;
        }

        public Builder setPathOnLoad(String path) {
            mDialog.setPathOnLoad(path);
            return this;
        }

        public Builder setFilesFilter(FileFilter filter) {
            mDialog.setFileFilter(filter);
            return this;
        }

        public Builder setIcon(int resId) {
            mDialog.setIcon(resId);
            return this;
        }

        public Builder setPositiveButtonText(String text) {
            mDialog.btnPositive.setText(text);
            return this;
        }

        public Builder setNegativeButtonText(String text) {
            mDialog.btnNegative.setText(text);
            return this;
        }

        public Builder setOnNoSelectListener(OnNoSelectListener listener) {
            mDialog.setOnNoSelectListener(listener);
            return this;
        }

        public Builder setOnSelectListener(OnSelectListener listener) {
            mDialog.setOnSelectListener(listener);
            return this;
        }

        public Builder setOnSelectsListener(OnSelectsListener listener) {
            mDialog.setOnSelectsListener(listener);
            return this;
        }

        public Builder setOnDismissListener(OnDismissListener listener) {
            mDialog.setOnDismissListener(listener);
            return this;
        }

        public Builder setFileFilter() {
            setFilesFilter(fileFilter);
            return this;
        }

        public Builder setDirFilter() {
            setFilesFilter(dirFilter);
            return this;
        }

        public Builder setCanBack(boolean isCan) {
            mDialog.setCanBack(isCan);
            return this;
        }

        public Builder setCanGetPath(boolean isCan) {
            mDialog.setCanGetPath(isCan);
            return this;
        }

        public Builder setCanDismiss(boolean isCan) {
            mDialog.setCanDissmiss(isCan);
            return this;
        }

        /*最终创建外部类的对象!*/
        public FilesSelectorDialog create() {
            return mDialog;
        }
    }

    private class SelectorAdapter extends ArrayAdapter<File> {
        /*
         * 存放ckBox状态
         * */
        private SparseBooleanArray mSelecteState = new SparseBooleanArray();

        private int resId;

        SelectorAdapter(Context context, int subItemLayout, List<File> datas) {
            super(context, subItemLayout, datas);
            resId = subItemLayout;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            //视图优化第一项
            View view = convertView;
            if (view == null) view = ViewUtil.inflate(getContext(), resId);
            //在这里解析显示信息
            File file = getItem(position);
            //判断这个文件是否被参数控制为已经选择的状态!
            boolean isSelected = false;
            if (file != null && fileSelectedHistory != null && fileSelectedHistory.length > 0) {
                for (File tmp : fileSelectedHistory) {
                    if (tmp.equals(file)) {
                        // 如果俩文件相同，则说明是被选中的那个!
                        isSelected = true;
                        break;
                    }
                }
            }
            //性能优化第二项
            MyHolder holder = (MyHolder) view.getTag();
            TextView showInfo;
            ImageView showType;
            final CheckBox showState;
            //设置名字
            if (holder != null) {
                showInfo = holder.info;
                showType = holder.type;
                showState = holder.state;
            } else {
                showInfo = view.findViewById(R.id.txtShowFileInfo);
                showType = view.findViewById(R.id.imgView_Selector_File_Type);
                showState = view.findViewById(R.id.ckBoxFileSelectorState);
                //在find后缓存
                holder = new MyHolder();
                holder.info = showInfo;
                holder.type = showType;
                holder.state = showState;
                view.setTag(holder);
            }
            //恢复ckBox的状态
            if (mSelecteState.get(position) || isSelected) {
                //如果在标记中存在该定位的选项，则将该定位的视图设置缓存的标记
                holder.state.setChecked(true);
                // 如果是历史选择判断结果，则将相关的实例放入到缓存中!
                if (isSelected) {
                    fileSelectedList.add(file);
                }
            } else {
                holder.state.setChecked(false);
            }
            //设置ckBox的事件.不能使用checked事件，这会导致其他的视图也调用checked事件
            showState.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //设置ckBox的状态记录，如果勾选了，则保存到map中
                    boolean isChecked = showState.isChecked();
                    //判断键值对数组内的元素个数，是否是多选了
                    isMultipled = mSelecteState.size() >= 1;
                    //判断是取消勾选，否则添加进去数据集
                    if (isChecked) {
                        mSelecteState.put(position, true);
                        fileSelectedList.add(SelectorAdapter.this.getItem(position));
                        //缓存到全局
                        if (!mIsCanMultiple)
                            if (!isMultipled)
                                preChioceItemIndex = position;
                    } else {
                        mSelecteState.delete(position);
                        fileSelectedList.remove(SelectorAdapter.this.getItem(position));
                        if (!mIsCanMultiple)
                            if (!isMultipled)
                                preChioceItemIndex = -1;
                    }
                    //单选视图更新
                    if (!mIsCanMultiple && isMultipled) {
                        int tmpPos = preChioceItemIndex;
                        if (tmpPos >= 10) tmpPos = preChioceItemIndex % 10;
                        //不可以多选,去除其他的被勾选的选项
                        if (tmpPos != -1) {
                            View tmpChildView = lstvFileLIst.getChildAt(tmpPos);
                            if (tmpChildView != null) {
                                MyHolder tmpChildHolder = (MyHolder) tmpChildView.getTag();
                                if (tmpChildHolder != null) {
                                    //得到当前的视图后设置控件的选择状态为不选择
                                    tmpChildHolder.state.setChecked(false);
                                    mSelecteState.delete(preChioceItemIndex);
                                    // FIXME: 2019/4/17
                                    //对于单选文件列表，永远只有一个被选择，
                                    //多出来的一个永远需要移除，那便是0索引上的文件!
                                    if (fileSelectedList.size() > 0)
                                        fileSelectedList.remove(0);
                                    //重置标志位!
                                    isMultipled = false;
                                    //记录取消前一个选择后当前单选的索引，准备给下一次多选判断使用
                                    preChioceItemIndex = position;
                                    lstvFileLIst.requestLayout();
                                    SelectorAdapter.this.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                }
            });
            if (file != null && file.exists()) {
                if (file.isFile()) {
                    showInfo.setText(file.getName());
                    //TODO 这里做出其他的状态（icon）切换
                    showType.setImageResource(R.drawable.file_file);
                }
                if (file.isDirectory()) {
                    showInfo.setText(file.getName());
                    showType.setImageResource(R.drawable.file_directory);
                }
            }
            return view;
        }

        private class MyHolder {
            ImageView type;
            TextView info;
            CheckBox state;
        }

        //当回退之后需要清除选择的状态
        private void clearSelecetedOnBack() {
            mSelecteState.clear();
        }
    }

    private class LoadThread extends Thread {

        //要开始加载的路径
        private File mDir;

        LoadThread(File dir) {
            mDir = dir;
        }

        @Override
        public void run() {
            //默认从根目录开始加载!,如果用户做了更改，则需要从用户指定的目录开始进入
            File[] files = mFileFilter != null ? mDir.listFiles(mFileFilter) : mDir.listFiles();
            //判断当前目录下是否有文件
            if (files == null || files.length <= 0) {
                Message message = Message.obtain();
                message.obj = mDir.getPath();
                message.what = -3;
                mHandler.sendMessage(message);
                return;
            }
            //排序
            Arrays.sort(files, comparator);
            //先发消息让handler做出视图上的一些改变
            mHandler.sendEmptyMessage(-2);
            for (int i = 0; i < files.length; i++) {
                //全部获得了文件列表之后用handler向消息队列提交消息
                Message msg = mHandler.obtainMessage();
                //用arg1储存数据个数!
                msg.arg1 = files.length;
                Bundle data = new Bundle();
                data.putSerializable("f", files[i]);
                msg.setData(data);
                //用arg2储存当前位置
                msg.arg2 = i;
                //发送消息到主线程
                mHandler.sendMessage(msg);
            }
            //在传输完了之后发送确认消息到主线程让主线程开始更新UI
            Message finallyMsg = Message.obtain();
            finallyMsg.what = -1;
            finallyMsg.obj = mDir.getPath();
            mHandler.sendMessage(finallyMsg);
        }

    }

    public interface OnSelectListener {
        /**
         * @param file 单选时的回调
         */
        void selected(File file);
    }

    public interface OnSelectsListener {
        /**
         * @param files 多选时的回调
         */
        void selected(File[] files);
    }

    public interface OnNoSelectListener {
        void noSelect();
    }

    public interface OnUnSelectListener {
        void onUnSelect(File[] file);
    }
}
