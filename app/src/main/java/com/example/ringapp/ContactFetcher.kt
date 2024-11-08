package com.example.ringapp


import android.content.Context
import android.provider.ContactsContract

class ContactFetcher(private val context: Context) {

    fun fetchContacts(): List<Contact> {
        val contactsList = mutableListOf<Contact>()
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val name = it.getString(0)
                val phoneNumber = it.getString(1)
                contactsList.add(Contact(name, phoneNumber))
            }
        }
        return contactsList
    }
}
