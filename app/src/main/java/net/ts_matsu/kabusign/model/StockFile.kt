package net.ts_matsu.kabusign.model

import net.ts_matsu.kabusign.R
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.util.ResourceApp
import net.ts_matsu.kabusign.util.StockInfo
import net.ts_matsu.kabusign.view.MainFragment
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException
import java.io.FilenameFilter
import java.lang.StringBuilder

class StockFile {
    private val cName = MainFragment::class.java.simpleName

    companion object{
        // stock_list.json から読み出した銘柄リスト
        private val stockInfo = mutableListOf<StockInfo>()
    }

    // 時系列データを保存する
    suspend fun saveStockData(code: String, data: MutableList<StockData>) {
        val path = "${ResourceApp.instance.filesDir.path}/${code}.csv"
        val saveFile = File(path)

        saveFile.bufferedWriter().use {
            for(d in data){
                // 行フォーマット："YYYYMMDD","始値","高値","安値","終値","出来高"
                val tmp = "\"${d.date}\",\"${d.open}\",\"${d.high}\",\"${d.low}\",\"${d.close}\",\"${d.volume}\"\n"
                it.write(tmp)
            }
        }
    }

    // 時系列データを読み出す
    suspend fun loadStockData(code: String): MutableList<StockData> {
        val path = "${ResourceApp.instance.filesDir.path}/${code}.csv"
        val readFile = File(path)
        val result: MutableList<StockData> = mutableListOf()
        try {
            // 行フォーマット："YYYYMMDD","始値","高値","安値","終値","出来高"
            val regex = Regex("^\"")                            // 株価データのある行（先頭が"）を抽出する
            val regexSd = Regex("\"[0-9.]+\"")                  // 各要素抽出
            val regexDate = Regex("\"[0-9]+-[0-9]+-[0-9]+\"")   // 日付データ抽出用
            for(line in readFile.readLines()){
                // 先頭が"で、抽出した要素が6つ(日付、始値、高値、安値、終値、出来高)の行のみが対象
                if(regex.containsMatchIn(line)){
                    val data = regexSd.findAll(line)
                    if(data.count() == 6){
                        result.add(StockData(
                            data.elementAt(0).value.replace("\"", ""),              // 日付
                            data.elementAt(1).value.replace("\"", "").toFloat(),    // 始値
                            data.elementAt(2).value.replace("\"", "").toFloat(),    // 高値
                            data.elementAt(3).value.replace("\"", "").toFloat(),    // 安値
                            data.elementAt(4).value.replace("\"", "").toFloat(),    // 終値
                            data.elementAt(5).value.replace("\"", "").toInt()       // 出来高
                        ))
                    }
                    else{
                        CommonInfo.warnInfo("$cName: loadStockData count not 6.")
                    }
                }
                else{
                    CommonInfo.warnInfo("$cName: loadStockData invalid line.")
                }
            }
        }
        catch (e: FileNotFoundException){
            CommonInfo.warnInfo("$cName: loadStockData FileNotFound(${path}).")
        }
        catch (e: Exception) {
            CommonInfo.warnInfo("$cName: loadStockData ${e.message}(${path}).")
        }
        return result
    }

    // 時系列データのある銘柄リスト取得
    fun getDownloadedStockList(): List<StockInfo> {
        val result = mutableListOf<StockInfo>()
        val fileList = File(ResourceApp.instance.filesDir.path).list(FilenameFilter { file, s -> s.endsWith(".csv") })
        val info = getSupportedStockList()

        // ファイルリストから、銘柄コード抽出し、銘柄名取得後、格納する
        for(d in fileList){
            val code = d.replace(".csv", "")
            for(d2 in info) {
                if(code == d2.code){
                    result.add(d2)
                }
            }
        }
        return result
    }

    // サポートしている銘柄リスト取得
    fun getSupportedStockList(): List<StockInfo>{
        // 空の場合のみ、stock_list.json から銘柄リストを読み出す
        // アプリ起動後、変更されることはないため、一度のみの読み出しで問題なし
        if(stockInfo.isEmpty()){
            val file = ResourceApp.instance.resources.openRawResource(R.raw.stock_list)
            val sb = StringBuilder()
            for(d in file.bufferedReader().readLines()){
                sb.append(d)
            }
            file.close()

            // JSON ファイルを解析して、StockInfo()に格納する
            val rootJSON = JSONObject(sb.toString())
            val stockList = rootJSON.getJSONArray("stockList")
            for(i in 0 until stockList.length()){
                val id = stockList.getJSONObject(i).getString("id")
                val name = stockList.getJSONObject(i).getString("name")
                val info = StockInfo(id, name)
                stockInfo.add(info)
            }
        }
        return stockInfo
    }
}