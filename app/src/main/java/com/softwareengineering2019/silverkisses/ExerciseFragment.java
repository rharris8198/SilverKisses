package com.softwareengineering2019.silverkisses;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.softwareengineering2019.silverkisses.MapsActivity.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;


public class ExerciseFragment extends Fragment implements OnMapReadyCallback {
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 100;
    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;

    View myView;

    private GoogleMap mMap;
    private MapView mapView;
    private PolylineOptions polylineOptions;
    private Location currentUserLocation;
    private Location lastUserLocation;
    private LocationManager locationManager;
    private LocationCallback locationCallback;


    private boolean mLocationPermissionGranted;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private Button startButton;
    private Button finishButton;
    private TextView timerView;
    private  TextView distanceView;

    private boolean trackingLocation;
    private boolean paused;



    //vars for time and distance
    private long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L ;
    Handler timeHandler;
    private int Seconds, Minutes, MilliSeconds ;
    private double distance;


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
        polylineOptions = new PolylineOptions();
        polylineOptions.width(12);
        polylineOptions.color(Color.RED);
        polylineOptions.geodesic(true);

        // Gets to GoogleMap from the MapView and does initialization stuff
        mapView.getMapAsync(this);


        GetFountainsTask fountainsTask = new GetFountainsTask();
        fountainsTask.execute();

        startButton= myView.findViewById(R.id.startButton);
        finishButton= myView.findViewById(R.id.finishButton);
        timerView= myView.findViewById(R.id.timer);
        distanceView = myView.findViewById(R.id.distance);
        distance=0.0;
        timeHandler = new Handler();

        trackingLocation=false; //var to begin location tracking
        paused=true;
        finishButton.setEnabled(false);




        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackingLocation=true;
                paused=false;
                startButton.setEnabled(false);
                finishButton.setEnabled(true);
                StartTime = SystemClock.uptimeMillis();
                timeHandler.postDelayed(timeRunnable, 0);
                finishButton.setText("Pause");
                lastUserLocation = currentUserLocation;

            }
        });
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(paused==false) {
                    paused=true;
                    trackingLocation = false;
                    TimeBuff += MillisecondTime;
                    timeHandler.removeCallbacks(timeRunnable);
                    startButton.setEnabled(true);
                    startButton.setText("Resume");
                    finishButton.setText("Finish");
                }else if(paused ==true){
                    MillisecondTime = 0L ;
                    StartTime = 0L ;
                    TimeBuff = 0L ;
                    UpdateTime = 0L ;
                    Seconds = 0 ;
                    Minutes = 0 ;
                    MilliSeconds = 0 ;
                    distance=0;
                    distanceView.setText("0.00");
                    timerView.setText("00:00");
                    startButton.setText("Start");
                    finishButton.setText("Pause");
                    finishButton.setEnabled(false);
                    mMap.clear();
                    distanceView.setText("0.00");
                    lastUserLocation = null;
                    GetFountainsTask fountainsTask = new GetFountainsTask();
                    fountainsTask.execute();


                }
            }
        });






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
                if(trackingLocation)
                    getDeviceLocation();
                handler.postDelayed(this, 2000);
            }
        }, 2000);  //the time is in miliseconds



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
                currentUserLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        //Log.d("GETTING LOCATION","LOCATION");
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
                            currentUserLocation = (Location) task.getResult();
                            assert currentUserLocation != null;
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(currentUserLocation.getLatitude(),
                                            currentUserLocation.getLongitude()), 17));
                            if(lastUserLocation==null){
                                lastUserLocation=currentUserLocation;
                            }else if(!lastUserLocation.equals(currentUserLocation)){
                               polylineOptions.add(new LatLng(currentUserLocation.getLatitude(),
                                       currentUserLocation.getLongitude()));
                                mMap.addPolyline(polylineOptions);

                                distance= distance+ (currentUserLocation.distanceTo(lastUserLocation)/1609.344);

                                distanceView.setText(String.format("%.2f",distance));
                            }

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





    public class GetFountainsTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();


        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL("https://data.cityofnewyork.us/api/views/bevm-apmm/rows.json");
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //Log.d("Result: ",result);
            try {
                JSONObject jObject = new JSONObject(result);
                JSONArray jArray = jObject.getJSONArray("data");
                //Log.d("Data: ",jArray.toString());
                for (int i=0; i < jArray.length(); i++)
                {
                    try {
                        //JSONObject oneObject = jArray.getJSONObject(i);

                        // Pulling items from the array

                        String point = jArray.getJSONArray(i).getString(9);
                       // Log.d("Point: ",point);

                        mMap.addMarker(new MarkerOptions().position(convertStringToPoint(point)).title("Water Fountain"));


                    } catch (JSONException e) {
                        // Oops
                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    public Runnable timeRunnable = new Runnable() {

        public void run() {
            MillisecondTime = SystemClock.uptimeMillis() - StartTime;

            UpdateTime = TimeBuff + MillisecondTime;

            Seconds = (int) (UpdateTime / 1000);

            Minutes = Seconds / 60;

            Seconds = Seconds % 60;

            MilliSeconds = (int) (UpdateTime % 1000);

            timerView.setText("" + Minutes + ":"
                    + String.format("%02d", Seconds) /*+ ":"
                    + String.format("%03d", MilliSeconds)*/);

            timeHandler.postDelayed(this, 0);
        }

    };

    public  LatLng convertStringToPoint(String s){
        String p = s.substring(s.indexOf("("));
        int separator = p.indexOf(' ');
        //Log.d("Sep",Integer.toString(separator));
        String longitude= p.substring(1,separator);
        String latitude = p.substring(separator,p.indexOf(")"));


        LatLng point = new LatLng(
                Double.parseDouble(latitude),
                Double.parseDouble(longitude)
                );

        return point;
    }





}


