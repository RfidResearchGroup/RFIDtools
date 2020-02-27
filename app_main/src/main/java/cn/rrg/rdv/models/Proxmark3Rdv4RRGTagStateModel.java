package cn.rrg.rdv.models;

import cn.rrg.natives.PM3Rdv4RRGMifare;

public class Proxmark3Rdv4RRGTagStateModel extends AbsTagStateModel {
    private PM3Rdv4RRGMifare mifare = PM3Rdv4RRGMifare.get();

    @Override
    protected boolean checkTagState() {
        if (mifare.scanning()) {
            if (mifare.connect()) {
                mifare.close();
                return true;
            } else {
                mifare.close();
                return false;
            }
        } else {
            mifare.close();
            return false;
        }
    }

    @Override
    protected boolean checkTagMagic() {
        if (!mifare.scanning()) {
            mifare.close();
            return false;
        }
        if (mifare.connect()) {
            mifare.close();
            if (mifare.unlock()) {
                mifare.close();
                return true;
            }
        }
        mifare.close();
        return false;
    }
}
