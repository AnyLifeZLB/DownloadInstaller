# DownloadInstaller
## Android 应用内下载，储存，安装 ，未知来源等问题处理
处理好了FileProvider,未知来源确认，通知栏等问题处理



# 使用 
  首先： implementation 'anylife.downloadinstaller:downloadInstaller:VersionCode'

    /**
     * 显示升级，显示升级
     *
     */
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
