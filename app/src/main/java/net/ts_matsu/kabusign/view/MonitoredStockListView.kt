package net.ts_matsu.kabusign.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ListView
import net.ts_matsu.kabusign.R
import net.ts_matsu.kabusign.util.MonitoredStockItem

class MonitoredStockListView: ListView {
    constructor(context: Context): super(context){}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle){}

    private var adapter: MonitoredStockAdapter? = null
    fun setList(list: MutableList<MonitoredStockItem>) {
        if (adapter == null) {
            adapter = MonitoredStockAdapter(
                context, R.layout.monitored_stock_item, list
            )
            setAdapter(adapter)
        }
        // この処理で表示反映されるらしい
        adapter!!.notifyDataSetChanged()
    }

    override fun performClick(): Boolean {
        println("22222")
        return super.performClick()
    }
}