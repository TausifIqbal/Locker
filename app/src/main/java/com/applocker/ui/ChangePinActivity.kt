package com.applocker.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.applocker.R
import com.applocker.data.AppLockRepository
import com.applocker.databinding.ActivityChangePinBinding
import com.applocker.security.PinSecurityManager
import com.google.android.material.snackbar.Snackbar

class ChangePinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePinBinding
    private lateinit var repository: AppLockRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePinBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        repository = AppLockRepository.getInstance(this)

        binding.updatePinButton.setOnClickListener {
            val currentPin = binding.currentPinEditText.text?.toString().orEmpty()
            val newPin = binding.newPinEditText.text?.toString().orEmpty()
            val confirmPin = binding.confirmNewPinEditText.text?.toString().orEmpty()

            when {
                !PinSecurityManager.isValidPinFormat(newPin) -> showMessage(getString(R.string.pin_format_error))
                newPin != confirmPin -> showMessage(getString(R.string.pin_mismatch_error))
                !repository.changePin(currentPin, newPin) -> showMessage(getString(R.string.current_pin_invalid))
                else -> {
                    showMessage(getString(R.string.pin_updated_successfully))
                    finish()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}
