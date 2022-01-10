package net.ts_matsu.kabusign.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import net.ts_matsu.kabusign.model.StockDataManager
import net.ts_matsu.kabusign.model.StockFile
import net.ts_matsu.kabusign.model.StockTodayData
import net.ts_matsu.kabusign.model.data.DatabaseCache
import net.ts_matsu.kabusign.util.ResourceApp
import net.ts_matsu.kabusign.util.StockInfo
import net.ts_matsu.kabusign.view.AdapterInfo
import net.ts_matsu.kabusign.view.ConditionItemCandle
import net.ts_matsu.kabusign.view.ConditionItemPriceDesignation

class ConditionMainViewModel(): ViewModel() {
    private val cName = ConditionMainViewModel::class.java.simpleName

    private var code = ""
    private var name = ""
    private val stockList: List<StockInfo>
    private lateinit var stockTodayData: StockTodayData
    val adapterList = mutableListOf<AdapterInfo>()

    val requireClose = MutableLiveData(false)   // CANCELがタップされて、フラグメントを閉じることを通知
    val requireOk = MutableLiveData(false)      // OKがタップされて、フラグメントを閉じることを通知
    val mainText = MutableLiveData("")          // 最上部の銘柄名表示
    val todayDate = MutableLiveData("")         // 日付
    val todayTime = MutableLiveData("")         // 時刻

    // チャート更新時のプログレスバー表示（LiveDataはこの形にすべきなのかな？？）
    private val _isUpdateProgress = MutableLiveData<Boolean>(false)
    val isUpdateProgress: LiveData<Boolean> get() = _isUpdateProgress

    init {
        // 銘柄リスト読み込み
        val stockFile = StockFile()
        stockList = stockFile.getSupportedStockList()
    }

    fun setCode(_code: String) {
        code = _code

        // 銘柄名を設定する
        for(info in stockList) {
            if(code == info.code) {
                name = info.name
            }
        }
        mainText.value = "$code : $name"
    }

    fun getTodayData() {
        // 本日データ取得
        viewModelScope.launch {
//            runBlocking {
                val stockDataManager = StockDataManager()
                _isUpdateProgress.value = true
                stockTodayData = stockDataManager.getTodayData(code)
                todayDate.value = "${stockTodayData.date.substring(4,6)}/${stockTodayData.date.substring(6)}"
                todayTime.value = stockTodayData.time
                _isUpdateProgress.value = false
//            }
        }
    }
    // CANCELボタン処理
    fun onCancelButtonClick() {
        requireClose.value = true
    }

    // OKボタン処理
    fun onOkButtonClick() {
        viewModelScope.launch {
            updateData()
        }
        requireOk.value = true
    }

    // DB 保存処理
    private suspend fun updateData() {
        withContext(Dispatchers.IO) {
            val databaseCache = DatabaseCache()
            // 価格指定データを更新
            for(entity in databaseCache.getPriceDesignationEntityList()) {
                val dao = ResourceApp.database.priceDesignationDao()
                dao.update(entity)
            }
            databaseCache.clearPriceDesignationEntityList()

            // ローソク足指定データを更新
            for(entity in databaseCache.getCandleConditionEntityList()) {
                val dao = ResourceApp.database.candleConditionDao()
                dao.update(entity)
            }
            databaseCache.clearCandleConditionEntityList()
        }
    }

    // アダプタを設定する
    fun createAdapter(code: String) {
        adapterList.add(AdapterInfo("価格指定1", ConditionItemPriceDesignation(0, code)))
        adapterList.add(AdapterInfo("価格指定2", ConditionItemPriceDesignation(1, code)))
        adapterList.add(AdapterInfo("ローソク足", ConditionItemCandle(code)))
    }

    init {
        // DB を読み出して、サポートしている指定種別を読み出し、Adapterに反映させる todo
    }
}