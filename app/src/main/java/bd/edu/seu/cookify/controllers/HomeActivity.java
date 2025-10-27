package bd.edu.seu.cookify.controllers;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

import bd.edu.seu.cookify.R;
import bd.edu.seu.cookify.adapters.RecipeClickAdapter;
import bd.edu.seu.cookify.models.Recipe;
import bd.edu.seu.cookify.navigate.NavigationHelper;

public class HomeActivity extends AppCompatActivity {

    // RecyclerView to show the list of recipes
    private RecyclerView recyclerRecipes;
    // Adapter to manage how recipes are displayed
    private RecipeClickAdapter adapter;
    // Keeps all recipes fetched from Firestore
    private final List<Recipe> allRecipes = new ArrayList<>();
    // Search bar input
    private EditText editSearch;
    // Holds the category buttons (All, Breakfast, etc.)
    private LinearLayout categoryTabs;
    // TextView to show when no recipes are available
    private TextView textEmpty;

    // Firebase Firestore instance
    private FirebaseFirestore db;
    // Stores which category button is currently selected
    private Button selectedCategoryButton = null;
    // Predefined categories
    private final String[] categories = {"All", "Breakfast", "Lunch", "Dinner", "Dessert"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(bd.edu.seu.cookify.R.layout.activity_home); // Set the layout file for this screen

        db = FirebaseFirestore.getInstance(); // Initialize Firestore database

        // Bind UI elements to variables
        recyclerRecipes = findViewById(bd.edu.seu.cookify.R.id.recyclerRecipes);
        editSearch = findViewById(bd.edu.seu.cookify.R.id.editSearch);
        categoryTabs = findViewById(bd.edu.seu.cookify.R.id.categoryTabs);
        textEmpty = findViewById(bd.edu.seu.cookify.R.id.textEmpty);

        // Setup RecyclerView adapter. When a recipe is clicked, open RecipeDetailsActivity
        adapter = new RecipeClickAdapter(allRecipes, recipe -> {
            Intent i = new Intent(this, RecipeDetailsActivity.class);
            i.putExtra("name", recipe.getName());
            i.putExtra("category", recipe.getCategory());
            i.putExtra("imageUrl", recipe.getImageUrl());
            startActivity(i);
        });

        recyclerRecipes.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecipes.setAdapter(adapter);

        setupCategoryTabs(); // Build the category buttons
        loadRecipes("All");  // Load all recipes initially

        // Logout button action
        findViewById(bd.edu.seu.cookify.R.id.iconLogout).setOnClickListener(v -> showLogoutDialog());

        // Add search listener: filters recipes as the user types
        editSearch.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBySearch(s.toString());
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}
        });

        // Bottom navigation actions
        findViewById(bd.edu.seu.cookify.R.id.navHome).setOnClickListener(v -> recyclerRecipes.smoothScrollToPosition(0));
        findViewById(bd.edu.seu.cookify.R.id.navBrowse).setOnClickListener(v ->
                NavigationHelper.navigate(this, BrowseActivity.class, false));
        findViewById(bd.edu.seu.cookify.R.id.navSubstitute).setOnClickListener(v ->
                NavigationHelper.navigate(this, SubstituteActivity.class, false));
        findViewById(bd.edu.seu.cookify.R.id.navPantry).setOnClickListener(v ->
                handlePantryNavigation());
    }

    // Builds the category tab buttons dynamically
    private void setupCategoryTabs() {
        categoryTabs.removeAllViews();

        for (String category : categories) {
            // Create a button for each category
            Button btn = new Button(this, null, android.R.attr.borderlessButtonStyle);
            btn.setText(category);
            btn.setAllCaps(false);
            btn.setPadding(36, 16, 36, 16);
            btn.setTextColor(getResources().getColor(bd.edu.seu.cookify.R.color.black));
            btn.setBackgroundResource(bd.edu.seu.cookify.R.drawable.bg_category_default);

            // Set layout margins for the button
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(12, 0, 12, 0);
            btn.setLayoutParams(params);

            // When a category is clicked, load that category’s recipes
            btn.setOnClickListener(v -> {
                if (selectedCategoryButton != null) {
                    selectedCategoryButton.setBackgroundResource(bd.edu.seu.cookify.R.drawable.bg_category_default);
                }
                btn.setBackgroundResource(bd.edu.seu.cookify.R.drawable.bg_selected_category);
                selectedCategoryButton = btn;
                loadRecipes(category);
            });

            // Highlight "All" category by default
            if ("All".equals(category) && selectedCategoryButton == null) {
                btn.setBackgroundResource(R.drawable.bg_selected_category);
                selectedCategoryButton = btn;
            }

            categoryTabs.addView(btn); // Add button to the layout
        }
    }

    // Loads recipes from Firestore for a specific category
    private void loadRecipes(String category) {
        textEmpty.setVisibility(View.GONE);

        // Firestore query: all recipes or filtered by category
        Query query = category.equals("All")
                ? db.collection("recipes")
                : db.collection("recipes").whereEqualTo("category", category);

        query.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("HomeActivity", "Failed to load recipes", task.getException());
                Toast.makeText(this, "Failed to load recipes", Toast.LENGTH_SHORT).show();
                return;
            }

            // Clear old list and add new recipes
            allRecipes.clear();
            for (DocumentSnapshot doc : task.getResult()) {
                Recipe r = doc.toObject(Recipe.class);
                if (r != null) {
                    r.setId(doc.getId());
                    allRecipes.add(r);
                }
            }

            // Update adapter with new list
            adapter.updateList(new ArrayList<>(allRecipes));
            textEmpty.setVisibility(allRecipes.isEmpty() ? View.VISIBLE : View.GONE);

            // If something is typed in the search bar, apply filtering
            String q = editSearch.getText().toString();
            if (!q.isEmpty()) filterBySearch(q);
        });
    }

    // Filters recipes by search text
    private void filterBySearch(String query) {
        List<Recipe> filtered = new ArrayList<>();
        for (Recipe r : allRecipes) {
            String name = r.getName();
            if (name != null && name.toLowerCase().contains(query.toLowerCase())) {
                filtered.add(r);
            }
        }
        adapter.updateList(filtered);
        textEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // Shows a confirmation dialog before logging out
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (d, w) -> {
                    FirebaseAuth.getInstance().signOut(); // Log out from Firebase
                    // Clear any cached data
                    allRecipes.clear();
                    adapter.updateList(new ArrayList<>());
                    NavigationHelper.navigate(this, LoginActivity.class, false); // Go to Login screen
                    finish(); // Close HomeActivity
                })
                .setNegativeButton("Cancel", null) // Do nothing if cancelled
                .show();
    }

    // Checks premium flag before opening Pantry
    private void handlePantryNavigation() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            NavigationHelper.navigate(this, LoginActivity.class, false);
            return;
        }
        // Always open Pantry so non-premium users can see the upgrade screen
        NavigationHelper.navigate(this, PantryActivity.class, false);
    }
}
