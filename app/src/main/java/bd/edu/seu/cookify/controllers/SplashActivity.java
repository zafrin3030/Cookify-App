package bd.edu.seu.cookify.controllers;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import bd.edu.seu.cookify.R;
import bd.edu.seu.cookify.navigate.NavigationHelper;

public class SplashActivity extends AppCompatActivity {

    // "Get Started" button on the splash screen
    private Button btnGetStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This makes the layout draw edge-to-edge (nice modern look, content goes under status bar/nav bar)
        EdgeToEdge.enable(this);

        // Attach the XML layout (activity_splash.xml) to this screen
        setContentView(R.layout.activity_splash);

        // Grab the "Get Started" button from the layout so we can work with it in code
        btnGetStarted = findViewById(R.id.getStartedButton);

        // When the button is clicked → move to the Login screen
        // true means finish() this activity, so user can’t come back to splash by pressing back
        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationHelper.navigate(SplashActivity.this, LoginActivity.class, true);
            }
        });
    }
}
