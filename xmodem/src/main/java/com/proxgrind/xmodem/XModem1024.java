package com.proxgrind.xmodem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.dxl.com.Communication;

public class XModem1024 extends AbstractXModem {

    //起始头!
    private byte STX = 0x02;

    public XModem1024(Communication com) {
        super(com);
    }

    @Override
    public boolean send(InputStream sources) throws IOException {
        return false;
    }

    @Override
    public boolean recv(OutputStream target) throws IOException {
        return false;
    }
}
