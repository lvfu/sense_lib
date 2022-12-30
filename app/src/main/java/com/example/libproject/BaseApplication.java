package com.example.libproject;

import android.app.Application;
import android.content.Context;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.HttpParams;
import com.sense.crashapp.SpiderMan;

import java.util.logging.Level;

//import com.sense.crashapp.SpiderMan;

public class BaseApplication extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;

        SpiderMan.init(mContext,  "");
        initOkGo(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }


    private void initOkGo(Application mContext) {
        //---------这里给出的是示例代码,告诉你可以这么传,实际使用的时候,根据需要传,不需要就不传-------------//
        HttpParams params = new HttpParams();
//        params.put("os", "Android");
//        //param支持中文,直接传,不要自己编码
//        params.put("sdk_version", Build.VERSION.SDK_INT);
////        params.put("app_version", BuildConfig.VERSION_CODE);
//        //手机厂商
//        params.put("brand", android.os.Build.BRAND);
//        //手机型号
//        params.put("model", android.os.Build.MODEL);

//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.put("Blade-Auth", str);

//                 .addCommonHeaders(httpHeaders)
        //初始化okgo
        OkGo.init(mContext);
        OkGo.getInstance()
                .setRetryCount(5)
                .debug("OKGO_HTTP_FILTER", Level.INFO, true)
                .addCommonParams(params);   //设置全局公共参数;

    }
}
