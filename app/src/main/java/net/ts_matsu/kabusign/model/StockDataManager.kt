package net.ts_matsu.kabusign.model

import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.util.StockInfo
import net.ts_matsu.kabusign.view.MainFragment

// 時系列データ管理クラス
class StockDataManager {
    private val cName = MainFragment::class.java.simpleName

    companion object {
        // 時系列データを保持するリスト
        // key: 銘柄コード(String)
        private val stockDataList: MutableMap<String, MutableList<StockData>> = mutableMapOf()
        private val stockTodayDataList: MutableMap<String, StockTodayData> = mutableMapOf()

        // ファイル保存されている時系列データ銘柄リスト
//        private val stockList: MutableList<StockInfo> = mutableListOf()
    }

    // 時系列データの取得
    suspend fun getDataList(code: String, isUpdate: Boolean): MutableList<StockData> {
        return getDataListInner(code, isUpdate).first
    }
    // 本日データのみ取得
    suspend fun getTodayData(code: String): StockTodayData {
        return getDataListInner(code, true).second
    }

    private suspend fun getDataListInner(code: String, isUpdate: Boolean): Pair<MutableList<StockData>, StockTodayData> {
        val download = StockDownload()
        if(stockDataList.containsKey(code)){
            // キャッシュされている場合（ダウンロード済み）
            CommonInfo.debugInfo("$cName: 111")
            if(isUpdate){
                // データ更新要求であれば、最新データをダウンロードして更新する
                CommonInfo.debugInfo("$cName: 222")
                val lastDate = stockDataList[code]!![0].date
                val stockData = download.get(code, lastDate)
                for(d in stockData.first.asReversed()){
                    if(d.date == lastDate){
                        // 現状保持されている最新のデータの場合は、更新する
                        stockDataList[code]!![0] = d
                        CommonInfo.debugInfo("$cName: aaa(${d.date})")
                    }
                    else{
                        // 現状保持されている最新のデータより新しいデータは先頭に追加していく
                        stockDataList[code]!!.add(0, d)
                        CommonInfo.debugInfo("$cName: bbb(${d.date})")
                    }
                }
                stockTodayDataList[code] = stockData.second
            }
        }
        else{
            CommonInfo.debugInfo("$cName: 444")
            // キャッシュされてない場合は、最新データをダウンロードしてキャッシュする
            val stockData = download.get(code, "")
            stockDataList[code] = stockData.first
            stockTodayDataList[code] = stockData.second
        }
        return Pair(stockDataList[code]!!.asReversed(), stockTodayDataList[code]!!)
    }
}

data class StockData(val date: String, val open: Float, val high: Float, val low: Float, val close: Float, val volume: Int)
data class StockTodayData(val date: String, val time: String,
                          val open: Float, val high: Float, val low: Float, val close: Float, val volume: Int,
                          val ratio1: Float, val ratio2: Float)
