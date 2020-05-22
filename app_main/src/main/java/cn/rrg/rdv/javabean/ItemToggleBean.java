package cn.rrg.rdv.javabean;

public class ItemToggleBean extends ItemCommonBean {
    private boolean checked;

    public ItemToggleBean(String title) {
        super(title);
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
