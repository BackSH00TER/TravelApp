package com.example.fragmentcreation;

/**
 * Created by ttyle on 3/21/2017.
 */
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SampleFragmentTwo extends Fragment{
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sample_fragment2, container, false);
        return view;
    }
}
