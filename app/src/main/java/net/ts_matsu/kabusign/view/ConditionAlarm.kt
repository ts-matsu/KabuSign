package net.ts_matsu.kabusign.view

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import net.ts_matsu.kabusign.R
import net.ts_matsu.kabusign.databinding.ConditionAlarmFragmentBinding
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.view.library.DateSelectPickerFragment
import net.ts_matsu.kabusign.view.library.PriceInputDialogFragment
import net.ts_matsu.kabusign.viewmodel.ConditionAlarmViewModel

class ConditionAlarm() : Fragment() {
    private val cName = ConditionAlarm::class.java.simpleName

    private lateinit var binding: ConditionAlarmFragmentBinding
    private val viewModel = ConditionAlarmViewModel()
    private val args: ConditionAlarmArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ConditionAlarmFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if(args.code.isNotEmpty()) {
            viewModel.setCode(args.code)
            viewModel.getTodayData()
        }

        // 一週間指定の日付設定
        viewModel.requireOneWeekDate.observe(viewLifecycleOwner) {
            if(it) {
                val dialogFragment = DateSelectPickerFragment()
                dialogFragment.arguments = Bundle().apply {
                    putString("requestKey", "OneWeekDate")
                    putIntArray("date", intArrayOf(2020,12,31))
                }
                dialogFragment.show(childFragmentManager, "")       // 入力ダイアログ表示
                viewModel.clearOneWeekDateDialog()                      // 入力ダイアログ表示要求クリア
            }
        }
        // DateSelectPickerFragment の結果を取得する(childFragmentManagerが必要！)
        childFragmentManager.setFragmentResultListener("OneWeekDate", viewLifecycleOwner) {requestKey, bundle ->
            CommonInfo.debugInfo("$cName: setFragmentResultListener1(${bundle.getIntArray("value")})")
            val dateArray = bundle.getIntArray("value")
            dateArray?.let {
                viewModel.setOnWeekDate(dateArray[0], dateArray[1], dateArray[2])
            }
        }

        // 期間指定の開始日付設定
        viewModel.requirePeriodStartDate.observe(viewLifecycleOwner) {
            if(it) {
                val dialogFragment = DateSelectPickerFragment()
                dialogFragment.arguments = Bundle().apply {
                    putString("requestKey", "PeriodStartDate")
                    putIntArray("date", intArrayOf(2020,12,31))
                }
                dialogFragment.show(childFragmentManager, "")       // 入力ダイアログ表示
                viewModel.clearPeriodDateDialog()                       // 入力ダイアログ表示要求クリア
            }
        }
        childFragmentManager.setFragmentResultListener("PeriodStartDate", viewLifecycleOwner) {requestKey, bundle ->
            CommonInfo.debugInfo("$cName: setFragmentResultListener2(${bundle.getIntArray("value")})")
            val dateArray = bundle.getIntArray("value")
            dateArray?.let {
                viewModel.setPeriodStartDate(dateArray[0], dateArray[1], dateArray[2])
            }
        }
        // 期間指定の終了日付設定
        viewModel.requirePeriodEndDate.observe(viewLifecycleOwner) {
            if(it) {
                val dialogFragment = DateSelectPickerFragment()
                dialogFragment.arguments = Bundle().apply {
                    putString("requestKey", "PeriodEndDate")
                    putIntArray("date", intArrayOf(2020,12,31))
                }
                dialogFragment.show(childFragmentManager, "")       // 入力ダイアログ表示
                viewModel.clearPeriodDateDialog()                       // 入力ダイアログ表示要求クリア
            }
        }
        childFragmentManager.setFragmentResultListener("PeriodEndDate", viewLifecycleOwner) {requestKey, bundle ->
            CommonInfo.debugInfo("$cName: setFragmentResultListener3(${bundle.getIntArray("value")})")
            val dateArray = bundle.getIntArray("value")
            dateArray?.let {
                viewModel.setPeriodEndDate(dateArray[0], dateArray[1], dateArray[2])
            }
        }
        // 期間指定の設定不正による Toast 表示
        viewModel.requirePeriodDateToast.observe(viewLifecycleOwner) {
            if(it) {
                Toast.makeText(context, "期間指定がおかしい旨を表示する", Toast.LENGTH_SHORT).show()
                viewModel.clearPeriodToast()
            }
        }
    }


}