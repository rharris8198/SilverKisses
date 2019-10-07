package com.softwareengineering2019.silverkisses;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import static com.softwareengineering2019.silverkisses.MapsActivity.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;


public class ExerciseFragment extends Fragment implements OnMapReadyCallback {
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 100;

    View myView;

    private GoogleMap mMap;
    private MapView mapView;
    private Location mLastKnownLocation;
    private LocationManager locationManager;
    private LocationCallback locationCallback;


    private boolean mLocationPermissionGranted;
    private FusedLocationProviderClient mFusedLocationProviderClient;



    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getLocationPermission();
        mFusedLocationProviderClient = new FusedLocationProviderClient(getContext());



        locationManager = (LocationManager) this.getActivity().getSystemService(Context.LOCATION_SERVICE);




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

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        updateLocationUI();


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("Hndler","HAndler");
                getDeviceLocation();
                handler.postDelayed(this, 5000);
            }
        }, 5000);  //the time is in miliseconds



        // Add a marker in Sydney and move the camera
        // LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


        getDeviceLocation();

    }

    private void updateLocationUI() {
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                //Log.d("LOCATION PERM: ","true");
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
              //  Log.d("LOCATION PERM: ","false");
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        Log.d("GETTING LOCATION","LOCATION");
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this.getActivity(), new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = (Location) task.getResult();
                            assert mLastKnownLocation != null;
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), 17));
                        } else {
                            Log.d("Current location: ", "Current location is null. Using defaults.");

                            Log.e("exception", "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom( new LatLng(-34, 151), 0));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }




}


