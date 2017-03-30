package com.example.travelapp;

import android.app.Application;
import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

/**
 * Used for controlling any objects that should be globally instantiated
 *
 * HOW TO USE THIS CLASS:
 *
 *         Master foo = (Master)getContext().getApplicationContext();
 *         AmazonS3Client s3Client = foo.getS3Client();
 *         ...
 *
 * Created by johan on 3/30/2017.
 */

public class Master extends Application {

    private AmazonS3Client s3Client;
    private CognitoCachingCredentialsProvider credentialsProvider;

    /*
     * Function is called on application start. Initialize anything we can here that only needs
     * to be initialized once across the session.
     */
    public void onCreate() {
        super.onCreate();

        // This might change, as we need to login the user, so maybe attempt to
        // get credentials if they've already logged in before, but if they've never logged
        // in, we cannot use this yet, so maybe add some sort of method later in this class
        // for logging people in
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "268293220984", //acountId
                "us-west-2:e01653c7-9aee-4237-bc20-50ede0c85edc", // Identity Pool ID
                "arn:aws:iam::268293220984:role/Cognito_ProjectGeckoUnauth_Role", //unauth role
                "arn:aws:iam::268293220984:role/Cognito_ProjectGeckoAuth_Role", //auth role
                Regions.US_WEST_2 // Region
        );
        s3Client = new AmazonS3Client(credentialsProvider);
    }

    public CognitoCachingCredentialsProvider getCredentialsProvider() {
        return this.credentialsProvider;
    }

    public AmazonS3Client getS3Client() {
        return this.s3Client;
    }

}
