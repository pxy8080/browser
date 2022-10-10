package de.baumann.browser.activity;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

import java.util.HashMap;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.baumann.browser.MainApplication;
import de.baumann.browser.R;
import de.baumann.browser.database.FaviconHelper;
import de.baumann.browser.database.Record;
import de.baumann.browser.database.RecordAction;

import de.baumann.browser.event.RefreshHistoryEvent;
import de.baumann.browser.unit.BrowserUnit;
import de.baumann.browser.unit.RecordUnit;


public class HistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RelativeLayout floatGroupLayout;
    private List<Map<String, Object>> list;
    private ImageView icon_back;
    Rv_History_Adapter adapter;
    private TextView clear_all_history;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        initStatusBar();
        EventBus.getDefault().register(this);
        initView();

        initData();
        adapter = new Rv_History_Adapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    private void initView() {
        floatGroupLayout = findViewById(R.id.floatGroupLayout);
        recyclerView = findViewById(R.id.recycler_view);
        icon_back = findViewById(R.id.icon_back);
        icon_back.setOnClickListener(view -> onBackPressed());      //返回键点击事件
        clear_all_history = findViewById(R.id.clear_all_history);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @SuppressLint("UnsafeOptInUsageError")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void NewDownTaskDel(RefreshHistoryEvent refreshHistoryEvent) {
        initData();
        adapter = new Rv_History_Adapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void initData() {
        list = new ArrayList<>();

        RecordAction action = new RecordAction(HistoryActivity.this);
        action.open(false);
        final List<Record> listhistory;
        listhistory = action.listHistory();
        Collections.reverse(listhistory);
        action.close();
        Set<String> recordset = new TreeSet<>((s, t1) -> {
            @SuppressLint("SimpleDateFormat") DateFormat f = new SimpleDateFormat("MM/dd/yyyy");
            try {
                return f.parse(t1).compareTo(f.parse(s));
            } catch (ParseException e) {
                throw new IllegalArgumentException(e);
            }
        });

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

        for (int i = 0; i < listhistory.size(); i++) {
            recordset.add(sdf.format(listhistory.get(i).getTime()));
        }

        //遍历set
        for (String str : recordset) {
            Map<String, Object> llmap = new HashMap<>();

            llmap.put("date", str);
            List<Record> colList = new ArrayList<>();
            for (int i = 0; i < listhistory.size(); i++) {
                if (str.equals(sdf.format(listhistory.get(i).getTime()))) {
                    colList.add(listhistory.get(i));
                }
            }
            llmap.put("col", colList);
            list.add(llmap);
        }
        //要放在初始后
        floatGroupLayout.setVisibility(View.VISIBLE);

        clear_all_history.setOnClickListener(view -> {
            action.open(true);
            action.deleteAll( RecordUnit.TABLE_HISTORY);
            action.close();
            list.clear();
            adapter.notifyDataSetChanged();
        });
    }

    public class Rv_History_Adapter extends RecyclerView.Adapter<Rv_History_Adapter.HistoryHolder> {
        private final Context context;

        public Rv_History_Adapter(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public Rv_History_Adapter.HistoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.item_history_head, null);
            return new HistoryHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Rv_History_Adapter.HistoryHolder holder, int position) {
            Map<String, Object> listItem = list.get(position);
            String date = listItem.get("date").toString();
            holder.tvDate.setText(date);

            List<Record> itemList = (List<Record>) listItem.get("col");
            for (int i = 0; i < itemList.size(); i++) {
                View itemView = LayoutInflater.from(context).inflate(R.layout.item_history_content, null);
                TextView history_title = itemView.findViewById(R.id.history_title);
                TextView history_content = itemView.findViewById(R.id.history_content);
                ImageView hostory_icon = itemView.findViewById(R.id.hostory_icon);
                RelativeLayout item_history = itemView.findViewById(R.id.item_history);
                ImageView item_history_del = itemView.findViewById(R.id.item_history_del);

                FaviconHelper faviconHelper = new FaviconHelper(MainApplication.mainApplication);
                Bitmap bitmap = faviconHelper.getFavicon(itemList.get(i).getURL());
                if (bitmap != null) {
                    hostory_icon.setImageBitmap(bitmap);
                } else {
                    hostory_icon.setImageResource(R.drawable.icon_image_broken);
                }

                history_content.setText(itemList.get(i).getURL());
                history_title.setText(itemList.get(i).getTitle());
                int finalI = i;
                item_history.setOnClickListener(v -> {
                    Uri webpage = Uri.parse(itemList.get(finalI).getURL());
                    BrowserUnit.intentURL(context, webpage);
                });
                item_history_del.setOnClickListener(view -> {
                    RecordAction action = new RecordAction(context);
                    action.open(true);
                    action.deleteURL(itemList.get(finalI).getURL(), RecordUnit.TABLE_HISTORY);
                    action.close();
                    EventBus.getDefault().post(new RefreshHistoryEvent());
                });
                holder.groupLayout.addView(itemView);
            }
        }


        @Override
        public int getItemCount() {
            return list.size();
        }

        public class HistoryHolder extends RecyclerView.ViewHolder {
            private final LinearLayout groupLayout;
            private final TextView tvDate;

            public HistoryHolder(@NonNull View itemView) {
                super(itemView);
                groupLayout = itemView.findViewById(R.id.groupLayout);
                tvDate = itemView.findViewById(R.id.tvDate);
            }
        }

    }
    //初始化状态栏
    void initStatusBar() {
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.md_theme_light_onBackground));
    }
}