package cn.rrg.rdv.presenter;

import cn.dxl.mifare.MfDataUtils;
import cn.rrg.rdv.javabean.M1KeyBean;
import cn.rrg.rdv.callback.WriterCallback;
import cn.rrg.rdv.models.AbsTagWriteModel;
import cn.rrg.rdv.view.TagWriteView;

public abstract class AbsTagWritePresenter extends BasePresenter<TagWriteView> {
    WriterCallback callback = new WriterCallback() {
        @Override
        public void onFinish() {
            if (isViewAttach()) {
                view.onWriteFinish();
            }
        }

        @Override
        public void onDataInvalid() {
            if (isViewAttach()) {
                view.onDataInvalid();
            }
        }

        @Override
        public void onTagAbnormal() {
            if (isViewAttach()) {
                view.onTagAbnormal();
            }
        }
    };

    private AbsTagWriteModel writeModel;

    // 写单块实现!
    public void writeNormallOne() {
        if (isViewAttach()) {
            String data = view.getData();
            int sector = view.getSector();
            int block = view.getBlock();
            M1KeyBean[] keyBeans = view.getKeyBeanForOne();
            if (data != null && keyBeans != null && keyBeans.length > 0 && sector != -1 && block != -1) {
                writeModel = getWriteModel();
                writeModel.reset();
                block = MfDataUtils.sectorToBlock(sector) + block;
                writeModel.writeBlock(
                        view.isWriteManufacturerAllow(),
                        block, keyBeans[0],
                        data,
                        callback);
            }
        }
    }

    // 写全部的扇区的实现!
    public void writeNormallAll() {
        if (isViewAttach()) {
            writeModel = getWriteModel();
            writeModel.reset();
            writeModel.writeSector(
                    view.isWriteManufacturerAllow(),
                    view.isWriteSecOrderImplement(),
                    view.getDatas(),
                    view.getKeyBeanForAll(),
                    callback);
        }
    }

    // 写单块实现!
    public void writeSpecoalOne() {
        if (isViewAttach()) {
            String data = view.getData();
            int sector = view.getSector();
            int block = view.getBlock();
            M1KeyBean[] keyBeans = view.getKeyBeanForOne();
            if (data != null && keyBeans != null && keyBeans.length > 0 && sector != -1 && block != -1) {
                writeModel = getWriteModel();
                writeModel.reset();
                block = MfDataUtils.sectorToBlock(sector) + block;
                writeModel.writeBlock(block, data, callback);
            }
        }
    }

    // 写全部的扇区的实现!
    public void writeSpecialAll() {
        if (isViewAttach()) {
            writeModel = getWriteModel();
            writeModel.reset();
            writeModel.writeSector(view.getDatas(), callback);
        }
    }

    protected abstract AbsTagWriteModel getWriteModel();
}
