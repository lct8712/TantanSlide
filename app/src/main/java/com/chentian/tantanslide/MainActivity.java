package com.chentian.tantanslide;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.chentian.tantanslide.widget.SlideCardContainer;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SlideCardContainer slideCardContainer = findViewById(R.id.slide_card_container);
        slideCardContainer.loadData();
    }
}
