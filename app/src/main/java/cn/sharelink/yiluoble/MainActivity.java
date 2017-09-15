package cn.sharelink.yiluoble;

import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.sharelink.yiluoble.util.BleDevice;
import cn.sharelink.yiluoble.util.BluetoothLeService;
import cn.sharelink.yiluoble.util.CheckSum;
import cn.sharelink.yiluoble.util.ItonAdecimalConver;
import cn.sharelink.yiluoble.util.SampleGattAttributes;
import cn.sharelink.yiluoble.util.SharedPrefsUtil;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_ble_name)
    TextView tvBleName;
    @BindView(R.id.rl_left)
    RelativeLayout rlLeft;
    @BindView(R.id.rl_bg)
    RelativeLayout rlBg;
    @BindView(R.id.rl_right)
    RelativeLayout rlRight;
    @BindView(R.id.lable)
    LinearLayout lable;
    @BindView(R.id.btn_connect)
    Button btnConnect;

    byte[] data;
    String message1 = "6974617A"; //命令头 ACK_TYPE
    String message2 = null; // 命令种类
    String message3 = "01"; // ACK_TRUE
    String message4 = "0000"; // 长度
    String message5 = "00"; // 校验位

    boolean isRecord = false;
    boolean isVoice = false;
    boolean isVedio = false;
    @BindView(R.id.rl_down)
    RelativeLayout rlDown;

    private ProgressDialog myDialog;

    BluetoothDevice bluetoothDevice;
    BleDevice bleDevice;
    private String mDeviceAddress = null;
    private String mDeviceAddress_old;//上次连接的蓝牙地址
    private String bleName;
    private String bleName_old;
    private String str;
    private BluetoothLeService mBluetoothLeService;//自定义的一个继承自Service的服务
    List<String> addresses;
//    private List<String> scanBleListAddress;

    private final static String TAG = MainActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final int REQUEST_OPEN_BT_CODE = 1;

    private boolean mConnected = false;
    private boolean isDiscovered = false;
    BluetoothAdapter adapter;
    boolean isToBleSet = false;

    public int getConnectBle() {
        return connectBle;
    }

    public void setConnectBle(int connectBle) {
        this.connectBle = connectBle;
    }

    int connectBle = 1; //0表示连接蓝牙，1表示未连接蓝牙
    boolean isBindService = false; //判断服务是否被绑定

    private BluetoothGattCharacteristic mCharacteristicWrite, mCharacteristicReadNotify;
    private BluetoothGattService mnotyGattService;//三个长得很像，由大到小的对象BluetoothGatt、
    //BluetoothGattService、BluetoothGattCharacteristic

    //蓝牙服务和特征值
    private static final UUID uuid = UUID
            .fromString(SampleGattAttributes.YJ_BLE_Service);
    private static final UUID UUID_WRITE = UUID
            .fromString(SampleGattAttributes.YJ_BLE_WRITE);
    private static final UUID UUID_READ_NOTIFY = UUID
            .fromString(SampleGattAttributes.YJ_BLE_READ_NOTIFY);

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        //服务连接建立之后的回调函数。

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            Log.e(TAG, "建立服务");
            isBindService = true;
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            Log.e("MainActivity", "onServiceDisconnected");
        }
    };

    // Handles various events fired by the Service.
    // data:This can be a result of read or notification operations.
    // 接受来自设备的数据，可以通过读或通知操作获得。

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            //通过intent获得的不同action，来区分广播该由谁接收(只有action一致,才能接收)。
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                SharedPrefsUtil.putStringValue(MainActivity.this, "mDeviceAddress", mDeviceAddress);
                Log.e(TAG, "连接");
                Toast.makeText(MainActivity.this, "蓝牙连接", Toast.LENGTH_LONG).show();

                setConnectBle(0);
                tvBleName.setText(bleName);
                tvBleName.setTextColor(Color.rgb(0, 255, 0));
                btnConnect.setVisibility(View.INVISIBLE);
                btnConnect.setEnabled(false);
                btnConnect.setBackgroundResource(R.mipmap.connect1);
                if (myDialog.isShowing() && isDiscovered == true && mConnected ==true){
                    myDialog.dismiss();
                }

                Log.e("MainActivity", "connectBle: " + getConnectBle());
