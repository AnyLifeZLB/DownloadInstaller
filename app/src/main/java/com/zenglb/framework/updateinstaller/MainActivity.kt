package com.zenglb.framework.updateinstaller

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dylanc.activityresult.launcher.StartActivityLauncher
import com.zenglb.downloadinstaller.AppUtils
import kotlinx.android.synthetic.main.activity_main.*

/**
 * app 内部升级Demo
 *
 */
class MainActivity : AppCompatActivity(){
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
            val data = CheckVersionResult(apkDownLoadUrl,1,"1.0.0","2021-11-11","description",0)
             UpdateAppDialog(this,data,startActivityLauncher).show()
        }

        /**
         * 测试升级安装2,无效下载链接.
         *
         */
        update2.setOnClickListener { v: View? ->
            val data = CheckVersionResult(apkDownLoadUrl2,1,"1.0.0","2021-11-11","description",1)
            UpdateAppDialog(this,data,startActivityLauncher).show()
        }

    }

}