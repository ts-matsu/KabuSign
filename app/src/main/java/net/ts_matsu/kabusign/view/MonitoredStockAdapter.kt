package net.ts_matsu.kabusign.view

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.ts_matsu.kabusign.R
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.util.MonitoredStockItem

class MonitoredStockAdapter(context: Context, resource: Int, items: List<MonitoredStockItem>): ArrayAdapter<MonitoredStockItem>(context, resource, items) {
    private var mResource = resource
    private var mInflater: LayoutInflater? = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var mItems: List<MonitoredStockItem>? = items
    var mClickItem = arrayOf<Boolean>(false, false, false, false)

    @SuppressLint("ClickableViewAccessibility")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view: View?

        view = convertView
        if(convertView == null){
            view = mInflater?.let {
                it.inflate(mResource, null)
            }
        }

        mItems?.let {
            val item = it[position]
            item.isClickItem = 0

            val code = view!!.findViewById<TextView>(R.id.tvMonitoredCode)
            code.text = item.code
            code.setOnTouchListener { view, motionEvent ->
                item.isClickItem = 1
                false
            }
            val name = view!!.findViewById<TextView>(R.id.tvMonitoredName)
            name.text = item.name
            name.setOnTouchListener { view, motionEvent ->
                item.isClickItem = 1
                false
            }
            val info = view!!.findViewById<ImageView>(R.id.ivMonitoredInfo)
            info.setOnTouchListener { view, motionEvent ->
                item.isClickItem = 2
                false
            }
            val alarm = view!!.findViewById<ImageView>(R.id.ivMonitoredAlarm)
            alarm.setOnTouchListener { view, motionEvent ->
                item.isClickItem = 3
                false
            }
        }
        return view!!
    }

}
