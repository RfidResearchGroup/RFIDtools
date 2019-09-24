package cn.rrg.rdv.implement;

import cn.rrg.console.define.ICommandType;

public class EntryICommandType implements ICommandType {
    @Override
    public boolean isKey(String output) {
        return false;
    }

    @Override
    public String parseKey(String output) {
        return null;
    }

    @Override
    public boolean isData(String output) {
        return false;
    }

    @Override
    public String parseData(String output) {
        return null;
    }

    @Override
    public boolean isText(String output) {
        return false;
    }

    @Override
    public Runnable parseText(String output) {
        return null;
    }
}
