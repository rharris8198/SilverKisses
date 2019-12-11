package com.softwareengineering2019.silverkisses;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;


public class UserProfileFragment extends Fragment {
    Button logOutBtn;
    View myView;
    TextView name;
    TextView workoutsCompleted;
    TextView totalMiles;
    TextView totalDuration;
    TextView longestDistance;
    TextView longestDuration;
    FirebaseUser user;
    String fName;
    String lName;
    ArrayList<Workout> userWorkouts;

    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_user_profile, container, false);
        name = myView.findViewById(R.id.userName);
        workoutsCompleted = myView.findViewById(R.id.numWorkouts);
        totalMiles = myView.findViewById(R.id.numMiles);
        totalDuration = myView.findViewById(R.id.timeSpent);
        longestDistance = myView.findViewById(R.id.longestDistance);
        longestDuration = myView.findViewById(R.id.longestDuration);

        userWorkouts= new ArrayList<Workout>();
        user = FirebaseAuth.getInstance().getCurrentUser();

        getWorkouts();
        getData();

        logOutBtn = myView.findViewById(R.id.logOut);
        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUserAccount();
            }
        });

        return myView;
    }



    //get userName
    public void getData(){

        Log.d("PROFILE UID:", user.getUid());
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Log.d("KEY", dataSnapshot.getKey());
                fName = dataSnapshot.child("firstname").getValue(String.class);
                lName = dataSnapshot.child("lastname").getValue(String.class);
                name.setText(fName + " " + lName);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    private void logoutUserAccount(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        Toast.makeText(getActivity(), "Logout Successfully!", Toast.LENGTH_SHORT).show();
    }

    public void getWorkouts(){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("workouts");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Log.d("PROFILE WORKOUT: ", child.getValue().toString());

                    userWorkouts.add(child.getValue(Workout.class));

                }
                int numWorkouts = userWorkouts.size();
                if(numWorkouts>0){
                    Double totalDistance = 0.0;
                    Double maxDistance = userWorkouts.get(0).getDistance();
                    int totalSeconds =0;
                    int maxtime=0;

                    for(int i=0;i<userWorkouts.size();i++){
                        totalDistance += userWorkouts.get(i).getDistance();
                        if(userWorkouts.get(i).getDistance() > maxDistance)
                            maxDistance = userWorkouts.get(i).getDistance();
                        int minutes = Integer.parseInt(userWorkouts.get(i).getDuration().substring(0,2));
                        int seconds = Integer.parseInt(userWorkouts.get(i).getDuration().substring(3,5));
                        int time = (minutes*60) + seconds;
                        if(time>maxtime)
                            maxtime = time;
                        totalSeconds +=  time;
                    }
                    workoutsCompleted.setText(Integer.toString(numWorkouts));
                    totalMiles.setText(Double.toString(totalDistance));
                    longestDistance.setText(Double.toString(maxDistance));
                    int secs = totalSeconds%60;
                    totalDuration.setText((totalSeconds/60) +":" + String.format("%02d", secs));
                    secs = maxtime%60;
                    longestDuration.setText((maxtime/60) + ":" + String.format("%02d", secs));
                }else{
                    workoutsCompleted.setText(Integer.toString(numWorkouts));
                    totalMiles.setText("-");
                    longestDistance.setText("-");
                    totalDuration.setText("-" );
                    longestDuration.setText("-");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


}
