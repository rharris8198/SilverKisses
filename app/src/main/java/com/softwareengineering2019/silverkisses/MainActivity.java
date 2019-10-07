package com.softwareengineering2019.silverkisses;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.Run:
                        Toast.makeText(MainActivity.this, "Run", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.Log:
                        Toast.makeText(MainActivity.this, "Log", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.Settings:
                        Toast.makeText(MainActivity.this, "Settings", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.More:
                        Toast.makeText(MainActivity.this, "More", Toast.LENGTH_SHORT).show();
                        break;
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
