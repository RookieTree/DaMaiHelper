package com.rookie.damaihelper

import android.app.Dialog
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import permissions.dispatcher.ktx.PermissionsRequester
import permissions.dispatcher.ktx.constructSystemAlertWindowPermissionRequest


class MainActivity : BaseActivity() {

    private lateinit var btnStart: Button
    private lateinit var etUser: EditText
    private lateinit var etSinger: EditText
    private lateinit var etDay: EditText
    private lateinit var etPrice: EditText
    private var dialog: Dialog? = null
    private var systemAlertRequest: PermissionsRequester? = null

    override fun getLayoutID(): Int = R.layout.activity_main

    override fun init() {
        btnStart = findViewById(R.id.btn_start)
        etUser = findViewById(R.id.et_user)
        etSinger = findViewById(R.id.et_singer)
        etDay = findViewById(R.id.et_day)
        etPrice = findViewById(R.id.et_price)
        btnStart.setOnClickListener {
            systemAlertRequest?.launch()
//            startQiangp()
        }
        systemAlertRequest = constructSystemAlertWindowPermissionRequest() {
            startQiangp()
        }
    }

    private fun startQiangp() {
        if (!isAccessibilitySettingsOn(DaMaiHelperService::class.java)) {
            showAccessDialog()
        } else {
            val names = etUser.text
            if (TextUtils.isEmpty(names)) {
                shortToast("请输入观演人名字，空格隔开")
            } else {
                UserManager.contactList.clear()
                val list = names.split(" ")
                UserManager.contactList.addAll(list)
                val singer = etSinger.text
                val day = etDay.text
                val price = etPrice.text
                singer?.let {
                    UserManager.singer=it.toString()
                }
                day?.let {
                    UserManager.day=it.toString()
                }
                price?.let {
                    UserManager.price=it.toString()
                }
                startDaMai()
                UserManager.startQp()
            }
        }
    }

    private fun startDaMai() {
        startApp("cn.damai", DaMaiHelperService.ME_UI, "未安装大麦")
    }

    override fun onResume() {
        super.onResume()
        if (isAccessibilitySettingsOn(DaMaiHelperService::class.java)) {
            dialog?.dismiss()
        }
    }

    private fun showAccessDialog() {
        if (dialog == null) {
            dialog = AlertDialog.Builder(this)
                .setTitle("请打开<<${this.getString(R.string.acc_des)}>>无障碍服务")
                .setPositiveButton(
                    "确认"
                ) { dialog, _ ->
                    dialog.dismiss()
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    startActivity(intent)
                }.create()
        }
        dialog!!.show()
    }

}