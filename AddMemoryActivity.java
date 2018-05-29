package com.example.kerekesnora.tripmemory;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;

public class AddMemoryActivity extends AppCompatActivity {

    //Members
    private TextView mLocationTextView, mDateTextView;
    private String mLocationString, mDateString, mStoryString, mImageString, mActualImageName;
    private EditText mStoryEditText;
    private ImageButton mLocationButton, mDateButton;
    private int currentYear, currentMonth, currentDay;
    private Button mChooseButton, mSaveButton;
    private ImageView mImageView;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;

    //Firebase
    private FirebaseStorage firebaseStorage;
    private StorageReference firebaseStorageReference;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_memory);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        firebaseStorage = FirebaseStorage.getInstance();
        firebaseStorageReference = firebaseStorage.getReference();

        currentYear = Calendar.getInstance().get(Calendar.YEAR);
        currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        mLocationTextView = (TextView) findViewById(R.id.location_id);
        mDateTextView = (TextView) findViewById(R.id.date_id);

        mDateButton = (ImageButton) findViewById(R.id.img_date);
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddMemoryActivity.this,datePickerDialog,currentYear,currentMonth,currentDay).show();
            }
        });

        /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },
                    PERMISSION_ACCESS_COARSE_LOCATION);
        }*/

        mLocationButton = (ImageButton) findViewById(R.id.img_location);
        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder locationDialog = new AlertDialog.Builder(AddMemoryActivity.this);
                locationDialog.setTitle("Add location");
                locationDialog.setMessage("Enable to find your location or type manually.");
                locationDialog.setNegativeButton("Cancel",null);
                locationDialog.setNeutralButton("Type manually",null);
                locationDialog.setPositiveButton("Enable", null);
                AlertDialog dialog = locationDialog.create();
                dialog.show();

            }
        });

        mStoryEditText = (EditText) findViewById(R.id.story_id);

        mChooseButton = (Button) findViewById(R.id.choose_button);
        mChooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        mImageView = (ImageView) findViewById(R.id.image_view);

        mSaveButton = (Button) findViewById(R.id.save_data_button);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMemory();
            }
        });
    }

    DatePickerDialog.OnDateSetListener datePickerDialog = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            int mon = (int)(view.getMonth()+1);
            mDateTextView.setText(view.getYear() + "/" + mon + "/" + view.getDayOfMonth());
        }
    };

    private void chooseImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                mImageView.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage(){
        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = firebaseStorageReference.child("images/"+ UUID.randomUUID().toString());
            mActualImageName = ref.getName();
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(AddMemoryActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(AddMemoryActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    })
            ;
        }
    }

    private void saveMemory(){
        uploadImage();
        mDateString = mDateTextView.getText().toString();
        mLocationString = mLocationTextView.getText().toString();
        mStoryString = mStoryEditText.getText().toString();
        mImageString = mActualImageName.toString();

        UserData userData = new UserData(mDateString,mLocationString,mStoryString,mImageString);
        FirebaseUser user = mAuth.getCurrentUser();
        String userId = user.getUid();
        databaseReference.child("data").child(userId).child(mDateString).child(mLocationString).setValue(userData);
        //databaseReference.child(userId).child("data").child(mDateString).child(mLocationString).child("images").setValue(mImageString);

        //Toast.makeText(AddMemoryActivity.this, R.string.save_memory_succ, Toast.LENGTH_SHORT).show();
    }
}


