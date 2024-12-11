package com.example.myapplication.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.LoginActivity
import com.example.myapplication.PreferenceManager
import com.example.myapplication.R
import com.example.myapplication.UserViewModel
import com.example.myapplication.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var loggedInUsername: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]


        val updatedCity = binding.cityEditText.text.toString()
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", AppCompatActivity.MODE_PRIVATE)
        loggedInUsername = sharedPreferences.getString("loggedInUsername", "") ?: ""
        sharedPreferences.edit().putString("userCity", updatedCity).apply()

        userViewModel.getUserDetails(loggedInUsername).observe(viewLifecycleOwner) { user ->
            binding.usernameEditText.setText(user.username)
            binding.passwordEditText.setText(user.password)
            binding.cityEditText.setText(user.city)
            binding.ageEditText.setText(user.age.toString())
            binding.postcodeEditText.setText(user.postcode.toString())
        }

        binding.updateButton.setOnClickListener {
            val updatedUsername = binding.usernameEditText.text.toString()
            val updatedPassword = binding.passwordEditText.text.toString()
            val updatedCity = binding.cityEditText.text.toString()
            val updatedAge = binding.ageEditText.text.toString().toInt()
            val updatedPostcode = binding.postcodeEditText.text.toString().toIntOrNull()

            if (updatedUsername.isEmpty() || updatedPassword.isEmpty() || updatedAge == null || updatedPostcode == null || updatedCity.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                userViewModel.updateUserDetails(updatedUsername, updatedPassword, updatedAge, updatedPostcode, updatedCity)
                Toast.makeText(requireContext(), "Details updated successfully", Toast.LENGTH_SHORT).show()
            }
        }

        binding.postcodeEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty() && s.length == 5) {
                    fetchCityFromPostcode(s.toString())
                } else {
                    binding.cityEditText.setText("")
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.signoutButton.setOnClickListener {
            signOut(sharedPreferences)
        }
        return binding.root
    }

    private fun signOut(sharedPreferences: SharedPreferences) {
        // Clear SharedPreferences
        sharedPreferences.edit().clear().apply()

        // Clear login state
        PreferenceManager.setLoggedIn(requireContext(), false)

        // Navigate to LoginActivity
        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)

        // Finish current activity
        requireActivity().finish()
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
