package cn.rrg.rdv.callback;

public interface DumpCallback {
    /*
     * 在dump内容无误时的回调
     */
    void showContents(String[] contents);

    /*
    * dump读取失败
    * */
    void onFileException();

    /*
    * dump格式错误!
    * */
    void onFormatNoSupport();

    /*
    * 成功后的回调
    * */
    void onSuccess();

}
 