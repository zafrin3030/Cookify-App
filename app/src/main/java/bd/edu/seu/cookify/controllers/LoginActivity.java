package bd.edu.seu.cookify.controllers;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.inputmethod.EditorInfo;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;

import bd.edu.seu.cookify.R;
import bd.edu.seu.cookify.navigate.NavigationHelper;

public class LoginActivity extends AppCompatActivity {

    // Email and password input fields
    private EditText editEmail, editPassword;
    // Normal login button
    private Button buttonLogin;
    // Google login button (icon)
    private ImageView googleLoginIcon;
    // Forgot password link + sign up link
    private TextView textForgotPassword, textSignUp;
    // Firebase authentication
    private FirebaseAuth mAuth;
    // Google Sign-In client (Google API handler)
    private GoogleSignInClient googleSignInClient;
    // Launcher to handle result when user picks Google account
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(bd.edu.seu.cookify.R.layout.activity_login); // Attach the login layout

        // Get Firebase authentication instance
        mAuth = FirebaseAuth.getInstance();

        // Link Java variables with XML views
        bindViews();

        // Prepare Google sign-in options (request email + ID token)
        setupGoogleLogin();

        // Prepare launcher that handles Google sign-in result
        setupGoogleSignInLauncher();

        // Connect buttons, links, and text fields to their actions
        wireEvents();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // If user already logged in before → go straight to Home screen
        // false = don’t finish login activity (back button still works)
        if (mAuth.getCurrentUser() != null) {
            NavigationHelper.navigate(this, HomeActivity.class, false);
        }
    }

    private void bindViews() {
        // Grab all UI elements from XML
        editEmail = findViewById(bd.edu.seu.cookify.R.id.editEmail);
        editPassword = findViewById(bd.edu.seu.cookify.R.id.editPassword);
        buttonLogin = findViewById(bd.edu.seu.cookify.R.id.buttonLogin);
        googleLoginIcon = findViewById(bd.edu.seu.cookify.R.id.layoutSocial).findViewById(bd.edu.seu.cookify.R.id.googleIcon);
        textForgotPassword = findViewById(bd.edu.seu.cookify.R.id.textForgotPassword);
        textSignUp = findViewById(bd.edu.seu.cookify.R.id.textSignUp);
    }

    private void wireEvents() {
        // Normal email/password login
        buttonLogin.setOnClickListener(v -> attemptEmailLogin());

        // If user presses "Done" on keyboard while typing password → try login
        editPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptEmailLogin();
                return true;
            }
            return false;
        });

        // Google login icon clicked → launch Google sign-in flow with account picker
        googleLoginIcon.setOnClickListener(v -> {
            // Sign out first to force account picker
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                googleSignInLauncher.launch(googleSignInClient.getSignInIntent());
            });
        });

        // Forgot password → send reset email
        textForgotPassword.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                editEmail.setError("Enter a valid email");
                editEmail.requestFocus();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_SHORT).show();
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(this, "Error: " + errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Navigate to SignupActivity if user doesn’t have an account
        // false = don’t finish LoginActivity (so back button works)
        textSignUp.setOnClickListener(v ->
                NavigationHelper.navigate(this, SignupActivity.class, false)
        );
    }

    private void attemptEmailLogin() {
        // Validate email and password before login
        if (!validateEmailAndPassword()) return;

        // Get user input
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        // Show loading state
        setLoading(true);

        // Firebase login attempt
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    setLoading(false); // Re-enable button

                    if (task.isSuccessful()) {
                        // Ensure Firestore user doc exists with default isPremium if missing
                        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
                        if (uid != null) {
                            FirebaseFirestore.getInstance().collection("users").document(uid)
                                    .set(new java.util.HashMap<String, Object>() {{
                                        put("email", editEmail.getText().toString().trim());
                                    }}, com.google.firebase.firestore.SetOptions.merge());
                        }
                        NavigationHelper.navigate(this, HomeActivity.class, false);
                    } else {
                        // Show error if login failed
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Login failed";
                        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateEmailAndPassword() {
        // Simple checks for valid email and password length
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Enter a valid email");
            editEmail.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            editPassword.setError("At least 6 characters");
            editPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void setupGoogleLogin() {
        // Build Google sign-in options → request ID token + email
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(bd.edu.seu.cookify.R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Create Google SignIn client with the above options
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupGoogleSignInLauncher() {
        // Register a launcher to handle Google login result
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // If Google login intent succeeded → get account
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);

                            // Get Firebase credential from Google token
                            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

                            // Sign in with Firebase using Google credentials
                            mAuth.signInWithCredential(credential)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            // Success → ensure Firestore user doc exists
                                            String uid2 = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
                                            if (uid2 != null) {
                                                FirebaseFirestore.getInstance().collection("users").document(uid2)
                                                        .set(new java.util.HashMap<String, Object>() {{
                                                            put("email", account.getEmail());
                                                        }}, com.google.firebase.firestore.SetOptions.merge());
                                            }
                                            NavigationHelper.navigate(this, HomeActivity.class, false);
                                        } else {
                                            String errorMsg = task1.getException() != null ? task1.getException().getMessage() : "Google login failed";
                                            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } catch (ApiException e) {
                            // Google sign-in failed
                            String errorMsg = e.getMessage() != null ? e.getMessage() : "Google sign-in error";
                            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void setLoading(boolean loading) {
        // Disable/enable button + change text to show loading state
        buttonLogin.setEnabled(!loading);
        buttonLogin.setText(loading ? "Logging in..." : getString(R.string.login));
    }
}
