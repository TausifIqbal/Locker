package com.applocker.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.applocker.R
import com.applocker.data.AppLockRepository
import com.applocker.databinding.ActivityLockScreenBinding
import com.applocker.security.PinSecurityManager
import com.applocker.utils.LockSessionManager
import com.google.android.material.snackbar.Snackbar

class LockScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockScreenBinding
    private lateinit var repository: AppLockRepository
    private val targetPackage: String by lazy {
        intent.getStringExtra(EXTRA_TARGET_PACKAGE).orEmpty()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repository = AppLockRepository.getInstance(this)

        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                moveTaskToBack(true)
            }
        })

        binding.lockedAppTextView.text = getString(R.string.locked_app_message, targetPackage)
        binding.unlockButton.setOnClickListener {
            val pin = binding.pinEditText.text?.toString().orEmpty()
            when {
                !PinSecurityManager.isValidPinFormat(pin) -> showError(getString(R.string.pin_format_error))
                repository.verifyPin(pin) -> {
                    LockSessionManager.markAppUnlocked(targetPackage)
                    finish()
                }
                else -> showError(getString(R.string.incorrect_pin))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LockSessionManager.isLockScreenVisible = true
    }

    override fun onDestroy() {
        LockSessionManager.isLockScreenVisible = false
        super.onDestroy()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_TARGET_PACKAGE = "target_package"
    }
}
