package bd.edu.seu.cookify.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import bd.edu.seu.cookify.R;
import bd.edu.seu.cookify.models.SubstituteItem;

public class SubstituteAdapter extends RecyclerView.Adapter<SubstituteAdapter.VH> {

    private final Context context;
    private final List<SubstituteItem> allItems = new ArrayList<>();
    private final List<SubstituteItem> visibleItems = new ArrayList<>();

    public SubstituteAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<SubstituteItem> items) {
        allItems.clear();
        if (items != null) allItems.addAll(items);
        filterByName("");
    }

    public void filterByName(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.getDefault());
        visibleItems.clear();
        if (TextUtils.isEmpty(q)) {
            visibleItems.addAll(allItems);
        } else {
            for (SubstituteItem it : allItems) {
                String name = it.getName() == null ? "" : it.getName();
                if (name.toLowerCase(Locale.getDefault()).contains(q)) {
                    visibleItems.add(it);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_substitute, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        SubstituteItem item = visibleItems.get(position);
        h.textName.setText(item.getName() == null ? "" : item.getName());
        h.textAmount.setText(item.getAmount() == null ? "" : item.getAmount());

        String url = item.getImageUrl();
        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(h.imageThumb);

        // Dynamic bullet list
        h.containerBullets.removeAllViews();
        java.util.List<String> subs = item.getSubstitutes();
        if (subs != null) {
            for (String s : subs) {
                TextView tv = new TextView(context);
                tv.setText("" + s);
                tv.setTextColor(0xFF000000);
                tv.setTextSize(14f);
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                p.topMargin = (int) (context.getResources().getDisplayMetrics().density * 4);
                tv.setLayoutParams(p);
                h.containerBullets.addView(tv);
            }
        }
    }

    @Override
    public int getItemCount() { return visibleItems.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView textName;
        final TextView textAmount;
        final ImageView imageThumb;
        final LinearLayout containerBullets;
        VH(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textAmount = itemView.findViewById(R.id.textAmount);
            imageThumb = itemView.findViewById(R.id.imageThumb);
            containerBullets = itemView.findViewById(R.id.containerBullets);
        }
    }
}


