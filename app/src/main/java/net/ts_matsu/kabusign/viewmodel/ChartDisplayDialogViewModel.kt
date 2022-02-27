package net.ts_matsu.kabusign.viewmodel

import android.graphics.drawable.LevelListDrawable
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.CombinedData
import kotlinx.coroutines.launch
import net.ts_matsu.kabusign.R
import net.ts_matsu.kabusign.model.StockChart
import net.ts_matsu.kabusign.model.StockDataManager
import net.ts_matsu.kabusign.util.CommonInfo

class ChartDisplayDialogViewModel : ViewModel() {
    private val cName = ChartDisplayDialogViewModel::class.java.simpleName

    val requireClose = MutableLiveData(false)   // CANCELがタップされて、ダイアログを閉じることを通知

    // チャートデータ
    private val _chartData = MutableLiveData<CombinedData>()
    val chartData: LiveData<CombinedData> get() = _chartData

    // チャートデータに対応した日付データ
    private val _dateData = MutableLiveData<MutableList<String>>()
    val dateData: LiveData<MutableList<String>> get() = _dateData

    // 横線描画用アイコンの状態
    val ivLineEditState = MutableLiveData(0)    // 横線アイコンの状態
    private val ivLineEditStateNone = 0
    private val ivLineEditStateAdd = 1
    private val ivLineEditStateMode = 2
    private val ivLineEditStateDelete = 3
    private val ivMaxState = 3

    // チャート更新時のプログレスバー表示
    private val _isUpdateProgress = MutableLiveData<Boolean>(false)
    val isUpdateProgress: LiveData<Boolean> get() = _isUpdateProgress

    // 保持する各種データ
    var currentStockCode = ""                                               // 現在表示中の銘柄コード
    private val chartList = mutableListOf<StockChart>()                     // 各銘柄のチャート情報(1銘柄だけになると思うが)

    // 横線情報
    private val limitLineInfo = mutableListOf<LimitLine>()

    // 銘柄情報（コード）を設定する
    fun setStockItem(code: String){
//        val code = item.substring(item.length-5, item.length-1)     // "${info.name} (${info.code})" の形式になっている
        CommonInfo.debugInfo("$cName: setStockItem($code), ${code.isDigitsOnly()}")
        if(code.isNotEmpty() && code.isDigitsOnly()){
            currentStockCode = code
            setChartData(true)
        }
    }

    // CANCELボタン処理
    fun onNegativeButtonClick() {
        requireClose.value = true
    }

    // チャート更新イメージボタンタップ
    fun onClickUpdateImageButton() {
        if(currentStockCode.isNotEmpty()){
            setChartData(true)
        }
    }

    // 横線追加
    fun setLimitLine(data: Float) {
        if(currentStockCode.isNotEmpty() && (ivLineEditState.value!! == ivLineEditStateAdd)){
            for(d in chartList){
                limitLineInfo.add(d.getLimitLineData(data))
                ivLineEditState.value  = ivLineEditStateNone
                break
            }
        }
    }

    // 横線情報削除＆追加（アイコンがインアクティブの場合のみ）
    // 移動させる時に使う想定
    fun removeAndSetLimitLine(rData: Float, aData: Float): Boolean {
        var result = false
        if(currentStockCode.isNotEmpty() && ivLineEditState.value!! == ivLineEditStateMode){
            removeLimitLine(rData)
            for(d in chartList) {
                limitLineInfo.add(d.getLimitLineData(aData))
                result = true
                break
            }
        }
        return result
    }

    // 横線情報削除
    fun removeLimitLine(data: Float){
        var removeIndex = -1
        for((i,info) in limitLineInfo.withIndex()){
            if(info.limit == data){
                removeIndex = i
            }
        }
        if(removeIndex != -1){
            limitLineInfo.removeAt(removeIndex)
        }
    }

    // 横線情報取得
    fun getLimitLine(data: Float): LimitLine?{
        // 横線描画OKの場合のみ設定する
        var result: LimitLine? = null
        var resultData = 1000000f     // 株価としては、ありえない大きな値にしておく
        if(currentStockCode.isNotEmpty()){
            for(d in chartList){
                if(d.stockCode == currentStockCode){
                    var tmpInfo: LimitLine? = null
                    // limitLineInfoで、最も近い値の情報を返す（位置の誤差が1%未満）
                    for(line in limitLineInfo){
                        val tmpAbs = Math.abs(data - line.limit)
                        if((tmpAbs < resultData) && (tmpAbs < data/100)){
                            resultData = tmpAbs
                            tmpInfo = line
                        }
                    }
                    if(resultData < 1000000f){
                        result = tmpInfo
                    }
                }
            }
        }
        return result
    }

    // 横線表示用のイメージボタン入力
    fun onClickLineImageButton() {
        if(ivLineEditState.value!! < ivMaxState){
            ivLineEditState.value = ivLineEditState.value!! + 1
        }
        else {
            ivLineEditState.value = 0
        }
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


}