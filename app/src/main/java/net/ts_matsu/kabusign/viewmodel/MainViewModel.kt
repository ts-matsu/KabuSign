package net.ts_matsu.kabusign.viewmodel

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.CombinedData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.ts_matsu.kabusign.model.StockChart
import net.ts_matsu.kabusign.model.StockData
import net.ts_matsu.kabusign.model.StockDataManager
import net.ts_matsu.kabusign.model.StockDownload
import net.ts_matsu.kabusign.model.data.db.entity.ConditionKindEntity
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.util.ResourceApp

class MainViewModel: ViewModel() {
    private val cName = MainViewModel::class.java.simpleName

    // チャートデータ
    private val _chartData = MutableLiveData<CombinedData>()
    val chartData: LiveData<CombinedData> get() = _chartData

    // チャートデータに対応した日付データ
    private val _dateData = MutableLiveData<MutableList<String>>()
    val dateData: LiveData<MutableList<String>> get() = _dateData

    // 横線描画用アイコンの状態
    private val _isEditImageButton = MutableLiveData<Boolean>(false)
    val isEditImageButton: LiveData<Boolean> get() = _isEditImageButton

    // チャート更新時のプログレスバー表示
    private val _isUpdateProgress = MutableLiveData<Boolean>(false)
    val isUpdateProgress: LiveData<Boolean> get() = _isUpdateProgress

    // 保持する各種データ
    var currentStockCode = ""                                               // 現在表示中の銘柄コード
    private val chartList = mutableListOf<StockChart>()                     // 各銘柄のチャート情報


    init {
        CommonInfo.debugInfo("$cName: init")

        // デフォルトの条件種別を設定しておく
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                val dao = ResourceApp.database.conditionKindDao()
                for((i,name) in CommonInfo.conditionDefault.withIndex()){
                    val entity = ConditionKindEntity(i, name, true)
                    dao.insert(entity)
                }
            }
        }
    }

    // 銘柄情報（コード）を設定する
    fun setStockItem(item: String){
        val code = item.substring(item.length-5, item.length-1)     // "${info.name} (${info.code})" の形式になっている
        CommonInfo.debugInfo("$cName: setStockItem($code), ${code.isDigitsOnly()}")
        if(code.isNotEmpty() && code.isDigitsOnly()){
            currentStockCode = code
            setChartData(true)
        }
    }

    // 横線表示用のイメージボタン入力
    fun onClickEditImageButton() {
        _isEditImageButton.value = !_isEditImageButton.value!!
    }

    // チャート更新イメージボタンタップ
    fun onClickUpdateImageButton() {
        CommonInfo.debugInfo("$cName: onClickUpdateImageButton")
        if(currentStockCode.isNotEmpty()){
            setChartData(true)
        }
    }

    // 横線情報取得
    fun getLimitLine(data: Float): LimitLine?{
        // 横線描画OKの場合のみ設定する
        var result: LimitLine? = null
        if(currentStockCode.isNotEmpty() && _isEditImageButton.value!!){
            for(d in chartList){
                if(d.stockCode == currentStockCode){
                    result = d.getLimitLineData(data)
                }
            }
        }
        return result
    }

    // チャートデータを設定する
    // どの銘柄のどのチャートを表示するかは、ViewModelが握っている
    private fun setChartData(isUpdate: Boolean) {
        viewModelScope.launch {
            CommonInfo.debugInfo("$cName: setChartData")
            _isUpdateProgress.value = true        // プログレスバー開始

            val stockDataManager = StockDataManager()
            val data = stockDataManager.getDataList(currentStockCode, isUpdate)
            // chartList に追加
            val stockChart = StockChart(currentStockCode)
            chartList.add(stockChart)

            // グラフ情報更新
            CommonInfo.debugInfo("size: ${data.size}")
            _dateData.value = stockChart.getDateList(data)
            _chartData.value = stockChart.getChartData(data)
            CommonInfo.debugInfo("$cName: setChartData2")
            _isUpdateProgress.value = false        // プログレスバー終了
        }
        CommonInfo.debugInfo("$cName: setChartData3")
    }

    override fun onCleared() {
        CommonInfo.debugInfo("$cName: onCleared.")
        super.onCleared()
    }
}