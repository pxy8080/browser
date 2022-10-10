package de.baumann.browser.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;

import android.graphics.Color;
import android.net.Uri;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Objects;

import de.baumann.browser.MainApplication;
import de.baumann.browser.R;
import de.baumann.browser.activity.BrowserActivity;
import de.baumann.browser.activity.VideoActivity;
import de.baumann.browser.database.RecordAction;
import de.baumann.browser.entity.DowningTask;
import de.baumann.browser.event.ShowToastMessageEvent;
import de.baumann.browser.unit.BrowserUnit;
import de.baumann.browser.util.BaseHandleMessage;
import de.baumann.browser.util.FileUtil;
import de.baumann.browser.util.GlideUtil;
import de.baumann.browser.util.IntentUtil;
import de.baumann.browser.util.VideoUtil;

public class AdapterVideoList extends RecyclerView.Adapter<AdapterVideoList.Holder> {
    private final Context context;
    private int mEditMode = VideoActivity.MYLIVE_MODE_CHECK;
    private OnItemClickListener mOnItemClickListener;
    public static int SINGLEFILEDEL = 88;

    public AdapterVideoList(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public AdapterVideoList.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.item_file, null);
        return new Holder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AdapterVideoList.Holder holder, int position) {
        DowningTask downingTask;
        if (BrowserActivity.Tasks != null && !BrowserActivity.Tasks.isEmpty())
            downingTask = BrowserActivity.Tasks.get(position);
        else
            return;
        if (downingTask.getmState() == 2) {
            holder.item_file_title.setText(downingTask.getmFileName());
            holder.video_frame.setVisibility(View.VISIBLE);
            holder.file_overflow.setVisibility(View.VISIBLE);
            holder.downloadingItemProgress.setVisibility(View.GONE);
            holder.StateImg.setVisibility(View.GONE);
            holder.fileSize.setVisibility(View.VISIBLE);
            holder.video_duration.setVisibility(View.VISIBLE);
            holder.downloaded.setVisibility(View.GONE);
            holder.downloadingItemDownloadInfo.setVisibility(View.GONE);
            holder.video_duration.setText(downingTask.getmDuration());
            GlideUtil.LoadVideoThumbnail(context, holder.video_frame, downingTask.getmVideoThumbnail());
            holder.file_overflow.setOnClickListener(view -> {
                Dialog dialog = new Dialog(context);//可以在style中设定dialog的样式
                dialog.setContentView(R.layout.item_file_op);
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(true);
                WindowManager.LayoutParams lp = Objects.requireNonNull(dialog.getWindow()).getAttributes();
                lp.gravity = Gravity.BOTTOM;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                dialog.getWindow().setAttributes(lp);
                //设置该属性，dialog可以铺满屏幕
                dialog.getWindow().setBackgroundDrawable(null);
                dialog.show();
                DialogInit(dialog, downingTask, position, downingTask.getmUuid());
            });
            if (mEditMode == VideoActivity.MYLIVE_MODE_CHECK) {
                holder.check_box.setVisibility(View.GONE);
                holder.cardview.setOnClickListener(view -> IntentUtil.openFileByUri((Activity) context, downingTask.getmFilePath()));
            } else {
                holder.check_box.setVisibility(View.VISIBLE);
                holder.file_overflow.setVisibility(View.GONE);
                if (downingTask.getIsSelect() == 1)
                    holder.check_box.setImageResource(R.drawable.icon_video_unselected);
                else
                    holder.check_box.setImageResource(R.drawable.icon_video_selected);

                holder.cardview.setOnClickListener(view -> mOnItemClickListener.onItemClickListener(downingTask));
            }
            holder.fileSize.setText(FileUtil.getFormatedFileSize(FileUtil.getFileSize(new File(downingTask.getmFilePath()))));

            holder.cardview.setOnLongClickListener(view -> {
                BaseHandleMessage.getInstance().setHandlerMessage(VideoActivity.CHANGE_MODE, null);
                return true;
            });
        } else {
            setVisibility(false, holder.itemView);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
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

    @Override
    public int getItemCount() {
        return BrowserActivity.Tasks.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        TextView item_file_title;                //文件标题
        TextView downloaded;                     //已下载大小
        TextView fileSize;                       //文件大小
        TextView downloadingItemDownloadInfo;    //下载速度
        ImageView StateImg;                      //下载状态icon
        ProgressBar downloadingItemProgress;     //下载进度条
        ImageView file_overflow;                 //下载-->更多
        ImageView video_frame;                   //视频帧
        View cardview;                           //item条目
        TextView video_duration;                 //视频时长
        ImageView check_box;                     //视频多选

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
            video_duration = itemView.findViewById(R.id.video_duration);
            check_box = itemView.findViewById(R.id.check_box);

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void DialogInit(Dialog dialog, DowningTask task, int position, Integer uuid) {
        EditText video_name = dialog.findViewById(R.id.video_name);
        ImageView btn_close_overflow = dialog.findViewById(R.id.btn_close_overflow);
        TextView btn_share = dialog.findViewById(R.id.btn_share);
        TextView btn_rename = dialog.findViewById(R.id.btn_rename);
        TextView btn_downloadpath = dialog.findViewById(R.id.btn_downloadpath);
        TextView btn_towebsite = dialog.findViewById(R.id.btn_towebsite);
        TextView btn_delete = dialog.findViewById(R.id.btn_delete);

        video_name.setText(FileUtil.getPureFileName(task.getmFileName()));
        btn_close_overflow.setOnClickListener(view -> dialog.onBackPressed());
        btn_share.setOnClickListener(view -> shareVideo(task.getmFilePath()));

        btn_rename.setOnClickListener(view -> {
            video_name.selectAll();   //默认选中EditText中的所有内容
            video_name.setFocusable(true);   //设置可以获取焦点
            video_name.setFocusableInTouchMode(true);     //触摸可以获取焦点? 什么鬼,我也不清楚……
            video_name.requestFocus();  //请求获取焦点(一般情况下,EditText是默认获取的)
            InputMethodManager inputManager =
                    (InputMethodManager) video_name.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(video_name, 0);
            video_name.setOnEditorActionListener((textView, i, keyEvent) -> {
                String aa = video_name.getText().toString().trim() + "." + FileUtil.getFileSuffix(task.getmFileName());
                video_name.setText(aa);
                File file = new File(task.getmFilePath());
                FileUtil.renameFile(file.getParent(), task.getmFileName(), aa);
                task.setmFileName(aa);
                task.setmFilePath(file.getParent() + File.separator + aa);
                dialog.dismiss();
                RecordAction action = new RecordAction(MainApplication.mainApplication);
                action.open(false);
                action.updateTaskName(task.getmUuid(), new String[]{aa, file.getParent() + File.separator + aa});
                action.close();
                dialog.dismiss();
                notifyDataSetChanged();
                return false;
            });

        });
        //确认下载路径
        btn_downloadpath.setOnClickListener(view -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
            View dialogView = View.inflate(context, R.layout.dialog_edit, null);
            LinearLayout ll = dialogView.findViewById(R.id.ll);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ll.getLayoutParams();
            lp.topMargin = 0;
            ll.setLayoutParams(lp);
            builder.setTitle("下载路径");
            builder.setView(dialogView);
            builder.setMessage(task.getmFilePath());

            TextInputLayout editTopLayout = dialogView.findViewById(R.id.editTopLayout);
            editTopLayout.setVisibility(View.GONE);
            TextInputLayout editBottomLayout = dialogView.findViewById(R.id.editBottomLayout);
            editBottomLayout.setVisibility(View.GONE);
            Button ib_cancel = dialogView.findViewById(R.id.editCancel);
            ib_cancel.setText("复制");
            ib_cancel.setBackgroundColor(Color.parseColor("#0099ff"));
            androidx.appcompat.app.AlertDialog dialog2 = builder.create();
            dialog2.show();

            Button ib_ok = dialogView.findViewById(R.id.editOK);
            ib_ok.setOnClickListener(view3 -> dialog2.cancel());
            ib_cancel.setOnClickListener(view12 -> {
                ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setText(task.getmFilePath());
                EventBus.getDefault().post(new ShowToastMessageEvent("内容已复制到剪切板"));
                dialog2.dismiss();
            });


        });
        //前往网站
        btn_towebsite.setOnClickListener(view -> {
            Uri webpage = Uri.parse(task.getmSourceUrl());
            BrowserUnit.intentURL(context, webpage);
        });
        //单个视频删除
        btn_delete.setOnClickListener(view -> {
            RecordAction action = new RecordAction(MainApplication.mainApplication);
            action.open(false);
            action.delTask(task.getmUuid());
            action.close();
            FileUtil.deleteFile(task.getmFilePath());
            BrowserActivity.Tasks.removeIf(downingTask -> Objects.equals(downingTask.getmUuid(), task.getmUuid()));
            dialog.dismiss();
            notifyDataSetChanged();
            BaseHandleMessage.getInstance().setHandlerMessage(SINGLEFILEDEL, MainApplication.DownTaskBadgeNum);
        });

    }

    //path为本地文件绝对路径
    public void shareVideo(String path) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, path);
//        ArrayList<Uri> imageUris = new ArrayList<Uri>();//不需要多文件可以删掉
//        for (File f : files) {
//            imageUris.add(Uri.fromFile(f));
//        }
//        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,imageUris);//多个文件
//        shareIntent.setType("image/*");//选择视频
        //shareIntent.setType(“audio/*”); //选择音频
        shareIntent.setType("video/*"); //选择视频
        //shareIntent.setType(“video/;image/”);//同时选择音频和视频
        context.startActivity(Intent.createChooser(shareIntent, "分享到"));
    }

    public static void setVisibility(boolean isVisible, View itemView) {
        RecyclerView.LayoutParams param = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (isVisible) {
            param.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            param.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            itemView.setVisibility(View.VISIBLE);
        } else {
            param.height = 0;
            param.width = 0;
            itemView.setVisibility(View.GONE);
        }
        itemView.setLayoutParams(param);
    }

}
