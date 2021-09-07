package com.rfidresearchgroup.mifare;

public interface PRNGAdapter extends TestTaskAdapter {
    byte[] prepare(int sector, boolean getKeyA);
}