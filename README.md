# DownloadInstaller

>App 应用内下载更新，希望小而美。更多需求请提Issues

Github :[https://github.com/AnyLifeZLB/DownloadInstaller](https://github.com/AnyLifeZLB/DownloadInstaller)

## 2.1.0 已经适配存储分区了，请大家验证是否符合自己的项目需求后进行更新


## Android 应用内下载，储存，安装 ，未知来源等问题处理

- 处理好了全局FileProvider,未知来源授权确认，通知栏等问题处理。
- 已经下载的文件不会重复下载
- 特别是Android 8 首次安装时候的未知来源问题处理，这里的处理方式很强硬，不授权安装未知来源就会一直跳转到授权页面，企业级别的App应用内更新很实用
- 当然这是可以配置是否需要强制授权安装未知来源 参考：new DownloadInstaller(mContext, downloadUrl, isForceGrantUnKnowSource
- 准备适配存储分区，以便更广泛的使用


# 使用 （1.1.2 版本后支持AndroidX 了）

  首先 Gradle 引入（1.1.1 版本是最后一个support 版本，后面是AndroidX了）

  Jcenter(): implementation 'anylife.downloadinstaller:downloadInstaller:1.1.3'   
  mavenCentral(): implementation 'io.github.anylifezlb:downloadInstaller:2.0.0'

  Jcenter 服务供应商已经停止服务了，建议尽快迁移mavenCentral

  然后 targetSDK>= 26
  
  ```
    //一般的弹出对话框提示升级
    //如果是企业内部应用升级，大部分都希望升级；其他情况请给予用户选择的自由，尊重用户。
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
 More,Contact me : anylife.zlb@gmail.com


 APK 包瘦身方法：https://juejin.cn/post/6844904103131234311
 
 
 ![image](https://user-images.githubusercontent.com/15169396/139646686-2ba0d2c1-c25c-4259-9f18-687d8bcb153d.png)

 

![image.png](https://upload-images.jianshu.io/upload_images/2376786-88bc9e308207e1e9.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
