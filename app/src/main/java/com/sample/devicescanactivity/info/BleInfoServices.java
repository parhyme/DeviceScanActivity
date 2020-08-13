package com.sample.devicescanactivity.info;

import java.util.HashMap;

public class BleInfoServices {

    private static HashMap<String, BleInfoService> SERVICES = new HashMap<String, BleInfoService>();

    static {
        final BleGattService gapSerivce = new BleGattService();
        final BleGapService gattSerivce = new BleGapService();
        final BleDeviceInfoService deviceInfoSerivce = new BleDeviceInfoService();

        SERVICES.put(gapSerivce.getUUID(), gapSerivce);
        SERVICES.put(gattSerivce.getUUID(), gattSerivce);
        SERVICES.put(deviceInfoSerivce.getUUID(), deviceInfoSerivce);
    }

    public static BleInfoService getService(String uuid) {
        return SERVICES.get(uuid);
    }
}