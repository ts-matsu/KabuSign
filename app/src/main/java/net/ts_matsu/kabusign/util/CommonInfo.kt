package net.ts_matsu.kabusign.util

import android.util.Log

object CommonInfo {
    const val DEBUG_TAG = "TagKabuSign"
    val conditionDefault = arrayOf<String>("価格指定", "ローソク足指定")

    fun debugInfo(info: String){
        Log.i(DEBUG_TAG, info)
    }
    fun warnInfo(info: String){
        Log.w(DEBUG_TAG, info)
    }
    const val MAX_STOCK_NUM = 20        // 同時に監視できる銘柄数
}
data class StockInfo(val code: String, val name: String)
data class MonitoredStockItem(val code: String, val name: String, val enableInfo: Boolean, val enableNotify: Boolean,
                              var isClickItem: Int)
