package de.baumann.browser.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.baumann.browser.MainApplication;
import de.baumann.browser.R;
import de.baumann.browser.database.RecordAction;
import de.baumann.browser.entity.DowningTask;

import de.baumann.browser.entity.LocalVideoInfo;
import de.baumann.browser.event.ShowToastMessageEvent;
import de.baumann.browser.util.BaseHandleMessage;
import de.baumann.browser.util.DisplayUtils;
import de.baumann.browser.util.FileUtil;
import de.baumann.browser.view.AdapterVideoList;

public class VideoActivity extends AppCompatActivity implements View.OnClickListener, AdapterVideoList.OnItemClickListener {
    private RecyclerView videoRV;
    private ImageView goBackButton;
    private ImageView editor_iv;
    AdapterVideoList adapter = null;
    private int total_download_num = 0;
    private LinearLayout delete_bottom_dialog;
    public static final int CHANGE_MODE = 90;
    public static final int MYLIVE_MODE_CHECK = 0;
    public static final int MYLIVE_MODE_EDIT = 1;
    private int mEditMode = MYLIVE_MODE_CHECK;
    private boolean editorStatus = false;
    private int index = 0;
    private boolean isSelectAll = false;
    private TextView tv_select_num;                    //选中删除的数量
    private TextView select_all;                       //全选
    private TextView btn_delete;                         //删除按钮
    private ImageView tip;
    private TextView text_tip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        initStatusBar();
        initView();
        initMain();
    }

    void initStatusBar() {
        if (this.getSupportActionBar() != null) this.getSupportActionBar().hide();
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.md_theme_light_onBackground));
    }

    //初始化事件
    private void initMain() {
        goBackButton.setOnClickListener(this);
        adapter = new AdapterVideoList(this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        videoRV.setLayoutManager(manager);
        videoRV.addItemDecoration(new DisplayUtils.SpacesItemDecoration());
        videoRV.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
        editor_iv.setOnClickListener(this);

        initVideoNum();
        RefreshBackground();

        Handler handler = new Handler(message -> {
            if (message.what == CHANGE_MODE) {
                changeEditorStatus();
            } else if (message.what == AdapterVideoList.SINGLEFILEDEL) {
                initVideoNum();
                RefreshBackground();
            }
            return false;
        });
        BaseHandleMessage.getInstance().addBaseHandleMessage(handler);
    }

    void initVideoNum() {
        total_download_num = 0;
        for (int i = 0; i < BrowserActivity.Tasks.size(); i++) {
            if (BrowserActivity.Tasks.get(i).getmState() == 2)
                total_download_num++;
        }
    }

    private void RefreshBackground() {
        if (total_download_num == 0) {
            tip.setVisibility(View.VISIBLE);
            text_tip.setVisibility(View.VISIBLE);
            videoRV.setVisibility(View.GONE);
        } else {
            tip.setVisibility(View.GONE);
            text_tip.setVisibility(View.GONE);
            videoRV.setVisibility(View.VISIBLE);
        }
    }

    //初始化控件
    private void initView() {
        videoRV = findViewById(R.id.videoRV);
        goBackButton = findViewById(R.id.goBackButton);
        editor_iv = findViewById(R.id.editor_iv);
        delete_bottom_dialog = findViewById(R.id.delete_ll);
        tv_select_num = findViewById(R.id.tv_select_num);
        select_all = findViewById(R.id.select_all);
        select_all.setOnClickListener(this);
        btn_delete = findViewById(R.id.btn_delete);
        btn_delete.setOnClickListener(this);
        tip = findViewById(R.id.tip);
        text_tip = findViewById(R.id.text_tip);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.editor_iv:
                if (total_download_num > 0)
                    changeEditorStatus();
                break;
            case R.id.goBackButton:
                onBackPressed();
                break;
            case R.id.btn_delete:
                deleteVideo();
                break;
            case R.id.select_all:
                selectAllMain();
                break;
            default:
                break;
        }
    }

    void changeEditorStatus() {
        mEditMode = mEditMode == MYLIVE_MODE_CHECK ? MYLIVE_MODE_EDIT : MYLIVE_MODE_CHECK;
        if (mEditMode == MYLIVE_MODE_EDIT) {
//            editor_iv.setImageResource(R.drawable.icon_download);
            editorStatus = true;
            delete_bottom_dialog.setVisibility(View.VISIBLE);
        } else {
//            editor_iv.setImageResource(R.drawable.icon_choose);
            index = 0;
            for (int i = 0; i < BrowserActivity.Tasks.size(); i++)
                BrowserActivity.Tasks.get(i).setIsSelect(1);
            editorStatus = false;
            delete_bottom_dialog.setVisibility(View.GONE);
            clearAll();
        }
        adapter.setEditMode(mEditMode);
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
                if (task.getIsSelect() == 2 && task.getmState() == 2) {
                    iterator.remove();
                    action.delTask(task.getmUuid());
                    FileUtil.deleteFile(task.getmFilePath());
                }
                action.close();
                initVideoNum();
                RefreshBackground();
            }
            editorStatus = false;
            mEditMode = MYLIVE_MODE_CHECK;
            editor_iv.setImageResource(R.drawable.icon_choose);
            delete_bottom_dialog.setVisibility(View.GONE);
            index = 0;
            tv_select_num.setText(String.valueOf(0));
            setBtnBackground(index);
            adapter.setEditMode(mEditMode);
            dialog.dismiss();
        });
    }

    private void selectAllMain() {
        if (adapter == null) return;
        if (!isSelectAll) {
            index = 0;
            for (int i = 0; i < BrowserActivity.Tasks.size(); i++) {
                if (BrowserActivity.Tasks.get(i).getmState() == 2) {
                    BrowserActivity.Tasks.get(i).setIsSelect(2);
                    index++;
                }
            }
            btn_delete.setEnabled(true);
            select_all.setText("取消全选");
            isSelectAll = true;
        } else {
            for (int i = 0; i < BrowserActivity.Tasks.size(); i++) {
                if (BrowserActivity.Tasks.get(i).getmState() == 2)
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

    private void setBtnBackground(int size) {
        if (size != 0) {
            btn_delete.setBackgroundResource(R.drawable.button_shape);
            btn_delete.setEnabled(true);
        } else {
            btn_delete.setBackgroundResource(R.drawable.button_noclickable_shape);
            btn_delete.setEnabled(false);
        }
    }

    private void clearAll() {
        tv_select_num.setText(String.valueOf(0));
        isSelectAll = false;
        select_all.setText("全选");
        setBtnBackground(0);
    }

    @Override
    public void onItemClickListener(DowningTask downingTask) {
        if (editorStatus) {
            Log.i("TAG", "onItemClickListener: 总数" + total_download_num);
            if (downingTask.getIsSelect() == 1) {
                index++;
                downingTask.setIsSelect(2);
                if (index == total_download_num) {
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
        if (editorStatus) {
            Log.i("TAG", "onBackPressed: 执行变化");
            for (int i = 0; i < BrowserActivity.Tasks.size(); i++)
                BrowserActivity.Tasks.get(i).setIsSelect(1);
            changeEditorStatus();
        } else
            super.onBackPressed();

    }
}