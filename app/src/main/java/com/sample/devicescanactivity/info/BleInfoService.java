package com.sample.devicescanactivity.info;

public abstract class BleInfoService {
    private final static String TAG = BleInfoService.class.getSimpleName();

    protected BleInfoService() {
    }

    public abstract String getUUID();

    public abstract String getName();

    public abstract String getCharacteristicName(String uuid);
}