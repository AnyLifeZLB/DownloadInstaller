package com.zenglb.downloadinstaller;

/**
 * 下载状态
 * 未下载，下载中，下载完成，下载失败，待安装
 *
 * 下载好了，有可能包有问题。所以假如安装失败要处理一下
 *
 */
public interface UpdateStatus {
    public final static int UN_DOWNLOAD =-1;
    public final static int DOWNLOADING=0;
    public final static int DOWNLOAD_ERROR =1;
    public final static int UNINSTALL=2;



}
