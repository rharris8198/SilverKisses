package com.softwareengineering2019.silverkisses;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
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
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
    private Button submitBathroomButton;
    private TextView timerView;
    private  TextView distanceView;

    private  Button waterBreakButton;
    private  Button bathroomBreakButton;
    private  Button clearRouteButton;
    private Polyline helperRoute;

    private boolean trackingLocation;
    private boolean paused;

    private ArrayList<Bathroom> bathrooms;
    private ArrayList<Bathroom> fountains;//fountains saved as bathrooms for simplicity

    private  ArrayList<String> ratedLocations;


    //vars for time and distance
    private long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L ;
    Handler timeHandler;
    private int Seconds, Minutes, MilliSeconds ;
    private double distance;

    Dialog myDialog;

    FirebaseUser user;
    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        getLocationPermission();
        user = FirebaseAuth.getInstance().getCurrentUser();
        mFusedLocationProviderClient = new FusedLocationProviderClient(getContext());
        bathrooms= new ArrayList<Bathroom>();
        fountains = new ArrayList<Bathroom>();
        ratedLocations = new ArrayList<String>();


        locationManager = (LocationManager) this.getActivity().getSystemService(Context.LOCATION_SERVICE);





        myView = inflater.inflate(R.layout.fragment_exercise, container, false);
        myDialog = new Dialog(this.getContext());



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
        getBathrooms();
        getRatedLocations();


        startButton= myView.findViewById(R.id.startButton);
        finishButton= myView.findViewById(R.id.finishButton);
        timerView= myView.findViewById(R.id.timer);
        distanceView = myView.findViewById(R.id.distance);
        submitBathroomButton= myView.findViewById(R.id.submitBathroomButton);
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
                    saveWorkout();
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
                    polylineOptions = new PolylineOptions();
                    polylineOptions.width(12);
                    polylineOptions.color(Color.RED);
                    polylineOptions.geodesic(true);

                    distanceView.setText("0.00");

                    GetFountainsTask fountainsTask = new GetFountainsTask();
                    fountainsTask.execute();
                    getBathrooms();


                }
            }
        });

        submitBathroomButton.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        submitBathroomLocation();
                                                    }
                                                }
        );

        helperRoute = null;
        waterBreakButton = myView.findViewById(R.id.waterBreakButton);
        bathroomBreakButton = myView.findViewById(R.id.bathroomBreakButton);
        clearRouteButton = myView.findViewById(R.id.clearRouteButton);


        clearRouteButton.setEnabled(false);

        bathroomBreakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng nearest = getNearestBathroom();
                displayRoute(nearest);

            }
        });

        waterBreakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng nearest = getNearestFountain();
                displayRoute(nearest);
            }
        });

        clearRouteButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(helperRoute!=null) {
                            helperRoute.remove();
                        }
                        bathroomBreakButton.setEnabled(true);
                        waterBreakButton.setEnabled(true);
                        clearRouteButton.setEnabled(false);

                    }
                }
        );



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
        getDeviceLocation();
        updateLocationUI();


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                //Log.d("Hndler","HAndler");
                if(trackingLocation)
                    getDeviceLocation();
                handler.postDelayed(this, 2000);
            }
        }, 2000);  //the time is in miliseconds


        // Add a marker at Pace
        InfoWindowData info = new InfoWindowData();
        info.setName("Pace Water Fountain");
        info.setRating(0);
        info.setBathroom(null);
        LatLng pace = new LatLng(41.127707, -73.8065);
        mMap.addMarker(new MarkerOptions().position(pace).title("Pace University Water Fountain")).setTag(info);
        fountains.add(new Bathroom(pace,"Pace Water Fountain",0));

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(pace));




        //Overriding clicks on Markers
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.i("onMarkerClick", "Marker clicked executing");
                LatLng position = marker.getPosition();
                InfoWindowData tag = (InfoWindowData) marker.getTag();
                if(tag.getName() == "Bathroom"){
                    showPopup(myView,position,marker.getTitle(),tag.getBathroom());
                }else {
                    showPopup(myView, position, marker.getTitle());
                }
                return false;
            }

        });

    }


    //show popup for NYC database Fountains unrateable
    public void showPopup( View v, final LatLng position, String title) {
        TextView txtclose;
        TextView titleTxt;
        TextView rateLabel;
        Button routeBtn;
        Button plusBtn;
        Button minusBtn;



        myDialog.setContentView(R.layout.bathroominfowindowlayout);
        txtclose =(TextView) myDialog.findViewById(R.id.txtclose);
        txtclose.setText("X");


        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });

        titleTxt = myDialog.findViewById(R.id.title);
        titleTxt.setText((CharSequence) title);

        routeBtn = myDialog.findViewById(R.id.routebutton);
        routeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
                displayRoute(position);
            }
        });

        plusBtn = myDialog.findViewById(R.id.btnplus);
        plusBtn.setVisibility(View.GONE);

        minusBtn = myDialog.findViewById(R.id.btnminus);
        minusBtn.setVisibility(View.GONE);

        rateLabel = myDialog.findViewById(R.id.rateMessage);
        rateLabel.setVisibility(View.GONE);



        myDialog.getWindow().setGravity(Gravity.TOP);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }

    //show popup for rateable bathrooms
    public void showPopup(View v, final LatLng position, final String title, final Bathroom bathroom) {
        TextView txtclose;
        TextView titleTxt;
        final TextView rateLabel;
        Button routeBtn;
        final Button plusBtn;
        final Button minusBtn;



        myDialog.setContentView(R.layout.bathroominfowindowlayout);
        txtclose =(TextView) myDialog.findViewById(R.id.txtclose);
        txtclose.setText("X");

        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });

        titleTxt = myDialog.findViewById(R.id.title);
        titleTxt.setText((CharSequence) title);

        routeBtn = myDialog.findViewById(R.id.routebutton);
        routeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
                displayRoute(position);
            }
        });

        rateLabel = myDialog.findViewById(R.id.rateMessage);
        plusBtn = myDialog.findViewById(R.id.btnplus);
        minusBtn = myDialog.findViewById(R.id.btnminus);
        //check if user has already rated this location
        if(ratedLocations.contains(bathroom.getName())) {
            //already rated
            plusBtn.setVisibility(View.INVISIBLE);
            minusBtn.setVisibility(View.INVISIBLE);
            rateLabel.setText("Thanks for rating this location");
        }else {
            //unrated
            plusBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    rateBathroom(1, bathroom);
                    rateLabel.setText("Thanks for rating this location");
                    plusBtn.setVisibility(View.INVISIBLE);
                    minusBtn.setVisibility(View.INVISIBLE);

                }
            });



            minusBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rateBathroom(-1, bathroom);
                    rateLabel.setText("Thanks for rating this location");
                    plusBtn.setVisibility(View.GONE);
                    minusBtn.setVisibility(View.GONE);

                }
            });

        }


        myDialog.getWindow().setGravity(Gravity.TOP);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }

    public void rateBathroom(final int rating, final Bathroom bathroom ){


        final DatabaseReference ref= FirebaseDatabase.getInstance()
                .getReference("bathrooms")
                .child(bathroom.getName());


        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //update rating of location in db
                Bathroom dbBathroom = dataSnapshot.getValue(Bathroom.class);
                dbBathroom.setRating(dbBathroom.getRating()+rating);
                ref.setValue(dbBathroom);
                ratedLocations.add(dbBathroom.getName());

                //add to list of locations rated by this user
                DatabaseReference addRating = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("rated");
                addRating.setValue(ratedLocations);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }





    public void displayRoute(LatLng position){
        // Getting URL to the Google Directions API
        if (currentUserLocation!=null) {
            Log.d("currentUserLocation", currentUserLocation.toString());
            String str_origin = "origin=" + currentUserLocation.getLatitude() + "," + currentUserLocation.getLongitude();

            String str_dest = "destination=" + position.latitude + "," + position.longitude;
            String sensor = "sensor=false";
            String parameters = str_origin + "&" + str_dest + "&" + sensor;
            String output = "json";
            String api_key = "AIzaSyAM0FU1_FBFuNNwP2JPiD1bBRhSd1LhDs8";
            String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" +api_key;
            Log.d("onMapClick", url.toString());
            FetchUrl FetchUrl = new FetchUrl();
            FetchUrl.execute(url);
        }
    }

    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }


        private String downloadUrl(String strUrl) throws IOException {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(strUrl);

                // Creating an http connection to communicate with url
                urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb = new StringBuffer();

                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                data = sb.toString();
                Log.d("downloadUrl", data.toString());
                br.close();

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            } finally {
                iStream.close();
                urlConnection.disconnect();
            }
            return data;

        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }


    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.BLUE);

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                if(helperRoute!=null) {
                    helperRoute.remove();
                }
               helperRoute= mMap.addPolyline(lineOptions);
               // bathroomBreakButton.setEnabled(false);
                //waterBreakButton.setEnabled(false);
                clearRouteButton.setEnabled(true);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }


    }





    private void updateLocationUI() {
        mMap.getUiSettings().setAllGesturesEnabled(true);
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


    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationClient;
    private void getDeviceLocation() {
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(30 * 1000)
                .setFastestInterval(5 * 1000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // Set the map's camera position to the current location of the device.
                currentUserLocation = (locationResult.getLastLocation());
                assert currentUserLocation != null;
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(currentUserLocation.getLatitude(),
                                currentUserLocation.getLongitude()), 17));
                if(lastUserLocation== null){
                    lastUserLocation = (locationResult.getLastLocation() );



                }else if(!lastUserLocation.equals(currentUserLocation)){
                    if(trackingLocation) {
                        polylineOptions.add(new LatLng(currentUserLocation.getLatitude(),
                                currentUserLocation.getLongitude()));
                        mMap.addPolyline(polylineOptions);

                        distance = distance + (currentUserLocation.distanceTo(lastUserLocation) / 1609.344);

                        distanceView.setText(String.format("%.2f", distance));
                    }
                    lastUserLocation.setLatitude(currentUserLocation.getLatitude());
                    lastUserLocation.setLongitude(currentUserLocation.getLongitude());
                }

            }
        };

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity().getApplicationContext());
        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);




        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();

                locationResult.addOnCompleteListener(this.getActivity(), new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            currentUserLocation = ((Location) task.getResult());
                            assert currentUserLocation != null;
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(currentUserLocation.getLatitude(),
                                            currentUserLocation.getLongitude()), 17));
                            if(lastUserLocation== null){
                                lastUserLocation = ((Location) task.getResult());



                            }else if(!lastUserLocation.equals(currentUserLocation)){
                               polylineOptions.add(new LatLng(currentUserLocation.getLatitude(),
                                       currentUserLocation.getLongitude()));
                                mMap.addPolyline(polylineOptions);

                                distance= distance+ (currentUserLocation.distanceTo(lastUserLocation)/1609.344);

                                distanceView.setText(String.format("%.2f",distance));
                                lastUserLocation.setLatitude(currentUserLocation.getLatitude());
                                lastUserLocation.setLongitude(currentUserLocation.getLongitude());
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
                   // Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

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


                        MarkerOptions markerOptions = new MarkerOptions()
                                .position((convertStringToPoint(point)))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));


                        InfoWindowData info = new InfoWindowData();
                        info.setName("NYC Water Fountain");
                        info.setRating(0);
                        info.setBathroom(null);//null because will not be rated


                        fountains.add(new Bathroom(convertStringToPoint(point),"NYC Water Fountain",0));


                        Marker marker = mMap.addMarker(markerOptions);

                        marker.setTag(info);

                       // mMap.addMarker(new MarkerOptions().position(convertStringToPoint(point)).title("Water Fountain"));


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



    public void saveWorkout(){
        DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        Date date = new Date();
        Workout currentWorkout= new Workout(distance,timerView.getText().toString(),dateFormat.format(date));
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        ref.child("workouts").child(currentWorkout.getDate()).setValue(currentWorkout);

    }

    public void getBathrooms(){
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("bathrooms");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Log.d("CHILD: ", child.getValue().toString());
                    Bathroom bath = child.getValue(Bathroom.class);
                    if (bath.getRating() > -5) {
                        bathrooms.add(bath);
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(new LatLng(bath.getLat(), bath.getLng())).title("Bathroom")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        // .snippet("Rating: " + bath.getRating());

                        InfoWindowData info = new InfoWindowData();
                        info.setName("Bathroom");
                        info.setRating(bath.getRating());
                        info.setBathroom(bath);


                        Marker marker = mMap.addMarker(markerOptions);

                        marker.setTag(info);
                        //marker.showInfoWindow();
                    }
                }
                Log.d("bathrooms: ", bathrooms.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void submitBathroomLocation(){
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Bathroom submission = new Bathroom(new LatLng(currentUserLocation.getLatitude(),currentUserLocation.getLongitude()),dateFormat.format(date) + " " + user.getUid(),0);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("bathrooms").child(submission.getName());
        ref.setValue(submission);

    }

    public void getRatedLocations(){
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("rated");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Log.d("CHILD: ", child.getValue().toString());
                    ratedLocations.add((String)child.getValue());
                }
                Log.d("rated locations: ", ratedLocations.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public LatLng getNearestBathroom(){
        Bathroom nearest= bathrooms.get(0);
        Bathroom current;
        Location currentBathroomLocation = new Location("");
        currentBathroomLocation.setLatitude(nearest.getLat());
        currentBathroomLocation.setLongitude(nearest.getLng());
        double shortestDistance=  currentUserLocation.distanceTo(currentBathroomLocation);
        for(int i=0;i<bathrooms.size();i++) {
            current = bathrooms.get(i);
            currentBathroomLocation.setLatitude(current.getLat());
            currentBathroomLocation.setLongitude(current.getLng());

            if(currentUserLocation.distanceTo(currentBathroomLocation)<shortestDistance) {
                shortestDistance = currentUserLocation.distanceTo(currentBathroomLocation);
                nearest=current;
            }


        }

        return new LatLng(nearest.getLat(),nearest.getLng());
    }
    public LatLng getNearestFountain(){
        Bathroom nearest= fountains.get(0);
        Bathroom current;
        Location currentFountainLocation = new Location("");
        currentFountainLocation.setLatitude(nearest.getLat());
        currentFountainLocation.setLongitude(nearest.getLng());
        double shortestDistance=  currentUserLocation.distanceTo(currentFountainLocation);
        for(int i=0;i<bathrooms.size();i++) {
            current = bathrooms.get(i);
            currentFountainLocation.setLatitude(current.getLat());
            currentFountainLocation.setLongitude(current.getLng());

            if(currentUserLocation.distanceTo(currentFountainLocation)<shortestDistance) {
                shortestDistance = currentUserLocation.distanceTo(currentFountainLocation);
                nearest=current;
            }


        }

        return new LatLng(nearest.getLat(),nearest.getLng());
    }








}


