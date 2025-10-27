package bd.edu.seu.cookify.controllers;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import bd.edu.seu.cookify.R;
import bd.edu.seu.cookify.adapters.SubstituteAdapter;
import bd.edu.seu.cookify.models.SubstituteItem;
import bd.edu.seu.cookify.navigate.NavigationHelper;

public class SubstituteActivity extends AppCompatActivity {

    private SubstituteAdapter adapter;
    private final List<SubstituteItem> items = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_substitute);

        RecyclerView recycler = findViewById(R.id.recyclerSubstitutes);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubstituteAdapter(this);
        recycler.setAdapter(adapter);

        EditText search = findViewById(R.id.editSearchSubstitute);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filterByName(s.toString());
            }
        });

        setupBottomNav();
        fetchData();
    }

    private void setupBottomNav() {
        android.widget.ImageView navHome = findViewById(R.id.navHome);
        android.widget.ImageView navBrowse = findViewById(R.id.navBrowse);
        android.widget.ImageView navSubstitute = findViewById(R.id.navSubstitute);
        android.widget.ImageView navPantry = findViewById(R.id.navPantry);

        navHome.setOnClickListener(v ->
                NavigationHelper.navigate(this, HomeActivity.class, true));

        navBrowse.setOnClickListener(v ->
                NavigationHelper.navigate(this, BrowseActivity.class, true));

        navPantry.setOnClickListener(v ->
                NavigationHelper.navigate(this, PantryActivity.class, true));

        // Already in SubstituteActivity → do nothing
        navSubstitute.setOnClickListener(v -> {});
    }

    private void fetchData() {
        FirebaseFirestore.getInstance()
                .collection("substitutes")
                .get()
                .addOnSuccessListener(snap -> {
                    items.clear();
                    for (QueryDocumentSnapshot d : snap) {
                        java.util.List<String> subs = (java.util.List<String>) d.get("substitute");
                        SubstituteItem item = new SubstituteItem(
                                d.getString("name"),
                                d.getString("amount"),
                                d.getString("imageUrl"),
                                subs
                        );
                        items.add(item);
                    }
                    adapter.setItems(items);

                    // ✅ Success feedback
                    Log.d("SubstituteActivity", "Fetched " + items.size() + " substitutes");
                    Toast.makeText(this,
                            "Loaded " + items.size() + " substitutes",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // ✅ Error feedback
                    Log.e("SubstituteActivity", "Error fetching substitutes", e);
                    Toast.makeText(this,
                            "Failed to load substitutes. Please try again.",
                            Toast.LENGTH_SHORT).show();
                });
    }
}