package com.rookie.damaihelper

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_RECENTS
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.*
import android.database.Cursor
import android.graphics.Path
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.text.TextUtils.SimpleStringSplitter
import android.util.Log
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast


/**
 * 检测无障碍服务是否开启
 * */
fun Context.isAccessibilitySettingsOn(clazz: Class<out AccessibilityService?>): Boolean {
    var accessibilityEnabled = false    // 判断设备的无障碍功能是否可用
    try {
        accessibilityEnabled = Settings.Secure.getInt(
            applicationContext.contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED
        ) == 1
    } catch (e: Settings.SettingNotFoundException) {
        e.printStackTrace()
    }
    val mStringColonSplitter = TextUtils.SimpleStringSplitter(':')
    if (accessibilityEnabled) {
        // 获取启用的无障碍服务
        val settingValue: String? = Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        if (settingValue != null) {
            // 遍历判断是否包含我们的服务
            mStringColonSplitter.setString(settingValue)
            while (mStringColonSplitter.hasNext()) {
                val accessibilityService = mStringColonSplitter.next()
                if (accessibilityService.equals(
                        "${packageName}/${clazz.canonicalName}",
                        ignoreCase = true
                    )
                ) return true

            }
        }
    }
    return false
}

@SuppressLint("UseCompatLoadingForDrawables")
fun Context.getDrawableRes(resId: Int): Drawable =
    applicationContext.resources.getDrawable(resId, null)

fun Context.getStringRes(resId: Int): String = applicationContext.resources.getString(resId, "")

fun Context.shortToast(msg: String) =
    Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()

fun Context.longToast(msg: String) =
    Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()

const val TAG = "大麦助手"
const val SEGMENT_SIZE = 3072

fun logD(content: String) {
    if (content.length < SEGMENT_SIZE) {
        Log.d(TAG, content)
        return
    } else {
        Log.d(TAG, content.substring(0, SEGMENT_SIZE))
        logD(content.substring(SEGMENT_SIZE))
    }
}

//int segmentSize = 3 * 1024;
//long length = msg.length();
//if (length <= segmentSize ) {// 长度小于等于限制直接打印
//    Log.e(tag, msg);
//}else {
//    while (msg.length() > segmentSize ) {// 循环分段打印日志
//        String logContent = msg.substring(0, segmentSize );
//        msg = msg.replace(logContent, "");
//        Log.e(tag, logContent);
//    }
//    Log.e(tag, msg);// 打印剩余日志
//}


/**
 * 跳转其它APP
 * @param packageName 跳转APP包名
 * @param activityName 跳转APP的Activity名
 * @param errorTips 跳转页面不存在时的提示
 * */
fun Context.startApp(packageName: String, activityName: String, errorTips: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = ComponentName(packageName, activityName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    } catch (e: ActivityNotFoundException) {
        shortToast(errorTips)
    } catch (e: Exception) {
        e.message?.let { logD(it) }
    }
}

/**
 * 跳转其它APP
 * @param urlScheme URL Scheme请求字符串
 * @param errorTips 跳转页面不存在时的提示
 * */
fun Context.startApp(urlScheme: String, errorTips: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlScheme)))
    } catch (e: ActivityNotFoundException) {
        shortToast(errorTips)
    } catch (e: Exception) {
        e.message?.let { logD(it) }
    }
}

/**
 * 根据id查找单个节点
 * @param id 控件id
 * @return 对应id的节点
 * */
fun AccessibilityNodeInfo.getNodeById(id: String): AccessibilityNodeInfo? {
    var count = 0
    while (count < 10) {
        findAccessibilityNodeInfosByViewId(id).let {
            if (!it.isNullOrEmpty()) return it[0]
        }
        sleep(100)
        count++
    }
    logD("查找组件，id:$id 找不到")
    return null
}

/**
 * 根据id查找多个节点
 * @param id 控件id
 * @return 对应id的节点列表
 * */
