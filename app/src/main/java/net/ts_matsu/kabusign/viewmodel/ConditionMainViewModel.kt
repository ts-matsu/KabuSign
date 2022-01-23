package net.ts_matsu.kabusign.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import net.ts_matsu.kabusign.R
import net.ts_matsu.kabusign.model.StockDataManager
import net.ts_matsu.kabusign.model.StockFile
import net.ts_matsu.kabusign.model.StockTodayData
import net.ts_matsu.kabusign.model.data.DatabaseCache
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.util.ResourceApp
import net.ts_matsu.kabusign.util.StockInfo
import net.ts_matsu.kabusign.view.AdapterInfo
import net.ts_matsu.kabusign.view.ConditionItemCandle
import net.ts_matsu.kabusign.view.ConditionItemPriceDesignation
import java.math.BigDecimal

class ConditionMainViewModel(): ViewModel() {
    private val cName = ConditionMainViewModel::class.java.simpleName

    private var code = ""
    private var name = ""
    private val stockList: List<StockInfo>
    private lateinit var stockTodayData: StockTodayData
    val adapterList = mutableListOf<AdapterInfo>()

    val requireClose = MutableLiveData(false)   // CANCELがタップされて、フラグメントを閉じることを通知
    val requireOk = MutableLiveData(false)      // OKがタップされて、フラグメントを閉じることを通知
    val mainCode = MutableLiveData("")          // 最上部の銘柄コード表示
    val mainTitle = MutableLiveData("")         // 最上部の銘柄名表示
    val todayDate = MutableLiveData("")         // 日付
    val todayTime = MutableLiveData("")         // 時刻
    val todayValue = MutableLiveData("")        // 本日(現在)の株価
    val todayDiffValue = MutableLiveData("")    // 前日比(価格)
    val todayDiffRatio = MutableLiveData("")    // 前日比(比率)

    val tvCodeColor = MutableLiveData(R.color.colorBlack)
    val tvTitleColor = MutableLiveData(R.color.colorBlack)
    val tvTimeColor = MutableLiveData(R.color.colorBlack)
    val tvValueColor = MutableLiveData(R.color.colorRed)

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
        mainCode.value = code
        mainTitle.value = name
    }

    fun getTodayData() {
        // 本日データ取得
        viewModelScope.launch {
            val stockDataManager = StockDataManager()
            _isUpdateProgress.value = true
            stockTodayData = stockDataManager.getTodayData(code)

            // 時刻更新
            todayDate.value = "${stockTodayData.date.substring(4,6)}/${stockTodayData.date.substring(6)}"
            todayTime.value = stockTodayData.time

            // 株価に関しては、小数点以下が0で無い場合のみ、少数を表示させる
            CommonInfo.debugInfo("$cName todayValue: ${1 % stockTodayData.close}")
            if(stockTodayData.close % 1 != 0f){
                todayValue.value = "%,.d".format(stockTodayData.close)
            }
            else {
                todayValue.value = "%,d".format(stockTodayData.close.toInt())
            }

            // 前日比(価格)
            if(stockTodayData.ratio1 % 1 != 0f){
                todayDiffValue.value = stockTodayData.ratio1.toString()
            }
            else {
                todayDiffValue.value = stockTodayData.ratio1.toInt().toString()
            }

            // (%)は、本来xmlファイル側で付けたいが、そうすると、初期状態からのデータ読み込み中表示で、
            // "(%)"と表示されて不細工なので、ここで付けることにする
            todayDiffRatio.value = "(${stockTodayData.ratio2}%)"

            // 前日比が＋の場合は、意図的に"+"を付加する（±0の場合は何も付加しない）
            if(stockTodayData.ratio1 > 0f){
                todayDiffValue.value = "+${todayDiffValue.value}"
                todayDiffRatio.value = todayDiffRatio.value!!.replace("(", "(+")
            }

            // 価格の色設定
            // 前日比がマイナスなら緑、プラスなら赤、±0なら黒とする
            when {
                stockTodayData.ratio1 > 0 -> tvValueColor.value = R.color.colorRed
                stockTodayData.ratio1 < 0 -> tvValueColor.value = R.color.colorGreen
                else -> tvValueColor.value = R.color.colorBlack
            }
            _isUpdateProgress.value = false
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