//                if (getConnectBle() == 0) { ////////////////
//                    tvBleName.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            showRenameDialog();
//                        }
//                    });
//                }
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                isDiscovered =false;
                Log.e(TAG, "未连接");
                if (myDialog.isShowing()) {
                    myDialog.dismiss();
                }
                tvBleName.setTextColor(Color.rgb(120, 120, 120));
                btnConnect.setVisibility(View.VISIBLE);
                btnConnect.setBackgroundResource(R.drawable.btn_connect_icon);
                btnConnect.setEnabled(true);
//                mBluetoothLeService.connect(bluetoothDevice.getAddress()); ////////////**********

                Toast.makeText(MainActivity.this, "蓝牙连接失败", Toast.LENGTH_LONG).show();

//                mBluetoothLeService.disconnect();
//                mBluetoothLeService.close();

                setConnectBle(1);
                unregisterReceiver(mGattUpdateReceiver);
                if (isBindService == true) {
                    unbindService(mServiceConnection);
                    isBindService = false;
                }

                mBluetoothLeService = null;
                Log.e("MainActivity", "connectBle" + getConnectBle());
            }
            //发现服务后，自动执行回调方法onServicesDiscovered(),发送一个action=ACTION_GATT_SERVICES_DISCOVERED的广播，其他情况同理
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the UI.
                Log.e(TAG, "DisCovered"); ////////如果蓝牙已经连接，而没有发现服务的Discovered，则数据无法write出去，如果在Discovered之前先write,则Discovered后，仍无法write
                isDiscovered = true;
                if (isDiscovered == true && mConnected ==true){
                    myDialog.dismiss();
                }
                mnotyGattService = mBluetoothLeService.getSupportedGattServices(uuid);//找特定的某个服务
                mCharacteristicWrite = mnotyGattService.getCharacteristic(UUID_WRITE);//获取可写的特征值
                mCharacteristicReadNotify = mnotyGattService.getCharacteristic(UUID_READ_NOTIFY);//获取有通知特性的特征值

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.e(TAG, "DATA AVAILABLE");
                str = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);   //接收广播发送的数据，不管读写，一律接收
                Log.e(TAG, "read : " + str);    //必须偶数个16进制字符发送，否则易丢失


                // 6974617A 001B 01 0001 0000 修改蓝牙名称成功的返回值
                // 6974617A 001B 00 0000 00  失败的返回值
                if (str.startsWith("6974617A")) {
                    String commandType = str.substring(8, 12); //001B
                    String responeT = str.substring(12, 14); //01
                    String res = str.substring(18, 20); //00
                    if (commandType.equals("001B") && responeT.equals("01")) {
                        if (res.equals("00")) {
                            Toast.makeText(MainActivity.this, "设备名称修改成功", Toast.LENGTH_SHORT).show();
                        } else if (res.equals("01")) {
                            Toast.makeText(MainActivity.this, "输入值超过取值范围", Toast.LENGTH_SHORT).show();
                        }

                    } else if (responeT.equals("00")) {
                        Toast.makeText(MainActivity.this, "设备名称修改失败", Toast.LENGTH_SHORT).show();
                    }
                }


            }
        }


    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Log.e("onCreate", "onCreate");

        bleDevice = new BleDevice();
        //Register a BroadcastReceiver to be run in the main activity thread.
//        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        adapter = BluetoothAdapter.getDefaultAdapter();
        mDeviceAddress_old = SharedPrefsUtil.getStringValue(MainActivity.this, "mDeviceAddress", null);
        if (mDeviceAddress_old!=null){
            bleName_old = adapter.getRemoteDevice(mDeviceAddress_old).getName();
        }
                Log.e(TAG,"old 地址/名称" + mDeviceAddress_old +"/"+bleName_old);
        addresses = new ArrayList<>();
