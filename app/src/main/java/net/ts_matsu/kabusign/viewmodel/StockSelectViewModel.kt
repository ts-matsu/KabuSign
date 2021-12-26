package net.ts_matsu.kabusign.viewmodel

// ViewModel のファイルに、android.* は インポートするべきでない
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import net.ts_matsu.kabusign.util.StockInfo
import net.ts_matsu.kabusign.model.StockSelect
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.view.StockSelectDialogFragment


class StockSelectViewModel: ViewModel() {
    private val cName = StockSelectViewModel::class.java.simpleName

    // LiveData だと、リアルタイムで表示更新されなかったため（理由わからず）
    // ObservableArray を使用した
    val stockList: ObservableArrayList<String>

    private val _selectedItem = MutableLiveData<String>("")
    val selectedItem: LiveData<String> get() = _selectedItem

    val requireClose = MutableLiveData(false)   // CANCELがタップされて、ダイアログを閉じることを通知


    init {
        CommonInfo.debugInfo("StockSelectViewModel: init start")

        // 銘柄リスト取得（初期化時は全銘柄リスト）
        stockList = ObservableArrayList()
        setStockList()

        CommonInfo.debugInfo("StockSelectViewModel: init end")
    }

    // StockInfoを検索リスト表示用の文字列に変換する
    private fun convertStockInfoToString(info: StockInfo): String{
        return "${info.name} (${info.code})"
    }
    // 検索リストに表示されている文字列を、監視銘柄リストに渡す形式に変換する
    private fun convertStringToSplitString(text: String): String{
        val code = text.takeLast(5).substring(0,4)
        val name = text.substring(0, text.length-7)
        CommonInfo.debugInfo("$cName  convertStringToStockInfo(code:$code, name:$name)")
        return "$code,$name"
    }

    // 銘柄リストを設定する
    private fun setStockList() {
        // コルーチンで銘柄情報を取得
        viewModelScope.launch {
            CommonInfo.debugInfo("setStockList: start")
            stockList.clear()
            val list = StockSelect.getStockList()
            for(d in list){
                val stockInfo = convertStockInfoToString(d)
                stockList.add(stockInfo)
            }
            CommonInfo.debugInfo("setStockList: end")
        }
    }

    // CANCELボタン処理
    fun onNegativeButtonClick() {
        requireClose.value = true
    }
    // 銘柄選択イベント
    fun onItemClick(item: String){
        // setStockList() が実行中かどうかをチェックした方がいいかもだが（排他）
        // setStockList() でSuspendするのは.getStockList() だけで、
        // リスト取得した後の stockList.add() は、suspend しないため、必要ないはず
        CommonInfo.debugInfo("onItemClick: $item")
        _selectedItem.value = convertStringToSplitString(item)
    }

    // 銘柄検索
    fun onQueryTextChange(text: String): Boolean{
        CommonInfo.debugInfo("$cName: onQueryTextChange($text)")

        // 取得した検索文字で、再度リストを取得する
        if(text != ""){
            val list = StockSelect.searchStockList(text)
            stockList.clear()
            for(d in list){
                val stockInfo = convertStockInfoToString(d)
                CommonInfo.debugInfo(stockInfo)
                stockList.add(stockInfo)
            }
        }
        else{
            setStockList()
        }
        return true
    }


    override fun onCleared() {
        CommonInfo.debugInfo("onCleared.")
    }

}