package com.applocker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.applocker.databinding.FragmentAppListBinding

class AppListFragment : Fragment() {

    private var _binding: FragmentAppListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = AppListAdapter { appInfo, isLocked ->
            viewModel.toggleLock(appInfo.packageName, isLocked)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val showLocked = requireArguments().getBoolean(ARG_SHOW_LOCKED)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.appsLiveData().observe(viewLifecycleOwner) {
            val filtered = viewModel.filterApps(showLocked)
            adapter.submitList(filtered)
            binding.emptyTextView.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_SHOW_LOCKED = "show_locked"

        fun newInstance(showLocked: Boolean) = AppListFragment().apply {
            arguments = Bundle().apply { putBoolean(ARG_SHOW_LOCKED, showLocked) }
        }
    }
}
