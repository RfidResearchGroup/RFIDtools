package cn.rrg.rdv.view;

public interface BaseMvpView {
    void showToast(String msg);

    void showDialog(String title, String msg); 

    void hideDialog();
}
