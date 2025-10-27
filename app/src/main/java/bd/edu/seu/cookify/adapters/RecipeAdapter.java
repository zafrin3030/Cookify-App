package bd.edu.seu.cookify.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import bd.edu.seu.cookify.R;
import bd.edu.seu.cookify.models.RecipeItem;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeVH> {

    private final Context context;
    private final List<RecipeItem> allItems = new ArrayList<>();
    private final List<RecipeItem> visibleItems = new ArrayList<>();

    public RecipeAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<RecipeItem> items) {
        allItems.clear();
        allItems.addAll(items);
        visibleItems.clear();
        visibleItems.addAll(items);
        notifyDataSetChanged();
    }

    public void filterByCulture(String culture) {
        visibleItems.clear();
        if (culture == null || culture.equalsIgnoreCase("All")) {
            visibleItems.addAll(allItems);
        } else {
            for (RecipeItem item : allItems) {
                if (culture.equalsIgnoreCase(item.getCulture())) {
                    visibleItems.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void filterByName(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        visibleItems.clear();
        
        android.util.Log.d("RecipeAdapter", "Filtering by name: '" + q + "'");
        android.util.Log.d("RecipeAdapter", "Total items to search: " + allItems.size());
        
        if (q.isEmpty()) {
            visibleItems.addAll(allItems);
            android.util.Log.d("RecipeAdapter", "Showing all items: " + visibleItems.size());
        } else {
            for (RecipeItem item : allItems) {
                String itemName = item.getName();
                if (itemName != null) {
                    String normalizedName = itemName.toLowerCase(Locale.ROOT);
                    if (normalizedName.contains(q)) {
                        visibleItems.add(item);
                        android.util.Log.d("RecipeAdapter", "Match found: " + itemName);
                    }
                } else {
                    android.util.Log.d("RecipeAdapter", "Item has null name: " + item.getId());
                }
            }
            android.util.Log.d("RecipeAdapter", "Filtered results: " + visibleItems.size());
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new RecipeVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeVH holder, int position) {
        RecipeItem item = visibleItems.get(position);
        holder.name.setText(item.getName());
        Glide.with(context)
                .load(item.getImageUrl())
                .centerCrop()
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.image);

        holder.itemView.setOnClickListener(v -> {
            android.content.Intent i = new android.content.Intent(context, bd.edu.seu.cookify.controllers.RecipeDetailsActivity.class);
            if (item.getId() != null) {
                i.putExtra("recipeId", item.getId());
            } else {
                i.putExtra("name", item.getName());
            }
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return visibleItems.size();
    }

    static class RecipeVH extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;
        RecipeVH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageRecipe);
            name = itemView.findViewById(R.id.textRecipeName);
        }
    }
}

