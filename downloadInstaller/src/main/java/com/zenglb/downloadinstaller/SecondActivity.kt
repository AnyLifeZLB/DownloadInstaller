package com.zenglb.downloadinstaller

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.second_layout.*

class SecondActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.second_layout)

        val name = intent.getStringExtra("name")
        textView.text = "接收到的数据为：$name"

        textView.setOnClickListener {
            val intent = Intent().apply {
                putExtra("result","Hello，依然范特西稀，我是回传的数据！")
            }
            setResult(Activity.RESULT_OK,intent)
            finish()
        }
    }
}