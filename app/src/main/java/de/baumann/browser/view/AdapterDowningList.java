package de.baumann.browser.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.excellence.downloader.FileDownloader;

import de.baumann.browser.MainApplication;
import de.baumann.browser.R;
import de.baumann.browser.activity.BrowserActivity;
import de.baumann.browser.activity.DowningActivity;
import de.baumann.browser.activity.VideoActivity;
import de.baumann.browser.entity.DowningTask;
import de.baumann.browser.util.BaseHandleMessage;
import de.baumann.browser.util.CalculateUtil;
import de.baumann.browser.util.FileUtil;
import de.baumann.browser.util.GlideUtil;

public class AdapterDowningList extends BaseAdapter /*RecyclerView.Adapter<Holder>*/ {
    private int mEditMode = VideoActivity.MYLIVE_MODE_CHECK;
    private Context context;
    private AdapterDowningList.OnItemClickListener mOnItemClickListener;

    public AdapterDowningList(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return BrowserActivity.Tasks.size();
    }

    @Override
    public Object getItem(int position) {
        return BrowserActivity.Tasks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        DowningTask task;
        if (BrowserActivity.Tasks != null && !BrowserActivity.Tasks.isEmpty())
            task = BrowserActivity.Tasks.get(position);
        else
            return convertView;
        Holder holder;

        if (task.getmState() == 1) {//正在下载的任务需要显示

            if (convertView == null || convertView.getTag() == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_file, null, false);
                holder = new Holder(convertView);
            } else
                holder = (Holder) convertView.getTag();

            holder.item_file_title.setText(task.getmFileName());
            FileDownloader.DownloadTask mDownloadTask = task.getDownloadTask();

            GlideUtil.LoadVideoThumbnail(context, holder.video_frame, task.getmVideoThumbnail());

            holder.downloaded.setVisibility(View.VISIBLE);
            holder.fileSize.setVisibility(View.VISIBLE);
            holder.downloaded.setText(FileUtil.getFormatedFileSize(mDownloadTask.getDownloadLength()) + "/");
            holder.fileSize.setText(FileUtil.getFormatedFileSize(mDownloadTask.getDownloadLength()));
            holder.video_duration.setText(task.getmDuration());

            holder.downloadingItemProgress.setVisibility(View.VISIBLE);
            holder.StateImg.setVisibility(View.VISIBLE);

            if (mDownloadTask.getStatus() == 1)
                holder.StateImg.setImageResource(R.drawable.icon_pause);
            else
                holder.StateImg.setImageResource(R.drawable.icon_play);
            holder.video_frame.setVisibility(View.VISIBLE);

            holder.downloadingItemDownloadInfo.setText(FileUtil.getFormatedFileSize(task.getSpeed()) + "/s");
            holder.downloadingItemProgress.setProgress(CalculateUtil.getPercent(task.getDownloadedSize(), task.getFileSize()));
            holder.downloaded.setText(FileUtil.getFormatedFileSize(task.getDownloadedSize()) + "/");
            holder.fileSize.setText(FileUtil.getFormatedFileSize(task.getFileSize()));

            if (mEditMode == VideoActivity.MYLIVE_MODE_CHECK) {
                holder.StateImg.setVisibility(View.VISIBLE);
                holder.cardview.setOnClickListener(view -> {
                    if (mDownloadTask.getStatus() == 1) {
                        mDownloadTask.pause();
                        holder.downloadingItemDownloadInfo.setText("暂停中");
                        holder.StateImg.setImageResource(R.drawable.icon_play);
                    } else {
                        mDownloadTask.resume();
                        holder.downloadingItemDownloadInfo.setText("恢复中");
                        holder.StateImg.setImageResource(R.drawable.icon_pause);
                    }
                });
            } else {
                holder.StateImg.setVisibility(View.GONE);
                holder.check_box.setVisibility(View.VISIBLE);
                holder.file_overflow.setVisibility(View.GONE);
                if (task.getIsSelect() == 1)
                    holder.check_box.setImageResource(R.drawable.icon_video_unselected);
                else
                    holder.check_box.setImageResource(R.drawable.icon_video_selected);
                holder.cardview.setOnClickListener(view -> mOnItemClickListener.onItemClickListener(task));
            }

            holder.cardview.setOnLongClickListener(view -> {
                BaseHandleMessage.getInstance().setHandlerMessage(DowningActivity.CHANGE_STATE, null);
                return false;
            });
        } else {
            if (convertView == null || convertView.getTag() == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_null, null, false);
            }
        }

        return convertView;
    }

    public void setOnItemClickListener(AdapterDowningList.OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClickListener(DowningTask downingTask);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setEditMode(int editMode) {
        mEditMode = editMode;
        notifyDataSetChanged();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView item_file_title;
        TextView downloaded;
        TextView fileSize;
        TextView downloadingItemDownloadInfo;
        ImageView StateImg;
        ProgressBar downloadingItemProgress;
        ImageView file_overflow;
        ImageView video_frame;
        View cardview;
        ImageView check_box;                     //视频多选
        TextView video_duration;

        public Holder(@NonNull View itemView) {
            super(itemView);
            item_file_title = itemView.findViewById(R.id.item_file_title);
            downloaded = itemView.findViewById(R.id.downloaded);
            fileSize = itemView.findViewById(R.id.fileSize);
            downloadingItemDownloadInfo = itemView.findViewById(R.id.downloadingItemDownloadInfo);
            StateImg = itemView.findViewById(R.id.StateImg);
            downloadingItemProgress = itemView.findViewById(R.id.task_progressbar);
            file_overflow = itemView.findViewById(R.id.file_overflow);
            video_frame = itemView.findViewById(R.id.video_frame);
            cardview = itemView.findViewById(R.id.downing_cardview);
            check_box = itemView.findViewById(R.id.check_box);
            video_duration = itemView.findViewById(R.id.video_duration);
        }
    }
}


