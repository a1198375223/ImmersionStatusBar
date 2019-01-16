package com.example.study;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.study.utils.StateBarUtil;

public class Main extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        StateBarUtil.fitSystemWindows(this, true);
        StateBarUtil.setTranslucentStatus(this);

        if (!StateBarUtil.setCommonUI(this, true)) {
            StateBarUtil.setStatusBarColor(this, 0x55000000);
        }
    }
}
