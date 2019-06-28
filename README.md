# DownloadInstaller

Github :[https://github.com/AnyLifeZLB/DownloadInstaller](https://github.com/AnyLifeZLB/DownloadInstaller)


## Android 应用内下载，储存，安装 ，未知来源等问题处理
- 处理好了全局FileProvider,未知来源授权确认，通知栏等问题处理。
- 已经下载的文件不会重复下载
- 特别是Android 8 首次安装时候的未知来源问题处理，这里的处理方式很强硬，不授权安装未知来源就会一直跳转到授权页面。比较流氓，但很实用!
- 当然这是可以配置是否需要强制授权安装未知来源 参考：new DownloadInstaller(mContext, downloadUrl, isForceGrantUnKnowSource


# 使用 
  
  首先 Gradle 引入
  implementation 'anylife.downloadinstaller:downloadInstaller:1.1.1'
  
  然后
  
  targetSDK>= 26
  
  ```
    //一般的弹出对话框提示升级
    //如果是企业内部应用升级，肯定是要这个权限; 其他情况不要太流氓，TOAST 提示
     new DownloadInstaller(mContext, downloadUrl, isForceGrantUnKnowSource,new DownloadProgressCallBack() {
         @Override
         public void downloadProgress(int progress) {
               Log.e("PROGRESS","Progress"+progress);
         }
    
         @Override
         public void downloadException(Exception e) {
               e.printStackTrace();
         }
    

         @Override
         public void onInstallStart() {
    
         }
     }).start();
     
  ```
 
 
 
 .
 

![image.png](https://upload-images.jianshu.io/upload_images/2376786-88bc9e308207e1e9.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
