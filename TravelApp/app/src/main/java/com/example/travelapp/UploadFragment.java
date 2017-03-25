package com.example.travelapp;

/**
 * Created by ttyle on 3/23/2017.
 */

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.internal.Constants;
import com.amazonaws.services.s3.model.PutObjectRequest;

public class UploadFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);
        return view;
    }

    //TODO: Call this function on button press
    public void loadImage() {
        //Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
       // intent.setType("image/*");
        //intent.addCategory(Intent.CATEGORY_OPENABLE);
        //startActivityForResult(intent, 1);
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String imgDecodableString;

        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getActivity().getApplicationContext(),
                "us-west-2:e01653c7-9aee-4237-bc20-50ede0c85edc", // Identity Pool ID
                Regions.US_WEST_2 // Region
        );
        AmazonS3Client s3Client = new AmazonS3Client(credentialsProvider);

        if(resultCode == -1) {
            Uri selectedImage = data.getData(); // path of file?
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            // Get the cursor
            Cursor cursor = getActivity().getContentResolver().query(selectedImage, //getAcittivity() added on
                    filePathColumn, null, null, null);
            // Move to first row
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            imgDecodableString = cursor.getString(columnIndex);
            cursor.close();

            //TODO: Fix the uplaoded, crashes at this point
            PutObjectRequest por = new PutObjectRequest("hilde2", "TestPictureFromPhone", new java.io.File(imgDecodableString));
            s3Client.putObject(por);
        }
    }
}
