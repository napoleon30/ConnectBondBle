package cn.sharelink.yiluoble.util;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 逻辑是通过扫描到Ble蓝牙中的address进行连接，然后获取Ble中的服务,然后通过服务来获取Catt
 */
public class BleDevice implements Parcelable{
    private String address;
    private String name;
    private boolean isConnectState;

    public boolean isConnectState() {
        return isConnectState;
    }

    public void setIsConnectState(boolean isConnectState) {
        this.isConnectState = isConnectState;
    }

    /**
     * 这是一个服务的集合
     * 在Ble设备中一个服务对应了很多的uuid
     * 在这里将服务的uuid作为key存储,然后value是服务对应的众多的uuid的字符串
     */
    private List<Map<String,String>> services;

//    /**
//     * 存放服务中的属性集合
//     */
//    private List<List<Map<String,String>>> gattDatas;

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics;

    public ArrayList<ArrayList<BluetoothGattCharacteristic>> getmGattCharacteristics() {
        return mGattCharacteristics;
    }

    public void setmGattCharacteristics(ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics) {
        this.mGattCharacteristics = mGattCharacteristics;
    }

//    public void setGattDatas(List<List<Map<String, String>>> gattDatas) {
//        this.gattDatas = gattDatas;
//    }
//
//    public List<List<Map<String, String>>> getGattDatas() {
//        return gattDatas;
//    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Map<String,String>> getServices() {
        return services;
    }

    public void setServices(List<Map<String,String>> services) {
        this.services = services;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BleDevice bleDevice = (BleDevice) o;

        if (address != null ? !address.equals(bleDevice.address) : bleDevice.address != null)
            return false;
        return !(name != null ? !name.equals(bleDevice.name) : bleDevice.name != null);

    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public int describeContents() {
        return 2;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(name);
    }

    public static final Creator<BleDevice> CREATOR = new Creator<BleDevice>() {
        @Override
        public BleDevice createFromParcel(Parcel source) {
            BleDevice device = new BleDevice();
            device.setAddress(source.readString());
            device.setName(source.readString());
            return device;
        }

        @Override
        public BleDevice[] newArray(int size) {
            return new BleDevice[size];
        }
    };
}
