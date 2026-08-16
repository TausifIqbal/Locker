package com.applocker.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.applocker.data.AppInfo
import com.applocker.databinding.ItemAppBinding

class AppListAdapter(
    private val onToggleChanged: (AppInfo, Boolean) -> Unit
) : ListAdapter<AppInfo, AppListAdapter.AppViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AppViewHolder(
        private val binding: ItemAppBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AppInfo) = with(binding) {
            iconImageView.setImageDrawable(item.icon)
            nameTextView.text = item.appName
            packageTextView.text = item.packageName
            lockSwitch.setOnCheckedChangeListener(null)
            lockSwitch.isChecked = item.isLocked
            lockSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked != item.isLocked) {
                    onToggleChanged(item, isChecked)
                }
            }
            root.setOnClickListener { lockSwitch.performClick() }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem == newItem
        }
    }
}
