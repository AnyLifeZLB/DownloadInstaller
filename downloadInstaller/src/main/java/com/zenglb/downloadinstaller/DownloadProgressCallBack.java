package com.zenglb.downloadinstaller;

/**
 * 下载进度回调
 *
 */
public interface DownloadProgressCallBack {
//     void downloadStart();

     void downloadProgress(int progress);
     void downloadException(Exception e);
     void installOnStart();

}
