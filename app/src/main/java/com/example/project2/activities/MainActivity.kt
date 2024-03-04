package com.example.project2.activities

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import com.example.project2.R
import com.example.project2.databinding.ActivityMainBinding
import com.example.project2.databinding.ActivitySignUpBinding
import com.example.project2.utilities.Constants
import com.example.project2.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null
    private var preferenceManager: PreferenceManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        preferenceManager = PreferenceManager(applicationContext)

        getToken()

        binding!!.imageSignOut.setOnClickListener{
            signOut()
        }
        binding!!.fabNewChat.setOnClickListener {
            var intent = Intent(applicationContext, UsersActivity::class.java)
            startActivity(intent)
        }

    }
    private fun loadUserDetails() {
        binding!!.textName.text = preferenceManager!!.getString(Constants.KEY_NAME)
        val bytes = Base64.decode(preferenceManager!!.getString(Constants.KEY_IMAGE), Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        binding!!.imageProfile.setImageBitmap(bitmap)
    }

    private fun showToast(message: String){
        Toast.makeText(applicationContext,message, Toast.LENGTH_SHORT).show()
    }

    private fun getToken(){
        FirebaseMessaging.getInstance().token.addOnSuccessListener(this::updateToken)
    }
    private fun updateToken(token: String){
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
            preferenceManager!!.getString(Constants.KEY_USER_ID)!!
        )
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
            .addOnSuccessListener {
                showToast("Token update successfully")
            }
            .addOnFailureListener {
                showToast("Unable to update token")
            }
    }

    private fun signOut() {
        showToast("Signing out...")
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
            preferenceManager!!.getString(Constants.KEY_USER_ID)
        )
        val updates = HashMap<String, Any>()
        updates[Constants.KEY_FCM_TOKEN] = FieldValue.delete()
        documentReference.update(updates)
            .addOnSuccessListener {
                preferenceManager!!.clear()
                startActivity(Intent(applicationContext, SignInActivity::class.java))
                finish()
            }
            .addOnFailureListener { e -> showToast("Unable to sign out") }
    }
}