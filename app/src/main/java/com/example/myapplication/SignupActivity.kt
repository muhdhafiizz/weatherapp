package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.databinding.ActivityLoginBinding
import com.example.myapplication.databinding.ActivitySignupBinding


class SignupActivity : AppCompatActivity() {
    private lateinit var userViewModel: UserViewModel
    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        window.statusBarColor = ContextCompat.getColor(this, R.color.accent_color)


        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        binding.ageEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                if (charSequence?.length ?: 0 > 2) {
                    binding.ageEditText.setText(charSequence?.substring(0, 2))
                    binding.ageEditText.setSelection(2)
                }
            }

            override fun afterTextChanged(editable: Editable?) {}
        })

        binding.signupButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val age = binding.ageEditText.text.toString()
            val postcode = binding.postcodeEditText.text.toString()
            val city = binding.cityEditText.text.toString()

            // Check if the fields are empty
            if (username.isEmpty() || password.isEmpty() || age.isEmpty() || postcode.isEmpty() || city.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Validate age (only two digits allowed)
                if (age.matches(Regex("^[0-9]{2}$"))) {
                    userViewModel.signup(username, password, age.toInt(), postcode.toInt(), city)
                    Toast.makeText(this, "Signup Successful", Toast.LENGTH_SHORT).show()

                     val intent = Intent(this, LoginActivity::class.java)
                     startActivity(intent)
                     finish()
                } else {
                    Toast.makeText(this, "Please enter a valid 2-digit age", Toast.LENGTH_SHORT).show()
                }
            }
        }

        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.postcodeEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty() && s.length == 5) { // Assuming 5 characters for postcode
                    fetchCityFromPostcode(s.toString())
                } else {
                    binding.cityEditText.setText("") // Clear city if postcode is invalid
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

    }

    private fun fetchCityFromPostcode(postcode: String) {
        userViewModel.getCityByPostcode(postcode).observe(this) { city ->
            if (city != null) {
                binding.cityEditText.setText(city)
            } else {
                binding.cityEditText.setText("Unknown City")
            }
        }
    }

}
