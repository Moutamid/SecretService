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
import com.moutamid.secretservice.R;
import com.moutamid.secretservice.adapters.AngelsAdapter;
import com.moutamid.secretservice.databinding.ActivityAngelsListBinding;
import com.moutamid.secretservice.models.ContactModel;
import com.moutamid.secretservice.utilis.Constants;

import java.util.ArrayList;

public class AngelsListActivity extends AppCompatActivity {
    ActivityAngelsListBinding binding;
    public final int PICK_CONTACT_REQUEST = 1;
    AngelsAdapter adapter;
    ArrayList<ContactModel> list = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAngelsListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.back.setOnClickListener(v -> onBackPressed());
        list = Stash.getArrayList(Constants.ANGELS_LIST, ContactModel.class);

        binding.addContact.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(AngelsListActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS);
                ActivityCompat.requestPermissions(AngelsListActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 2);
            } else {
                Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                pickContact.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(pickContact, PICK_CONTACT_REQUEST);
            }
        });


        binding.contactRc.setLayoutManager(new LinearLayoutManager(this));
        binding.contactRc.setHasFixedSize(false);

        adapter = new AngelsAdapter(this, list);
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
                Toast.makeText(this, "Permission is required to get the Contact Details", Toast.LENGTH_SHORT).show();
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
                        list = Stash.getArrayList(Constants.ANGELS_LIST, ContactModel.class);
                        list.add(new ContactModel(contactName, contactNumber));
                        Log.d("CHECK123", "SIZE " + list.size());
                        Stash.put(Constants.ANGELS_LIST, list);
                        adapter = new AngelsAdapter(this, list);
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