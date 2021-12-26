package net.ts_matsu.kabusign.model

import android.graphics.Paint
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import net.ts_matsu.kabusign.R
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.util.ResourceApp

class StockChart(code: String) {
    private val combinedData = CombinedData()
    private var maData: LineData? = null
    private var candleData: CandleData? = null
    var stockCode = ""

    init {
        stockCode = code
    }

    fun getChartData(stockData: MutableList<StockData>):  CombinedData {
        // チャート情報クリア
        combinedData.clearValues()

        // ローソク足データ
        candleData = getCandleStickData(stockData)

        // 移動平均線データ
        maData = getMAData(stockData, listOf(60, 20, 5))

        // CombinedChart にデータをセット
        combinedData.setData(candleData)
        combinedData.setData(maData)

        return combinedData
    }

    // 横線用のLimitLineデータ取得
    fun getLimitLineData(yValue: Float): LimitLine {
        // 値をlabelに表示させる
        return  LimitLine(yValue, "$yValue").apply {
            lineWidth = 1.0f
            labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
            textSize = 10f
        }
    }

    // 日付データ取得（この関数は、ここにあるべきではないかもしれない）
    fun getDateList(stockData: MutableList<StockData>):MutableList<String> {
        val data = mutableListOf<String>()
        for((i,d) in stockData.withIndex()){
            val tDate = d.date.substring(2,4) + "/" + d.date.substring(4,6) + "/" + d.date.substring(6)
            data.add(tDate)
        }
        return data
    }

    // ローソク足データ取得
    private fun getCandleStickData(stockData: MutableList<StockData>): CandleData{
        fun getData(): MutableList<Array<Float>>{
            val data = mutableListOf<Array<Float>>()
            for(d in stockData){
                val tmpData = arrayOf<Float>(0f, 0f, 0f, 0f)
                tmpData.set(0, d.open)
                tmpData.set(1, d.high)
                tmpData.set(2, d.low)
                tmpData.set(3, d.close)
                data.add(tmpData)
            }
            return data
        }
        val entryList = mutableListOf<CandleEntry>()
        for((i, e) in getData().withIndex()){
            entryList.add(
                CandleEntry(i.toFloat(), e[1], e[2], e[0], e[3])
            )
        }

        // ローソク足チャートの属性設定
        val set = CandleDataSet(entryList, "Candle Stick")
        set.apply {
            setDrawIcons(false)
            setDrawValues(false)

            axisDependency = YAxis.AxisDependency.RIGHT
            shadowColor = ResourceApp.instance.getColor(R.color.colorPrimary)
            shadowWidth = 1.5f

            //color for open < close
            decreasingColor = ResourceApp.instance.getColor(R.color.colorCandleSkyBlue)
            decreasingPaintStyle = Paint.Style.FILL

            //color for open > close
            increasingColor = ResourceApp.instance.getColor(R.color.colorCandleRed)
            increasingPaintStyle = Paint.Style.FILL
            neutralColor = ResourceApp.instance.getColor(R.color.colorPrimary)
        }
        return CandleData(set)
    }

    // 移動平均線データ取得
    private fun getMAData(stockData: MutableList<StockData>, dayList: List<Int>): LineData {
        fun getData(day: Int): List<Float> {
            val data = mutableListOf<Float>()
            val tmpData = mutableListOf<Float>()
            for((i,d) in stockData.withIndex()){
                tmpData.add(d.close)
                if(i + 1 >= day){
                    data.add(tmpData.average().toFloat())   // 移動平均を計算して格納
                    tmpData.removeAt(0)                 // 一番古いデータを削除
                }
                else{
                    // 移動平均が計算できるまでのデータは、-1f　にしておく
                    data.add(-1f)
                }
            }
            return data
        }

        val dataSets = mutableListOf<ILineDataSet>()
        for(d in dayList){
            val entryList = mutableListOf<Entry>()
            // 移動平均データを取得
            val data = getData(d)

            // Entry 作成
            for((i,d) in data.withIndex()){
                // 0f 以上の値を有効データとする（-1fは含めない）
                if(d >= 0f){
                    entryList.add(Entry(i.toFloat(), d))
                }
            }
            val yValue =LineDataSet(entryList, "MA Data($d)").apply {
                axisDependency = YAxis.AxisDependency.RIGHT
                color = ResourceApp.instance.getColor(R.color.colorGreen)
                setDrawValues(false)     // 各値のラベルを表示しない
                setDrawCircles(false)   // 各値の点を表示しない
                lineWidth = 1.5f        // 線の太さ
            }
            dataSets.add(yValue)
        }
        return LineData(dataSets)
    }
}