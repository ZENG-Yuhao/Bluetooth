package com.example.enzo.bluetooth.Bluetooth;

import java.util.UUID;

/**
 * <p>
 * Created by ZENG Yuhao(Enzo)<br>
 * Contact: enzo.zyh@gmail.com
 * </p>
 */

public class MyUUID {
    public static final String STR_UUID = "D228C8DA-991C-4D39-9995-AF60A9D7093D";

    public static UUID getUUID() {
        return UUID.fromString(STR_UUID);
    }
}
