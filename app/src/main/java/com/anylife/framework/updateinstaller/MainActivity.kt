package com.anylife.framework.updateinstaller

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.dylanc.activityresult.launcher.StartActivityLauncher
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.EasyPermissions


/**
 * app 内部升级Demo
 *
 */
class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    //URL 下载有时间效益.自己替换可以正常下载的地址
    private val apkDownLoadUrl =
        "https://lebang-img.4009515151.com/2022/01/12/a6a58983-2059-4e6d-82a0-6a8c763a8806.apk"


    //这个链接会有多次重定向，会有失败问题。请替换自己的测试链接
    private val apkDownLoadUrl2 =
        "https://api.developer.xiaomi.com/autoupdate/updateself/download/fc6b8eba5351dedf2a79dab71c9bb299edcd1fb85AppStore_06af5546730ca4df0191ab263a2ae82b/com.engineer.map_1038.apk"


    private  var startActivityLauncher: StartActivityLauncher=StartActivityLauncher(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        startActivityLauncher=StartActivityLauncher(this@MainActivity)

        /**
         * 测试升级安装
         */
        update.setOnClickListener { v: View? ->
            val data = CheckVersionResult(
                apkDownLoadUrl,
                1,
                "1.0.0",
                "2021-11-11",
                "description",
                0
            )
             UpdateAppDialog(
                 this,
                 data,
                 startActivityLauncher
             ).show()
        }

        /**
         * 测试升级安装2,无效下载链接.
         *
         */
        update2.setOnClickListener { v: View? ->
            val data = CheckVersionResult(apkDownLoadUrl2,1,"1.0.0","2021-11-11","description",1)
            UpdateAppDialog(this, data, startActivityLauncher)
                .show()
        }


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            methodRequiresPermission()
        } else {
            //  低于24即为7.0以下执行内容
        }

        methodRequiresPermission()
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
        val a=3
    }

    override fun onPermissionsDenied(requestCode: Int, list: List<String>) {
        // Some permissions have been denied
        // ...
        val a=1
    }

}