package net.ts_matsu.kabusign.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import net.ts_matsu.kabusign.R
import net.ts_matsu.kabusign.util.CommonInfo

class ConditionItemCandle : Fragment() {
    private val cName = ConditionItemCandle::class.java.simpleName

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_condition_item_candle, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        CommonInfo.debugInfo("$cName 111")

//        arguments.takeIf { it!!.containsKey("test") }?.apply {
//            val textView : TextView = view.findViewById<TextView>(R.id.tvCandleTest)
//            textView.text = "candle ${getInt("test")}"
//        }
    }
}