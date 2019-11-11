package com.softwareengineering2019.silverkisses;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class LogFragment extends Fragment{

    View myView;
    RecyclerView workoutList;
    ListAdapter adapter;
    //convert event into object
    ArrayList<Workout> userWorkouts;

    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_log, container, false);
        workoutList= myView.findViewById(R.id.workoutList);
        userWorkouts= new ArrayList<Workout>();



        workoutList.setLayoutManager(new LinearLayoutManager(getActivity().getBaseContext()));
        adapter= new ListAdapter(getActivity(), userWorkouts);

        workoutList.setAdapter(adapter);
        getWorkouts();


        return myView;
    }

    public void getWorkouts(){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("workouts");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Log.d("CHILD: ", child.getValue().toString());

                    userWorkouts.add(child.getValue(Workout.class));

                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}


