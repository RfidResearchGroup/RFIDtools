package cn.rrg.rdv.presenter;

import java.io.File;

import cn.rrg.rdv.callback.DumpCallback;
import cn.rrg.rdv.models.DumpModel;
import cn.rrg.rdv.view.DumpEditotView;

public class DumpPresenter extends BasePresenter<DumpEditotView> {

    private DumpCallback mContentCallback = new DumpCallback() {
        @Override
        public void showContents(String[] contents) {
            if (isViewAttach()) view.showDumpContent(contents);
        }

        @Override
        public void onFileException() {
            if (isViewAttach()) view.onFileException();
        }

        @Override
        public void onFormatNoSupport() {
            if (isViewAttach()) view.onFormatNoSupport();
        }

        @Override
        public void onSuccess() {
            if (isViewAttach()) view.onSuccess();
        }
    };

    /*
     * 显示dump内容从文件中
     * */
    public void showContents(File dump) {
        DumpModel.showContents(dump, mContentCallback);
    }

    /*
     * 显示dump内容从传输过来的字符串数组中
     * */
    public void showContents(String[] datas) {
        DumpModel.showContents(datas, mContentCallback);
    }

    /*
     * 保存当前对dump的操作
     */
    public void saveDumpModify(String[] src, File file) {
        DumpModel.saveDumpModify(mContentCallback, src, file);
    }

}
