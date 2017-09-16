package com.huweiqiang.circleprogressbar;

import android.animation.ObjectAnimator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.huweiqiang.circleprogressbar.widget.CircleProgressBar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        CircleProgressBar progressBar1 = (CircleProgressBar) findViewById(R.id.progress1);
        CircleProgressBar progressBar2 = (CircleProgressBar) findViewById(R.id.progress2);
        CircleProgressBar progressBar3 = (CircleProgressBar) findViewById(R.id.progress3);
        CircleProgressBar progressBar4 = (CircleProgressBar) findViewById(R.id.progress4);

        ObjectAnimator.ofInt(progressBar1,"progress",0,90).setDuration(2000).start();
        ObjectAnimator.ofInt(progressBar2,"progress",0,65).setDuration(2000).start();
        ObjectAnimator.ofInt(progressBar3,"progress",125).setDuration(2000).start();
        ObjectAnimator.ofInt(progressBar4,"progress",0,356).setDuration(2000).start();

    }
}
