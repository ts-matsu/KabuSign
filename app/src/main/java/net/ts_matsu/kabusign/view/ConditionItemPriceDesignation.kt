package net.ts_matsu.kabusign.view

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import net.ts_matsu.kabusign.R
import net.ts_matsu.kabusign.databinding.FragmentConditionItemPriceDesignationBinding
import net.ts_matsu.kabusign.databinding.FragmentPriceInputDialogBinding
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.util.ResourceApp
import net.ts_matsu.kabusign.view.library.PriceInputDialogFragment
import net.ts_matsu.kabusign.viewmodel.ConditionItemPriceDesignationViewModel
import net.ts_matsu.kabusign.viewmodel.library.PriceInputViewModel

class ConditionItemPriceDesignation(number: Int, code: String) : Fragment() {
    private val cName = ConditionItemPriceDesignation::class.java.simpleName

    private lateinit var binding: FragmentConditionItemPriceDesignationBinding
    private val viewModel = ConditionItemPriceDesignationViewModel(number, code)
    private lateinit var spinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConditionItemPriceDesignationBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        CommonInfo.debugInfo("$cName 111  ${viewModel.kindSpinnerPosition.value!!}")

        // 基準値設定のスピナーを設定する
        spinner = binding.root.findViewById<Spinner>(R.id.spReference)
        val adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner, resources.getStringArray(R.array.reference_kind))
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown)
        spinner.adapter = adapter
        spinner.setSelection(viewModel.kindSpinnerPosition.value!!)

        // 表示色を変更するため、スピナーを切り替える
        viewModel!!.isPriceDesignationMode.observe(viewLifecycleOwner) {
            var adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner, resources.getStringArray(R.array.reference_kind))
            if(it) {
                adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_off, resources.getStringArray(R.array.reference_kind))
            }
            adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown)
            spinner.adapter = adapter
            spinner.setSelection(viewModel.kindSpinnerPosition.value!!)
        }
        viewModel!!.enable.observe(viewLifecycleOwner) {
            var adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_off, resources.getStringArray(R.array.reference_kind))
            if(it && !viewModel.isPriceDesignationMode.value!!) {
                adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner, resources.getStringArray(R.array.reference_kind))
            }
            adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown)
            spinner.adapter = adapter
            spinner.setSelection(viewModel.kindSpinnerPosition.value!!)
        }

        // 価格指定のダイアログ処理
        viewModel!!.requireInputDialog.observe(viewLifecycleOwner) {
            if (it) {
                // 価格入力ダイアログに渡す値を設定
                var isDotInput = false
                var isMinusInput = false
                var defaultValue = viewModel.getPrice()
                var requestKey = "DesignationPrice"
                if(viewModel.designationMode == ConditionItemPriceDesignationViewModel.DesignationMode.REFERENCE_DESIGNATION) {
                    when (viewModel.inputDialogMode){
                        ConditionItemPriceDesignationViewModel.InputDialogMode.PRICE_INPUT -> {
                            isMinusInput = true
                            defaultValue = viewModel.getReferencePrice()
                            requestKey = "TargetPrice"
                        }
                        ConditionItemPriceDesignationViewModel.InputDialogMode.RATIO_INPUT -> {
                            isDotInput = true
                            isMinusInput = true
                            defaultValue = viewModel.getReferenceRatio()
                            requestKey = "TargetRatio"
                        }
                    }
                }
                val dialogFragment = PriceInputDialogFragment(isDotInput, isMinusInput)
                dialogFragment.arguments = Bundle().apply {
                    putString("requestKey", requestKey)
                    putString("value", defaultValue)
                }
                dialogFragment.show(childFragmentManager, "")       // 入力ダイアログ表示
                viewModel.clearInputDialog()                            // 入力ダイアログ表示要求クリア
            }
        }
        // PriceInputDialog の結果を取得する(childFragmentManagerが必要！)
        childFragmentManager.setFragmentResultListener("DesignationPrice", viewLifecycleOwner) {requestKey, bundle ->
            CommonInfo.debugInfo("$cName: setFragmentResultListener(${bundle.getString("value")})")
            viewModel.setPrice(bundle.getString("value"))
        }
        // 基準値設定時の目標差分価格取得
        childFragmentManager.setFragmentResultListener("TargetPrice", viewLifecycleOwner) {requestKey, bundle ->
            CommonInfo.debugInfo("$cName: setFragmentResultListener2(${bundle.getString("value")})")
            viewModel.setReferencePrice(bundle.getString("value"))
        }
        // 基準値設定時の目標差分比率取得
        childFragmentManager.setFragmentResultListener("TargetRatio", viewLifecycleOwner) {requestKey, bundle ->
            CommonInfo.debugInfo("$cName: setFragmentResultListener3(${bundle.getString("value")})")
            viewModel.setReferenceRatio(bundle.getString("value"))
        }
    }
}