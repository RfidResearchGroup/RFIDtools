package cn.rrg.rdv.view;

public interface DumpEditotView extends BaseMvpView {

    /*
     * 显示dump的内容
     */
    void showDumpContent(String[] contents);

    /*
     * dump读取失败
     * */
    void onFileException();

    /*
     * dump格式错误!
     * */
    void onFormatNoSupport();

    void onSuccess();
}
 