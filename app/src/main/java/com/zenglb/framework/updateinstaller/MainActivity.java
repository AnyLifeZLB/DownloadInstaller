package com.zenglb.framework.updateinstaller;

import android.Manifest;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.zenglb.downloadinstaller.DownloadInstaller;
import com.zenglb.downloadinstaller.DownloadProgressCallBack;
import java.util.List;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * app 内部升级
 *
 */
public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    String apkDownLoadUrl = "http://img.4009515151.com//2019/04/02/14/653371b458-comvankewyguide_3.8.4.1_412b07a6-44cc-5953-a8a6-0a1a5dd535e1.apk";
    String apkDownLoadUrl2 = "https://ali-fir-pro-binary.fir.im/ea7df71390403635b5f744d82d28c13fc865c325.apk?auth_key=1557455233-0-0-2b4c71ac353961eab7fa2a65ec641bb4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * 测试升级安装
         */
        findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdate(apkDownLoadUrl);
            }
        });

        /**
         * 测试升级安装
         */
        findViewById(R.id.update2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdate(apkDownLoadUrl2);
            }
        });

        methodRequiresPermission();
    }


    /**
     * 弹出升级对话框,强制升级可以监听 installOnStart 事件
     *
     */
    private void showUpdate(final String downloadUrl){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Update App")
                .setMessage("1.update aaaaa\n2.update bbbbb")
                .setPositiveButton("update",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog1, int which) {
                                goDownloadInstall(downloadUrl);
                            }
                        })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }


    /**
     * 去下载
     *
     */
    private void goDownloadInstall(final String downloadUrl){

//        new DownloadInstaller(MainActivity.this,downloadUrl).start();

        new DownloadInstaller(MainActivity.this, downloadUrl, new DownloadProgressCallBack() {
            @Override
            public void downloadProgress(int progress) {
                Log.e("PROGRESS","Progress"+progress);
            }

            @Override
            public void downloadException(Exception e) {
                e.printStackTrace();
            }

            /**
             * 开始安装
             */
            @Override
            public void onInstallStart() {

            }

        }).start();
    }


    /**
     *
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
