package com.softwareengineering2019.silverkisses;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class MoreFragment extends Fragment {
    private Button logOutBtn;

    View myView;

    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.more_layout, container, false);
        logOutBtn = myView.findViewById(R.id.logOut);
        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUserAccount();
            }
        });
        return myView;
    }

    private void logoutUserAccount(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        Toast.makeText(getActivity(), "Logout Successfully!", Toast.LENGTH_SHORT).show();
    }
}
