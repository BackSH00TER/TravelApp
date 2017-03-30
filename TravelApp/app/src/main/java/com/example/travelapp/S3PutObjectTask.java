package com.example.travelapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.File;

/**
 * Created by johan on 3/28/2017.
 *
 * Not all methods are required, they're just used to inform
 * the UI thread whats going on. The only method that works
 * in the background thread is doInBackground, all others work
 * on the UI thread.
 */

public class S3PutObjectTask extends AsyncTask<Uri, Integer, S3PutObjectTask.S3TaskResult> {
    public class S3TaskResult {
        private Uri uri = null;
        private String errorMessage = null;

        public Uri getUri() { return this.uri; }
        public void setUri(Uri newUri) { this.uri = newUri; }
        public String getErrorMessage() { return this.errorMessage; }
        public void setErrorMessage(String newMsg) { this.errorMessage = newMsg; }
    }

    private ProgressDialog pDialog; // track upload progress
    private AmazonS3Client s3Client; // used for uploading
    private Context activityContext; // getting the context from the calling activity

    // Constructor, need to have some things passed in
    public S3PutObjectTask(Context context) {
        Master m = (Master)context.getApplicationContext();
        this.s3Client = m.getS3Client();
        this.activityContext = context;
    }



    @Override
    protected  void onPreExecute() {    // set up the progress dialog
        pDialog = new ProgressDialog(this.activityContext);
        pDialog.setMessage("Uploading image...");
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    protected  S3TaskResult doInBackground(Uri... uris) {   // does work in background thread
        String filePath = getRealPathFromURI(uris[0]);  // get real path of image
        File imgFile = new File(filePath);

        pDialog.setMax((int)imgFile.length()); // set max length of dialog

        S3TaskResult result = new S3TaskResult();   // create a new S3TaskResult object
        result.setUri(uris[0]);

        // Put image into s3
        try {
            PutObjectRequest por = new PutObjectRequest("hilde2", "TestPictureFromPhoneASYNC", new java.io.File(filePath));

            // Link event transfer amount back to progress dialog
            por.setGeneralProgressListener(new ProgressListener() {
                int total = 0; // track amount of data progress
                @Override
                public void progressChanged(ProgressEvent progressEvent) {
                    total += (int) progressEvent.getBytesTransferred();
                    publishProgress(total);
                }
            });

            s3Client.putObject(por);
        }
        catch (Exception ex) {
            result.setErrorMessage(ex.getMessage());
        }
        return result;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {    // invoked from doInBackground
        pDialog.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(S3TaskResult result) { // invoked after doInBackground is complete
        pDialog.dismiss(); // close the progress dialog
        if (result.getErrorMessage() != null) { // display error if there is one
            Toast.makeText(this.activityContext,
                    "Error: " + result.getErrorMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    // And to convert the image URI to the direct file system path of the image file
    private String getRealPathFromURI(Uri contentUri) {
        // can post image
        String [] proj={MediaStore.Images.Media.DATA};
        Cursor cursor = this.activityContext.getContentResolver().query( contentUri,
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
}
