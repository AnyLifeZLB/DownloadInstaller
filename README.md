# DownloadInstaller
## Android 应用内下载，储存，安装 ，未知来源等问题处理
处理好了FileProvider,未知来源确认，通知栏等问题处理



# 使用 
  
  - 首先： implementation 'anylife.downloadinstaller:downloadInstaller:1.x.y'
  
  
  
  - 2019年5月7日 刚提交Jcenter,可能还没有通过审核要先在root build.gradle中引入私有maven 路径
  maven { url 'https://dl.bintray.com/anylifezlb/DownloadInstaller' }


  - 显示对话框提示升级 new DownloadInstaller(MainActivity.this).download(apkDownLoadUrl)

    //一般的弹出对话框提示升级，需要强制升级的大家一起提issues 来完善啊
    private void showUpdate(String apkDownLoadUrl){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Update App")
                .setMessage("1.update aaaaa\n2.update bbbbb")
                .setPositiveButton("update",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog1, int which) {
                                new DownloadInstaller(MainActivity.this).download(apkDownLoadUrl);
                            }
                        })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }
