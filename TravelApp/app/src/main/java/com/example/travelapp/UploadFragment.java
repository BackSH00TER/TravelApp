package com.example.travelapp;

/**
 * Created by ttyle on 3/23/2017.
 */

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transfermanager.Upload;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.internal.Constants;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;

import static android.app.Activity.RESULT_OK;

public class UploadFragment extends Fragment {

    private String imagePath;

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
        //Grant permission to access photos
        int hasGalleryPermission = ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasGalleryPermission != PackageManager.PERMISSION_GRANTED) {
            // request permission
            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1); // 1 is the request code for READ_EXTERNAL_STORAGE                 // (it wasn't predefined, johan just made it up)
            return; // exit here
        }
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // <---might not need this line
        // Start the Intent
        startActivityForResult(galleryIntent, 1);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            //String path = getRealPathFromURI(selectedImage);

            uploadImage(selectedImage);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 1) { // My request code from earlier, this corresponds to requesting
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                 // permission granted
                //Toast.makeText(this.getContext(), "READ_EXTERNAL_STORAGE Granted :)", Toast.LENGTH_SHORT).show();
                //uploadImage(); //TODO: how to call uploadImage from here and giv eit the path? need to make path global var?
            }
            else {
                // permission denied
                Toast.makeText(this.getContext(), "Allow access to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getPath(Uri uri ) {
        Context context = getContext();
        String result = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver( ).query( uri, proj, null, null, null );
        if(cursor != null){
            if ( cursor.moveToFirst( ) ) {
                int column_index = cursor.getColumnIndexOrThrow( proj[0] );
                result = cursor.getString( column_index );
            }
            cursor.close( );
        }
        if(result == null) {
            result = "Not found";
        }
        return result;
    }

    // And to convert the image URI to the direct file system path of the image file
    public String getRealPathFromURI(Uri contentUri) {
        // can post image
        String [] proj={MediaStore.Images.Media.DATA};
        Cursor cursor = getContext().getContentResolver().query( contentUri,
                proj, // Which columns to return
                null,       // WHERE clause; which rows to return (all rows)
                null,       // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    public void uploadImage(Uri imageUri) {
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getActivity().getApplicationContext(),
                "268293220984", //acountId
                "us-west-2:e01653c7-9aee-4237-bc20-50ede0c85edc", // Identity Pool ID
                "arn:aws:iam::268293220984:role/Cognito_ProjectGeckoUnauth_Role", //unauth role
                "arn:aws:iam::268293220984:role/Cognito_ProjectGeckoAuth_Role", //auth role
                Regions.US_WEST_2 // Region
        );
        AmazonS3Client s3Client = new AmazonS3Client(credentialsProvider);

        // Synchronous upload
//        try {
//            enableStrictMode(); //TODO BAD BAD REMOVE
//            PutObjectRequest por = new PutObjectRequest("hilde2", "TestPictureFromPhone", new java.io.File(path));
//            s3Client.putObject(por);
//        }
//        catch (Exception ex) {
//            System.out.println("Exception: " + ex.getMessage());
//        }

        // Async upload
        new S3PutObjectTask(s3Client, this.getContext()).execute(imageUri);


    }
    //VERY BAD WORKAROUND ONLY KEEP FOR TESTING, GET THE RIGHT WAY TO DO IT
    //TODO IMPLEMENT REAL WAY REMOVE THIS ITS BAD
    //http://simpledeveloper.com/network-on-main-thread-error-solution/  (Example)
    //NEED TO IMPLEMENT ASYNC
    public void enableStrictMode()
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
    }

    //old code to see other ways to upload, remove if not needed
    private void oldUploadImage() {
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getActivity().getApplicationContext(),
                "268293220984", //acountId
                "us-west-2:e01653c7-9aee-4237-bc20-50ede0c85edc", // Identity Pool ID
                "arn:aws:iam::268293220984:role/Cognito_ProjectGeckoUnauth_Role", //unauth role
                "arn:aws:iam::268293220984:role/Cognito_ProjectGeckoAuth_Role", //auth role
                Regions.US_WEST_2 // Region
        );
        AmazonS3Client s3Client = new AmazonS3Client(credentialsProvider);


        //OTHER CODE FOR uploading , remove if we dont need this anymore
        /*try {
            PutObjectRequest por = new PutObjectRequest("hilde2", "TestPictureFromPhone", new java.io.File(imagePath));
            s3Client.putObject(por);
        }
        catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }*/
//
//        try {
//            TransferUtility transferUtility = new TransferUtility(s3Client, getContext());
//            TransferObserver observer = transferUtility.upload("hilde2", "TestPictureFromPhone", new java.io.File(imagePath));
//        }
//        catch (Exception ex) {
//            System.out.println("Exception: " + ex.getMessage());
//        }
    }

}
