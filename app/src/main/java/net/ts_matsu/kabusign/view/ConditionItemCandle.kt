package net.ts_matsu.kabusign.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import net.ts_matsu.kabusign.R
import net.ts_matsu.kabusign.databinding.FragmentConditionItemCandleBinding
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.viewmodel.ConditionItemCandleViewModel

class ConditionItemCandle(code: String) : Fragment() {
    private val cName = ConditionItemCandle::class.java.simpleName

    private lateinit var binding: FragmentConditionItemCandleBinding
    private val viewModel = ConditionItemCandleViewModel(code)
    private lateinit var shapeSpinner: Spinner
    private lateinit var patternSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConditionItemCandleBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        CommonInfo.debugInfo("$cName 111")
        // ローソク足形状のスピナーを設定する
        shapeSpinner = binding.root.findViewById<Spinner>(R.id.spCandleShape)
        val shapeAdapter = ArrayAdapter(requireContext(), R.layout.custom_spinner, resources.getStringArray(R.array.candle_shape_info))
        shapeAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown)
        shapeSpinner.adapter = shapeAdapter
//        shapeSpinner.setSelection(viewModel.kindSpinnerPosition.value!!)

        // ローソク足パターンのスピナーを設定する
        patternSpinner = binding.root.findViewById<Spinner>(R.id.spCandlePattern)
        val patternAdapter = ArrayAdapter(requireContext(), R.layout.custom_spinner, resources.getStringArray(R.array.candle_pattern_info))
        patternAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown)
        patternSpinner.adapter = patternAdapter
//        patternSpinner.setSelection(viewModel.kindSpinnerPosition.value!!)

        // 表示色を変更するため、ローソク足形状のスピナーを切り替える
        viewModel.isCandleShapeMode.observe(viewLifecycleOwner) {
            var adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner, resources.getStringArray(R.array.candle_shape_info))
            if(!it) {
                adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_off, resources.getStringArray(R.array.candle_shape_info))
            }
            adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown)
            shapeSpinner.adapter = adapter
//            shapeSpinner.setSelection(viewModel.kindSpinnerPosition.value!!)
        }
        viewModel.enable.observe(viewLifecycleOwner) {
            var adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_off, resources.getStringArray(R.array.candle_shape_info))
            if(it && viewModel.isCandleShapeMode.value!!) {
                adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner, resources.getStringArray(R.array.candle_shape_info))
            }
            adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown)
            shapeSpinner.adapter = adapter
//            shapeSpinner.setSelection(viewModel.kindSpinnerPosition.value!!)
        }

        // 表示色を変更するため、ローソク足パターンのスピナーを切り替える
        viewModel.isCandleShapeMode.observe(viewLifecycleOwner) {
            var adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner, resources.getStringArray(R.array.candle_pattern_info))
            if(it) {
                adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_off, resources.getStringArray(R.array.candle_pattern_info))
            }
            adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown)
            patternSpinner.adapter = adapter
//            shapeSpinner.setSelection(viewModel.kindSpinnerPosition.value!!)
        }
        viewModel.enable.observe(viewLifecycleOwner) {
            var adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_off, resources.getStringArray(R.array.candle_pattern_info))
            if(it && !viewModel.isCandleShapeMode.value!!) {
                adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner, resources.getStringArray(R.array.candle_pattern_info))
            }
            adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown)
            patternSpinner.adapter = adapter
//            shapeSpinner.setSelection(viewModel.kindSpinnerPosition.value!!)
        }
    }
}