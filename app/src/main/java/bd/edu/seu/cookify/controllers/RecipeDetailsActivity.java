package bd.edu.seu.cookify.controllers;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import bd.edu.seu.cookify.R;

public class RecipeDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        ImageView iconBack = findViewById(R.id.iconBack);
        TextView textTitle = findViewById(R.id.textTitle);
        ImageView detailImage = findViewById(R.id.detailImage);
        LinearLayout containerIngredients = findViewById(R.id.containerIngredients);
        LinearLayout containerInstructions = findViewById(R.id.containerInstructions);

        iconBack.setOnClickListener(v -> finish());

        String recipeId = getIntent().getStringExtra("recipeId");
        String recipeName = getIntent().getStringExtra("name");

        if (recipeId != null && !recipeId.isEmpty()) {
            FirebaseFirestore.getInstance()
                    .collection("recipes")
                    .document(recipeId)
                    .get()
                    .addOnSuccessListener(doc -> populateUiFromDocument(doc, textTitle, detailImage, containerIngredients, containerInstructions))
                    .addOnFailureListener(e -> finish());
        } else if (recipeName != null && !recipeName.isEmpty()) {
            FirebaseFirestore.getInstance()
                    .collection("recipes")
                    .whereEqualTo("name", recipeName)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            populateUiFromDocument(query.getDocuments().get(0), textTitle, detailImage, containerIngredients, containerInstructions);
                        } else {
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> finish());
        } else {
            finish();
        }
    }

    private void populateUiFromDocument(DocumentSnapshot doc,
                                        TextView textTitle,
                                        ImageView detailImage,
                                        LinearLayout containerIngredients,
                                        LinearLayout containerInstructions) {
        String name = doc.getString("name");
        String imageUrl = doc.getString("imageUrl");
        List<String> ingredients = (List<String>) doc.get("ingredients");
        List<String> instructions = (List<String>) doc.get("instructions");

        if (name != null) textTitle.setText(name);

        Glide.with(this)
                .load(imageUrl)
                .centerCrop()
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(detailImage);

        containerIngredients.removeAllViews();
        if (ingredients != null) {
            for (String ing : ingredients) {
                TextView tv = createBulletItem(" " + ing);
                containerIngredients.addView(tv);
            }
        }

        containerInstructions.removeAllViews();
        if (instructions != null) {
            int i = 1;
            for (String step : instructions) {
                TextView tv = createBulletItem(i + " " + step);
                containerInstructions.addView(tv);
                i++;
            }
        }
    }

    private TextView createBulletItem(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(0xFF000000);
        tv.setTextSize(14);
        tv.setPadding(4, 6, 4, 6);
        return tv;
    }
}
