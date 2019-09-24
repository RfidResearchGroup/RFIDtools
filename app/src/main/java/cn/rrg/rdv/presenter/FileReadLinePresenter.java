package cn.rrg.rdv.presenter;

import java.io.File;

import cn.rrg.rdv.callback.DumpCallback;
import cn.rrg.rdv.models.DumpModel;
import cn.rrg.rdv.view.FileReadLineView;

public class FileReadLinePresenter extends BasePresenter<FileReadLineView> {
    public void load(File file) {
        DumpModel.showContents(file, new DumpCallback() {
            @Override
            public void showContents(String[] contents) {
                if (isViewAttach()) view.onReadFinish(contents);
            }

            @Override
            public void onFileException() {
                if (isViewAttach()) view.onReadFail("error");
            }

            @Override
            public void onFormatNoSupport() {
                if (isViewAttach()) view.onReadFail("error");
            }

            @Override
            public void onSuccess() {

            }
        });
    }
}
