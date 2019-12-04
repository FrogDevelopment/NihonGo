package fr.frogdevelopment.nihongo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreenActivity extends Activity {

    // Splash screen timer
    private static final long SPLASH_TIME_OUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen_activity);

        new Handler().postDelayed(() -> {
            // This method will be executed once the timer is over
            // Start your app main activity
            Intent i = new Intent(SplashScreenActivity.this, MainActivity.class);
            startActivity(i);

            // close this activity
            finish();
        }, SPLASH_TIME_OUT);
    }

}
