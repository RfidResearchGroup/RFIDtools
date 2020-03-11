package cn.dxl.mifare;

public interface NestedAdapter extends TestTaskAdapter {
    /**
     * 以一个已知秘钥测试其他的秘钥!
     *
     * @param srcSector    已知的秘钥的扇区
     * @param srcKey       已知的秘钥
     * @param isKeyA       已知的秘钥的类型
     * @param targetSector 欲测试的扇区
     * @param getKeyA      欲获得的扇区的秘钥类型
     */
    void prepare(int srcSector, byte[] srcKey, boolean isKeyA, int targetSector, boolean getKeyA);
}
