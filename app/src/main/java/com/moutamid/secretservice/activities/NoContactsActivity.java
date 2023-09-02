package com.moutamid.secretservice.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import com.fxn.stash.Stash;
import com.moutamid.secretservice.R;
import com.moutamid.secretservice.adapters.ContactsAdapter;
import com.moutamid.secretservice.databinding.ActivityNoContactsBinding;
import com.moutamid.secretservice.listners.ContactDeleteListners;
import com.moutamid.secretservice.models.ContactModel;
import com.moutamid.secretservice.utilis.Constants;

import java.util.ArrayList;

public class NoContactsActivity extends AppCompatActivity {
    ActivityNoContactsBinding binding;
    public final int PICK_CONTACT_REQUEST = 1;
    ContactsAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoContactsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.back.setOnClickListener(v -> onBackPressed());

        binding.addContact.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, PICK_CONTACT_REQUEST);
        });

        binding.contactRc.setLayoutManager(new LinearLayoutManager(this));
        binding.contactRc.setHasFixedSize(false);

        ArrayList<ContactModel> list = Stash.getArrayList(Constants.EXCLUDE_CONTACTS, ContactModel.class);
        adapter = new ContactsAdapter(this, list, contactDeleteListners);
        Log.d("CHECK123", "SIZE " + list.size());
        binding.contactRc.setAdapter(adapter);
    }

    ContactDeleteListners contactDeleteListners = new ContactDeleteListners() {
        @Override
        public void onClick(ContactModel model) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_CONTACT_REQUEST && resultCode == Activity.RESULT_OK) {
            Uri contactUri = data.getData();
            if (contactUri != null) {
                Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        @SuppressLint("Range") String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        String contactNumber = getContactPhoneNumber(contactUri);
                        ArrayList<ContactModel> list = Stash.getArrayList(Constants.EXCLUDE_CONTACTS, ContactModel.class);
                        list.add(new ContactModel(contactName, contactNumber));
                        Log.d("CHECK123", "SIZE " + list.size());
                        Stash.put(Constants.EXCLUDE_CONTACTS, list);
                        adapter = new ContactsAdapter(this, list, contactDeleteListners);
                        binding.contactRc.setAdapter(adapter);
                    } else {
                        Log.d("CHECK123", "Empty");
                    }
                    cursor.close();
                }
            }
        }
    }

    @SuppressLint("Range")
    private String getContactPhoneNumber(Uri contactUri) {
        String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
            cursor.close();
        }
        return "";
    }


}