package bd.edu.seu.cookify.controllers;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bd.edu.seu.cookify.R;
import bd.edu.seu.cookify.adapters.RecipeAdapter;
import bd.edu.seu.cookify.models.RecipeItem;

public class BrowseActivity extends AppCompatActivity {

    private RecipeAdapter adapter; // uses RecipeItem + Glide
    private final List<RecipeItem> all = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_recipe);

        RecyclerView recycler = findViewById(R.id.recyclerRecipes);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this);
        recycler.setAdapter(adapter);

        Spinner spinner = findViewById(R.id.spinnerCulture);
        setupSpinner(spinner);

        EditText search = findViewById(R.id.editSearch);
        
        // Real-time search as user types
        search.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performNameFilter(s.toString());
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        
        // Detect taps on the drawableEnd (search icon) and trigger filtering
        search.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                if (search.getCompoundDrawables()[DRAWABLE_RIGHT] != null) {
                    int drawableWidth = search.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();
                    if (event.getX() >= (search.getWidth() - search.getPaddingRight() - drawableWidth)) {
                        performNameFilter(search.getText().toString());
                        return true;
                    }
                }
            }
            return false;
        });
        
        search.setOnEditorActionListener((v, actionId, keyEvent) -> {
            performNameFilter(search.getText().toString());
            return true;
        });

        setupBottomNav();
        fetchAll();

        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                if ("Select".equalsIgnoreCase(selected) || "All".equalsIgnoreCase(selected)) {
                    adapter.filterByCulture("All");
                } else {
                    adapter.filterByCulture(selected);
                }
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
    }

    private void setupSpinner(Spinner spinner) {
        List<String> cultures = new ArrayList<>();
        FirebaseFirestore.getInstance()
                .collection("cultures")
                .get()
                .addOnSuccessListener(snap -> {
                    cultures.clear();
                    cultures.add("Select");
                    for (com.google.firebase.firestore.QueryDocumentSnapshot d : snap) {
                        String name = d.getString("name");
                        if (name != null && !name.trim().isEmpty()) {
                            cultures.add(name.trim());
                        }
                    }
                    cultures.add("All");

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cultures);
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(arrayAdapter);
                    spinner.setSelection(0);
                })
                .addOnFailureListener(e -> {
                    Log.e("BrowseActivity", "Failed to load cultures", e);
                    Toast.makeText(this, "Failed to load cultures", Toast.LENGTH_SHORT).show();
                    // Fallback minimal options so UI remains usable
                    List<String> fallback = Arrays.asList("Select", "All");
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, fallback);
                    spinner.setAdapter(arrayAdapter);
                    spinner.setSelection(0);
                });
    }

    private void performNameFilter(String text) {
        String searchText = text == null ? "" : text.trim();
        Log.d("BrowseActivity", "Searching for: '" + searchText + "'");
        Log.d("BrowseActivity", "Total recipes loaded: " + all.size());
        
        if (TextUtils.isEmpty(searchText)) {
            adapter.filterByName("");
            Log.d("BrowseActivity", "Showing all recipes");
        } else {
            adapter.filterByName(searchText);
            Log.d("BrowseActivity", "Filtering recipes by name: " + searchText);
        }
    }

    private void setupBottomNav() {
        ImageView navHome = findViewById(R.id.navHome);
        ImageView navBrowse = findViewById(R.id.navBrowse);
        ImageView navSubstitute = findViewById(R.id.navSubstitute);
        ImageView navPantry = findViewById(R.id.navPantry);


        navHome.setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        navBrowse.setOnClickListener(v -> {});
        navSubstitute.setOnClickListener(v -> startActivity(new Intent(this, SubstituteActivity.class)));
        navPantry.setOnClickListener(v -> startActivity(new Intent(this, PantryActivity.class)));
    }

    private void fetchAll() {
        Log.d("BrowseActivity", "Fetching all recipes from Firestore...");
        FirebaseFirestore.getInstance()
                .collection("recipes")
                .get()
                .addOnSuccessListener(snap -> {
                    all.clear();
                    Log.d("BrowseActivity", "Firestore returned " + snap.size() + " documents");
                    
                    for (QueryDocumentSnapshot d : snap) {
                        String name = d.getString("name");
                        String imageUrl = d.getString("imageUrl");
                        String culture = d.getString("culture");
                        
                        Log.d("BrowseActivity", "Recipe: " + name + " (ID: " + d.getId() + ")");
                        
                        RecipeItem item = new RecipeItem(
                                d.getId(),
                                name,
                                imageUrl,
                                culture
                        );
                        all.add(item);
                    }
                    
                    Log.d("BrowseActivity", "Loaded " + all.size() + " recipes into adapter");
                    adapter.setItems(all);
                    
                    // Show success message
                    Toast.makeText(this, "Loaded " + all.size() + " recipes", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("BrowseActivity", "Failed to load recipes", e);
                    Toast.makeText(this, "Failed to load recipes: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}