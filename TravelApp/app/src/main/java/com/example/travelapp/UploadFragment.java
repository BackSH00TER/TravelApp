package com.example.travelapp;

/**
 * Created by ttyle on 3/23/2017.
 */

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.internal.Constants;
import com.amazonaws.services.s3.model.PutObjectRequest;

import static android.app.Activity.RESULT_OK;

public class UploadFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);
        //Set the on click listener for the upload button
        Button button2 = (Button)view.findViewById(R.id.uploadButton);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadImage(v);
            }
        });
        return view;
    }

    //Creates an intent used to open image gallery and select an image
    public void loadImage(View view) {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, 1);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getActivity().getApplicationContext(),
                "us-west-2:e01653c7-9aee-4237-bc20-50ede0c85edc", // Identity Pool ID
                Regions.US_WEST_2 // Region
        );
        AmazonS3Client s3Client = new AmazonS3Client(credentialsProvider);

        if(resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String imagePath = selectedImage.getPath();

            //TODO: Fix the uplaod, crashes at this point (permissions issue?)
            PutObjectRequest por = new PutObjectRequest("hilde2", "TestPictureFromPhone", new java.io.File(imagePath));
            s3Client.putObject(por);
        }
    }
}
