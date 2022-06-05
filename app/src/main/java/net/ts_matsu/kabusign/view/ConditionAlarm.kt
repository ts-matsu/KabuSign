package net.ts_matsu.kabusign.view

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import net.ts_matsu.kabusign.R
import net.ts_matsu.kabusign.databinding.ConditionAlarmFragmentBinding
import net.ts_matsu.kabusign.viewmodel.ConditionAlarmViewModel

class ConditionAlarm() : Fragment() {

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
    }
}