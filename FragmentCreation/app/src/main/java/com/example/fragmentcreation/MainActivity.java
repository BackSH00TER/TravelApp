package com.example.fragmentcreation;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;


public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragContainer, new SampleFragment());
        fragmentTransaction.commit();
    }

    public void switchToFragmentOne(View view) {
        Fragment fragment = new SampleFragment();
        switchFragment(fragment);
    }

    public void switchToFragmentTwo(View view) {
        Fragment fragment = new SampleFragmentTwo();
        switchFragment(fragment);
    }

    public void switchToFragmentThree(View view) {
        Fragment fragment = new SampleFragmentThree();
        switchFragment(fragment);
    }

    public void switchFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragContainer, fragment, fragment.toString());
        fragmentTransaction.addToBackStack(fragment.toString());
        fragmentTransaction.commit();
    }
}
