package com.moutamid.secretservice.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.fxn.stash.Stash;
import com.moutamid.secretservice.adapters.ContactsAdapter;
import com.moutamid.secretservice.databinding.ActivityNoContactsBinding;
import com.moutamid.secretservice.models.ContactModel;
import com.moutamid.secretservice.utilis.Constants;
import com.moutamid.secretservice.utilis.ContactManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class NoContactsActivity extends AppCompatActivity {
    ActivityNoContactsBinding binding;
    public final int PICK_CONTACT_REQUEST = 1;
    ContactsAdapter adapter;
    ArrayList<ContactModel> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoContactsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.back.setOnClickListener(v -> onBackPressed());
        list = Stash.getArrayList(Constants.EXCLUDE_CONTACTS, ContactModel.class);
        list.sort(Comparator.comparing(ContactModel::getContactName));

        binding.addContact.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(NoContactsActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS);
                ActivityCompat.requestPermissions(NoContactsActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 2);
            } else {
                Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                pickContact.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(pickContact, PICK_CONTACT_REQUEST);
            }
        });

        binding.delete.setOnClickListener(v -> {
            list.clear();
            adapter.notifyDataSetChanged();
            Stash.put(Constants.EXCLUDE_CONTACTS, list);
        });

        binding.selectAll.setOnClickListener(v -> {
            list.clear();
            list = ContactManager.getAllContacts(this);
            list.sort(Comparator.comparing(ContactModel::getContactName));
            adapter = new ContactsAdapter(this, list);
            Log.d("CHECK123", "SIZE " + list.size());
            binding.contactRc.setAdapter(adapter);
            Stash.put(Constants.EXCLUDE_CONTACTS, list);
        });

        binding.contactRc.setLayoutManager(new LinearLayoutManager(this));
        binding.contactRc.setHasFixedSize(false);

        adapter = new ContactsAdapter(this, list);
        Log.d("CHECK123", "SIZE " + list.size());
        binding.contactRc.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                pickContact.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(pickContact, PICK_CONTACT_REQUEST);
            } else {
                Constants.showToast(this, "Permission is required to get the Contact Details");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_CONTACT_REQUEST && resultCode == Activity.RESULT_OK) {
            Uri contactUri = data.getData();
            if (contactUri != null) {
                Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                        int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        String contactName = cursor.getString(nameIndex);
                        String contactNumber = cursor.getString(phoneIndex);
                        list = Stash.getArrayList(Constants.EXCLUDE_CONTACTS, ContactModel.class);
                        list.add(new ContactModel(contactName, contactNumber));
                        Log.d("CHECK123", "SIZE " + list.size());
                        list.sort(Comparator.comparing(ContactModel::getContactName));
                        Stash.put(Constants.EXCLUDE_CONTACTS, list);
                        adapter = new ContactsAdapter(this, list);
                        binding.contactRc.setAdapter(adapter);
                    } else {
                        Log.d("CHECK123", "Empty");
                    }
                    cursor.close();
                }
            }
        }
    }

}