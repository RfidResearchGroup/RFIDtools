package cn.dxl.mifare;

public interface PRNGAdapter extends TaskAdapter {
    byte[] doPRNGTest(int sector, boolean getKeyA);
}