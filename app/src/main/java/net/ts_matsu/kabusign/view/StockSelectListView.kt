package net.ts_matsu.kabusign.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.SearchView

// 銘柄選択のDialogFragmentに表示するリストビュー
// DataBinding に対応するため、カスタムビューにしている
class StockSelectListView: ListView {
    constructor(context: Context): super(context){}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle){}

    private var adapter: ArrayAdapter<String>? = null
    fun setList(list: MutableList<String>) {
        if (adapter == null) {
            adapter = ArrayAdapter(
                context, android.R.layout.simple_list_item_1, list
            )
            setAdapter(adapter)
        }
        // この処理で表示反映されるらしい
        adapter!!.notifyDataSetChanged()
    }
}
