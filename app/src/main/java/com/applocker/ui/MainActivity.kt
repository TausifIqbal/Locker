package com.applocker.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.applocker.R
import com.applocker.databinding.ActivityMainBinding
import com.applocker.service.AppLockAccessibilityService
import com.applocker.data.AppLockRepository
import com.applocker.utils.AppUtils
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var repository: AppLockRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        repository = AppLockRepository.getInstance(this)
        if (!repository.hasPin()) {
            startActivity(Intent(this, PinSetupActivity::class.java))
            finish()
            return
        }

        setupViewPager()
        setupSearch()
        setupPermissionCards()
        viewModel.loadApps()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionState()
        viewModel.loadApps()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_change_pin -> {
                startActivity(Intent(this, ChangePinActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = AppListPagerAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) getString(R.string.locked_apps_tab) else getString(R.string.unlocked_apps_tab)
        }.attach()
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(editable: Editable?) {
                viewModel.setQuery(editable?.toString().orEmpty())
            }
        })
    }

    private fun setupPermissionCards() {
        binding.enableAccessibilityButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        binding.pinInfoTextView.setOnClickListener {
            startActivity(Intent(this, ChangePinActivity::class.java))
        }
    }

    private fun updatePermissionState() {
        val enabled = AppUtils.isAccessibilityServiceEnabled(
            this,
            AppLockAccessibilityService.serviceId(packageName)
        )
        binding.accessibilityStatusTextView.text = if (enabled) {
            getString(R.string.accessibility_enabled)
        } else {
            getString(R.string.accessibility_disabled)
        }
        binding.enableAccessibilityButton.text = if (enabled) {
            getString(R.string.manage_permission)
        } else {
            getString(R.string.enable_accessibility)
        }
    }
}
