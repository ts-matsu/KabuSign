package net.ts_matsu.kabusign.viewmodel

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.*
import com.github.mikephil.charting.data.CombinedData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.ts_matsu.kabusign.model.StockFile
import net.ts_matsu.kabusign.model.data.db.entity.ConditionKindEntity
import net.ts_matsu.kabusign.model.data.db.entity.MonitoredStockEntity
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.util.MonitoredStockItem
import net.ts_matsu.kabusign.util.ResourceApp
import net.ts_matsu.kabusign.util.StockInfo

class MonitoredStockViewModel: ViewModel() {
    private val cName = MonitoredStockViewModel::class.java.simpleName

    val monitoredStockList: ObservableArrayList<MonitoredStockItem> = ObservableArrayList()
    val requireClose = MutableLiveData(false)   // CANCELがタップされて、ダイアログを閉じることを通知
    val selectedCode = MutableLiveData("")      // 選択された銘柄コード
    var selectingCode = ""                           // 選択中の銘柄コード

    val conditionKindList = mutableListOf<String>()
    val selectedConditionKind = MutableLiveData(false)

    private val stockList: List<StockInfo>

    init {
        // 銘柄リスト読み込み
        val stockFile = StockFile()
        stockList = stockFile.getSupportedStockList()

        // 監視銘柄リストを設定する
        setMonitoredStockList()
    }

    // CANCELボタン処理
    fun onNegativeButtonClick() {
        requireClose.value = true
    }

    // 監視銘柄リストをデータベースがら読み出して設定する
    private fun setMonitoredStockList(){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                monitoredStockList.clear()

                val dao = ResourceApp.database.monitoredStockDao()
                val entities = dao.getAll()
                entities?.let {
                    for(e in entities){
                        for(d in stockList){
                            if(e.code == d.code){
                                // 銘柄の名前は、サポート銘柄リストから取得したものを使用する。
                                val tmp = MonitoredStockItem(e.code, d.name, false, false, 0)
                                monitoredStockList.add(tmp)
                            }
                        }
                    }
                }
                for(d in monitoredStockList){
                    CommonInfo.debugInfo("$cName  setMonitoredStockList(${d.code}, ${d.name})")
                }
            }
        }
    }

    // 監視銘柄リストに登録する
    fun registerMonitoredStock(item: String){
        val splitItem = item.split(",")
        if(splitItem.size >= 2){
            viewModelScope.launch {
                withContext(Dispatchers.IO){
                    val dao = ResourceApp.database.monitoredStockDao()
                    val entity = MonitoredStockEntity(splitItem[0], "", 0, false, false, false)
                    dao.insert(entity)

                    // 再度読み出して、テンポラリに設定
                    monitoredStockList.clear()
                    val entities = dao.getAll()
                    entities?.let {
                        for(e in entities){
                            for(d in stockList){
                                if(e.code == d.code){
                                    // 銘柄の名前は、サポート銘柄リストから取得したものを使用する。
                                    val tmp = MonitoredStockItem(e.code, d.name, false, false, 0)
                                    monitoredStockList.add(tmp)
                                }
                            }
                        }
                    }
                    for(d in monitoredStockList){
                        CommonInfo.debugInfo("$cName  registerMonitoredStock(${d.code}, ${d.name})")
                    }
                }
            }
        }
    }

    // 銘柄選択イベント
    fun onItemClick2(pos: Int) {
        val item = monitoredStockList[pos]
        selectingCode = item.code

        when(item.isClickItem){
            1 -> {
                // 銘柄コード、銘柄名がタップされた場合は、メイン画面（グラフ表示）へ
                selectedCode.value = "${item.name} (${item.code})"
            }
            2 -> {
                // 条件種別アイコンをタップされた場合は、ダイアログを出して選択させる
                viewModelScope.launch {
                    setConditionKindList()
                    selectedConditionKind.value = true
                }
            }
            3 -> {
                println("3333333333333333")
            }
            4 -> {
                println("4444444444444444")
            }
        }
        // 空白をタップすると、前回のダイアログが表示されているためisClickItemを初期化するため再設定
        monitoredStockList[pos].isClickItem = 0
    }

    // 条件種別をDBから取得する
    private suspend fun setConditionKindList() {
        withContext(Dispatchers.IO){
            val dao = ResourceApp.database.conditionKindDao()
            val items = dao.getAll()
            items?.let {
                conditionKindList.clear()
                for(item in items){
                    if(item.enable){
                        conditionKindList.add(item.name)
                    }
                }
            }
        }
    }
}