//        scanBleListAddress = new ArrayList<>();


        controlDirection();

    }


    private void showRenameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_rename, null);
        builder.setView(view);
        final AlertDialog alert = builder.create();

        LinearLayout llRename = (LinearLayout) view.findViewById(R.id.ll_rename);
        TextView tvNameStart = view.findViewById(R.id.tv_name_start);
        final EditText etNameEnd = (EditText) view.findViewById(R.id.et_name_end);
        Button cancel = (Button) view.findViewById(R.id.cancel_btn);
        Button confirm = (Button) view.findViewById(R.id.confirm_btn);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mes4 = "ZJH-" + etNameEnd.getText().toString().trim();
                Log.e("MainActivity", "mes4: " + mes4);
                int length = mes4.length();
                Log.e("MainActivity", "mes4的LENGTH: " + length);
                String messag1 = "6974637A";
                String messag2 = "001B";
                String messag3 = "00" + ItonAdecimalConver.algorismToHEXString(length);
                Log.e("MainActivity", "messag3: " + messag3);
                String messag4 = ItonAdecimalConver.str2HexStr(mes4).replace(" ", "");
                Log.e("MainActivity", "messag4: " + messag4);
                String messag5 = CheckSum.checkSum(messag4);
                Log.e("MainActivity", "messag5: " + messag5);

                data = ItonAdecimalConver.hexStr2Bytes(messag1 + messag2 + messag3 + messag4 + messag5);
                Log.e("MainActivity", "data1111: " + data);
                //发送数据
                if (getConnectBle() == 0) {
                    write(data);
                }
                alert.dismiss();
            }
        });

        alert.show();
    }


    private void controlDirection() {
        rlLeft.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Vibrator vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
                        vibrator.vibrate(new long[]{0, 40}, -1);

                        rlBg.setBackgroundResource(R.mipmap.keyboard_left);
                        message2 = "8880";
                        data = ItonAdecimalConver.hexStr2Bytes(message1 + message2 + message3 + message4 + message5);
                        //发送数据
                        if (getConnectBle() == 0) {
                            write(data);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        rlBg.setBackgroundResource(R.mipmap.keyboard_normal);
                        message2 = null; // 命令种类
                        break;
                }
                return true;
            }
        });
        rlRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Vibrator vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
                        vibrator.vibrate(new long[]{0, 40}, -1);
                        rlBg.setBackgroundResource(R.mipmap.keyboard_right);
                        message2 = "8881";
                        data = ItonAdecimalConver.hexStr2Bytes(message1 + message2 + message3 + message4 + message5);
                        Log.e(TAG, "right:" + message1 + message2 + message3 + message4 + message5);
                        //发送数据
                        if (getConnectBle() == 0) {
                            write(data);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        rlBg.setBackgroundResource(R.mipmap.keyboard_normal);
                        message2 = null; // 命令种类
                        break;
                }
                return true;
            }
        });
        rlDown.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Vibrator vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
                        vibrator.vibrate(new long[]{0, 40}, -1);
                        rlBg.setBackgroundResource(R.mipmap.keyboard_down);
                        message2 = "8882";
                        data = ItonAdecimalConver.hexStr2Bytes(message1 + message2 + message3 + message4 + message5);
                        //发送数据
                        if (getConnectBle() == 0) {
                            write(data);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        rlBg.setBackgroundResource(R.mipmap.keyboard_normal);
                        message2 = null; // 命令种类
                        break;
                }

                return true;
            }
        });
    }


    public void getBle() {
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        addresses.clear();
        //得到所有已配对的蓝牙适配器对象
        Set<BluetoothDevice> devices = adapter.getBondedDevices();
        List<BluetoothDevice> bles = new ArrayList<>();
        if (devices.size() > 0) {
            for (Iterator iterator = devices.iterator(); iterator.hasNext(); ) {
                bluetoothDevice = (BluetoothDevice) iterator.next();
                //得到远程已配对蓝牙设备的mac地址
//                Log.e(TAG, bluetoothDevice.getAddress());
                Log.e(TAG, bluetoothDevice.getName()+"/"+bluetoothDevice.getAddress());
                Log.e(TAG, bluetoothDevice.getBondState() + ""); // 获取配对状态  BOND_BONDED = 12;BOND_BONDING = 11;BOND_NONE = 10;
                String name = adapter.getRemoteDevice(bluetoothDevice.getAddress()).getName();
                if (name!=null) {
                    if (name.startsWith("1132") || name.startsWith("ZJH-")) {
                        bleDevice.setName(TextUtils.isEmpty(bluetoothDevice.getName()) ? "Unkown Device" : bluetoothDevice.getName());
                        bleDevice.setAddress(bluetoothDevice.getAddress());

                        addresses.add(bleDevice.getAddress());
//                    mDeviceAddress = bleDevice.getAddress();
//                    bleName = bleDevice.getName();
//                    //连接蓝牙
//                    Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
//                    bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
//
//                    circle();
//                    Log.e("MainActivity", "connectBle" + getConnectBle());

                    }
                }


            }
            Log.e(TAG,"addresses.size "+ addresses.size());
            Log.e(TAG,"addresses.cotains "+addresses.contains(mDeviceAddress_old));
            if (addresses.size() > 0) {
                if (mDeviceAddress_old != null && addresses.contains(mDeviceAddress_old)) {
                    Log.e(TAG,"addresses123 ");
                    mDeviceAddress = mDeviceAddress_old;
                    bleName = bleName_old;
                } else {
                    mDeviceAddress = addresses.get(0);
                    bleName = adapter.getRemoteDevice(mDeviceAddress).getName();
                    Log.e(TAG,"addresses1234 ");
                }
                Log.e(TAG, "要连接的蓝牙名称/地址： " + bleName + "/" + mDeviceAddress);
                //连接蓝牙
                Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
                bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                circle();
                Log.e("MainActivity", "connectBle" + getConnectBle());
            }
            if (addresses.size() == 0) {
                setConnectBle(1);
//                Toast.makeText(MainActivity.this, "未有合适的已配对蓝牙，请检查配对列表", Toast.LENGTH_SHORT).show();
                showBleOnOffDialog("未有合适的配对蓝牙,请查看配对列表");
            }

        } else {
            setConnectBle(1);
            Log.e(TAG, "蓝牙未开启或未配对，是否开启或配对");
//            Toast.makeText(MainActivity.this,"蓝牙未开启或未配对，是否开启或配对",Toast.LENGTH_SHORT).show();
            showBleOnOffDialog("蓝牙未开启或配对，请开启蓝牙");

        }
    }

    private void showBleOnOffDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        AlertDialog dialog;
        builder.setIcon(R.mipmap.bluetooth_icon).setTitle("蓝牙设置").setMessage(message);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
