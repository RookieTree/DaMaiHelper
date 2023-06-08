package com.rookie.damaihelper

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat


/*
 *  @项目名：  AddFriend 
 *  @包名：    com.rookie.addfriend
 *  @文件名:   AddFriendService
 *  @创建者:   rookietree
 *  @创建时间:  2023/3/17 15:34
 *  @描述：
 */
class DaMaiHelperService : AccessibilityService(), UserManager.IStartListener {

    companion object {
        //首页-我的
        const val ME_UI = "cn.damai.mine.activity.MineMainActivity"  // 我的
        //演唱会信息页
        const val LIVE_DETAIL_UI =
            "cn.damai.trade.newtradeorder.ui.projectdetail.ui.activity.ProjectDetailActivity"
        //购票选择页
        const val LIVE_SELECT_DETAIL_UI =
            "cn.damai.commonbusiness.seatbiz.sku.qilin.ui.NcovSkuActivity"
        //购票结算页
        const val LIVE_TOTAL_UI = "cn.damai.ultron.view.activity.DmOrderActivity"

        const val ID_LIVE_DETAIL_BUY = "tv_left_main_text"//详情页-开抢
        const val ID_CONFIRM_BUY = "btn_buy" //确认购买

        const val STEP_READY = 0
        const val STEP_FIRST = 1
        const val STEP_SECOND = 2
        const val STEP_THIRD = 3
        const val STEP_FOURTH = 4
        const val STEP_FINISH = 5
    }

    private var isStop = false
    private var step = STEP_READY

    private var overlayView: View? = null
    private var mWindowManager: WindowManager? = null
    private val kaiQiangStr = "即将开抢"
    private val queHuoStr = "缺货登记"

    override fun onCreate() {
        super.onCreate()
        // 创建Notification渠道，并开启前台服务
        createForegroundNotification()?.let { startForeground(1, it) }
        UserManager.startListener = this
    }

    private fun showWindow() {
        if (mWindowManager == null) {
            // 获取 WindowManager
            mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            // 创建一个悬浮窗口 View
            overlayView =
                (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                    R.layout.float_app_view, null
                ) as ConstraintLayout
            // 设置悬浮窗口参数
            val flag =
                (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                flag,
                PixelFormat.TRANSLUCENT
            )
            val tvSwitch = overlayView?.findViewById<TextView>(R.id.tv_switch)
            tvSwitch?.setOnClickListener {
                stopForeground(true)
                mWindowManager?.removeView(overlayView)
                stopSelf()
                isStop = true
            }
            // 设置窗口布局的位置和大小
            params.gravity = Gravity.END or Gravity.TOP
            // 将悬浮窗口 View 添加到 WindowManager 中
            mWindowManager?.addView(overlayView, params)
        }
    }

    private var hasCheck: Boolean = false

    /**
     * 监听窗口变化的回调
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        logD("event_name:$event")
        if (event == null || isStop || step == STEP_FINISH) {
            return
        }
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            when (event.className.toString()) {
                ME_UI -> {
                    step = STEP_FIRST
                    event.source?.let { source ->
                        sleep(500)
                        val wantView = source.getNodeByText("想看&想玩")
                        wantView?.click()
                        val buyView = source.getNodeByText(UserManager.singer)
                        buyView?.click()
                    }
                }

                LIVE_DETAIL_UI -> {
                    step = STEP_SECOND
                    startQ(event)
                }

                LIVE_SELECT_DETAIL_UI -> {
                    step = STEP_THIRD
                    confirmOrder(event)
                }

                LIVE_TOTAL_UI -> {
                    step = STEP_FOURTH
                    requestOrder(event)
                }
            }
        } else if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            fullPrintNode("content_change",event.source)
            when (step) {
                STEP_SECOND -> {
                    startQ(event)
                }
                STEP_THIRD -> {
                    confirmOrder(event)
                }
                STEP_FOURTH -> {
                    requestOrder(event)
                }
            }
        }
    }

    private fun startQ(event: AccessibilityEvent) {
        event.source?.let { source ->
            val startBuy = source.getNodeById(dmNodeId(ID_LIVE_DETAIL_BUY))
            startBuy?.text()?.let {
                if (it != kaiQiangStr && it != queHuoStr) {
                    startBuy.click()
                    logD("buy_text:$it")
                }
            }
        }
    }

    private fun confirmOrder(event: AccessibilityEvent) {
        event.source?.let { source ->
            sleep(100)
            val buyView = source.getNodeById(dmNodeId(ID_CONFIRM_BUY))
            buyView?.click()
        }
    }

    private fun requestOrder(event: AccessibilityEvent) {
        event.source?.let { source ->
            val nodeByText = source.getNodeByText("提交订单", true)
            nodeByText?.let {
                it.click()
                step = STEP_FINISH
            }
        }
    }

    override fun onStart() {
        isStop = false
        hasCheck = false
        step = STEP_READY
        showWindow()
    }

    override fun onInterrupt() {

    }

    private fun createForegroundNotification(): Notification? {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val channelId = "damai"
        notificationManager?.createNotificationChannel(
            NotificationChannel(
                channelId, "大麦抢票", NotificationManager.IMPORTANCE_HIGH // 发送通知的等级，此处为高
            )
        )
        return NotificationCompat.Builder(this, channelId)
            // 设置点击notification跳转，比如跳转到设置页
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0, Intent(this, MainActivity::class.java), FLAG_IMMUTABLE
                )
            ).setSmallIcon(R.drawable.ic_app) // 设置小图标
            .setContentTitle(getString(R.string.acc_des)).setContentText("大麦抢票")
            .setTicker("大麦抢票").build()
    }

    override fun onDestroy() {
        stopForeground(true)
        mWindowManager?.removeView(overlayView)
        super.onDestroy()
    }

}