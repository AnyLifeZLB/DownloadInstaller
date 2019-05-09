package com.zenglb.downloadinstaller;

/**
 * 下载状态
 * 未下载，下载中，下载完成，下载失败，待安装
 *
 */
public interface DownloadInstallStatus {
    public final static int UN_DOWNLOAD =-1;
    public final static int DOWNLOADING=0;
    public final static int DOWNLOAD_ERROR =1;
    public final static int UNINSTALL=2;



}
