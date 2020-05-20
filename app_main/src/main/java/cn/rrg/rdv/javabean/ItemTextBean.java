package cn.rrg.rdv.javabean;

public class ItemTextBean extends ItemCommonBean {
    private String message;

    public ItemTextBean(String title) {
        super(title);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
