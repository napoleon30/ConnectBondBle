/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.sharelink.yiluoble.util;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    //这样写只是赋了一个常量值
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";

    //the descriptor of battery characteristic(battery service)
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public static String YJ_BLE_Service = "0000ff00-0000-1000-8000-00805f9b34fb";
    public static String YJ_BLE_WRITE = "0000ff01-0000-1000-8000-00805f9b34fb";
    public static String YJ_BLE_READ_NOTIFY = "0000ff02-0000-1000-8000-00805f9b34fb";

    static {

        // Sample Services.给自己用到的服务命名
        attributes.put("0000fff0-0000-1000-8000-00805f9b34fb", "颐佳经络仪");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "经络仪设备信息");

        //Sample Characteristics.给自己用到的特征值命名
        attributes.put(YJ_BLE_WRITE, "WRITE");
        attributes.put("YJ_BLE_READ_NOTIFY", "READ");

        attributes.put("00002a37-0000-1000-8000-00805f9b34fb", "YJ Name");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }
    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
