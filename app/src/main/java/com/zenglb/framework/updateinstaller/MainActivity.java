package com.zenglb.framework.updateinstaller;

import android.Manifest;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.zenglb.downloadinstaller.DownloadInstaller;
import com.zenglb.downloadinstaller.DownloadProgressCallBack;
import java.util.List;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * app 内部升级
 *
 */
public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private String apkDownLoadUrl = "http://img.4009515151.com//2019/04/02/14/653371b458-comvankewyguide_3.8.4.1_412b07a6-44cc-5953-a8a6-0a1a5dd535e1.apk";
    private String apkDownLoadUrl2 = "https://ali-fir-pro-binary.fir.im/ea7df71390403635b5f744d82d28c13fc865c325.apk?auth_key=1557455233-0-0-2b4c71ac353961eab7fa2a65ec641bb4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * 测试升级安装
         */
        findViewById(R.id.update).setOnClickListener(v -> showUpdateDialog("1.升级后App 会自动攒钱\n2.还可以做白日梦",true,apkDownLoadUrl));

        /**
         * 测试升级安装2,无效下载链接
         */
        findViewById(R.id.update2).setOnClickListener(v -> showUpdateDialog("1.升级后App 会自动攒钱\n2.还可以做白日梦",false,apkDownLoadUrl2));

        methodRequiresPermission();
    }


    /**
     * 显示下载的对话框,是否要强制的升级还是正常的升级
     *
     * @param UpdateMsg     升级信息
     * @param isForceUpdate  是否是强制升级
     * @param downloadUrl    APK 下载URL
     */
    private void showUpdateDialog(String UpdateMsg, boolean isForceUpdate, String downloadUrl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final LayoutInflater inflater = LayoutInflater.from(this);
        View updateView = inflater.inflate(R.layout.update_layout, null);
        NumberProgressBar progressBar = updateView.findViewById(R.id.tips_progress);
        TextView updateMsg = updateView.findViewById(R.id.update_mess_txt);
        updateMsg.setText(UpdateMsg);
        builder.setTitle("发现新版本");
        String negativeBtnStr = "以后再说";

        if (isForceUpdate) {
            builder.setTitle("强制升级");
            negativeBtnStr = "退出应用";
        }

        builder.setView(updateView);
        builder.setNegativeButton(negativeBtnStr, null);
        builder.setPositiveButton(R.string.apk_update_yes, null);

        AlertDialog downloadDialog = builder.create();
        downloadDialog.setCanceledOnTouchOutside(false);
        downloadDialog.setCancelable(false);
        downloadDialog.show();

        downloadDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (isForceUpdate) {
                progressBar.setVisibility(View.VISIBLE);
                new DownloadInstaller(this, downloadUrl, new DownloadProgressCallBack() {
                    @Override
                    public void downloadProgress(int progress) {

                        //Android 9可以非UI线程更新？奇怪
//                        progressBar.setProgress(progress);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(progress);
                            }
                        });

                    }

                    @Override
                    public void downloadException(Exception e) {
                    }

                    @Override
                    public void onInstallStart() {
                        downloadDialog.dismiss();
                    }
                }).start();

            } else {
                new DownloadInstaller(this,downloadUrl).start();
                downloadDialog.dismiss();
            }
        });

        downloadDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
            if (isForceUpdate) {
                MainActivity.this.finish();
            } else {
                downloadDialog.dismiss();
            }
        });

    }


    /**
     * 请求权限
     *
     */
    private void methodRequiresPermission() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            // ...
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(MainActivity.this, "App 升级需要储存权限",10086, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Some permissions have been granted
        // ...
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Some permissions have been denied
        // ...
    }


}
