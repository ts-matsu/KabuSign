package net.ts_matsu.kabusign.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.*
import net.ts_matsu.kabusign.databinding.FragmentStockSelectDialogBinding
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.viewmodel.StockSelectViewModel


/**
 * A simple [Fragment] subclass.
 * Use the [StockSelectDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StockSelectDialogFragment : DialogFragment() {
    private val cName = StockSelectDialogFragment::class.java.simpleName

    private lateinit var binding: FragmentStockSelectDialogBinding
    private val viewModel = StockSelectViewModel()

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//
//        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_stock_select_dialog, null, false)
//        binding.viewModel = StockSelectViewModel()
//
//        val builder = AlertDialog.Builder(activity)
//        builder.setView(binding.root)
//        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialogInterface, i ->  })
//
//        return builder.create()
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStockSelectDialogBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 銘柄選択されることを監視する
        binding.viewModel!!.selectedItem.observe(viewLifecycleOwner) {
            // ViewModel生成時の初期値代入チェック
            if(it != ""){
                CommonInfo.debugInfo("$cName: selectedItemObserve  $it")
                val action = StockSelectDialogFragmentDirections.actionStockSelectDialogFragmentToMonitoredStockFragment(it)
                findNavController().navigate(action)
            }
        }
        // Cancelボタンを監視する
        binding.viewModel!!.requireClose.observe(viewLifecycleOwner) {
            if(it) findNavController().popBackStack()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        CommonInfo.debugInfo("$cName: onDestroy.")
    }
}