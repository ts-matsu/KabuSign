package net.ts_matsu.kabusign.model

import androidx.core.text.isDigitsOnly
import com.google.gson.GsonBuilder
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.view.MainFragment
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*
import java.time.LocalDate


// 時系列データのダウンロード、データ保持用オブジェクト
// 現状(2022/1/23)は、kabutan からダウンロードしているが、
// 当日データについては、ゆくゆくは、ログインしているサイト（楽天証券やSBI）から
// 取得できるようにする
class StockDownload {
    private val maxPageForKabutan = 10
    private val cName = MainFragment::class.java.simpleName

    // これをしないと、「Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 1 path $」が出る
    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://kabutan.jp/stock/")                // Kabutan からダウンロードする
        .addConverterFactory(ScalarsConverterFactory.create())      // これがないと、HTMLデータが処理できない
        .addConverterFactory(GsonConverterFactory.create(gson))     // JSON使うときはこれ、らしい
        .build()
    private val service = retrofit.create(DownloadService::class.java)
    interface  DownloadService {
        @GET
        suspend fun getStockData(@Url url: String): Response<String>
    }

    // 時系列データを取得する
    // lastDate: YYYYMMDD
    //   空""でない場合は、lastDate以降のデータのみを返す
    suspend fun get(code: String, lastDate: String): Pair<MutableList<StockData>, StockTodayData> {
        val stockData = mutableListOf<StockData>()
        var stockTodayData = StockTodayData("", "", 0f, 0f, 0f, 0f, 0, 0f,0f)
        for(i in 1..maxPageForKabutan){
            val url = "kabuka?code=${code}&ashi=day&page=${i}"
            val response = service.getStockData(url)
            CommonInfo.debugInfo("$cName: $url")
            if(response.isSuccessful){
                parseData(response.body(), stockData)
                if(i == 1){
                    CommonInfo.debugInfo("先頭：${stockData[0].date}")
                    // 先頭データの場合は、本日データを取得する
                    val todayData = parseTodayData(response.body())
                    val todayDataSub = parseTodayDataSub(response.body())
                    if(todayData != null){
                        if(stockData[0].date == todayData.date){
                            stockData[0] = todayData
                        }
                        else{
                            stockData.add(0, todayData)
                        }
                        stockTodayData = createTodayData(todayData, todayDataSub.first, todayDataSub.second)
                    }
                }
            }
            if(lastDate.isNotEmpty()){
                // lastDate 以降のデータが取得できた場合は、そこで終了
                if(lastDate.toInt() >= stockData.last().date.toInt()) break
            }
        }
        // lastDate が指定されている場合は、lastDate以降のデータを返す
        if(lastDate.isNotEmpty()){
            var removeIndex = 0
            for((i,d) in stockData.withIndex()){
                if(lastDate.toInt() >= d.date.toInt()){
                    removeIndex = i + 1
                    break
                }
            }
            val removeNum = stockData.size - removeIndex
            for(i in 0 until removeNum) {
                stockData.removeAt(removeIndex)
            }
        }
        return Pair(stockData, stockTodayData)
    }

    // Kabutanから取得したHTLMをパースして、StockDataクラスに格納する
    private fun parseData(body: String?, savingList: MutableList<StockData>) {
        body?.let {
            val doc = Jsoup.parse(body)
            val table = doc.getElementsByClass("stock_kabuka_dwm")
            val rows = table.select("tr")
            for(row in rows) {
                var sDate = ""
                var sOpen:Float? = 0f
                var sHigh:Float? = 0f
                var sLow:Float? = 0f
                var sClose:Float? = 0f
                var sVolume:Int? = 0

                val tmp = row.select("th").text().split("/")
                if (tmp.size == 3) {
                    sDate = "20${tmp[0]}${tmp[1]}${tmp[2]}"

                    val dateInfo = row.select("td")
                    for ((i, info) in dateInfo.withIndex()) {
                        when (i) {
                            0 -> sOpen = info.text().replace(",", "").toFloatOrNull()
                            1 -> sHigh = info.text().replace(",", "").toFloatOrNull()
                            2 -> sLow = info.text().replace(",", "").toFloatOrNull()
                            3 -> sClose = info.text().replace(",", "").toFloatOrNull()
                            6 -> sVolume = info.text().replace(",", "").toIntOrNull()
                        }
                    }
                    // 有効なデータの場合のみ格納
                    // 例えば、2020/10/1 は東証システムダウンで無効データになっているので、格納しない
                    if (sOpen != null && sHigh != null && sLow != null && sClose != null && sVolume != null) {
                        savingList.add(StockData(sDate, sOpen.toFloat(), sHigh.toFloat(), sLow.toFloat(), sClose.toFloat(), sVolume.toInt()))
                    }
                }
            }
        }
    }

    // 本日データの取得
    private fun parseTodayData(body: String?): StockData? {
        var result: StockData? = null
        val doc = Jsoup.parse(body)
        val table = doc.getElementsByClass("stock_kabuka0")
        val rows = table.select("tr")
        val date = rows.select("th").select("time").text()
        val valueList = rows.select("td")

        var sOpen:Float? = 0f
        var sHigh:Float? = 0f
        var sLow:Float? = 0f
        var sClose:Float? = 0f
        var sVolume:Int? = 0
        for((i,d) in valueList.withIndex()){
            when(i){
                0 -> sOpen = d.text().replace(",", "").toFloatOrNull()
                1 -> sHigh = d.text().replace(",", "").toFloatOrNull()
                2 -> sLow = d.text().replace(",", "").toFloatOrNull()
                3 -> sClose = d.text().replace(",", "").toFloatOrNull()
                6 -> sVolume = d.text().replace(",", "").toIntOrNull()
            }
        }
        if (sOpen != null && sHigh != null && sLow != null && sClose != null && sVolume != null) {
            val sDate = "20${date.replace("/","")}"
            result = StockData(sDate, sOpen.toFloat(), sHigh.toFloat(), sLow.toFloat(), sClose.toFloat(), sVolume.toInt())
            CommonInfo.debugInfo("$cName: parseTodayData $date, $sOpen, $sHigh, $sLow, $sClose, $sVolume")
        }
        return result
    }

    private fun parseTodayDataSub(body: String?): Pair<String, ArrayList<Float>> {
        val doc = Jsoup.parse(body)
        val infoId = doc.getElementById("stockinfo")
        val dateData = infoId.getElementsByClass("si_i1_1")
        val ratioData = infoId.getElementsByClass("si_i1_dl1")
        var resultTime = ""
        var resultRatio = arrayListOf<Float>(0f, 0f)
        CommonInfo.debugInfo("$cName parseTodayData2")
        dateData?.let {
            resultTime = it.select("time").text()
        }
        ratioData?.let {

            for((i,ratio) in ratioData.select("dd").withIndex()){
                if(i < resultRatio.size){
                    resultRatio[i] = ratio.text().replace("%", "").toFloat()
                }
            }
        }
        return Pair(resultTime, resultRatio)
    }

    private fun createTodayData(stockData: StockData, time: String, ratio: ArrayList<Float>): StockTodayData {
        return  StockTodayData(
            stockData.date, time, stockData.open, stockData.high, stockData.low, stockData.close, stockData.volume,
            ratio[0], ratio[1]
        )
    }
}

