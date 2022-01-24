package com.zenglb.downloadinstaller;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArrayMap;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;


import com.dylanc.activityresult.launcher.StartActivityLauncher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * App 下载升级管理器.单线程稳定，多线程下载异常多！！！  30M 以内还是没有问题
 * <p>
 * https://bintray.com/anylifezlb
 * <p>
 * 5.新的进程处理？app 杀了也没有关系
 * <p>
 * 7.安装时候APK MD5 检查，断点续传，多线程下载
 * 8.存储分区适配
 * <p>
 * https://github.com/miomin/Multiple-ChannelResumeDownloader
 * https://github.com/yaowen369/DownloadHelper
 */
public class DownloadInstaller {
    private String authority;
    private static final String intentType = "application/vnd.android.package-archive";

    private NotificationManager notificationManager;
    private Notification notification;
    private NotificationCompat.Builder builder;

    private Context mContext;
    private int progress;
    private int oldProgress;

    private boolean isForceGrantUnKnowSource;

    //新包的下载地址
    private String downloadApkUrl;
    private String downloadApkUrlMd5;
    private int downloadApkNotifyId;

    //local saveFilePath
    private String storageApkPath;

    //事件监听器
    private DownloadProgressCallBack downloadProgressCallBack;

    //保存下载状态信息，临时过度的方案。
    public static ArrayMap<String, Integer> downLoadStatusMap = new ArrayMap<>();

    private String storagePrefix;

    private boolean isDownloadOnly=false;
    private StartActivityLauncher startActivityLauncher;


    /**
     * 不需要下载进度回调的
     *
     * @param context        上下文
     * @param downloadApkUrl apk 下载地址
     */
    public DownloadInstaller(Context context, String downloadApkUrl,StartActivityLauncher startActivityLauncher) {
        this(context, downloadApkUrl, startActivityLauncher,false, null);
    }


    /**
     * 需要下载进度回调的
     *
     * @param context        上下文
     * @param downloadApkUrl apk下载地址
     * @param callBack       进度状态回调
     */
    public DownloadInstaller(Context context, String downloadApkUrl,StartActivityLauncher startActivityLauncher, DownloadProgressCallBack callBack) {
        this(context, downloadApkUrl, startActivityLauncher,false, callBack);
    }


    /**
     * 下载安装App
     *
     * @param context                  上下文
     * @param downloadApkUrl           下载URL
     * @param isForceGrantUnKnowSource 是否是强制的要授权未知来源
     * @param callBack                 回调
     */
    public DownloadInstaller(Context context, String downloadApkUrl, StartActivityLauncher startActivityLauncher, boolean isForceGrantUnKnowSource, DownloadProgressCallBack callBack) {
        this.mContext = context;
        this.downloadApkUrl = downloadApkUrl;
        this.startActivityLauncher=startActivityLauncher;
        this.isForceGrantUnKnowSource = isForceGrantUnKnowSource;
        this.downloadProgressCallBack = callBack;
    }

    private void setDownloadOnly(boolean isDownloadOnly){
        this.isDownloadOnly=isDownloadOnly;
    }


