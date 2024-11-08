package com.example.ringapp

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import ContactAdapter

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CALL_PERMISSION = 1
        const val REQUEST_SMS_PERMISSION = 2
    }

    private lateinit var contactAdapter: ContactAdapter
    private val contacts = mutableListOf<Contact>() // Original list of contacts

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.contact)

        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        contactAdapter = ContactAdapter(this)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = contactAdapter

        val searchView: SearchView = findViewById(R.id.recherche)
        setupSearchView(searchView)

        requestPermissions() // Request necessary permissions
        requestContactsPermission() // Request contact permission
    }

    // Setup the SearchView functionality
    private fun setupSearchView(searchView: SearchView) {
        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                searchView.isIconified = false // Ensure search bar is fully visible
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterContacts(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterContacts(newText)
                return true
            }
        })
    }

    // Filter contacts based on search query
    private fun filterContacts(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            contacts // Show all contacts if query is empty
        } else {
            contacts.filter {
                it.name.contains(query, ignoreCase = true) || it.phoneNumber.contains(query)
            }
        }
        contactAdapter.submitList(filteredList)
    }

    // Request permissions to read contacts, make calls, and send SMS
    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS),
                REQUEST_CALL_PERMISSION
            )
        }
    }

    // Handle permissions request result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CALL_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Call Permission Granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Call Permission Denied", Toast.LENGTH_SHORT).show()
                }
                if (grantResults.size > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "SMS Permission Granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Request permission to access contacts
    private fun requestContactsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            loadContacts()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            loadContacts()
        } else {
            Toast.makeText(this, "Permission nécessaire pour accéder aux contacts", Toast.LENGTH_SHORT).show()
        }
    }

    // Load contacts from the device
    private fun loadContacts() {
        contacts.clear()
        val contentResolver = contentResolver
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val name = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phoneNumber = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                contacts.add(Contact(name, phoneNumber))
            }
        }
        contactAdapter.submitList(contacts) // Show all contacts initially
    }

}
