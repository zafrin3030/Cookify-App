package bd.edu.seu.cookify.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import bd.edu.seu.cookify.R;

/**
 * Adapter for displaying pantry items in a RecyclerView
 */
public class PantryItemAdapter extends RecyclerView.Adapter<PantryItemAdapter.PantryItemViewHolder> {
    private final List<String> items;
    private OnItemRemovedListener onItemRemovedListener;
    
    public PantryItemAdapter(List<String> items) { 
        this.items = items; 
    }
    
    public void setOnItemRemovedListener(OnItemRemovedListener listener) {
        this.onItemRemovedListener = listener;
    }
    
    @NonNull
    @Override
    public PantryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new PantryItemViewHolder(v, this);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PantryItemViewHolder holder, int position) { 
        holder.bind(items.get(position), position); 
    }
    
    @Override
    public int getItemCount() { 
        return items.size(); 
    }
    
    void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
            if (onItemRemovedListener != null) {
                onItemRemovedListener.onItemRemoved();
            }
        }
    }
    
    /**
     * Interface for handling item removal events
     */
    public interface OnItemRemovedListener {
        void onItemRemoved();
    }
    
    /**
     * ViewHolder for pantry items
     */
    static class PantryItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView text;
        private PantryItemAdapter adapter;
        
        PantryItemViewHolder(@NonNull View itemView, PantryItemAdapter adapter) {
            super(itemView);
            this.adapter = adapter;
            text = itemView.findViewById(R.id.textItemName);
        }
        
        void bind(String name, int position) { 
            text.setText(name);
            // Add long press to remove item
            itemView.setOnLongClickListener(v -> {
                adapter.removeItem(position);
                return true;
            });
        }
    }
}
