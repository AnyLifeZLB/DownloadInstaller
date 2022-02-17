package com.zenglb.framework.updateinstaller;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.dylanc.activityresult.launcher.StartActivityLauncher;
import com.zenglb.downloadinstaller.DownloadInstaller;
import com.zenglb.downloadinstaller.DownloadProgressCallBack;

/**
 * 升级对话框dialog
 */
public class UpdateAppDialog extends AlertDialog {
    private Context mContext;
    private TextView mUnConfirm, mContent, mBtnConfirm, mVersion, mTitle;
    private NumberProgressBar progressBar;
    private CheckVersionResult checkVersionResult;
    private StartActivityLauncher startActivityLauncher;

    public UpdateAppDialog(Context context, CheckVersionResult checkVersionResult, StartActivityLauncher startActivityLauncher) {
        super(context);
        this.mContext = context;
        this.checkVersionResult = checkVersionResult;
        this.startActivityLauncher=startActivityLauncher;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().setBackgroundDrawable(new BitmapDrawable());
        setContentView(R.layout.update_app_alert);
        this.setCancelable(false);
        mVersion = findViewById(R.id.version);
        mUnConfirm = findViewById(R.id.btn_close);
        mContent = findViewById(R.id.content);
        mTitle = findViewById(R.id.title);
        progressBar = findViewById(R.id.tips_progress);
        mBtnConfirm = findViewById(R.id.btn_confirm);

//         mContent.setText(checkVersionResult.getDescription());
        mVersion.setText(checkVersionResult.getVersionName());

        if (checkVersionResult.getUpdateType() == 1) {
            mTitle.setText("强制更新");
            mUnConfirm.setText("退出应用");
        }

        mBtnConfirm.setOnClickListener(v -> {
            mBtnConfirm.setBackgroundResource(R.drawable.shape_btn_grey_bg);
            if (checkVersionResult.getUpdateType() == 1) {
                progressBar.setVisibility(View.VISIBLE);
                new DownloadInstaller(mContext, checkVersionResult.getPackageUrl(), startActivityLauncher, true,
                        new DownloadProgressCallBack() {
                            @Override
                            public void downloadProgress(int progress) {
                                ((Activity)mContext).runOnUiThread(() -> progressBar.setProgress(progress));
                                if (progress == 100) {
                                    dismiss();
                                }
                            }

                            @Override
                            public void downloadException(Exception e) {
                               Log.e("DownloadException","DownloadException:"+e.toString());

                            }

                            @Override
                            public void onInstallStart() {
                               dismiss();
                            }
                        }).start();

            } else {
                dismiss();
                new DownloadInstaller(mContext, checkVersionResult.getPackageUrl(), startActivityLauncher).start();
                Toast.makeText(mContext, "后台下载升级中", Toast.LENGTH_LONG).show();
            }
        });

        mUnConfirm.setOnClickListener(v -> {
            if (checkVersionResult.getUpdateType() == 1) {
                System.exit(0);
            } else {
               dismiss();
            }
        });
    }
}