package com.applocker.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.applocker.R
import com.applocker.data.AppLockRepository
import com.applocker.databinding.ActivityPinSetupBinding
import com.applocker.security.PinSecurityManager
import com.google.android.material.snackbar.Snackbar

class PinSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPinSetupBinding
    private lateinit var repository: AppLockRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = AppLockRepository.getInstance(this)

        binding.savePinButton.setOnClickListener {
            val pin = binding.pinEditText.text?.toString().orEmpty()
            val confirmPin = binding.confirmPinEditText.text?.toString().orEmpty()

            when {
                !PinSecurityManager.isValidPinFormat(pin) -> showMessage(getString(R.string.pin_format_error))
                pin != confirmPin -> showMessage(getString(R.string.pin_mismatch_error))
                else -> {
                    repository.setPin(pin)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}
