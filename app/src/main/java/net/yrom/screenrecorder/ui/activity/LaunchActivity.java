package net.yrom.screenrecorder.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import net.yrom.screenrecorder.R;
import net.yrom.screenrecorder.view.MyWindowManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LaunchActivity extends AppCompatActivity {

    @BindView(R.id.btn_screen_record)
    Button btnScreenRecord;
    @BindView(R.id.btn_camera_record)
    Button btnCameraRecord;

    private static final int REQUEST_OVERLAY_PERMISSION = 2;
    private static final int REQUEST_STREAM = 1;
    private static String[] PERMISSIONS_STREAM = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.SYSTEM_ALERT_WINDOW
    };

    boolean authorized = false;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        ButterKnife.bind(this);
        verifyPermissions();
    }

    @OnClick({R.id.btn_screen_record, R.id.btn_camera_record})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_screen_record:
                ScreenRecordActivity.launchActivity(this);
                break;
            case R.id.btn_camera_record:
                CameraActivity.launchActivity(this);
                break;
        }
    }

    public void verifyPermissions() {
        int CAMERA_permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int RECORD_AUDIO_permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int WRITE_EXTERNAL_STORAGE_permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (CAMERA_permission != PackageManager.PERMISSION_GRANTED ||
                RECORD_AUDIO_permission != PackageManager.PERMISSION_GRANTED ||
                WRITE_EXTERNAL_STORAGE_permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STREAM,
                    REQUEST_STREAM
            );
            authorized = false;
        } else {
            authorized = true;
        }

        // 开启悬浮窗权限
        if (!canDrawOverlays(this)) {
            requestOverlayPermission(LaunchActivity.this, REQUEST_OVERLAY_PERMISSION);
        }

        if (canDrawOverlays(this)) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    // 当前界面是桌面，且没有悬浮窗显示，则创建悬浮窗。
                    if (!MyWindowManager.isWindowShowing()) {
                        MyWindowManager.createSmallWindow(getApplicationContext());
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STREAM) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                authorized = true;
            }
        }
    }


    // 检查悬浮窗权限
    public static boolean canDrawOverlays(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true; // 6.0以下无需权限
        }
        return Settings.canDrawOverlays(context);
    }

    // 请求权限的方法
    public static void requestOverlayPermission(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + activity.getPackageName()));
            activity.startActivityForResult(intent, requestCode);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && Settings.canDrawOverlays(this)) {

            } else {
                Toast.makeText(this, "悬浮窗权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
