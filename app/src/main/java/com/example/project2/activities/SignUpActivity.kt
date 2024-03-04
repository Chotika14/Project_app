package com.example.project2.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.project2.databinding.ActivitySignUpBinding
import com.example.project2.utilities.Constants
import com.example.project2.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream


class SignUpActivity : AppCompatActivity() {

    var binding: ActivitySignUpBinding? = null
    var preferenceManager: PreferenceManager? = null
    var encodedImage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        preferenceManager = PreferenceManager(applicationContext)
       setListeners()

    }

    private fun setListeners(){
        binding!!.textSignIn.setOnClickListener {
            onBackPressed()
        }
        binding!!.buttonSignUp.setOnClickListener {
            if(isValidSignUpDetails()){
                signUp()
            }
        }
        binding!!.layoutImage.setOnClickListener {
            var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)
        }
    }

    private fun showToast(message: String){
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun signUp() {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        val user = hashMapOf(
            Constants.KEY_NAME to binding!!.inputName.text.toString(),
            Constants.KEY_EMAIL to binding!!.inputEmail.text.toString(),
            Constants.KEY_PASSWORD to binding!!.inputPassword.text.toString(),
            Constants.KEY_IMAGE to encodedImage
        )
        database.collection(Constants.KEY_COLLECTION_USERS)
            .add(user)
            .addOnSuccessListener { documentReference ->
                loading(false)
                preferenceManager!!.putBoolean(Constants.KEY_IS_SIGNED_IN,true)
                preferenceManager!!.putString(Constants.KEY_USER_ID, documentReference.id)
                preferenceManager!!.putString(Constants.KEY_NAME,binding!!.inputName.text.toString())
                preferenceManager!!.putString(Constants.KEY_IMAGE,encodedImage)
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
            .addOnFailureListener { exception ->
                loading(false)

            }
    }


    fun encodeImage(bitmap: Bitmap): String? {
        var previewWidth = 150
        var previewHeight = bitmap.height * previewWidth / bitmap.width
        var previewBitmap: Bitmap? = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false)
        var byteArrayOutputStream: ByteArrayOutputStream? = ByteArrayOutputStream()

        previewBitmap!!.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream)
        val bytes: ByteArray = byteArrayOutputStream!!.toByteArray()
        return Base64.encodeToString(bytes,Base64.DEFAULT)
    }

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(imageUri!!)
                val bitmap: Bitmap? = BitmapFactory.decodeStream(inputStream)
                binding!!.imageProfile.setImageBitmap(bitmap)
                binding!!.textAddImage.visibility = View.GONE
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }

private fun  isValidSignUpDetails(): Boolean {

//        if(encodedImage == null){
//            showToast("Select profile image")
//            return false;
//        }
        if (binding!!.inputName.text.toString().trim().isEmpty()){
            showToast("Enter name")
            return false;
        }else if(binding!!.inputEmail.text.toString().trim().isEmpty()){
            showToast("Enter email")
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding!!.inputEmail.text.toString()).matches()){
            showToast("Enter valid image")
            return false;
        }else if(binding!!.inputPassword.text.toString().trim().isEmpty()){
            showToast("Enter password")
            return false;
        }else if(binding!!.inputConfirmPassword.text.toString().trim().isEmpty()){
            showToast("Confirm your password")
            return false;
        }else if(binding!!.inputPassword.text.toString() != binding!!.inputConfirmPassword.text.toString()){
            showToast("Password and Confirm Password must to same")
        }else{
            return true;
        }
        return false
    }

    fun loading(isLoading: Boolean){
        if(isLoading){
            binding!!.buttonSignUp.visibility = View.INVISIBLE
            binding!!.progressBar.visibility = View.INVISIBLE
        }else{
            binding!!.progressBar.visibility = View.INVISIBLE
            binding!!.buttonSignUp.visibility = View.INVISIBLE
        }
    }
}