package com.example.simplebluetooth;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class gattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String CUSTOM_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";
    public static String READ_CHARACTERISTIC = "0000fff2-0000-1000-8000-00805f9b34fb";
    public static String WRITE_CHARACTERISTIC = "0000fff3-0000-1000-8000-00805f9b34fb";
    static {
        // Sample Services.
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Generic Access Service");
        attributes.put("00001801-0000-1000-8000-00805f9b34fb", "Generic Attribute Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        // Sample Characteristics.
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put(CLIENT_CHARACTERISTIC_CONFIG, "Client Characteristic Config");
        attributes.put(CUSTOM_SERVICE, "PH Service");
        attributes.put(READ_CHARACTERISTIC, "PH Read");
        attributes.put(WRITE_CHARACTERISTIC, "PH Write");
    }
    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}