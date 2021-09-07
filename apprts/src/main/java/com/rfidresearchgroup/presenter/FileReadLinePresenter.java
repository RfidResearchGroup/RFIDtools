package com.rfidresearchgroup.presenter;

import com.rfidresearchgroup.callback.DumpCallback;
import com.rfidresearchgroup.models.DumpModel;
import com.rfidresearchgroup.view.FileReadLineView;

import java.io.File;

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