    /**
     * 获取16位的MD5 值，大写
     *
     * @param str
     * @return
     */
    private String getUpperMD5Str16(String str) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException caught!");
            System.exit(-1);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] byteArray = messageDigest.digest();
        StringBuffer md5StrBuff = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                md5StrBuff.append("0").append(
                        Integer.toHexString(0xFF & byteArray[i]));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        return md5StrBuff.toString().toUpperCase().substring(8, 24);
    }


    /**
     * app下载升级管理
     */
    public void start() {
        String applicationID = mContext.getPackageName();
        //防止不同的app 下载同一个链接的App 失败
        downloadApkUrlMd5 = getUpperMD5Str16(downloadApkUrl + applicationID);
        downloadApkNotifyId = downloadApkUrlMd5.hashCode();

        //https://developer.android.com/studio/build/application-id?hl=zh-cn
        authority = applicationID + ".fileProvider";

        //todo 路径要支持自定义，适配分区储存，卸载后App缓存也要删除
//        storagePrefix = Environment.getExternalStorageDirectory().getPath() + "/";
        storagePrefix = mContext.getFilesDir().getPath() + "/update/";
        storageApkPath = storagePrefix + AppUtils.getAppName(mContext) + downloadApkUrlMd5 + ".apk";

        Integer downloadStatus = downLoadStatusMap.get(downloadApkUrlMd5);

        if (downloadStatus == null || downloadStatus == UpdateStatus.UN_DOWNLOAD || downloadStatus == UpdateStatus.DOWNLOAD_ERROR) {
            initNotification();
            //如果没有正在下载&&没有下载好了还没有升级
            new Thread(mDownApkRunnable).start();
        } else if (downloadStatus == UpdateStatus.DOWNLOADING) {
            Toast.makeText(mContext, "正在下载App", Toast.LENGTH_SHORT).show();
        }else if (downloadStatus==UpdateStatus.UNINSTALL){
            downloadProgressCallBack.downloadProgress(100);
            if(!isDownloadOnly){
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        downLoadStatusMap.put(downloadApkUrlMd5, UpdateStatus.UNINSTALL);
                        installProcess();
                    }
                });
            }
        }

    }


    /**
     * 下载线程,使用最原始的HttpURLConnection，减少依赖
     * 大的APK下载还是比较慢的，后面改为多线程下载
     */
    private Runnable mDownApkRunnable = new Runnable() {
        @Override
        public void run() {
            downLoadStatusMap.put(downloadApkUrlMd5, UpdateStatus.DOWNLOADING);
            try {
                URL url = new URL(downloadApkUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();

                //处理下载重定向问题，302 CODE
                conn.setInstanceFollowRedirects(false);
                if (conn.getResponseCode() == 302) {
                    //如果会重定向，保存302重定向地址，以及Cookies,然后重新发送请求(模拟请求)
                    String location = conn.getHeaderField("Location");
                    conn.disconnect();
                    downloadApkUrl = location;
                    conn = (HttpURLConnection) new URL(downloadApkUrl).openConnection();
                }

                int length = conn.getContentLength();

                File file = new File(storagePrefix);
                if (!file.exists()) {
                    file.mkdir();
                }

                File apkFile = new File(storageApkPath);
                if (apkFile.exists() && apkFile.length() == length) {
                    //已经下载过了，直接的progress ==100,然后去安装
                    //data/user/0/com.zenglb.framework.updateinstaller/files/AppUpdate15975F54AB360A6E.apk
                    progress=100;

                    updateNotify(progress);
                    if (downloadProgressCallBack != null) {
                        downloadProgressCallBack.downloadProgress(progress);
                    }


                    conn.disconnect();

                    if(!isDownloadOnly){
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                downLoadStatusMap.put(downloadApkUrlMd5, UpdateStatus.UNINSTALL);
                                installProcess();
                            }
                        });
                    }
                    return;
                }


                FileOutputStream fos = new FileOutputStream(apkFile);
                int count = 0;
                byte[] buf = new byte[2048];
                int byteCount;

                InputStream is = conn.getInputStream();

                while ((byteCount = is.read(buf)) > 0) {
                    count += byteCount;
                    progress = (int) (((float) count / length) * 100);
                    if (progress > oldProgress) {
                        updateNotify(progress);
                        if (downloadProgressCallBack != null) {
                            downloadProgressCallBack.downloadProgress(progress);
                        }
                        oldProgress = progress;
                    }
                    fos.write(buf, 0, byteCount);
                }

                if(!isDownloadOnly){
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            downLoadStatusMap.put(downloadApkUrlMd5, UpdateStatus.UNINSTALL);
                            installProcess();
                        }
                    });
                }

                fos.flush();
                fos.close();
                is.close();

            } catch (Exception e) {
                downLoadStatusMap.put(downloadApkUrlMd5, UpdateStatus.DOWNLOAD_ERROR);

                if (downloadProgressCallBack != null) {
                    downloadProgressCallBack.downloadException(e);
                }

                //后面有时间再完善异常的处理
                if (e instanceof FileNotFoundException) {
                    notifyError(getStringFrom(R.string.download_failure_file_not_found));
                    toastError(R.string.download_failure_file_not_found);
                } else if (e instanceof ConnectException) {
                    notifyError(getStringFrom(R.string.download_failure_net_deny));
                    toastError(R.string.download_failure_net_deny);
                } else if (e instanceof UnknownHostException) {
                    notifyError(getStringFrom(R.string.download_failure_net_deny));
                    toastError(R.string.download_failure_net_deny);
                } else if (e instanceof UnknownServiceException) {
                    notifyError(getStringFrom(R.string.download_failure_net_deny));
                    toastError(R.string.download_failure_net_deny);
                } else if (e.toString().contains("Permission denied")) {
                    notifyError(getStringFrom(R.string.download_failure_storage_permission_deny));
                    toastError(R.string.download_failure_storage_permission_deny);
                } else {
                    notifyError(getStringFrom(R.string.apk_update_download_failed));
                    toastError(R.string.apk_update_download_failed);
                }

            } finally {
                //finally do something
            }
        }
    };


    /**
     * get String from id
     *
     * @param id res id
     * @return string
     */
    @NonNull
    public String getStringFrom(@StringRes int id) {
        return mContext.getResources().getString(id);
    }

    /**
     * Toast error message
     *
     * @param id res id
     */
    private void toastError(@StringRes int id) {
        Looper.prepare();
        Toast.makeText(mContext, getStringFrom(id), Toast.LENGTH_LONG).show();
        Looper.loop();
    }


    /**
     * 安装过程处理
     *
     * 取消安装未知来源后不可以用，Activity For Result 不可用
     *
     */
    public void installProcess() {
        if (isDownloadOnly) return;
//        if (progress < 100) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean canInstallPackage = mContext.getPackageManager().canRequestPackageInstalls();
            final Integer downloadStatus = downLoadStatusMap.get(downloadApkUrlMd5); //unboxing

            if (canInstallPackage) {
                if (downloadStatus == UpdateStatus.UNINSTALL) {
                    installApk();
                    downLoadStatusMap.put(downloadApkUrlMd5, UpdateStatus.UN_DOWNLOAD);
                }
            } else {
                Uri packageURI = Uri.parse("package:" + AppUtils.getPackageName(mContext));
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
                
                //奇怪，这里竟然拿不到返回的值，生气😠
                startActivityLauncher.launch(intent, new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Log.e("GGGGGGGG","--111  ------------------------");
                        if(result.getResultCode()==Activity.RESULT_OK){
                            if (downloadStatus == UpdateStatus.UNINSTALL) {
                                installProcess();
                            }
                        } else {
                            //如果是企业内部应用升级，肯定是要这个权限，其他情况不要太流氓，TOAST 提示
                            if (isForceGrantUnKnowSource) {
                                installProcess();
                            } else {
                                Toast.makeText(mContext, "你没有授权安装App", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });

            }
        } else {
            installApk();
            downLoadStatusMap.put(downloadApkUrlMd5, UpdateStatus.UN_DOWNLOAD);
        }
    }


    /**
     * 跳转到安装apk的页面
     */
    private void installApk() {
        File apkFile = new File(storageApkPath);
        if (!apkFile.exists()) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri contentUri = FileProvider.getUriForFile(mContext, authority, apkFile);
            intent.setDataAndType(contentUri, intentType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent.setDataAndType(Uri.parse("file://" + apkFile.toString()), intentType);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);

        /**
         * 开始安装了
         */
        if (downloadProgressCallBack != null) {
            downloadProgressCallBack.onInstallStart();
        }
    }


    /**
     * 初始化通知 initNotification
     */
    private void initNotification() {
        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(downloadApkUrlMd5, downloadApkUrlMd5, NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(mChannel);
        }

        builder = new NotificationCompat.Builder(mContext, downloadApkUrl);
        builder.setContentTitle(mContext.getResources().getString(R.string.apk_update_tips_title)) //设置通知标题
                .setSmallIcon(R.drawable.download)
                .setDefaults(Notification.DEFAULT_LIGHTS) //设置通知的提醒方式： 呼吸灯
                .setPriority(NotificationCompat.PRIORITY_MAX) //设置通知的优先级：最大
                .setAutoCancel(true)  //
                .setOngoing(true)     // 不可以删除
                .setContentText(mContext.getResources().getString(R.string.apk_update_downloading_progress))
                .setChannelId(downloadApkUrlMd5)
                .setProgress(100, 0, false);
        notification = builder.build();//构建通知对象
    }


    /**
     * 通知下载更新过程中的错误信息
     *
     * @param errorMsg 错误信息
     */
    private void notifyError(String errorMsg) {
        builder.setContentTitle(mContext.getResources().getString(R.string.apk_update_tips_error_title));
        builder.setContentText(errorMsg);
        notification = builder.build();
        notificationManager.notify(downloadApkNotifyId, notification);
    }


    /**
     * 更新下载的进度
     *
     * @param progress
     */
    private void updateNotify(int progress) {
        builder.setProgress(100, progress, false);
        builder.setContentText(mContext.getResources().getString(R.string.apk_update_downloading_progress) + " 「" + progress + "%」");
        notification = builder.build();

        //点击通知栏到安装界面，可能下载好了，用户没有安装
        if (progress == 100) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri contentUri = FileProvider.getUriForFile(mContext, authority, new File(storageApkPath));
                intent.setDataAndType(contentUri, intentType);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                intent.setDataAndType(Uri.parse("file://" + new File(storageApkPath).toString()), intentType);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            notification.contentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        }
        notificationManager.notify(downloadApkNotifyId, notification);
    }


}
