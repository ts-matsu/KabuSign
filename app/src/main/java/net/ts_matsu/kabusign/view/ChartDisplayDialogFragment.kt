package net.ts_matsu.kabusign.view

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.ts_matsu.kabusign.R
import net.ts_matsu.kabusign.viewmodel.ChartDisplayDialogViewModel
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import net.ts_matsu.kabusign.databinding.ChartDisplayDialogFragmentBinding
import net.ts_matsu.kabusign.databinding.FragmentStockNotificationDialogBinding

class ChartDisplayDialogFragment : DialogFragment() {
    private val viewModel = ChartDisplayDialogViewModel()
    private lateinit var binding: ChartDisplayDialogFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ChartDisplayDialogFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    }
}