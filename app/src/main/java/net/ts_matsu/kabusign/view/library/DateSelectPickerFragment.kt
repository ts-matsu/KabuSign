package net.ts_matsu.kabusign.view.library

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.view.ConditionAlarmArgs
import net.ts_matsu.kabusign.view.MainFragment
import java.util.*

class DateSelectPickerFragment: DialogFragment(), DatePickerDialog.OnDateSetListener {
    private val cName = DateSelectPickerFragment::class.java.simpleName
    private var requestKey = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var year = 2020
        var month = 12
        var day = 31
        arguments?.getString("requestKey")?.let {
            requestKey = it
        }
        arguments?.getIntArray("date")?.let {
            CommonInfo.debugInfo("$cName: date(${it})")
            if(it.isNotEmpty()){
                year = it[0]
            }
            if(it.size >= 2){
                // 月は、システム的には 0 ～ 11 なので、-1 する
                month = it[1] - 1
            }
            if(it.size >= 3){
                day = it[2]
            }
        }
        return DatePickerDialog(context as Context, this, year, month, day)
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        CommonInfo.debugInfo("$cName $p1, $p2, $p3")
        setFragmentResult(requestKey, bundleOf("value" to intArrayOf(p1, p2 + 1, p3)))
    }
}

