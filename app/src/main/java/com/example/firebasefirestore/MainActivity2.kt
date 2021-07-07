package com.example.firebasefirestore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity2 : AppCompatActivity() {

    private val db = Firebase.firestore.collection("Biography")

    private lateinit var spinner: Spinner
    private lateinit var tvText: TextView

    private lateinit var etFirstname: EditText
    private lateinit var etLastname: EditText
    private lateinit var etFromAge: EditText
    private lateinit var etAge: EditText
    private lateinit var etAddress: EditText
    private lateinit var sGender: Spinner
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button
    private lateinit var btnBatchWrite: Button
    private lateinit var btnTransaction: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        spinner = findViewById(R.id.spinner)
        etFromAge = findViewById(R.id.etFromAge)
        tvText = findViewById(R.id.tvText)
        etFirstname = findViewById(R.id.etFirstname)
        etLastname = findViewById(R.id.etLastname)
        etAge = findViewById(R.id.etAge)
        etAddress = findViewById(R.id.etAddress)
        sGender = findViewById(R.id.sGender)


        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
            {
                if(etFromAge.text.isNotEmpty()) sortDatabaseQueries()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        btnUpdate = findViewById(R.id.btnUpdate)
        btnUpdate.setOnClickListener {
            updateData(getOldBio())
        }

        btnDelete = findViewById(R.id.btnDelete)
        btnDelete.setOnClickListener {
            deleteData(getOldBio())
        }

        btnBatchWrite = findViewById(R.id.btnBatchWrite)
        btnBatchWrite.setOnClickListener {
            doBatch("ZGbYHFnFQBlNw6BnCXur", "selena", "miko", "female", 22, "xyz")
        }

        btnTransaction = findViewById(R.id.btnTransaction)
        btnTransaction.setOnClickListener { 
            doTransaction("ZGbYHFnFQBlNw6BnCXur")
        }
    }

    private fun doTransaction(id: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            Firebase.firestore.runTransaction {
                val reference = db.document(id)
                val biography = it.get(reference)
                val newAge = biography["age"] as Long + 1
                it.update(reference, "age", newAge)
                null
            }.await()
        }catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity2, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun doBatch(
        id: String,
        firstname: String,
        lastname: String,
        gender: String,
        age: Int,
        address: String
    ) = CoroutineScope(Dispatchers.IO).launch {
        // only possible for "write" operation
        try {
            Firebase.firestore.runBatch { writeToDatabase ->
                val reference = db.document(id)
                writeToDatabase.update(reference, "firstname", firstname)
                writeToDatabase.update(reference, "lastname", lastname)
                writeToDatabase.update(reference, "gender", gender)
                writeToDatabase.update(reference, "age", age)
                writeToDatabase.update(reference, "address", address)
                // set/delete/etc..
            }.await()
        }catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity2, e.message, Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun deleteData(biography: Biography) = CoroutineScope(Dispatchers.IO).launch {
        val query = db
            .whereEqualTo("firstname", biography.firstname)
            .whereEqualTo("lastname", biography.lastname)
            .whereEqualTo("gender", biography.gender)
            .whereEqualTo("age", biography.age)
            .whereEqualTo("address", biography.address)
            .get().await()

        if (query.documents.isNotEmpty()) {
            for (documents in query) {
                try {
                    db.document(documents.id).delete().await()
                    withContext(Dispatchers.Main) {
                        refresh()
                        Toast.makeText(this@MainActivity2, "Data deleted successfully!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity2, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity2, "No query matched!", Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun updateData(biography: Biography) = CoroutineScope(Dispatchers.IO).launch {
        // find which document ref wants to update
        val query = db
            .whereEqualTo("firstname", biography.firstname)
            .whereEqualTo("lastname", biography.lastname)
            .whereEqualTo("gender", biography.gender)
            .whereEqualTo("age", biography.age)
            .whereEqualTo("address", biography.address)
            .get().await()

        if (query.documents.isNotEmpty()) {
            for (documents in query) {
                try {
                    db.document(documents.id)       // get the id that matches to the documents
                        .set(getNewBio(biography), SetOptions.merge())   //*
                        .await()
                    withContext(Dispatchers.Main) {
                        refresh()
                        Toast.makeText(this@MainActivity2, "Data updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity2, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity2, "No query matched!", Toast.LENGTH_SHORT).show()

            }
        }

    }

    private fun getNewBio(biography: Biography): Map<String, Any> {
        val newFirstname = etFirstname.text.toString()
        val newLastname = etLastname.text.toString()
        val newGender =
            if (sGender.selectedItem.toString().lowercase().equals("male")) "male"
             else
                 "female"
        val newAge = etAge.text.toString().toInt()
        val newAddress = etAddress.text.toString()

        val map = mutableMapOf<String, Any>()
        if (newFirstname != biography.firstname) map["firstname"] = newFirstname
        if (newLastname != biography.lastname) map["lastname"] = newLastname
        if (newGender != biography.gender) map["gender"] = newGender
        if (newAge != biography.age) map["age"] = newAge
        if (newAddress != biography.address) map["address"] = newAddress

        return map
    }

    private fun getOldBio(): Biography{
        val oldFirstname = mFirstName
        val oldLastname = mLastName
        val oldGender = mGender
        val oldAge = mAge
        val oldAddress = mAddress

        return Biography(oldFirstname, oldLastname, oldGender, oldAge, oldAddress)
    }

    private var mFirstName = ""
    private var mLastName = ""
    private var mGender = ""
    private var mAge = 0
    private var mAddress = ""

    private fun sortDatabaseQueries() = CoroutineScope(Dispatchers.IO).launch {
        val getGender = spinner.selectedItem.toString().lowercase()
        val getFromAge = etFromAge.text.toString().toInt()
        val stringBuilder = StringBuilder()
        try {
            db
                .whereEqualTo("gender", getGender)
                .whereGreaterThanOrEqualTo("age", getFromAge)
                .orderBy("age")
                .get().addOnSuccessListener {
                    for (documents in it.documents) {
                        val getBio = documents.toObject<Biography>()!!
                        stringBuilder.append("$getBio\n")

                        mFirstName = getBio.firstname
                        mLastName = getBio.lastname
                        mGender = getBio.gender
                        mAge = getBio.age
                        mAddress = getBio.address

                    }
                }.await()
            withContext(Dispatchers.Main) {
                tvText.text = stringBuilder.toString()
                etFirstname.setText(mFirstName)
                etLastname.setText(mLastName)
                if (mGender.equals("male")) sGender.setSelection(1)
                else
                    sGender.setSelection(2)
                etAge.setText(mAge.toString())
                etAddress.setText(mAddress)

                getOldBio()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity2, e.message, Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun refresh(){
        spinner.setSelection(0)
        etFromAge.text.clear()
        etFirstname.text.clear()
        etLastname.text.clear()
        sGender.setSelection(0)
        etAge.text.clear()
        etAddress.text.clear()
    }
}

