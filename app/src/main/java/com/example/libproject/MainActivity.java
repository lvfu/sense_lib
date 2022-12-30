package com.example.libproject;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.azhon.appupdate.config.UpdateConfiguration;
import com.azhon.appupdate.listener.OnButtonClickListener;
import com.azhon.appupdate.listener.OnDownloadListenerAdapter;
import com.azhon.appupdate.manager.DownloadManager;

public class MainActivity extends AppCompatActivity implements OnButtonClickListener {

    private TextView crashAppTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        crashAppTv = findViewById(R.id.crash_app_tv);
        crashAppTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                crashAppTv = null;
                crashAppTv.setText("");
            }
        });


        showUpdateDialog();
    }

    private void showUpdateDialog() {


        UpdateConfiguration configuration = new UpdateConfiguration()
                //输出错误日志
                .setEnableLog(true)
                //设置自定义的下载
                //.setHttpManager()
                //下载完成自动跳动安装页面
                .setJumpInstallPage(true)
                //设置对话框背景图片 (图片规范参照demo中的示例图)
//                    .setDialogImage(R.drawable.ic_dialog)
                //设置按钮的颜色
//                .setDialogButtonColor(Color.parseColor("#33ceaf"))
                //设置对话框强制更新时进度条和文字的颜色
                //.setDialogProgressBarColor(Color.parseColor("#E743DA"))
                //设置按钮的文字颜色
                .setDialogButtonTextColor(Color.WHITE)
                //设置是否显示通知栏进度
                .setShowNotification(true)
                //设置是否提示后台下载toast
                .setShowBgdToast(false)
                //设置强制更新
                .setForcedUpgrade(false)
                //设置对话框按钮的点击监听
                .setButtonClickListener(MainActivity.this)
                //设置下载过程的监听
                .setOnDownloadListener(new OnDownloadListenerAdapter() {

                    @Override
                    public void downloading(int max, int progress) {
                        int curr = (int) (progress / (double) max * 100.0);

                        Log.e("DownloadListener", String.valueOf(curr));


                    }
                });

        DownloadManager manager = DownloadManager.getInstance(MainActivity.this);
        manager.setApkName("jichai_mes-" + "1" + "-relase.apk")
                .setApkUrl("jichai_mes-")
                .setShowNewerToast(false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setConfiguration(configuration)
                .setApkVersionName("1")
                .setApkSize("7")
                .setDialogThemeColor(1)
                .setVersionNameCheck(true)
                .setApkVersionCode(Long.parseLong("2"))
                .setApkVersionName("3.0")
                .setApkDescription("更新说明:<br/>1.增加退出功能<br/> 2.修复不能自动更新的BUG <br/>3.增加微信登录功能")
                .download();

    }

    @Override
    public void onButtonClick(int id) {

    }
}