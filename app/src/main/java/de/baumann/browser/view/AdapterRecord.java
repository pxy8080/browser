package de.baumann.browser.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import de.baumann.browser.R;
import de.baumann.browser.database.FaviconHelper;
import de.baumann.browser.database.Record;

public class AdapterRecord extends ArrayAdapter<Record> {
    private final Context context;
    private final int layoutResId;
    private final List<Record> list;

    private static class Holder {
        TextView title;
        TextView time;
        ImageView favicon;
        LinearLayout cardView;
    }

    public AdapterRecord(Context context, List<Record> list) {
        super(context, R.layout.item_list, list);
        this.context = context;
        this.layoutResId = R.layout.item_list;
        this.list = list;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Holder holder;
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(layoutResId, parent, false);
            holder = new Holder();
            holder.title = view.findViewById(R.id.titleView);
            holder.time = view.findViewById(R.id.dateView);
            holder.favicon = view.findViewById(R.id.faviconView);
            holder.cardView = view.findViewById(R.id.albumCardView);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }

        Record record = list.get(position);
//        long filter = record.getIconColor();
//        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/HH:mm", Locale.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd", Locale.getDefault());
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.title.getLayoutParams();
        lp.rightMargin = 150;

        holder.title.setLayoutParams(lp);

        holder.title.setText(record.getTitle());
        holder.time.setText(sdf.format(record.getTime()));

//        if (filter == 11) {
//            holder.cardView.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.red, null));
//        } else if (filter == 10) {
//            holder.cardView.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.pink, null));
//        } else if (filter == 9) {
//            holder.cardView.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.purple, null));
//        } else if (filter == 8) {
//            holder.cardView.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.blue, null));
//        } else if (filter == 7) {
//            holder.cardView.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.teal, null));
//        } else if (filter == 6) {
//            holder.cardView.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.green, null));
//        } else if (filter == 5) {
//            holder.cardView.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.lime, null));
//        } else if (filter == 4) {
//            holder.cardView.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.yellow, null));
//        } else if (filter == 3) {
//            holder.cardView.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.orange, null));
//        } else if (filter == 2) {
//            holder.cardView.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.brown, null));
//        } else if (filter == 1) {
//            holder.cardView.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.grey, null));
//        } else {
//            TypedValue typedValue = new TypedValue();
//            context.getTheme().resolveAttribute(R.attr.colorSurfaceVariant, typedValue, true);
//            int color = typedValue.data;
//            holder.cardView.setBackgroundColor(color);
//        }

        FaviconHelper faviconHelper = new FaviconHelper(context);
        Bitmap bitmap = faviconHelper.getFavicon(record.getURL());

        if (bitmap != null) {
            holder.favicon.setImageBitmap(bitmap);
        } else {
            holder.favicon.setImageResource(R.drawable.icon_image_broken);
        }

        return view;
    }
}