package cn.sharelink.yiluoble;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StartActivity extends AppCompatActivity {

    @BindView(R.id.ll)
    LinearLayout ll;

//    BluetoothDevice bluetoothDevice;
//    BluetoothAdapter adapter;
//    Set<BluetoothDevice> devices;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);
        Animation animation = AnimationUtils.loadAnimation(StartActivity.this, R.anim.view_anima);
        ll.startAnimation(animation);

        // 2s跳转到主界面
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(StartActivity.this, MainActivity.class));
                finish();
            }
        }, 2000);

    }

//    private void showBleOnOffDialog(String message) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
//        AlertDialog dialog;
//        builder.setIcon(R.mipmap.bluetooth_icon).setTitle("蓝牙设置").setMessage(message);
//        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                StartActivity.this.startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
//                dialogInterface.dismiss();
//            }
//        });
//        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                dialogInterface.dismiss();
//
//            }
//        });
//        dialog = builder.create();
//        dialog.setCanceledOnTouchOutside(false);
//        dialog.show();
//
//    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("StartActivity", "onResume");
//        adapter = BluetoothAdapter.getDefaultAdapter();
//        //得到所有已配对的蓝牙适配器对象
//        devices = adapter.getBondedDevices();
//        if (devices.size() > 0) {
//            for (Iterator iterator = devices.iterator(); iterator.hasNext(); ) {
//                bluetoothDevice = (BluetoothDevice) iterator.next();
//                //得到远程已配对蓝牙设备的mac地址
//                Log.e("StartActivity", bluetoothDevice.getAddress());
//                Log.e("StartActivity", bluetoothDevice.getName());
//                Log.e("StartActivity", bluetoothDevice.getBondState() + ""); // 获取配对状态  BOND_BONDED = 12;BOND_BONDING = 11;BOND_NONE = 10;
//
//                if (bluetoothDevice.getName().startsWith("1132") || bluetoothDevice.getName().startsWith("STB-") || bluetoothDevice.getName().startsWith("ZJH-")) {
////                    startActivity(new Intent(StartActivity.this, MainActivity.class));
////                    finish();
//
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            startActivity(new Intent(StartActivity.this, MainActivity.class));
//                            adapter = null;
//                            bluetoothDevice = null;
//                            finish();
//                        }
//                    }, 2000);
//
//                } else {
//                    Log.e("StartActivity", "111");
////                    showBleOnOffDialog("没有合适的已配对蓝牙，是否配对");
//                }
//            }
//        } else {
//            showBleOnOffDialog("蓝牙未开启或未配对，是否开启或配对");
//        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("StartActivity", "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("StartActivity", "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("StartActivity", "onDestroy");
    }



}