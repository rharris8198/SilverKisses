package com.softwareengineering2019.silverkisses;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

public class ExerciseFragment extends Fragment implements OnMapReadyCallback {
    View myView;

    private GoogleMap mMap;
    private MapView mapView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_exercise, container, false);

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) myView.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);


        // Gets to GoogleMap from the MapView and does initialization stuff
        mapView.getMapAsync(this);
        

        return myView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }
    @Override
     public void onPause() {
        super.onPause();
        mapView.onPause();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