//                MainActivity.this.finish();
                isToBleSet = true;
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

            }
        });
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

    }


    /**
     * 圆形进度条测试..
     */
    public void circle() {
        myDialog = new ProgressDialog(MainActivity.this); // 获取对象
        myDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // 设置样式为圆形样式
//        myDialog.setTitle("友情提示"); // 设置进度条的标题信息
        myDialog.setMessage("蓝牙连接中..."); // 设置进度条的提示信息
//        myDialog.setIcon(R.drawable.ic_launcher); // 设置进度条的图标
        myDialog.setCanceledOnTouchOutside(false);
        myDialog.setIndeterminate(false); // 设置进度条是否为不明确
        myDialog.setCancelable(true); // 设置进度条是否按返回键取消

        myDialog.show(); // 显示进度条
    }


    /*
    * *****************************写函数*****************************
    */
    private void write(byte[] data) {
//        mnotyGattService = mBluetoothLeService.getSupportedGattServices(uuid);
//        mCharacteristicWrite = mnotyGattService.getCharacteristic(UUID_WRITE);//服务和写入特性反复获取，测试好像没意义
        if (data.length > 20) {
            byte[] data1 = Arrays.copyOfRange(data, 0, 20);
            byte[] data2 = Arrays.copyOfRange(data, 20, data.length);
//            if (
            mBluetoothLeService.writeCharacteristic(mCharacteristicWrite, data1);

//                    ) {
            //此处执行的写操作，该工程中mCharacteristic可读可写，此处演示用的同一个特征值，由于硬件方面没有对写操作做处理，所以只写进去了一个字节。实际上是写进了2个字节。
//                mBluetoothLeService.setCharacteristicNotification(mCharacteristicReadNotify, true); //项目中用来接收的特征值，具有通知特性，可监听特征值的变化。一有改变，立刻通知。具体到自己项目可不予考虑
//            }
            try {
                Thread.sleep(10);
                mBluetoothLeService.writeCharacteristic(mCharacteristicWrite, data2);
                mBluetoothLeService.setCharacteristicNotification(mCharacteristicReadNotify, true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            if (mBluetoothLeService.writeCharacteristic(mCharacteristicWrite, data)) {
                mBluetoothLeService.setCharacteristicNotification(mCharacteristicReadNotify, true); //项目中用来接收更新的特征值，具有通知特性，可监听特征值的变化。一有改变，立刻通知。具体到自己项目可不予考虑
            }
        }

    }

    /*
    * *****************************读函数*****************************
    */
    private void read() {
        // TODO Auto-generated method stub
        mBluetoothLeService.readCharacteristic(mCharacteristicReadNotify);
        Log.e(TAG, "打开read");
//        Toast.makeText(MainActivity.this, "读成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("onResume", "onResume");
//        if (adapter != null) {//blueadapter为null表示手机没有蓝牙模块
//            if (adapter.isEnabled() == false) { //false 未启用
//                //打开蓝牙
//                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(intent, REQUEST_OPEN_BT_CODE);
//
//            }
//        } else {
//            Toast.makeText(MainActivity.this, "您的设备不具备蓝牙功能", Toast.LENGTH_SHORT).show();
//        }

        Log.e(TAG,"mConnected1 " + mConnected);
        if (mConnected == true){
            isToBleSet = false;
        }


        if (mConnected == false || isToBleSet == true) {
            Log.e(TAG, "mConnected2 " + mConnected);
            getBle();
        }

        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OPEN_BT_CODE) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(MainActivity.this, "打开蓝牙请求被拒绝", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "蓝牙已开启", Toast.LENGTH_SHORT).show();
                if (mConnected == false) {
                    getBle();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        if (mConnected==true) {
            unregisterReceiver(mGattUpdateReceiver);
        }
        if (isBindService == true) {
//            mBluetoothLeService.disconnect();
//            mBluetoothLeService.close();
            unbindService(mServiceConnection);
        }
        mBluetoothLeService = null;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @OnClick({R.id.iv_back, R.id.btn_connect})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                if (mConnected == true) {
                    mBluetoothLeService.disconnect();
                    mBluetoothLeService.close();
                }
                finish();
                break;
            case R.id.btn_connect:
                getBle();
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (mConnected == false) {
//                            mBluetoothLeService.connect(bluetoothDevice.getAddress());
//                        }
//                    }
//                }).start();

                break;
        }

    }

    //记录用户首次点击返回键的时间
    private long firstTime = 0;

    /**
     * 第一种解决办法 通过监听keyUp
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 2000) {
                Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                firstTime = secondTime;
                return true;
            } else {
                if (mConnected == true) {
                    mBluetoothLeService.disconnect();
                    mBluetoothLeService.close();
                }
//                finish();
                onDestroy();
                System.exit(0);
            }
        }

        return super.onKeyUp(keyCode, event);
    }

}
