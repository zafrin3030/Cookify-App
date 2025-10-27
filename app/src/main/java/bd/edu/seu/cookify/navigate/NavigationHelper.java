package bd.edu.seu.cookify.navigate;

import android.app.Activity;
import android.content.Intent;

public class NavigationHelper {

    // 🔹 This method is used for switching from one Activity (screen) to another
    public static void navigate(Activity currentActivity, Class<?> targetActivity, boolean finishCurrent) {

        // Create an Intent to move from the current Activity to the target Activity
        Intent intent = new Intent(currentActivity, targetActivity);

        // Start the new Activity
        currentActivity.startActivity(intent);

        // If finishCurrent = true, then the current Activity will be closed
        // (so the user cannot go back to it by pressing the back button)
        if (finishCurrent) {
            currentActivity.finish();
        }
    }
}
