package com.rookie.damaihelper

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


/*
 *  @项目名：  LearnLaw 
 *  @包名：    com.rookie.ll_common.base
 *  @文件名:   BaseActivity
 *  @创建者:   rookietree
 *  @创建时间:  2023/2/2 14:31
 *  @描述：    activity基类
 */
abstract class BaseActivity : AppCompatActivity() {

    val TAG: String = this.javaClass.simpleName

    abstract fun getLayoutID(): Int

    abstract fun init()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutID())
        init()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    fun skipActivity(cls: Class<*>) {
        startActivity(Intent(this, cls))
    }

}