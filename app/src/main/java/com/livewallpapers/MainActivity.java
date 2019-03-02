package com.livewallpapers;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.livewallpapers.lwp.ManagerFallingHeartsLwp;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {
        new ManagerFallingHeartsLwp(this).set();
    }
}
