package com.example.project2.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project2.databinding.ActivitySignInBinding
import com.example.project2.utilities.Constants
import com.example.project2.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore


class SignInActivity : AppCompatActivity() {
    var binding: ActivitySignInBinding? = null
    var preferenceManage: PreferenceManager?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManage = PreferenceManager(applicationContext)
        if (preferenceManage!!.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
//        setListeners()
        binding!!.textCreateNewAccount.setOnClickListener {
            var intent = Intent(applicationContext,SignUpActivity::class.java)
            startActivity(intent)
        }
        binding!!.buttonSignIn.setOnClickListener {
            if(isValidSignInDetails()){
                signIn()
            }
        }
    }

    private fun signIn() {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USERS)
            .whereEqualTo(Constants.KEY_EMAIL, binding!!.inputEmail.text.toString())
            .whereEqualTo(Constants.KEY_PASSWORD, binding!!.inputPassword.text.toString())
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null && task.result.documents.size > 0) {
                    val documentSnapshot = task.result.documents[0]
                    preferenceManage!!.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                    preferenceManage!!.putString(Constants.KEY_USER_ID, documentSnapshot.id)
                    preferenceManage!!.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME))
                    preferenceManage!!.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE))
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                } else {
                    loading(false)
                    showToast("Unable to sign in")
                }
            }
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding!!.buttonSignIn.visibility = View.INVISIBLE
            binding!!.progressBar.visibility = View.VISIBLE
        } else {
            binding!!.progressBar.visibility = View.INVISIBLE
            binding!!.buttonSignIn.visibility = View.VISIBLE
        }
    }

    private fun showToast(message: String){
        Toast.makeText(applicationContext,message,Toast.LENGTH_SHORT).show()
    }

    private fun isValidSignInDetails():Boolean {
        if(binding!!.inputEmail.text.toString().trim().isEmpty()){
            showToast("Enter email")
            return false
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding!!.inputEmail.text.toString()).matches()){
            showToast("Enter valid email")
        }else if(binding!!.inputPassword.text.toString().trim().isEmpty()){
            showToast("Enter password")
            return false
        }else{
            return true
        }
        return false
    }

}