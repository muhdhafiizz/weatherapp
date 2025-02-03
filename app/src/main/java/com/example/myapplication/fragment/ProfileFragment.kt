package com.example.myapplication.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.LoginActivity
import com.example.myapplication.PreferenceManager
import com.example.myapplication.R
import com.example.myapplication.UserViewModel
import com.example.myapplication.databinding.FragmentProfileBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

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

        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", AppCompatActivity.MODE_PRIVATE)
        loggedInUsername = sharedPreferences.getString("loggedInUsername", "") ?: ""

        sharedPreferences.edit().putString("userCity", binding.cityEditText.text.toString()).apply()

        userViewModel.getUserDetails(loggedInUsername).observe(viewLifecycleOwner) { user ->
            binding.usernameEditText.setText(user.username)
            binding.passwordEditText.setText(user.password)
            binding.cityEditText.setText(user.city)
            binding.ageEditText.setText(user.age.toString())
            binding.postcodeEditText.setText(user.postcode.toString())
        }

//        binding.scanQRCode.setOnClickListener {
//            val poiId = (1..2).random() // Generate a random POI ID
//            val qrCodeBitmap = generateQRCode("POI_ID_$poiId") // Generate QR code with POI ID
//
//            if (qrCodeBitmap != null) {
//                showQRCodeAndConfirmationDialog(poiId, qrCodeBitmap)
//            } else {
//                Toast.makeText(requireContext(), "Failed to generate QR Code", Toast.LENGTH_SHORT).show()
//            }
//        }


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

    private fun generateQRCode(content: String): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun showQRCodeAndConfirmationDialog(poiId: Int, qrCodeBitmap: Bitmap) {
        // Create a custom dialog to display the QR code
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_qr_code, null)
        val qrImageView = dialogView.findViewById<ImageView>(R.id.qrCodeImageView)
        qrImageView.setImageBitmap(qrCodeBitmap)

        val qrDialog = AlertDialog.Builder(requireContext())
            .setTitle("Scan this QR Code")
            .setView(dialogView)
            .setPositiveButton("Done") { _, _ ->
                showConfirmationDialog(poiId)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        qrDialog.show()
    }

    private fun showConfirmationDialog(poiId: Int) {
        // Show confirmation dialog after QR scan
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Navigate to POI")
        builder.setMessage("Do you want to navigate to POI with ID $poiId?")
        builder.setPositiveButton("Yes") { _, _ ->
            Toast.makeText(requireContext(), "Success! Navigating to POI $poiId.", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }


    private fun signOut(sharedPreferences: SharedPreferences) {
        sharedPreferences.edit().clear().apply()
        PreferenceManager.setLoggedIn(requireContext(), false)
        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
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