fun AccessibilityNodeInfo.getNodesById(id: String): List<AccessibilityNodeInfo>? {
    var count = 0
    while (count < 10) {
        findAccessibilityNodeInfosByViewId(id).let {
            if (!it.isNullOrEmpty()) return it
        }
        sleep(100)
        count++
    }
    return null
}

/**
 * 根据文本查找单个节点
 * @param text 匹配文本
 * @param allMatch 是否全匹配，默认false，contains()方式的匹配
 * @return 匹配文本的节点
 * */
fun AccessibilityNodeInfo.getNodeByText(
    text: String,
    allMatch: Boolean = false
): AccessibilityNodeInfo? {
    var count = 0
    while (count < 10) {
        findAccessibilityNodeInfosByText(text).let {
            if (!it.isNullOrEmpty()) {
                if (allMatch) {
                    it.forEach { node -> if (node.text == text) return node }
                } else {
                    return it[0]
                }
            }
            sleep(100)
            count++
        }
    }
    logD("查找组件，text:$text 找不到")
    return null
}

/**
 * 根据文本查找多个节点
 * @param text 匹配文本
 * @param allMatch 是否全匹配，默认false，contains()方式的匹配
 * @return 匹配文本的节点列表
 * */
fun AccessibilityNodeInfo.getNodesByText(
    text: String,
    allMatch: Boolean = false
): List<AccessibilityNodeInfo>? {
    var count = 0
    while (count < 10) {
        findAccessibilityNodeInfosByText(text).let {
            if (!it.isNullOrEmpty()) {
                return if (allMatch) {
                    val tempList = arrayListOf<AccessibilityNodeInfo>()
                    it.forEach { node -> if (node.text == text) tempList.add(node) }
                    if (tempList.isEmpty()) null else tempList
                } else {
                    it
                }
            }
            sleep(100)
            count++
        }
    }
    return null
}

/**
 * 获取结点的文本
 * */
fun AccessibilityNodeInfo?.text(): String {
    return this?.text?.toString() ?: ""
}


/**
 * 点击，迭代能点击的父节点
 * */
fun AccessibilityNodeInfo?.click() {
    if (this == null) return
    if (this.isClickable) {
        sleep(100)
        this.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        return
    } else {
        this.parent.click()
    }
}

// 长按
fun AccessibilityNodeInfo?.longClick() {
    if (this == null) return
    if (this.isClickable) {
        this.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
        return
    } else {
        this.parent.longClick()
    }
}

// 向下滑动一下
fun AccessibilityNodeInfo.scrollForward() =
    performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)

// 向上滑动一下
fun AccessibilityNodeInfo.scrollBackward() =
    performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)

// 填充文本
fun AccessibilityNodeInfo.input(content: String) = performAction(
    AccessibilityNodeInfo.ACTION_SET_TEXT, Bundle().apply {
        putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, content)
    }
)

fun AccessibilityService.back() {
    performGlobalAction(GLOBAL_ACTION_BACK)

}

fun AccessibilityService.recentTask(){
    performGlobalAction(GLOBAL_ACTION_RECENTS)
}

/**
 * 利用手势模拟点击
 * @param node: 需要点击的节点
 * */
fun AccessibilityService.gestureClick(node: AccessibilityNodeInfo?) {
    if (node == null) return
    val tempRect = Rect()
    node.getBoundsInScreen(tempRect)
    val x = ((tempRect.left + tempRect.right) / 2).toFloat()
    val y = ((tempRect.top + tempRect.bottom) / 2).toFloat()
    dispatchGesture(
        GestureDescription.Builder().apply {
            addStroke(GestureDescription.StrokeDescription(Path().apply { moveTo(x, y) }, 0L, 200L))
        }.build(),
        object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                logD("手势点击完成: 【$x - $y】")
            }
        },
        null
    )
}

fun sleep(millisecond: Long) {
    Thread.sleep((millisecond))
}

