package de.baumann.browser.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;

import android.view.View;

import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Iterator;

import de.baumann.browser.MainApplication;
import de.baumann.browser.R;

import de.baumann.browser.database.RecordAction;
import de.baumann.browser.entity.DowningTask;

import de.baumann.browser.util.BaseHandleMessage;
import de.baumann.browser.util.FileUtil;
import de.baumann.browser.view.AdapterDowningList;


public class DowningActivity extends AppCompatActivity implements View.OnClickListener, AdapterDowningList.OnItemClickListener {
    private ImageView goBackButton;
    private TextView tip_text;
    private ImageView tip;
    private ListView downinglist;
    private Button to_video_center;
    private AdapterDowningList adapter;
    private ImageView editor_iv;
    public static final int CHANGE_STATE = 89;
    private int mEditMode = VideoActivity.MYLIVE_MODE_CHECK;
    private LinearLayout delete_bottom_dialog;
    private boolean editorStatus = false;
    private int index = 0;
    private boolean isSelectAll = false;
    private TextView tv_select_num;                    //选中删除的数量
    private TextView select_all;                       //全选
    private TextView btn_delete;                         //删除按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downing);
        initStatusBar();
        initView();
        initData();
    }

    private void initView() {
        downinglist = findViewById(R.id.downinglist);
        tip = findViewById(R.id.tip);
        tip_text = findViewById(R.id.tip_text);
        goBackButton = findViewById(R.id.goBackButton);
        goBackButton.setOnClickListener(this);
        to_video_center = findViewById(R.id.to_video_center);
        to_video_center.setOnClickListener(this);
        editor_iv = findViewById(R.id.editor_iv);
        editor_iv.setOnClickListener(this);

        delete_bottom_dialog = findViewById(R.id.delete_ll);
        tv_select_num = findViewById(R.id.tv_select_num);
        select_all = findViewById(R.id.select_all);
        select_all.setOnClickListener(this);
        btn_delete = findViewById(R.id.btn_delete);
        btn_delete.setOnClickListener(this);
    }

    private void initData() {

        adapter = new AdapterDowningList(MainApplication.mainApplication);
        //downinglist.setLayoutManager(new LinearLayoutManager(this));
        downinglist.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
        RefreshBackground();

        Handler handler = new Handler(msg -> {
            if (msg.what == DowningTask.ID_NOTIFYDATASETCHANGED) {
                adapter.notifyDataSetChanged();
                RefreshBackground();
            }
            return false;
        });

        BaseHandleMessage.getInstance().addBaseHandleMessage(handler);

        Handler handler2 = new Handler(message -> {
            if (message.what == CHANGE_STATE) {
                changeEditorStatus();
                RefreshBackground();
            }
            return false;
        });
        BaseHandleMessage.getInstance().addBaseHandleMessage(handler2);
    }

    private void RefreshBackground() {
        if (MainApplication.DownTaskBadgeNum == 0) {
            tip.setVisibility(View.VISIBLE);
            tip_text.setVisibility(View.VISIBLE);
            downinglist.setVisibility(View.GONE);
        } else {
            tip_text.setVisibility(View.GONE);
            tip.setVisibility(View.GONE);
            downinglist.setVisibility(View.VISIBLE);
        }
    }

    void initStatusBar() {
        if (this.getSupportActionBar() != null) this.getSupportActionBar().hide();
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.md_theme_light_onBackground));
    }

    private void changeEditorStatus() {
        mEditMode = mEditMode == VideoActivity.MYLIVE_MODE_CHECK ? VideoActivity.MYLIVE_MODE_EDIT : VideoActivity.MYLIVE_MODE_CHECK;
        if (mEditMode == VideoActivity.MYLIVE_MODE_EDIT) {
            editor_iv.setImageResource(R.drawable.icon_download);
            editorStatus = true;
            delete_bottom_dialog.setVisibility(View.VISIBLE);
        } else {
            editor_iv.setImageResource(R.drawable.icon_choose);
            index = 0;
            for (int i = 0; i < BrowserActivity.Tasks.size(); i++)
                BrowserActivity.Tasks.get(i).setIsSelect(1);
            editorStatus = false;
            delete_bottom_dialog.setVisibility(View.GONE);
            clearAll();
        }
        adapter.setEditMode(mEditMode);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.editor_iv:
                changeEditorStatus();
                break;
            case R.id.goBackButton:
                Intent intent = new Intent(this, BrowserActivity.class);
                startActivity(intent);
                break;
            case R.id.to_video_center:
                Intent intent2 = new Intent(DowningActivity.this, VideoActivity.class);
                startActivity(intent2);
                break;
            case R.id.select_all:
                selectAllMain();
                break;
            case R.id.btn_delete:
                deleteVideo();
                break;
            default:
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    private void deleteVideo() {
        if (index == 0) {
            btn_delete.setEnabled(false);
            return;
        }
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View dialogView = View.inflate(this, R.layout.dialog_edit, null);
        LinearLayout ll = dialogView.findViewById(R.id.ll);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ll.getLayoutParams();
        lp.topMargin = 0;
        ll.setLayoutParams(lp);

        builder.setView(dialogView);
        if (index == 1)
            builder.setMessage("删除后不可恢复，是否删除该条目？");
        else
            builder.setMessage("删除后不可恢复，是否删除这" + index + "个条目？");
        builder.setTitle("删除");

        TextInputLayout editTopLayout = dialogView.findViewById(R.id.editTopLayout);
        editTopLayout.setVisibility(View.GONE);
        TextInputLayout editBottomLayout = dialogView.findViewById(R.id.editBottomLayout);
        editBottomLayout.setVisibility(View.GONE);
        Button ib_cancel = dialogView.findViewById(R.id.editCancel);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
        ib_cancel.setOnClickListener(view3 -> dialog.cancel());
        Button ib_ok = dialogView.findViewById(R.id.editOK);
        ib_ok.setOnClickListener(view12 -> {
            Iterator<DowningTask> iterator = BrowserActivity.Tasks.iterator();
            while (iterator.hasNext()) {
                RecordAction action = new RecordAction(MainApplication.mainApplication);
                action.open(true);
                DowningTask task = iterator.next();
                if (task.getIsSelect() == 2 && task.getmState() == 1) {
                    iterator.remove();
                    action.delTask(task.getmUuid());
                    FileUtil.deleteFile(task.getmFilePath());
                    task.getDownloadTask().discard();
                    MainApplication.DownTaskBadgeNum--;
                    BaseHandleMessage.getInstance().setHandlerMessage(DowningTask.ID_DOWNNUM, MainApplication.DownTaskBadgeNum);
                }
                action.close();
            }
            mEditMode = VideoActivity.MYLIVE_MODE_CHECK;
            editor_iv.setImageResource(R.drawable.icon_choose);
            delete_bottom_dialog.setVisibility(View.GONE);
            index = 0;
            tv_select_num.setText(String.valueOf(0));
            setBtnBackground(index);
            adapter.setEditMode(mEditMode);
            dialog.dismiss();
            RefreshBackground();
        });

    }

    private void selectAllMain() {
        if (adapter == null) return;
        if (!isSelectAll) {
            index = 0;
            for (int i = 0; i < BrowserActivity.Tasks.size(); i++) {
                if (BrowserActivity.Tasks.get(i).getmState() == 1) {
                    BrowserActivity.Tasks.get(i).setIsSelect(2);
                    index++;
                }
            }
            btn_delete.setEnabled(true);
            select_all.setText("取消全选");
            isSelectAll = true;
        } else {
            for (int i = 0; i < BrowserActivity.Tasks.size(); i++) {
                if (BrowserActivity.Tasks.get(i).getmState() == 1)
                    BrowserActivity.Tasks.get(i).setIsSelect(1);
            }
            index = 0;
            btn_delete.setEnabled(false);
            select_all.setText("全选");
            isSelectAll = false;
        }
        adapter.notifyDataSetChanged();
        setBtnBackground(index);
        tv_select_num.setText(String.valueOf(index));
    }

    private void clearAll() {
        tv_select_num.setText(String.valueOf(0));
        isSelectAll = false;
        select_all.setText("全选");
        setBtnBackground(0);
    }

    private void setBtnBackground(int size) {
        if (size != 0) {
            btn_delete.setBackgroundResource(R.drawable.button_shape);
            btn_delete.setEnabled(true);
        } else {
            btn_delete.setBackgroundResource(R.drawable.button_noclickable_shape);
            btn_delete.setEnabled(false);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onItemClickListener(DowningTask downingTask) {
        if (editorStatus) {
            if (downingTask.getIsSelect() == 1) {
                index++;
                downingTask.setIsSelect(2);
                if (index == MainApplication.DownTaskBadgeNum) {
                    isSelectAll = true;
                    select_all.setText("取消全选");
                }
            } else {
                downingTask.setIsSelect(1);
                index--;
                isSelectAll = false;
                select_all.setText("全选");
            }
            setBtnBackground(index);
            tv_select_num.setText(String.valueOf(index));
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        for (int i = 0; i < BrowserActivity.Tasks.size(); i++)
            BrowserActivity.Tasks.get(i).setIsSelect(1);
        super.onBackPressed();
    }
}