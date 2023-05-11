package com.rookie.damaihelper


/*
 *  @项目名：  DaMaiHelper 
 *  @包名：    com.rookie.damaihelper
 *  @文件名:   UserManager
 *  @创建者:   rookietree
 *  @创建时间:  2023/5/11 14:19
 *  @描述：    
 */
object UserManager {

    interface IStartListener {
        fun onStart()
    }

    var startListener: IStartListener? = null

    var contactList = mutableListOf<String>()

    var day: String = "周五"
    var singer: String = "五月天"
    var price: String = "2"

    fun startQp() {
        startListener?.onStart()
    }

}