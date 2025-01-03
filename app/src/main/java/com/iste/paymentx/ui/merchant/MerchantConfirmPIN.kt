package com.iste.paymentx.ui.merchant

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.iste.paymentx.R
import com.iste.paymentx.ui.auth.CreateTransPIN
import com.iste.paymentx.ui.main.MainScreen

class MerchantConfirmPIN : AppCompatActivity() {
    private lateinit var backarrow: ImageView
    private lateinit var vibrator: Vibrator

    // store credientials
    private var userName: String? = null
    private var userEmail: String? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_merchant_confirm_pin)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Get the user information from intent
        userName = intent.getStringExtra("USER_NAME")
        userEmail = intent.getStringExtra("USER_EMAIL")
        userId = intent.getStringExtra("USER_ID")

        // Initialize vibrator
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Retrieve the PIN passed from CreateTransPIN
        val transactionPin = intent.getStringExtra("transactionPin")
        backarrow = findViewById(R.id.merchback)

        backarrow.setOnClickListener {
            val intent = Intent(this, MerchantCreateTransPIN::class.java)
            startActivity(intent)
        }

        if (transactionPin == null) {
            Toast.makeText(this, "Transaction PIN not received!", Toast.LENGTH_SHORT).show()
            vibratePhone()
            finish() // Exit if no PIN is passed
            return
        }

        val pinInputs = listOf(
            findViewById<EditText>(R.id.merchinput1),
            findViewById<EditText>(R.id.merchinput2),
            findViewById<EditText>(R.id.merchinput3),
            findViewById<EditText>(R.id.merchinput4),
            findViewById<EditText>(R.id.merchinput5),
            findViewById<EditText>(R.id.merchinput6)
        )
        pinInputs.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1) {
                        // Move to the next EditText
                        if (index < pinInputs.size - 1) {
                            pinInputs[index + 1].requestFocus()
                        }
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    // Clear current EditText and move to the previous one
                    if (editText.text.isEmpty() && index > 0) {
                        pinInputs[index - 1].requestFocus()
                        pinInputs[index - 1].text.clear()
                    }
                }
                false
            }
        }

        val btnVerify = findViewById<Button>(R.id.merchbtnConfirm)
        btnVerify.setOnClickListener {
            // Gather input PIN
            val enteredPin = pinInputs.joinToString("") { it.text.toString() }

            if (enteredPin.length == 6) {
                if (enteredPin == transactionPin) {
                    val intent = Intent(this, MerchantAccountInfo::class.java).apply {
                        putExtra("USER_NAME", userName)
                        putExtra("USER_EMAIL", userEmail)
                        putExtra("USER_ID", userId)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    // Show error if PINs do not match
                    Toast.makeText(this, "PINs do not match. Try again.", Toast.LENGTH_SHORT).show()
                    vibratePhone()
                }
            } else {
                // Show error if PIN is incomplete
                Toast.makeText(this, "Please enter all 6 digits.", Toast.LENGTH_SHORT).show()
                vibratePhone()
            }
        }
        val sharedPref = getSharedPreferences("PaymentX", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("transactionPin", transactionPin)  // Save PIN
        editor.apply()
    }
    private fun vibratePhone() {
        if (vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(200)
            }
        }
    }
}