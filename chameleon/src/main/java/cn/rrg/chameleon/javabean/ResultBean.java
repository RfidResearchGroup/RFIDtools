package cn.rrg.chameleon.javabean;

public class ResultBean {
    private String id;
    private int block;
    private int sector;
    private boolean isKeyA;
    private String key;

    public int getSector() {
        return sector;
    }

    public void setSector(int sector) {
        this.sector = sector;
    }

    public int getBlock() {
        return block;
    }

    public void setBlock(int block) {
        this.block = block;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isKeyA() {
        return isKeyA;
    }

    public void setKeyA(boolean keyA) {
        isKeyA = keyA;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "块: " + getBlock() + "\n" +
                "扇区: " + getSector() + "\n" +
                "类型:" + (isKeyA ? "A" : "B") + "\n" +
                "密钥: " + getKey() + "\n" +
                "UID: " + getId();
    }
}
