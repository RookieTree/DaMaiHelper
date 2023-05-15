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
    private lateinit var etSinger: EditText
    private var dialog: Dialog? = null
    private var systemAlertRequest: PermissionsRequester? = null

    override fun getLayoutID(): Int = R.layout.activity_main

    override fun init() {
        btnStart = findViewById(R.id.btn_start)
        etSinger = findViewById(R.id.et_singer)
        btnStart.setOnClickListener {
            systemAlertRequest?.launch()
        }
        systemAlertRequest = constructSystemAlertWindowPermissionRequest() {
            startQiangp()
        }
    }

    private fun startQiangp() {
        if (!isAccessibilitySettingsOn(DaMaiHelperService::class.java)) {
            showAccessDialog()
        } else {
            val single = etSinger.text
            if (!TextUtils.isEmpty(single)) {
                UserManager.singer = single.toString()
            }
            startDaMai()
            UserManager.startQp()
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