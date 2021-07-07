package com.example.firebasefirestore

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import kotlin.Exception

class MainActivity : AppCompatActivity() {

    //INIT CLOUD FIRESTORE and ADD NEW DOC
    private val db = Firebase.firestore.collection("Biography")

    private lateinit var etFirstname: EditText
    private lateinit var etLastname: EditText
    private lateinit var spGender: Spinner
    private lateinit var etAge: EditText
    private lateinit var etAddress: EditText
    private lateinit var btnSave: Button
    private lateinit var btnRetrieve: Button

    private lateinit var tvData: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvData = findViewById(R.id.tvData)
        etFirstname = findViewById(R.id.etFirstname)
        etLastname = findViewById(R.id.etLastname)
        spGender = findViewById(R.id.spGender)
        etAge = findViewById(R.id.etAge)
        etAddress = findViewById(R.id.etAddress)
        btnSave = findViewById(R.id.btnSave)

        btnSave.setOnClickListener {
            val firstname = etFirstname.text.toString()
            val lastname = etLastname.text.toString()
            val gender = spGender.selectedItem.toString().lowercase()
            val age = etAge.text.toString().toInt()
            val address = etAddress.text.toString()

            val getBio = Biography(firstname, lastname, gender, age, address)
            saveBio(getBio)
        }

        btnRetrieve = findViewById(R.id.btnRetrieve)
        btnRetrieve.setOnClickListener {
            retrieveBio()
        }

        realtimeUpdateBio()

    }

    private fun realtimeUpdateBio(){
        db.addSnapshotListener { result, firebaseException ->
            firebaseException?.let {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            result?.let {
                val stringBuilder = StringBuilder()
                for (documents in it.documents) {
                    //get the document data from firestore then convert it to object(Biography::class.java)
                    val bio = documents.toObject<Biography>()
                    stringBuilder.append("$bio\n")
                }
                tvData.text = stringBuilder.toString()
            }
        }
    }

    private fun retrieveBio() =  CoroutineScope(Dispatchers.IO).launch {
        try {
            val stringBuilder = StringBuilder()
            db.get()
                .addOnSuccessListener { result ->
                    for (documents in result.documents) {
                        val bio = documents.toObject<Biography>()
                        stringBuilder.append("$bio\n")
                    }
                    tvData.text = stringBuilder.toString()
                }.await()
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun saveBio(bio: Biography) = CoroutineScope(Dispatchers.IO).launch {
        try {
            db.add(bio).await()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Data saved successfully", Toast.LENGTH_SHORT)
                    .show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }

        }
    }

    fun gotoNext(view: View) {
        val intent = Intent(this, MainActivity2::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
