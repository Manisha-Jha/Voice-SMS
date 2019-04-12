package com.android.learning.speechsms;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.race604.drawable.wave.WaveDrawable;

/**
 * First activity to be displayed to the user
 * Shows an animation
 */
public class SplashActivity extends AppCompatActivity {

    ImageView ivIcon;
    TextView tvAppName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);


        setContentView(R.layout.activity_splash);
//ok we will check tommorrow bye..
        ivIcon = ((ImageView)findViewById(R.id.ivSplash));
        tvAppName = findViewById(R.id.tvSplash);

        WaveDrawable drawable = new WaveDrawable(this, R.drawable.ic_record_voice_over_black_240dp);
        drawable.setIndeterminate(true);
        drawable.setWaveLength(750);

        ivIcon.setImageDrawable(drawable);
        WaveDrawable drawableText = new WaveDrawable(new ColorDrawable());
        drawableText.setIndeterminate(true);
        drawableText.setWaveLength(750);
        tvAppName.setBackground(drawableText);
        String strAppDetails = getString(R.string.app_name)+" "+ BuildConfig.VERSION_NAME;
        tvAppName.setText(strAppDetails);

        new Handler().postDelayed(new Runnable()
        {
            public void run()
            {
                SplashActivity.this.startActivity(new Intent(SplashActivity.this, MainActivity.class));
                SplashActivity.this.finish();
            }
        }, 2800L);

    }
}
