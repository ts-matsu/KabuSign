package net.ts_matsu.kabusign.model

import kotlinx.coroutines.delay
import net.ts_matsu.kabusign.MainActivity
import net.ts_matsu.kabusign.R
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.util.ResourceApp
import net.ts_matsu.kabusign.util.StockInfo
import net.ts_matsu.kabusign.viewmodel.StockSelectViewModel
import org.json.JSONObject
import java.io.File
import java.lang.StringBuilder
import java.lang.Thread.sleep

object StockSelect {
    private val cName = StockSelect::class.java.simpleName
    private val stockInfo = mutableListOf<StockInfo>()

    // 銘柄リスト取得
    suspend fun getStockList(): List<StockInfo>{
        val stockFile = StockFile()
        for(d in stockFile.getSupportedStockList()){
            stockInfo.add(d)
        }
        return stockInfo
    }

    // 銘柄リスト検索
    fun searchStockList(text: String): List<StockInfo>{
        CommonInfo.debugInfo("$cName  searchStockList($text)")
        return  stockInfo.filter {
            it.code.contains(text) || it.name.contains(text)
        }
    }
}

