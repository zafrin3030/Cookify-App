package bd.edu.seu.cookify.controllers;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import bd.edu.seu.cookify.R;
import bd.edu.seu.cookify.navigate.NavigationHelper;

public class SignupActivity extends AppCompatActivity {

    // Input fields
    private EditText editEmail, editPassword, editConfirmPassword;
    // Button to trigger sign up
    private Button buttonSignUp;
    // Text link to go to sign-in screen
    private TextView textSignIn;
    // Firebase authentication object
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup); // Attach the signup layout

        // Get Firebase authentication instance (we’ll use it to create a new user)
        mAuth = FirebaseAuth.getInstance();

        // Find all UI components from the layout
        bindViews();

        // Connect button/text clicks to actions
        wireEvents();
    }

    private void bindViews() {
        // Hook up layout elements with Java variables
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        textSignIn = findViewById(R.id.textSignIn);
    }

    private void wireEvents() {
        // When user clicks "Sign Up" → try to register account
        buttonSignUp.setOnClickListener(v -> attemptSignup());

        // When user clicks "Already have an account? Sign in" → go to login page
        // false = don’t finish this activity (so back button still works)
        textSignIn.setOnClickListener(v ->
                NavigationHelper.navigate(this, LoginActivity.class, false)
        );
    }

    private void attemptSignup() {
        // First check if all inputs are valid
        if (!validateFields()) return;

        // Get values from input fields
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        // Disable the button temporarily to avoid multiple clicks
        buttonSignUp.setEnabled(false);
        buttonSignUp.setText("Creating account...");

        // Firebase call: try to create a new account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    // Re-enable button no matter success/failure
                    buttonSignUp.setEnabled(true);
                    buttonSignUp.setText("SIGN UP");

                    if (task.isSuccessful()) {
                        // Account creation worked 🎉
                        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();

                        // Ensure Firestore user document with isPremium=false exists
                        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
                        if (uid != null) {
                            java.util.Map<String, Object> data = new java.util.HashMap<>();
                            data.put("email", email);
                            data.put("isPremium", false);
                            FirebaseFirestore.getInstance().collection("users").document(uid).set(data, com.google.firebase.firestore.SetOptions.merge());
                        }

                        // Navigate to home screen (don’t kill this activity, so back works)
                        NavigationHelper.navigate(this, HomeActivity.class, false);
                    } else {
                        // Show error from Firebase if available
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Signup failed";
                        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateFields() {
        // Grab text from fields
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();

        // Check empty fields
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if email format is valid
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Enter a valid email");
            editEmail.requestFocus();
            return false;
        }

        // Check password length
        if (password.length() < 6) {
            editPassword.setError("Password must be at least 6 characters");
            editPassword.requestFocus();
            return false;
        }

        // Check if password and confirm password match
        if (!password.equals(confirmPassword)) {
            editConfirmPassword.setError("Passwords do not match");
            editConfirmPassword.requestFocus();
            return false;
        }

        // If everything is fine → return true
        return true;
    }
}
