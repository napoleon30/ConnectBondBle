package cn.sharelink.yiluoble.util;

import android.app.Application;

/**
 * Created by WangLei on 2017/8/16.
 */

public class MyApplication extends Application{
    private static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

    }
    public static MyApplication getInstance() {
        // 因为我们程序运行后，Application是首先初始化的，如果在这里不用判断instance是否为空
        return instance;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
