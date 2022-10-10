package de.baumann.browser.view;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.yausername.youtubedl_android.mapper.VideoFormat;
import java.util.List;
import de.baumann.browser.R;
import de.baumann.browser.util.StringUtil;

public class AdapterDetectedVideo extends RecyclerView.Adapter<AdapterDetectedVideo.Holder> {
    private final Context context;
    private final List<VideoFormat> videoFormats;
    private AdapterDetectedVideo.OnItemClickListener mOnItemClickListener;
    private int PerPostion = -1;

    public AdapterDetectedVideo(Context context, List<VideoFormat> videoFormats) {
        this.context = context;
        this.videoFormats = videoFormats;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.item_detected_video, null);
        return new Holder(view);
    }


    @SuppressLint({"ResourceAsColor", "SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull Holder holder, @SuppressLint("RecyclerView") int position) {
        if (position == PerPostion)
            holder.video_info.setBackground(context.getDrawable(R.drawable.button_shape));
        else
            holder.video_info.setBackground(context.getDrawable(R.drawable.button_noclickable_shape));
        holder.video_info.setText(StringUtil.getQuantity(videoFormats.get(position).getFormat().split("x")[1]) + "P");
        holder.video_info.setOnClickListener(view -> {
            mOnItemClickListener.onItemClickListener(videoFormats.get(position));
            notifyItemChanged(PerPostion);
            PerPostion = position;
            notifyItemChanged(position);
        });

    }

    public void setOnItemClickListener(AdapterDetectedVideo.OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClickListener(VideoFormat videoFormat);
    }

    @Override
    public int getItemCount() {
        return videoFormats.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        private final TextView video_info;

        public Holder(@NonNull View itemView) {
            super(itemView);
            video_info = itemView.findViewById(R.id.video_info);
        }
    }
}
