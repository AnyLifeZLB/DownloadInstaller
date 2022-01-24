package com.zenglb.framework.updateinstaller;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.dylanc.activityresult.launcher.StartActivityLauncher;
import com.zenglb.downloadinstaller.DownloadInstaller;
import com.zenglb.downloadinstaller.DownloadProgressCallBack;

/**
 * 升级app，后期拓展全部放到这里来
 *
 */
public class UpdateAppActivity extends AppCompatActivity {

    private UpdateDialog mDialog;
    private CheckVersionResult checkVersionResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

        checkVersionResult=getIntent().getParcelableExtra("update");
        final StartActivityLauncher startActivityLauncher = new StartActivityLauncher(UpdateAppActivity.this);


        mDialog = new UpdateDialog(this,startActivityLauncher);
        mDialog.show();
    }


    /**
     * 自定义隐私授权Dialog
     *
     */
    public class UpdateDialog extends AlertDialog {
        Context mContext;
        TextView mUnConfirm, mContent, mBtnConfirm,mVersion,mTitle;
        NumberProgressBar progressBar;
        StartActivityLauncher startActivityLauncher;

        public UpdateDialog(Context context,StartActivityLauncher startActivityLauncher) {
            super(context);
            mContext = context;
            this.startActivityLauncher=startActivityLauncher;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            this.getWindow().setBackgroundDrawable(new BitmapDrawable());
            setContentView(R.layout.update_app_alert);
            this.setCancelable(false);
            mVersion= findViewById(R.id.version);
            mUnConfirm = findViewById(R.id.btn_close);
            mContent = findViewById(R.id.content);
            mTitle = findViewById(R.id.title);
            progressBar= findViewById(R.id.tips_progress);
            mBtnConfirm = findViewById(R.id.btn_confirm);

            // mContent.setText(checkVersionResult.getDescription());
            mVersion.setText(checkVersionResult.getVersionName());

            if (checkVersionResult.getUpdateType()==1) {
                mTitle.setText("强制更新");
                mUnConfirm.setText("退出应用");
            }

            mBtnConfirm.setOnClickListener(v -> {
                mBtnConfirm.setBackgroundResource(R.drawable.shape_btn_grey_bg);
                if (checkVersionResult.getUpdateType()==1) {
                    progressBar.setVisibility(View.VISIBLE);
                    new DownloadInstaller(UpdateAppActivity.this, checkVersionResult.getPackageUrl(),startActivityLauncher, true,
                            new DownloadProgressCallBack() {
                                @Override
                                public void downloadProgress(int progress) {
                                    runOnUiThread(() -> progressBar.setProgress(progress));
                                    if (progress == 100) {
                                        UpdateAppActivity.this.finish();
                                    }
                                }

                                @Override
                                public void downloadException(Exception e) {
                                    int a=1;
                                }

                                @Override
                                public void onInstallStart() {
                                    UpdateAppActivity.this.finish();
                                }
                            }).start();

                } else {
                    new DownloadInstaller(UpdateAppActivity.this, checkVersionResult.getPackageUrl(),startActivityLauncher).start();
                    Toast.makeText(UpdateAppActivity.this,"后台下载升级中",Toast.LENGTH_LONG).show();
                    UpdateAppActivity.this.finish();
                }
            });

            mUnConfirm.setOnClickListener(v -> {
                if (checkVersionResult.getUpdateType()==1) {
//                    Intent mHomeIntent = new Intent(Intent.ACTION_MAIN);
//                    mHomeIntent.addCategory(Intent.CATEGORY_HOME);
//                    mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//                    startActivity(mHomeIntent);
                    dismiss();
                    System.exit(0);
                } else {
                    dismiss();
                    UpdateAppActivity.this.finish();
                }
            });
        }
    }
}
