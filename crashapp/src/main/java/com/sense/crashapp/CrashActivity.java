package com.sense.crashapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;


public class CrashActivity extends AppCompatActivity {

    public static final String CRASH_MODEL = "crash_model";
    public static final String CRASH_STR = "crash_str";
    public static final String DATE_FORMAT_PATTERN_YMD_HM = "yyyy-MM-dd HH-mm";

    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault());

    private CrashModel model;
    private String str;

    private ScrollView sScrollView;
    private ViewGroup sToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.SpiderManTheme_Dark);
        setContentView(R.layout.activity_crash);
        model = getIntent().getParcelableExtra(CRASH_MODEL);
        str = getIntent().getStringExtra(CRASH_STR);

        if (model == null) {
            return;
        }
        Log.e("SpiderMan", Log.getStackTraceString(model.getEx()));

        sScrollView = findViewById(R.id.sScrollView);
        sToolbar = findViewById(R.id.sToolbar);
//        TextView tv_packageName = findViewById(R.id.tv_packageName);
        TextView textMessage = findViewById(R.id.sTextMessage);
        TextView tv_className = findViewById(R.id.sTvClassName);
        TextView tv_methodName = findViewById(R.id.sTvMethodName);
        TextView tv_lineNumber = findViewById(R.id.sTvLineNumber);
        TextView tv_exceptionType = findViewById(R.id.sTvExceptionType);
        TextView tv_fullException = findViewById(R.id.sTvFullException);
        TextView tv_time = findViewById(R.id.sTvTime);
        TextView tv_model = findViewById(R.id.sTvModel);
        TextView tv_brand = findViewById(R.id.sTvBrand);
        TextView tv_version = findViewById(R.id.sTvVersion);
        TextView tv_more = findViewById(R.id.sTvMore);
        TextView tv_cpuAbi = findViewById(R.id.sTvCpuAbi);
        TextView tv_versionCode = findViewById(R.id.sTvVersionCode);
        TextView tv_versionName = findViewById(R.id.sTvVersionName);
        RelativeLayout restarAppRl = findViewById(R.id.restar_app);

