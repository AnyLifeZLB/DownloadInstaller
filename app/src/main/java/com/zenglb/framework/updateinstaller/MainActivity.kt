package com.zenglb.framework.updateinstaller

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

/**
 * app 内部升级
 *
 */
class MainActivity : AppCompatActivity(){
    //URL 下载有时间效益.自己替换可以正常下载的地址
    private val apkDownLoadUrl =
        "https://lebang-img.4009515151.com/2022/01/12/a6a58983-2059-4e6d-82a0-6a8c763a8806.apk"

    private val apkDownLoadUrl2 =
        "https://api.developer.xiaomi.com/autoupdate/updateself/download/fc6b8eba5351dedf2a79dab71c9bb299edcd1fb85AppStore_06af5546730ca4df0191ab263a2ae82b/com.engineer.map_1038.apk"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**
         * 测试升级安装
         */
        update.setOnClickListener { v: View? ->
            val data = CheckVersionResult(apkDownLoadUrl,1,"1.0.0","2021-11-11","description",0)
            startActivity(
                Intent(
                    this,
                    UpdateAppActivity::class.java
                ).putExtra("update", data)
            )
        }

        /**
         * 测试升级安装2,无效下载链接
         */
        update2.setOnClickListener { v: View? ->
            val data = CheckVersionResult(apkDownLoadUrl2,1,"1.0.0","2021-11-11","description",1)

            startActivity(
                Intent(
                    this,
                    UpdateAppActivity::class.java
                ).putExtra("update", data)
            )
        }
//        methodRequiresPermission()
    }

//
//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//
//        // Checks the orientation of the screen
//        if (newConfig.orientation === Configuration.ORIENTATION_LANDSCAPE) {
//            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show()
//        } else if (newConfig.orientation === Configuration.ORIENTATION_PORTRAIT) {
//            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//
//    /**
//     * 请求权限,创建目录的权限
//     */
//    private fun methodRequiresPermission() {
//        val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//        if (EasyPermissions.hasPermissions(this, *perms)) {
//            // Already have permission, do the thing
//            // ...
//        } else {
//            // Do not have permissions, request them now
//            EasyPermissions.requestPermissions(this@MainActivity, "App 升级需要储存权限", 10086, *perms)
//        }
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        // Forward results to EasyPermissions
//        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
//    }
//
//    override fun onPermissionsGranted(requestCode: Int, list: List<String>) {
//        // Some permissions have been granted
//        // ...
//    }
//
//    override fun onPermissionsDenied(requestCode: Int, list: List<String>) {
//        // Some permissions have been denied
//        // ...
//    }
}