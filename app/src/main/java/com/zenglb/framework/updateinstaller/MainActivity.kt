package com.zenglb.framework.updateinstaller

import android.Manifest
import android.content.res.Configuration
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import android.os.Bundle
import com.zenglb.framework.updateinstaller.R
import android.view.LayoutInflater
import android.view.View
import com.daimajia.numberprogressbar.NumberProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.zenglb.downloadinstaller.DownloadInstaller
import com.zenglb.downloadinstaller.DownloadProgressCallBack
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.EasyPermissions
import java.lang.Exception

/**
 * app 内部升级
 *
 */
class MainActivity : AppCompatActivity(), PermissionCallbacks {
    //URL 下载有时间效益.自己替换可以正常下载的地址
    private val apkDownLoadUrl =
        "https://dldir1.qq.com/weixin/android/weixin7020android1780_arm64.apk"

    //这是一个无效的下载网址，你可以改为你自己的下载地址
    private val apkDownLoadUrl2 =
        "https://api.developer.xiaomi.com/autoupdate/updateself/download/fc6b8eba5351dedf2a79dab71c9bb299edcd1fb85AppStore_06af5546730ca4df0191ab263a2ae82b/com.engineer.map_1038.apk"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /**
         * 测试升级安装
         */
        update.setOnClickListener { v: View? ->
            showUpdateDialog(
                "1.升级后App 会自动攒钱\n2.还可以做白日梦",
                true,
                apkDownLoadUrl
            )
        }
        /**
         * 测试升级安装2,无效下载链接
         */
        update2.setOnClickListener { v: View? ->
            showUpdateDialog(
                "1.升级后App 会自动攒钱\n2.还可以做白日梦",
                false,
                apkDownLoadUrl2
            )
        }
        methodRequiresPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Checks the orientation of the screen
        if (newConfig.orientation === Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show()
        } else if (newConfig.orientation === Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show()
        }
    }




    /**
     * 显示下载的对话框,是否要强制的升级还是正常的升级
     *
     *
     * @param UpdateMsg     升级信息
     * @param isForceUpdate 是否是强制升级
     * @param downloadUrl   APK 下载URL
     */
    private fun showUpdateDialog(UpdateMsg: String, isForceUpdate: Boolean, downloadUrl: String) {
        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val updateView = inflater.inflate(R.layout.update_layout, null)
        val progressBar: NumberProgressBar = updateView.findViewById(R.id.tips_progress)
        val updateMsg = updateView.findViewById<TextView>(R.id.update_mess_txt)
        updateMsg.text = UpdateMsg
        builder.setTitle("发现新版本")
        var negativeBtnStr = "以后再说"
        if (isForceUpdate) {
            builder.setTitle("强制升级")
            negativeBtnStr = "退出应用"
        }
        builder.setView(updateView)
        builder.setNegativeButton(negativeBtnStr, null)
        builder.setPositiveButton(R.string.apk_update_yes, null)
        val downloadDialog = builder.create()
        downloadDialog.setCanceledOnTouchOutside(false)
        downloadDialog.setCancelable(false)
        downloadDialog.show()
        downloadDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { v: View? ->
            if (isForceUpdate) {
                progressBar.visibility = View.VISIBLE

                //新加 isForceGrantUnKnowSource 参数

                //如果是企业内部应用升级，肯定是要这个权限，其他情况不要太流氓，TOAST 提示
                //这里演示要强制安装
                DownloadInstaller(this, downloadUrl, true, object : DownloadProgressCallBack {
                    override fun downloadProgress(progress: Int) {
                        runOnUiThread { progressBar.progress = progress }
                        if (progress == 100) {
                            downloadDialog.dismiss()
                        }
                    }

                    override fun downloadException(e: Exception) {}
                    override fun onInstallStart() {
                        downloadDialog.dismiss()
                    }
                }).start()

                //升级按钮变灰色
                downloadDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GRAY)
            } else {
                DownloadInstaller(this, downloadUrl).start()
                downloadDialog.dismiss()
            }
        }
        
        downloadDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { v: View? ->
            if (isForceUpdate) {
                downloadDialog.dismiss()
                finish()
            } else {
                downloadDialog.dismiss()
            }
        }
    }

    /**
     * 请求权限,创建目录的权限
     */
    private fun methodRequiresPermission() {
        val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            // Already have permission, do the thing
            // ...
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this@MainActivity, "App 升级需要储存权限", 10086, *perms)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, list: List<String>) {
        // Some permissions have been granted
        // ...
    }

    override fun onPermissionsDenied(requestCode: Int, list: List<String>) {
        // Some permissions have been denied
        // ...
    }
}