//
//        tv_packageName.setText(model.getClassName());
        textMessage.setText(model.getExceptionMsg());
        tv_className.setText(model.getFileName());
        tv_methodName.setText(model.getMethodName());
        tv_lineNumber.setText(String.valueOf(model.getLineNumber()));
        tv_exceptionType.setText(model.getExceptionType());
        tv_fullException.setText(model.getFullException());
        tv_time.setText(df.format(model.getTime()));

        CrashModel.Device device = model.getDevice();
        tv_model.setText(model.getDevice().getModel());
        tv_brand.setText(model.getDevice().getBrand());
        String platform = "Android " + device.getRelease() + "-" + device.getVersion();
        tv_version.setText(platform);

        String cpuAbi = device.getCpuAbi();
        tv_cpuAbi.setText(cpuAbi);

        tv_versionCode.setText(model.getVersionCode());
        tv_versionName.setText(model.getVersionName());

        tv_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu(v);
            }
        });

        restarAppRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartApp(CrashActivity.this);
            }
        });


        //保存到本地
        exportExceptionToSD(model);

        //上传的服务器的代码
        if (null != model.getBaseUrl() && !TextUtils.isEmpty(model.getBaseUrl())) {
            exportExceptionToNet(str);
        }

    }


    @Override
    public void onBackPressed() {
        restartApp(CrashActivity.this);
    }

    /**
     * 重启应用
     *
     * @param context
     */
    public static void restartApp(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    /**
     * 保存文件到本地
     *
     * @param model
     */
    private void exportExceptionToSD(CrashModel model) {

//        String name = "/jc_mes_log";
        String name = "/zq_zly_log";
        String path = "";

        if (Build.VERSION.SDK_INT >= 30) {
            path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + name;
        } else {
            path = Environment.getExternalStorageDirectory().toString() + name;
        }

        Log.d("uploadOsData---" + Build.VERSION.SDK_INT, path + "");

//        String path = Environment.getExternalStorageDirectory().getPath() + name;

        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String logPath = path + File.separator + "log-" + long2Str(System.currentTimeMillis()) + ".txt";
        String text = SpiderManUtils.getShareText(getApplicationContext(), model);
        saveToFile(text, logPath);

    }

    public void saveToFile(String text, String filePath) {
        try {
            SpiderManUtils.saveTextToFile(text, filePath);
        } catch (Throwable e) {
            e.printStackTrace();
            Log.d("uploadOsData---", e.toString() + "");
        }
    }


    /**
     * 上传异常信息到网络
     *
     * @param
     */
    private void exportExceptionToNet(String json) {

        sendErrorData(json, new StringCallback() {

            @Override
            public void onSuccess(String s, Call call, Response response) {
                Log.d("uploadOsData", s + "");
            }

            @Override
            public void onError(Call call, Response response, Exception e) {
                super.onError(call, response, e);
                Log.d("uploadOsData", e.toString() + "");
            }
        });

    }


    public void sendErrorData(String errorContent, StringCallback callback) {

        JSONObject json1 = new JSONObject();
        try {
            json1.put("errorId", "appError");
            json1.put("errorContent", errorContent);

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, json1.toString());

            OkGo.post(model.getBaseUrl())
                    .requestBody(body)
                    .cacheMode(CacheMode.DEFAULT)
                    .execute(callback);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    private void showMenu(View v) {
        PopupMenu menu = new PopupMenu(CrashActivity.this, v);
        menu.inflate(R.menu.menu_more);
        menu.show();
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.menu_copy_text) {
                    String crashText = getShareText(model);
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    if (cm != null) {
                        ClipData mClipData = ClipData.newPlainText("crash", crashText);
                        cm.setPrimaryClip(mClipData);
                        showToast(getString(R.string.simpleCopied));
                    }
                } else if (id == R.id.menu_share_text) {
                    String crashText = getShareText(model);
                    shareText(crashText);
                } else if (id == R.id.menu_share_image) {
                    shareImage();
                }
                return true;
            }
        });
    }


    private String getShareText(CrashModel model) {
        return SpiderManUtils.getShareText(this.getApplicationContext(), model);
    }

    private void shareText(String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.simpleCrashInfo));
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, getString(R.string.simpleShareTo)));
    }

    public Bitmap getBitmapByView(ViewGroup toolbar, ScrollView scrollView) {
        if (toolbar == null || scrollView == null) return null;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int svHeight = 0;
        for (int i = 0; i < scrollView.getChildCount(); i++) {
            svHeight += scrollView.getChildAt(i).getHeight();
        }
        int height = svHeight + toolbar.getHeight();
        //
        Bitmap resultBitmap = Bitmap.createBitmap(toolbar.getWidth(), height, Bitmap.Config.ARGB_8888);
        Canvas rootCanvas = new Canvas(resultBitmap);
        //
        Bitmap toolbarBitmap = Bitmap.createBitmap(toolbar.getWidth(), toolbar.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas toolbarCanvas = new Canvas(toolbarBitmap);
        toolbarCanvas.drawRGB(255, 255, 255);
        toolbar.draw(toolbarCanvas);
        //
        Bitmap svBitmap = Bitmap.createBitmap(toolbar.getWidth(), svHeight,
                Bitmap.Config.ARGB_8888);
        Canvas svCanvas = new Canvas(svBitmap);
        svCanvas.drawRGB(255, 255, 255);
        scrollView.draw(svCanvas);

        rootCanvas.drawBitmap(toolbarBitmap, 0, 0, paint);
        rootCanvas.drawBitmap(svBitmap, 0, toolbar.getHeight(), paint);
        return resultBitmap;
    }

    private File BitmapToFile(Bitmap bitmap) {
        if (bitmap == null) return null;
        String path = SpiderManUtils.getCachePath(this.getApplicationContext());
        File imageFile = new File(path, "SpiderMan-" + df.format(model.getTime()) + ".png");
        try {
            FileOutputStream out = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            bitmap.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageFile;
    }

    private void shareImage() {
        File file = BitmapToFile(getBitmapByView(sToolbar, sScrollView));
        if (file == null || !file.exists()) {
            showToast(R.string.simpleImageNotExist);
            return;
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri contentUri = FileProvider.getUriForFile(getApplicationContext(),
                    getApplicationContext().getPackageName() + ".spidermanfileprovider", file);
            intent.putExtra(Intent.EXTRA_STREAM, contentUri);
        } else {
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, getString(R.string.simpleShareTo)));
    }

    private void showToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    private void showToast(@StringRes int textId) {
        Toast.makeText(getApplicationContext(), textId, Toast.LENGTH_SHORT).show();
    }


    /**
     * 时间戳转字符串
     *
     * @param timestamp 时间戳
     * @return 格式化的日期字符串
     */
    public static String long2Str(long timestamp) {
        return long2Str(timestamp, getFormatPattern());
    }

    private static String long2Str(long timestamp, String pattern) {
        return new SimpleDateFormat(pattern, Locale.CHINA).format(new Date(timestamp));
    }

    private static String getFormatPattern() {
        return DATE_FORMAT_PATTERN_YMD_HM;
    }


}
