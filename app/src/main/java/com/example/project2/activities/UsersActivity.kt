package com.example.project2.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.project2.adapters.UsersAdapter
import com.example.project2.databinding.ActivityUsersBinding
import com.example.project2.models.User
import com.example.project2.utilities.Constants
import com.example.project2.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore

class UsersActivity : AppCompatActivity() {

    private var binding: ActivityUsersBinding? = null
    private var preferenceManager: PreferenceManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        preferenceManager = PreferenceManager(applicationContext)
        setListeners()
        getUsers()
    }

    private fun setListeners(){
        binding!!.imageBack.setOnClickListener {
            onBackPressed()
        }
    }
    private fun getUsers() {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USERS)
            .get()
            .addOnCompleteListener { task ->
                loading(false)
                val currentUserId = preferenceManager!!.getString(Constants.KEY_USER_ID)
                if (task.isSuccessful && task.result != null) {
                    val users = ArrayList<User>()
                    for (queryDocumentSnapshot in task.result!!) {
                        if (currentUserId == queryDocumentSnapshot.id) {
                            continue
                        }
                        val user = User()
                        user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME)
                        user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL)
                        user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE)
                        user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN)
                        users.add(user)
                    }
                    if (users.size > 0) {
                        val usersAdapter = UsersAdapter(users)
                        binding!!.usersRecyclerView.adapter = usersAdapter
                        binding!!.usersRecyclerView.visibility = View.VISIBLE
                    } else {
                        showErrorMessage()
                    }
                } else {
                    showErrorMessage()
                }
            }

    }
    private fun showErrorMessage(){
        binding!!.textErrorMessage.setText(String.format("%s","No user available"))
        binding!!.textErrorMessage.visibility = View.VISIBLE
    }
    private fun loading(isLoading: Boolean){
        if(isLoading){
            binding!!.progressBar.visibility = View.VISIBLE
        }else{
            binding!!.progressBar.visibility = View.INVISIBLE
        }
    }
}