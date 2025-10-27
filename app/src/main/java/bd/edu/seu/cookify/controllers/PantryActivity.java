package bd.edu.seu.cookify.controllers;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import bd.edu.seu.cookify.R;
import bd.edu.seu.cookify.models.Recipe;
import bd.edu.seu.cookify.adapters.PantryItemAdapter;
import bd.edu.seu.cookify.adapters.RecipeClickAdapter;
import bd.edu.seu.cookify.navigate.NavigationHelper;
import bd.edu.seu.cookify.utils.RecipeMapper;

public class PantryActivity extends AppCompatActivity {

    // Layouts for Premium and Non-Premium users
    private LinearLayout layoutPremium, layoutNonPremium;
    private EditText editItem;          // Input field to add pantry items
    private ImageButton buttonAdd;      // Button to add new item
    private RecyclerView recyclerPantry, recyclerRecipes; // Lists to display pantry items & suggested recipes

    // List to hold pantry items locally
    private final List<String> pantryItems = new ArrayList<>();
    private PantryItemAdapter pantryAdapter;    // Adapter to display pantry items
    private RecipeClickAdapter recipeAdapter;   // Adapter to display suggested recipes

    // Firebase authentication and Firestore database
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if user is logged in
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            NavigationHelper.navigate(this, LoginActivity.class, true);
            return; // Stop execution if not logged in
        }

        // Bind XML views to Java objects
        layoutPremium = findViewById(R.id.layoutPremium);
        layoutNonPremium = findViewById(R.id.layoutNonPremium);
        editItem = findViewById(R.id.editItem);
        buttonAdd = findViewById(R.id.buttonAdd);
        recyclerPantry = findViewById(R.id.recyclerPantry);
        recyclerRecipes = findViewById(R.id.recyclerRecipes);

        // Bottom navigation buttons
        ImageView navHome = findViewById(R.id.navHome);
        ImageView navBrowse = findViewById(R.id.navBrowse);
        ImageView navSubstitute = findViewById(R.id.navSubstitute);
        ImageView navPantry = findViewById(R.id.navPantry);

        // Set click listeners for bottom navigation
        navHome.setOnClickListener(v -> NavigationHelper.navigate(this, HomeActivity.class, true));
        navBrowse.setOnClickListener(v -> NavigationHelper.navigate(this, BrowseActivity.class, true));
        navSubstitute.setOnClickListener(v -> NavigationHelper.navigate(this, SubstituteActivity.class, true));
        navPantry.setOnClickListener(v -> {}); // Already in Pantry, do nothing

        // Set layout managers for RecyclerViews
        recyclerPantry.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecipes.setLayoutManager(new LinearLayoutManager(this));

        // Initialize pantry adapter
        pantryAdapter = new PantryItemAdapter(pantryItems);
        pantryAdapter.setOnItemRemovedListener(() -> loadSuggestedRecipes()); // Refresh recipes if item removed
        recyclerPantry.setAdapter(pantryAdapter);

        // Initialize recipe adapter with click listener to open details
        recipeAdapter = new RecipeClickAdapter(new ArrayList<>(), recipe -> {
            android.content.Intent i = new android.content.Intent(this, RecipeDetailsActivity.class);
            i.putExtra("name", recipe.getName());
            i.putExtra("category", recipe.getCategory());
            i.putExtra("imageUrl", recipe.getImageUrl());
            startActivity(i); // Open RecipeDetailsActivity
        });
        recyclerRecipes.setAdapter(recipeAdapter);

        // Check if user is premium and initialize UI
        checkPremiumAndInit();
    }

    // Check if user is premium and configure UI accordingly
    private void checkPremiumAndInit() {
        clearPantryData(); // Remove previous pantry & recipe data

        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            boolean isPremium = doc.exists() && Boolean.TRUE.equals(doc.getBoolean("isPremium"));

            // Show premium layout or non-premium layout
            layoutPremium.setVisibility(isPremium ? View.VISIBLE : View.GONE);
            layoutNonPremium.setVisibility(isPremium ? View.GONE : View.VISIBLE);

            if (isPremium) {
                wirePremiumUi(uid); // Set up add button for premium
                loadPantry(uid);    // Load pantry items
            } else {
                // For non-premium users, allow upgrade
                android.widget.Button btnUpgrade = findViewById(R.id.buttonUpgrade);
                btnUpgrade.setOnClickListener(v -> upgradeToPremium(uid));
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to check premium", Toast.LENGTH_SHORT).show();
            layoutPremium.setVisibility(View.GONE);
            layoutNonPremium.setVisibility(View.VISIBLE);
        });
    }

    // Clear pantry items and suggested recipes
    private void clearPantryData() {
        pantryItems.clear();
        pantryAdapter.notifyDataSetChanged();
        recipeAdapter.updateList(new ArrayList<>());
    }

    // Upgrade user to premium
    private void upgradeToPremium(String uid) {
        db.collection("users").document(uid)
                .update("isPremium", true)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Upgraded to Premium!", Toast.LENGTH_SHORT).show();
                    layoutNonPremium.setVisibility(View.GONE);
                    layoutPremium.setVisibility(View.VISIBLE);
                    wirePremiumUi(uid); // Enable premium features
                    loadPantry(uid);    // Load pantry
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Upgrade failed", Toast.LENGTH_SHORT).show());
    }

    // Wire the add button for premium users
    private void wirePremiumUi(String uid) {
        buttonAdd.setOnClickListener(v -> {
            String item = editItem.getText().toString().trim();
            if (item.equals("")) return; // Ignore empty input
            addPantryItem(uid, item);
        });
    }

    // Add a pantry item to Firestore and local list
    private void addPantryItem(String uid, String item) {
        DocumentReference doc = db.collection("pantryItems").document(uid);
        doc.set(java.util.Collections.singletonMap("items", FieldValue.arrayUnion(item)),
                        com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    editItem.setText(""); // Clear input
                    if (!pantryItems.contains(item)) {
                        pantryItems.add(item); // Add locally
                        pantryAdapter.notifyItemInserted(pantryItems.size() - 1);
                        loadSuggestedRecipes(); // Refresh suggested recipes
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add", Toast.LENGTH_SHORT).show());
    }

    // Load pantry items from Firestore
    private void loadPantry(String uid) {
        db.collection("pantryItems").document(uid).get().addOnSuccessListener(snap -> {
            pantryItems.clear();
            List<String> items = (List<String>) snap.get("items");
            if (items != null) pantryItems.addAll(items);
            pantryAdapter.notifyDataSetChanged();
            loadSuggestedRecipes(); // Update recipe suggestions
        });
    }

    // Load suggested recipes based on current pantry items
    private void loadSuggestedRecipes() {
        if (pantryItems.isEmpty()) {
            recipeAdapter.updateList(new ArrayList<>());
            return;
        }

        android.util.Log.d("PantryActivity", "Loading suggested recipes for pantry items: " + pantryItems);

        // Map pantry items to recipes using utility class
        List<Recipe> matches = RecipeMapper.getMappedRecipes(pantryItems);

        android.util.Log.d("PantryActivity", "Total matches found: " + matches.size());
        recipeAdapter.updateList(matches);

        // Show feedback to user
        if (matches.isEmpty()) {
            Toast.makeText(this, "No recipes found for your pantry items", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Found " + matches.size() + " suggested recipes", Toast.LENGTH_SHORT).show();
        }
    }
}