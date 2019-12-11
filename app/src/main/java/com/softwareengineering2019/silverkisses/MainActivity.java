package com.softwareengineering2019.silverkisses;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        final Fragment exerciseFragment = new ExerciseFragment();
        final Fragment logFragment = new LogFragment();
        final Fragment settingsFragment = new UserProfileFragment();
       // final Fragment moreFragment = new MoreFragment();
        final FragmentManager fm = getSupportFragmentManager();
        final Fragment[] active = {exerciseFragment};
        fm.beginTransaction().add(R.id.fragment, exerciseFragment, "1").commit();
        fm.beginTransaction().add(R.id.fragment, logFragment, "2").hide(logFragment).commit();
        fm.beginTransaction().add(R.id.fragment, settingsFragment, "3").hide(settingsFragment).commit();
       // fm.beginTransaction().add(R.id.fragment, moreFragment, "4").hide(moreFragment).commit();


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.Run:
                        //Toast.makeText(MainActivity.this, "Run", Toast.LENGTH_SHORT).show();
                        fm.beginTransaction().hide(active[0]).show(exerciseFragment).commit();
                        active[0] = exerciseFragment;
                        return true;

                    case R.id.Log:
                        Toast.makeText(MainActivity.this, "Log", Toast.LENGTH_SHORT).show();

                        fm.beginTransaction().hide(active[0]).show(logFragment).commit();
                        active[0] = logFragment;
                        return true;
                    case R.id.Settings:
                        Toast.makeText(MainActivity.this, "MyProfile", Toast.LENGTH_SHORT).show();
                        fm.beginTransaction().hide(active[0]).show(settingsFragment).commit();
                        active[0] = settingsFragment;
                        return true;

//                    case R.id.More:
//                        Toast.makeText(MainActivity.this, "More", Toast.LENGTH_SHORT).show();
//                        fm.beginTransaction().hide(active[0]).show(moreFragment).commit();
//                        active[0] = moreFragment;
//
//                        return true;
                }
                return true;
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();


    }


}
