package net.ts_matsu.kabusign.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import net.ts_matsu.kabusign.databinding.FragmentStockValuePickerDialogBinding
import net.ts_matsu.kabusign.viewmodel.StockValuePickerDialogViewModel

class StockValuePickerDialogFragment : DialogFragment() {
    private val cName = StockValuePickerDialogFragment::class.java.simpleName

    private lateinit var binding: FragmentStockValuePickerDialogBinding
    private val viewModel = StockValuePickerDialogViewModel()
    private val args: StockValuePickerDialogFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStockValuePickerDialogBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Max/Min は、xml で設定できなさそうなので、ここで設定しておく
        binding.npValue1.apply { maxValue = 9; minValue = 0 }
        binding.npValue2.apply { maxValue = 9; minValue = 0 }
        binding.npValue3.apply { maxValue = 9; minValue = 0 }
        binding.npValue4.apply { maxValue = 9; minValue = 0 }
        binding.npValue5.apply { maxValue = 9; minValue = 0 }

        if(args.defaultValue.isNotEmpty()){
            // CandleInfoDialogFragmentから渡されたデフォルト値を設定する
            viewModel.setDefaultValue(args.defaultValue)
        }

        // OK、CANCELボタンを監視する
        binding.viewModel!!.requireCancel.observe(viewLifecycleOwner) {
            if(it) {
                dismiss()
            }
        }
        binding.viewModel!!.requireOk.observe(viewLifecycleOwner) {
            if(it) {
                dismiss()

                // CandleInfoDialogFragment に、設定された株価を渡す
                val value = binding.viewModel!!.stockValue
                setFragmentResult("stockValue", bundleOf("value" to value))
            }
        }
    }
}