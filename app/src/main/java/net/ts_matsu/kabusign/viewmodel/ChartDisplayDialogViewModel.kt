package net.ts_matsu.kabusign.viewmodel

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.LevelListDrawable
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.CombinedData
import kotlinx.coroutines.launch
import net.ts_matsu.kabusign.R
import net.ts_matsu.kabusign.model.StockChart
import net.ts_matsu.kabusign.model.StockData
import net.ts_matsu.kabusign.model.StockDataManager
import net.ts_matsu.kabusign.model.StockTodayData
import net.ts_matsu.kabusign.util.CommonInfo

class ChartDisplayDialogViewModel : ViewModel() {
    private val cName = ChartDisplayDialogViewModel::class.java.simpleName

    val minChartData = 10f   // 画面表示するチャートデータの最小数
    val yAxisLabelWidth = 20f // Y軸ラベル領域の幅(dp)

    // CANCELがタップされて、ダイアログを閉じることを通知
    val requireClose = MutableLiveData(false)

    // チャートデータ
    private val _chartData = MutableLiveData<CombinedData>()
    val chartData: LiveData<CombinedData> get() = _chartData

    // 出来高チャートデータ
    private val _volumeChartData = MutableLiveData<BarData>()
    val volumeChartData: LiveData<BarData> get() = _volumeChartData

    // チャートデータに対応した日付データ
    private val _dateData = MutableLiveData<MutableList<String>>()
    val dateData: LiveData<MutableList<String>> get() = _dateData

    // 選択日の価格表示色
    private val _colorDayValue = MutableLiveData(R.color.colorBlack)
    val colorDayValue: LiveData<Int> get() = _colorDayValue

    // 選択日の前日比表示色
    private val _colorBeforeRatio = MutableLiveData(R.color.colorBlack)
    val colorBeforeRatio: LiveData<Int> get() = _colorBeforeRatio

    // 選択日の株価データ
    private val _openData = MutableLiveData<String>()
    val openData: LiveData<String> get() = _openData
    private val _closeData = MutableLiveData<String>()
    val closeData: LiveData<String> get() = _closeData
    private val _highData = MutableLiveData<String>()
    val highData: LiveData<String> get() = _highData
    private val _lowData = MutableLiveData<String>()
    val lowData: LiveData<String> get() = _lowData
    private val _volumeData = MutableLiveData<String>()
    val volumeData: LiveData<String> get() = _volumeData
    private val _ratioData = MutableLiveData<String>()
    val ratioData: LiveData<String> get() = _ratioData
    private val _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String> get() = _selectedDate


    // 横線描画用アイコンの状態
    // 値については、line_ic_list.xml で定義している値と同じにしないといけない
    val ivLineEditState = MutableLiveData(0)    // 横線アイコンの状態
    private val ivLineEditStateNone = 0
    private val ivLineEditStateAdd = 1
    private val ivLineEditStateMove = 2
    private val ivLineEditStateDelete = 3
    private val ivMaxState = 3

    // チャート更新時のプログレスバー表示
    private val _isUpdateProgress = MutableLiveData<Boolean>(false)
    val isUpdateProgress: LiveData<Boolean> get() = _isUpdateProgress

    // 保持する各種データ
    var currentStockCode = ""                                               // 現在表示中の銘柄コード
    private val chartList = mutableListOf<StockChart>()                     // 各銘柄のチャート情報(1銘柄だけになると思うが)
    private val stockData = mutableListOf<StockData>()                      // 銘柄データ（時系列データ９

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

    // 横線情報削除＆追加（アイコンがMOVE or DELETEの時のみ）
    // 移動させる時、もしくは削除する時に使う想定
    fun removeAndSetLimitLine(rData: Float, aData: Float): Boolean {
        var result = false
        if(currentStockCode.isNotEmpty() &&
            (ivLineEditState.value!! == ivLineEditStateMove || ivLineEditState.value!! == ivLineEditStateDelete)){
            removeLimitLine(rData)
            // MOVE の場合、追加処理を行う
            if(ivLineEditState.value!! == ivLineEditStateMove){
                for(d in chartList) {
                    limitLineInfo.add(d.getLimitLineData(aData))
                    break
                }
            }
            result = true
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

    // 指定日のデータ取得
    fun setStockDataAt(x: Int) {
        var index = x
        if(index == -1){
            index = stockData.size - 1
        }
        // 日付
        stockData[index].date.let {
            _selectedDate.value = "${it.substring(4,6)}/${it.substring(6)}"
        }
        // 始値
        stockData[index].open.let {
            _openData.value = "%,d".format(it.toInt())
            if(it % 1 != 0f){
                _openData.value = "%,.1f".format(it)
            }
        }
        // 終値
        stockData[index].close.let {
            _closeData.value = "%,d".format(it.toInt())
            if(it % 1 != 0f){
                _closeData.value = "%,.1f".format(it)
            }
        }
        // 高値
        stockData[index].high.let {
            _highData.value = "%,d".format(it.toInt())
            if(it % 1 != 0f){
                _highData.value = "%,.1f".format(it)
            }
        }
        // 安値
        stockData[index].low.let {
            _lowData.value = "%,d".format(it.toInt())
            if(it % 1 != 0f){
                _lowData.value = "%,.1f".format(it)
            }
        }
        // 出来高
        stockData[index].volume.let {
            _volumeData.value = "%,d".format(it)
        }
        // 前日比
        if(index > 0){
            val diff = stockData[index].close - stockData[index-1].close
            val diffRatio = diff / stockData[index-1].close * 100
            if((stockData[index].close % 1 != 0f) || stockData[index-1].close % 1 != 0f){
                // 小数点あり
                _ratioData.value = "0.0 "
                if(diff > 0) {
                    _ratioData.value = "+%,.2f ".format(diff)
                }
                else if( diff < 0){
                    _ratioData.value = "-%,.2f ".format(diff)
                }
            }
            else{
                // 小数点なし
                _ratioData.value = "0 "
                _colorBeforeRatio.value = R.color.colorBlack
                if(diff > 0) {
                    _ratioData.value = "+%,d (+%,.2f".format(diff.toInt(), diffRatio) + "%)"
                    _colorBeforeRatio.value = R.color.colorRed
                }
                else if( diff < 0){
                    // マイナスの記号は勝手につくので、意図的に付けない
                    _ratioData.value = "%,d (%,.2f".format(diff.toInt(), diffRatio) + "%)"
                    _colorBeforeRatio.value = R.color.colorGreen
                }
            }
        }
        else {
            _ratioData.value = "- (-.-%)"
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
            stockData.addAll(stockDataManager.getDataList(currentStockCode, isUpdate))
            // chartList に追加
            val stockChart = StockChart(currentStockCode)
            chartList.add(stockChart)

            // 出来高データを先に設定（チャートデータをFragmentで監視しているため）
            _volumeChartData.value = stockChart.getVolumeChartData(stockData)

            // グラフ情報更新
            CommonInfo.debugInfo("size: ${stockData.size}")
            _dateData.value = stockChart.getDateList(stockData)
            _chartData.value = stockChart.getChartData(stockData)
            CommonInfo.debugInfo("$cName: setChartData2")

            _isUpdateProgress.value = false        // プログレスバー終了
        }
        CommonInfo.debugInfo("$cName: setChartData3")
    }


}