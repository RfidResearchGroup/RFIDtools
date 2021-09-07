package com.proxgrind.devices;

import com.proxgrind.com.Communication;
import com.proxgrind.com.DeviceChecker;

import java.io.IOException;

public class EmptyDeivce extends DeviceChecker {

    public EmptyDeivce(Communication communication) {
        super(communication);
    }

    @Override
    protected boolean checkDevice() throws IOException {
        return true;
    }


    @Override
    public void close() throws IOException {
    }
}