// 遍历所有结点的方法
fun fullPrintNode(
    tag: String,
    parentNode: AccessibilityNodeInfo?,
    spaceCount: Int = 0
): AccessibilityNodeInfo? {
    if (parentNode == null) return null
    val spaceSb = StringBuilder().apply { repeat(spaceCount) { append("  ") } }
    logD("$tag: $spaceSb${parentNode.text} → ${parentNode.viewIdResourceName} → ${parentNode.className} → Clickable: ${parentNode.isClickable}")
    if (parentNode.childCount == 0) return null
    for (i in 0 until parentNode.childCount) {
        fullPrintNode(tag, parentNode.getChild(i), spaceCount + 1)
    }
    return null
}

/**
 * 遍历打印结点
 * */
fun AccessibilityNodeInfo?.fullPrintNode(
    tag: String,
    spaceCount: Int = 0
) {
    if (this == null) return
    val spaceSb = StringBuilder().apply { repeat(spaceCount) { append("  ") } }
    logD("$tag: $spaceSb$text | $viewIdResourceName | $className | Clickable: $isClickable")
    if (childCount == 0) return
    for (i in 0 until childCount) getChild(i).fullPrintNode(tag, spaceCount + 1)
}

const val WX_PKG_NAME = "cn.damai"
fun dmNodeId(id: String) = "$WX_PKG_NAME:id/$id"

fun AccessibilityService.refreshTask() {
    recentTask()
    back()
}

@Throws(RuntimeException::class)
fun Context.isAccessibilityEnabled(): Boolean {
    // 检查AccessibilityService是否开启
    val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val isAccessibilityEnabled_flag = am.isEnabled
    var isExploreByTouchEnabled_flag = false

    // 检查无障碍服务是否以语音播报的方式开启
    isExploreByTouchEnabled_flag = isScreenReaderActive(this)
    return isAccessibilityEnabled_flag && isExploreByTouchEnabled_flag
}

private const val SCREEN_READER_INTENT_ACTION = "android.accessibilityservice.AccessibilityService"
private const val SCREEN_READER_INTENT_CATEGORY =
    "android.accessibilityservice.category.FEEDBACK_SPOKEN"

private fun isScreenReaderActive(context: Context): Boolean {

    // 通过Intent方式判断是否存在以语音播报方式提供服务的Service，还需要判断开启状态
    val screenReaderIntent = Intent(SCREEN_READER_INTENT_ACTION)
    screenReaderIntent.addCategory(SCREEN_READER_INTENT_CATEGORY)
    val screenReaders = context.packageManager.queryIntentServices(screenReaderIntent, 0)
    // 如果没有，返回false
    if (screenReaders == null || screenReaders.size <= 0) {
        return false
    }
    var hasActiveScreenReader = false
    if (Build.VERSION.SDK_INT >= 26) {
        // 高版本可以直接判断服务是否处于开启状态
        for (screenReader in screenReaders) {
            hasActiveScreenReader = hasActiveScreenReader or isAccessibilitySettingsOn(
                context,
                screenReader.serviceInfo.packageName + "/" + screenReader.serviceInfo.name
            )
        }
    } else {
        // 判断正在运行的Service里有没有上述存在的Service
        val runningServices: MutableList<String> = ArrayList()
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            runningServices.add(service.service.packageName)
        }
        for (screenReader in screenReaders) {
            if (runningServices.contains(screenReader.serviceInfo.packageName)) {
                hasActiveScreenReader = hasActiveScreenReader or true
            }
        }
    }
    return hasActiveScreenReader
}

// To check if service is enabled
private fun isAccessibilitySettingsOn(context: Context, service: String): Boolean {
    val mStringColonSplitter = SimpleStringSplitter(':')
    val settingValue = Settings.Secure.getString(
        context.applicationContext.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    if (settingValue != null) {
        mStringColonSplitter.setString(settingValue)
        while (mStringColonSplitter.hasNext()) {
            val accessibilityService = mStringColonSplitter.next()
            if (accessibilityService.equals(service, ignoreCase = true)) {
                return true
            }
        }
    }
    return false
}
