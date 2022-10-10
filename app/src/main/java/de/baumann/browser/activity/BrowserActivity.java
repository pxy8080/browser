package de.baumann.browser.activity;

import static android.content.ContentValues.TAG;
import static android.webkit.WebView.HitTestResult.IMAGE_TYPE;
import static android.webkit.WebView.HitTestResult.SRC_ANCHOR_TYPE;
import static android.webkit.WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE;
import static de.baumann.browser.database.RecordAction.BOOKMARK_ITEM;
import static de.baumann.browser.database.RecordAction.STARTSITE_ITEM;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yausername.youtubedl_android.mapper.VideoFormat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import de.baumann.browser.MainApplication;
import de.baumann.browser.R;
import de.baumann.browser.browser.AlbumController;
import de.baumann.browser.browser.BrowserContainer;
import de.baumann.browser.browser.BrowserController;
import de.baumann.browser.browser.DataURIParser;
import de.baumann.browser.browser.List_protected;
import de.baumann.browser.browser.List_standard;
import de.baumann.browser.browser.List_trusted;
import de.baumann.browser.database.FaviconHelper;
import de.baumann.browser.database.Record;
import de.baumann.browser.database.RecordAction;
import de.baumann.browser.entity.DetectedVideoInfo;
import de.baumann.browser.entity.DowningTask;

import de.baumann.browser.event.ShowToastMessageEvent;
import de.baumann.browser.event.VideoDectedEvent;
import de.baumann.browser.unit.BrowserUnit;
import de.baumann.browser.unit.HelperUnit;
import de.baumann.browser.unit.RecordUnit;
import de.baumann.browser.util.BaseHandleMessage;
import de.baumann.browser.util.GlideUtil;
import de.baumann.browser.util.UUIDUtil;
import de.baumann.browser.util.VideoDected;
import de.baumann.browser.util.ViewUtil;
import de.baumann.browser.view.AdapterDetectedVideo;
import de.baumann.browser.view.AdapterSearch;
import de.baumann.browser.view.GridAdapter;
import de.baumann.browser.view.GridItem;
import de.baumann.browser.view.NinjaToast;
import de.baumann.browser.view.NinjaWebView;
import de.baumann.browser.view.AdapterRecord;
import de.baumann.browser.view.SwipeTouchListener;


public class BrowserActivity extends AppCompatActivity implements BrowserController, AdapterDetectedVideo.OnItemClickListener {
    // Menus
    private static final int INPUT_FILE_REQUEST_CODE = 1;
    private AdapterRecord adapter;
    private RelativeLayout omniBox;
    // Views
    private EditText omniBox_text;
    private EditText searchBox;
    private RelativeLayout bottomSheetDialog_OverView;
    private NinjaWebView ninjaWebView;
    private View customView;
    private VideoView videoView;
    private KeyListener listener;
    private BadgeDrawable DownTaskBadgeNum;
    private BadgeDrawable DownloadedBadgeNum;
    // Layouts
    private ProgressBar progressBar;
    private RelativeLayout searchPanel;
    private FrameLayout contentFrame;
    private LinearLayout tab_container;
    private LinearLayout tab_container2;
    private FrameLayout fullscreenHolder;
    private ListView list_search;
    private ListView mainlist;   //历史记录and书签的listview
    private int current = 0;    //记录当前是历史记录还是书签
    // Others
    private ImageView omnibox_close;
    private ImageButton searchBox_refresh;
    private BottomNavigationView bottom_navigation;
    private BottomNavigationView bottom_op;
    private LinearLayout bottomAppBar;
    private String overViewTab;
    private BroadcastReceiver downloadReceiver;
    private Activity activity;
    private Context context;
    private SharedPreferences sp;
    private List_trusted listTrusted;
    private List_standard listStandard;
    private List_protected listProtected;

    private long newIcon;
    private long filterBy;
    private boolean filter;
    private boolean isNightMode;
    private boolean orientationChanged;

    private ValueCallback<Uri[]> filePathCallback = null;
    private AlbumController currentAlbumController = null;
    private ValueCallback<Uri[]> mFilePathCallback;

    private ImageView multitab;             //多窗口
    private ImageView back;                 //返回按钮
    private DrawerLayout drawerLayout;      //整体布局DrawerLayout
    private TextView righht_dw_tv;          //右滑窗口只是label
    private LinearLayout dialog_findvideo;//发现视频弹窗
    private LinearLayout VideoManagement;   //视频管理
    private LinearLayout downing_view;     //下载管理
    private ImageView bt_home;              //Home按钮
    private ImageView icon_video;           //视频管理图标
    private ImageView icon_downing;         //下载中图标
    private TextInputEditText home_input;
    private RelativeLayout sample_website;
    private FloatingActionButton action_button;       //发现视频
    private VideoFormat videoFormat;  //选中的视频分辨率

    public Bitmap favicon() {
        return ninjaWebView.getFavicon();
    }

    public static List<DowningTask> Tasks = new ArrayList<>();

    private AlbumController nextAlbumController(boolean next) {
        if (BrowserContainer.size() <= 1) return currentAlbumController;
        List<AlbumController> list = BrowserContainer.list();
        int index = list.indexOf(currentAlbumController);
        if (next) {
            index++;
            if (index >= list.size()) index = 0;
        } else {
            index--;
            if (index < 0) index = list.size() - 1;
        }
        return list.get(index);
    }


    private class VideoCompletionListener implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            return false;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            onHideCustomView();
        }
    }

    @Override
    public void onPause() {
        //Save open Tabs in shared preferences
        saveOpenedTabs();
        super.onPause();
    }

    //初始化状态栏
    void initStatusBar() {
        if (this.getSupportActionBar() != null) this.getSupportActionBar().hide();
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.md_theme_light_onBackground));
    }

    // Overrides
    @SuppressLint("CheckResult")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = BrowserActivity.this;
        context = BrowserActivity.this;
        sp = PreferenceManager.getDefaultSharedPreferences(context);

        initStatusBar();

        if (sp.getBoolean("sp_screenOn", false))
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Objects.equals(sp.getString("nightMode", "2"), "3")) isNightMode = true;
        if (Objects.equals(sp.getString("nightMode", "2"), "2")) isNightMode = false;
        if (Objects.equals(sp.getString("nightMode", "2"), "1")) {
            int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            switch (nightModeFlags) {
                case Configuration.UI_MODE_NIGHT_YES:
                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    isNightMode = true;
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    isNightMode = false;
                    break;
            }
        }

        HelperUnit.initTheme(activity);

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.colorSecondary, typedValue, true);
//        colorSecondary = typedValue.data;

        OrientationEventListener mOrientationListener = new OrientationEventListener(getApplicationContext()) {
            @Override
            public void onOrientationChanged(int orientation) {
                orientationChanged = true;
            }
        };
        if (mOrientationListener.canDetectOrientation()) mOrientationListener.enable();

        sp.edit()
                .putInt("restart_changed", 0)
                .putBoolean("pdf_create", false).putBoolean("redirect", sp.getBoolean("sp_youTube_switch", false) || sp.getBoolean("sp_twitter_switch", false) || sp.getBoolean("sp_instagram_switch", false))
                .putString("profile", sp.getString("profile_toStart", "profileStandard")).apply();

        switch (Objects.requireNonNull(sp.getString("start_tab", "3"))) {
            case "3":
                overViewTab = getString(R.string.album_title_bookmarks);
                break;
            case "4":
                overViewTab = getString(R.string.album_title_history);
                break;
            default:
                overViewTab = getString(R.string.album_title_home);
                break;
        }

        setContentView(R.layout.activity_main);
        //清空嗅探的视频
        VideoDected.ClearQueue();
        //这里开始重载下载任务
        RecordAction action = new RecordAction(MainApplication.mainApplication);
        action.open(false);
        Tasks = action.ListAllDownTask();
        action.close();

        //关闭drawerLayout收拾滑动
        drawerLayout = findViewById(R.id.drawerlayout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        contentFrame = findViewById(R.id.main_content);


        downloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
                builder.setTitle(R.string.menu_download);
                builder.setIcon(R.drawable.icon_alert);
                builder.setMessage(R.string.toast_downloadComplete);
                builder.setPositiveButton(R.string.app_ok, (dialog, whichButton) -> startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)));
                builder.setNegativeButton(R.string.app_cancel, (dialog, whichButton) -> dialog.cancel());
                Dialog dialog = builder.create();
                dialog.show();
                HelperUnit.setupDialog(context, dialog);
            }
        };

        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadReceiver, filter);


        initNavigation();
        initOmniBox();
        initSearchPanel();
        initOverview();
        initwebsite();

        //restore open Tabs from shared preferences if app got killed
        if (sp.getBoolean("sp_restoreTabs", false)
                || sp.getBoolean("sp_reloadTabs", false)
                || sp.getBoolean("restoreOnRestart", false)) {
            String saveDefaultProfile = sp.getString("profile", "profileStandard");
            ArrayList<String> openTabs;
            ArrayList<String> openTabsProfile;
            openTabs = new ArrayList<>(Arrays.asList(TextUtils.split(sp.getString("openTabs", ""), "‚‗‚")));
            openTabsProfile = new ArrayList<>(Arrays.asList(TextUtils.split(sp.getString("openTabsProfile", ""), "‚‗‚")));
            if (openTabs.size() > 0) {
                for (int counter = 0; counter < openTabs.size(); counter++) {
                    addAlbum(getString(R.string.app_name), openTabs.get(counter), BrowserContainer.size() < 1, false, openTabsProfile.get(counter));
                }
            }
            sp.edit().putString("profile", saveDefaultProfile).apply();
            sp.edit().putBoolean("restoreOnRestart", false).apply();
        }
        mainlist = findViewById(R.id.history_bookmark_list);
        righht_dw_tv = findViewById(R.id.righht_dw_tv);
        initlistview(current);

        dispatchIntent(getIntent());
    }


    private void initwebsite() {
        sample_website = findViewById(R.id.sample_website);
        LinearLayout to_vimeo = findViewById(R.id.to_vimeo);
        LinearLayout to_twitter = findViewById(R.id.to_twitter);
        LinearLayout to_dailymotion = findViewById(R.id.to_dailymotion);
        LinearLayout to_youtube = findViewById(R.id.to_youtube);
        home_input = findViewById(R.id.home_input);
        home_input.setFocusable(false);
        home_input.setOnClickListener(view -> {
            sample_website.setVisibility(View.GONE);
            bottomAppBar.setVisibility(View.VISIBLE);
            bt_home.setVisibility(View.VISIBLE);
            omniBox_text.setVisibility(View.VISIBLE);
            omniBox_text.setFocusable(true);
            omniBox_text.setFocusableInTouchMode(true);
            omniBox_text.requestFocus();
            omniBox_text.findFocus();
        });
        to_vimeo.setOnClickListener(view -> ninjaWebView.loadUrl("https://www.vimeo.com/"));
        to_twitter.setOnClickListener(view -> ninjaWebView.loadUrl("https://mobile.twitter.com/explore/"));
        to_dailymotion.setOnClickListener(view -> ninjaWebView.loadUrl("https://www.dailymotion.com/video"));
        to_youtube.setOnClickListener(view -> ninjaWebView.loadUrl("https://www.youtube.com/"));
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onItemClickListener(VideoFormat videoFormat) {
//        textView.setBackgroundColor(R.color.blue);
        this.videoFormat = videoFormat;
    }

    //初始化导航栏
    @SuppressLint({"UseCompatLoadingForDrawables", "UnsafeOptInUsageError"})
    private void initNavigation() {
        downing_view = findViewById(R.id.downing_view);
        dialog_findvideo = findViewById(R.id.dialog_findvideo);               //发现视频的弹窗view
        //视频中心
        VideoManagement = findViewById(R.id.VideoManagement);                           //导航栏第四个视频图标

        icon_video = findViewById(R.id.icon_video);
        icon_downing = findViewById(R.id.icon_downing);

        VideoManagement.setOnClickListener(view -> {
            Intent intent = new Intent(context, VideoActivity.class);
            MainApplication.DownloadedBadgeNum = 0;
            BaseHandleMessage.getInstance().setHandlerMessage(DowningTask.ID_DOWNNUM, MainApplication.DownTaskBadgeNum);
            startActivity(intent);
        });


        downing_view.setOnClickListener(view -> {
            Intent intent = new Intent(BrowserActivity.this, DowningActivity.class);
            startActivity(intent);
        });
        action_button = findViewById(R.id.action_button);


        action_button.setOnClickListener(view -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);

            View dialogView = View.inflate(context, R.layout.dialog_findvideo, null);
            builder.setView(dialogView);
            builder.setTitle("发现视频");
            builder.setMessage(null);

            //视频第一帧
            ImageView detected_video_frame = dialogView.findViewById(R.id.detected_video_frame);
            //视频名
            EditText detected_video_name = dialogView.findViewById(R.id.detected_video_name);
            //重命名
            TextView detected_video_rename = dialogView.findViewById(R.id.detected_video_rename);
            //立即下载
            Button btn_downnow = dialogView.findViewById(R.id.btn_downnow);

            TextView video_duration = dialogView.findViewById(R.id.video_duration);
            RecyclerView videolist_rv = dialogView.findViewById(R.id.videolist_rv);
            androidx.appcompat.app.AlertDialog dialog2 = builder.create();
            Window window = dialog2.getWindow();
            if (window != null)
                window.setGravity(Gravity.BOTTOM);
            if (VideoDected.getdetectedVideo() != null) {
                dialog2.show();
                DetectedVideoInfo videoInfo = VideoDected.getdetectedVideo();
                GridLayoutManager manager = new GridLayoutManager(this, 4);
                AdapterDetectedVideo adapterDetectedVideo = new AdapterDetectedVideo(this, videoInfo.getVideoFormatlist());
                videolist_rv.setLayoutManager(manager);
                videolist_rv.setAdapter(adapterDetectedVideo);
                adapterDetectedVideo.setOnItemClickListener(this);

                String regEx = "[\n`~!@#$%^&*+=|{}':;',\\[\\].<>/?~！@#￥%……&*——+|{}【】‘；：”“’。，、？]";
                Integer uuid = UUIDUtil.getNumUUID();
                videoInfo.setSourcePageTitle(videoInfo.getSourcePageTitle().replaceAll(regEx, ""));
                GlideUtil.LoadVideoThumbnail(this, detected_video_frame, videoInfo.getVideoThumbnail());
                video_duration.setText(videoInfo.getmDuration());
                detected_video_name.setText(videoInfo.getSourcePageTitle());
                detected_video_name.setFocusable(false);

                detected_video_rename.setOnClickListener(view2 -> {
                    MaterialAlertDialogBuilder builder2 = new MaterialAlertDialogBuilder(activity);
                    View dialogView2 = View.inflate(activity, R.layout.dialog_edit, null);
                    builder2.setView(dialogView2);
                    builder2.setTitle("重命名");
                    builder2.setMessage(null);
                    TextInputLayout editTopLayout = dialogView2.findViewById(R.id.editTopLayout);
                    editTopLayout.setVisibility(View.GONE);
                    TextInputLayout editBottomLayout = dialogView2.findViewById(R.id.editBottomLayout);
                    EditText editBottom = dialogView2.findViewById(R.id.editBottom);
                    editBottomLayout.setHint(null);
                    editBottom.setHint("重命名");
                    editBottom.setText(videoInfo.getSourcePageTitle());
                    Button ib_cancel = dialogView2.findViewById(R.id.editCancel);
                    editBottom.selectAll();
                    editBottom.requestFocus();
                    InputMethodManager inputManager =
                            (InputMethodManager) editBottom.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.showSoftInput(editBottom, InputMethodManager.SHOW_FORCED);
                    AlertDialog dialog = builder2.create();
                    dialog.show();
                    ib_cancel.setOnClickListener(view3 -> dialog.cancel());
                    Button ib_ok = dialogView2.findViewById(R.id.editOK);
                    ib_ok.setOnClickListener(view12 -> {
                        videoInfo.setSourcePageTitle(editBottom.getText().toString().trim());
                        detected_video_name.setText(editBottom.getText().toString().trim());
                        dialog.dismiss();
                    });

                });

                btn_downnow.setOnClickListener(view1 -> {
                    if (videoFormat == null) {
                        EventBus.getDefault().post(new ShowToastMessageEvent("请选择一个分辨率"));
                        return;
                    }
                    Toast.makeText(context, "下载:" + videoInfo.getSourcePageTitle() + ".mp4", Toast.LENGTH_SHORT).show();

                    DowningTask task = new DowningTask(uuid, videoFormat.getUrl(), videoInfo.getSourcePageTitle() + ".mp4",
                            videoInfo.getSourcePageUrl(), MainApplication.appConfig.rootDownPath + videoInfo.getSourcePageTitle() + ".mp4", videoInfo.getVideoThumbnail(), videoInfo.getmDuration(), 1);
                    Tasks.add(task);
                    RecordAction action = new RecordAction(context);
                    action.open(false);
                    action.addTask(uuid, task);
                    action.close();
                    dialog_findvideo.setVisibility(View.GONE);
                    VideoDected.ClearQueue();
                    videoFormat = null;
                    EventBus.getDefault().post(new VideoDectedEvent());
                    dialog2.dismiss();
                });
            } else {
                EventBus.getDefault().post(new ShowToastMessageEvent("当前页面还没有检测到视频"));
            }

        });

        initBadgeDrawable();
        Handler handler = new Handler(message -> {
            if (message.what == DowningTask.ID_DOWNNUM) {
                DownTaskBadgeNum.setNumber(MainApplication.DownTaskBadgeNum);
                BadgeUtils.attachBadgeDrawable(DownTaskBadgeNum, icon_downing, findViewById(R.id.layout));
                DownTaskBadgeNum.setVisible(MainApplication.DownTaskBadgeNum > 0);

                DownloadedBadgeNum.setNumber(MainApplication.DownloadedBadgeNum);
                BadgeUtils.attachBadgeDrawable(DownloadedBadgeNum, icon_video, findViewById(R.id.layout));
                DownloadedBadgeNum.setVisible(MainApplication.DownloadedBadgeNum > 0);
            }
            return false;
        });
        BaseHandleMessage.getInstance().addBaseHandleMessage(handler);
    }

    void initBadgeDrawable() {
        DownTaskBadgeNum = BadgeDrawable.create(context);
        DownTaskBadgeNum.setBadgeGravity(BadgeDrawable.TOP_END);
        DownTaskBadgeNum.setVerticalOffset(20);
        DownTaskBadgeNum.setHorizontalOffset(20);
        DownTaskBadgeNum.setBackgroundColor(Color.parseColor("#ff0000"));

        DownloadedBadgeNum = BadgeDrawable.create(context);
        DownloadedBadgeNum.setBadgeGravity(BadgeDrawable.TOP_END);
        DownloadedBadgeNum.setVerticalOffset(20);
        DownloadedBadgeNum.setHorizontalOffset(20);
        DownloadedBadgeNum.setBackgroundColor(Color.parseColor("#ff0000"));
    }

    //书签栏和历史记录列表初始化
    private void initlistview(int current) {
        righht_dw_tv.setOnClickListener(view -> drawerLayout.closeDrawer(GravityCompat.END));
        if (current == 2) {
            righht_dw_tv.setText("书签栏");
            tab_container.setVisibility(View.GONE);
            mainlist.setVisibility(View.VISIBLE);
            overViewTab = getString(R.string.album_title_bookmarks);
            RecordAction action = new RecordAction(context);
            action.open(false);
            final List<Record> list;
            list = action.listBookmark(activity, filter, filterBy);
            action.close();
            adapter = new AdapterRecord(context, list);
            mainlist.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            filter = false;
            mainlist.setOnItemClickListener((parent, view, position, id) -> {
                if (list.get(position).getType() == BOOKMARK_ITEM || list.get(position).getType() == STARTSITE_ITEM) {
                    if (list.get(position).getDesktopMode() != ninjaWebView.isDesktopMode())
                        ninjaWebView.toggleDesktopMode(false);
                    if (list.get(position).getNightMode() == ninjaWebView.isNightMode() && !isNightMode) {
                        ninjaWebView.toggleNightMode();
                        isNightMode = ninjaWebView.isNightMode();
                    }
                }
                drawerLayout.closeDrawer(GravityCompat.END);
                ninjaWebView.loadUrl(list.get(position).getURL());
                hideOverview();
            });
            mainlist.setOnItemLongClickListener((parent, view, position, id) -> {
                showContextMenuList(list.get(position).getTitle(), list.get(position).getURL(), adapter, list, position);
                return true;
            });
        } else if (current == 1) {
            righht_dw_tv.setText("历史记录");
            tab_container.setVisibility(View.GONE);
            mainlist.setVisibility(View.VISIBLE);
            overViewTab = getString(R.string.album_title_history);
            RecordAction action = new RecordAction(context);
            action.open(false);
            final List<Record> list;
            list = action.listHistory();
            Collections.reverse(list);
            action.close();
            adapter = new AdapterRecord(context, list) {
                @Override
                public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                    View v = super.getView(position, convertView, parent);
                    //历史记录时间
                    TextView record_item_time = v.findViewById(R.id.dateView);
                    record_item_time.setVisibility(View.VISIBLE);
                    //历史记录标题
                    TextView record_item_title = v.findViewById(R.id.titleView);
                    record_item_title.setPadding(0, 0, 150, 0);
                    ImageView iconView = v.findViewById(R.id.iconView);
                    iconView.setVisibility(View.VISIBLE);
                    iconView.setOnClickListener(view -> showContextMenuList(list.get(position).getTitle(), list.get(position).getURL(), adapter, list, position));
                    return v;
                }
            };

            mainlist.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            mainlist.setOnItemClickListener((parent, view, position, id) -> {
                if (list.get(position).getType() == BOOKMARK_ITEM || list.get(position).getType() == STARTSITE_ITEM) {
                    if (list.get(position).getDesktopMode() != ninjaWebView.isDesktopMode())
                        ninjaWebView.toggleDesktopMode(false);
                    if (list.get(position).getNightMode() == ninjaWebView.isNightMode() && !isNightMode) {
                        ninjaWebView.toggleNightMode();
                        isNightMode = ninjaWebView.isNightMode();
                    }
                }
                drawerLayout.closeDrawer(GravityCompat.END);
                ninjaWebView.loadUrl(list.get(position).getURL());
                hideOverview();
            });

            mainlist.setOnItemLongClickListener((parent, view, position, id) -> {
                showContextMenuList(list.get(position).getTitle(), list.get(position).getURL(), adapter, list, position);
                return true;
            });
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        Uri[] results = null;
        // Check that the response is a good one
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                // If there is not data, then we may have taken a photo
                String dataString = data.getDataString();
                if (dataString != null) results = new Uri[]{Uri.parse(dataString)};
            }
        }
        mFilePathCallback.onReceiveValue(results);
        mFilePathCallback = null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (sp.getBoolean("sp_camera", false)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            }
        }
        if (sp.getInt("restart_changed", 1) == 1) {
            saveOpenedTabs();
            HelperUnit.triggerRebirth(context);
        }
        if (sp.getBoolean("pdf_create", false)) {
            sp.edit().putBoolean("pdf_create", false).apply();
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
            builder.setTitle(R.string.menu_download);
            builder.setIcon(R.drawable.icon_alert);
            builder.setMessage(R.string.toast_downloadComplete);
            builder.setPositiveButton(R.string.app_ok, (dialog, whichButton) -> startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)));
            builder.setNegativeButton(R.string.app_cancel, (dialog, whichButton) -> dialog.cancel());
            AlertDialog dialog = builder.create();
            dialog.show();
            HelperUnit.setupDialog(context, dialog);
        }
        dispatchIntent(getIntent());
    }

    @Override
    public void onDestroy() {

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(2);
        notificationManager.cancel(1);

        if (sp.getBoolean("sp_clear_quit", true)) {
            boolean clearCache = sp.getBoolean("sp_clear_cache", true);
            boolean clearCookie = sp.getBoolean("sp_clear_cookie", false);
            boolean clearHistory = sp.getBoolean("sp_clear_history", false);
            boolean clearIndexedDB = sp.getBoolean("sp_clearIndexedDB", true);
            if (clearCache) BrowserUnit.clearCache(this);
            if (clearCookie) BrowserUnit.clearCookie();
            if (clearHistory) BrowserUnit.clearHistory(this);
            if (clearIndexedDB) {
                BrowserUnit.clearIndexedDB(this);
                WebStorage.getInstance().deleteAllData();
            }
        }

        BrowserContainer.clear();

        if (!sp.getBoolean("sp_reloadTabs", false) || sp.getInt("restart_changed", 1) == 1) {
            sp.edit().putString("openTabs", "").apply();   //clear open tabs in preferences
            sp.edit().putString("openTabsProfile", "").apply();
        }

        unregisterReceiver(downloadReceiver);
        super.onDestroy();
    }

    //重写返回事件
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
//            case KeyEvent.KEYCODE_MENU:
//                showOverflow();
            case KeyEvent.KEYCODE_BACK:
                if (bottomAppBar.getVisibility() == View.GONE) hideOverview();
                else if (fullscreenHolder != null || customView != null || videoView != null)
                    Log.v(TAG, "FOSS Browser in fullscreen mode");
                else if (list_search.getVisibility() == View.VISIBLE) omniBox_text.clearFocus();
                else if (searchPanel.getVisibility() == View.VISIBLE) {
                    searchBox.setText("");
                    searchPanel.setVisibility(View.GONE);
                    omniBox.setVisibility(View.VISIBLE);
                } else if (ninjaWebView.canGoBack()) {
                    WebBackForwardList mWebBackForwardList = ninjaWebView.copyBackForwardList();
                    String historyUrl = mWebBackForwardList.getItemAtIndex(mWebBackForwardList.getCurrentIndex() - 1).getUrl();
                    ninjaWebView.initPreferences(historyUrl);
                    goBack_skipRedirects();
                } else removeAlbum(currentAlbumController);
                return true;
        }
        return false;
    }

    @Override
    public synchronized void showAlbum(AlbumController controller) {
        View av = (View) controller;
        if (currentAlbumController != null) currentAlbumController.deactivate();
        currentAlbumController = controller;
        currentAlbumController.activate();
        contentFrame.removeAllViews();
        contentFrame.addView(av);
        updateOmniBox();
        if (searchPanel.getVisibility() == View.VISIBLE) {
            searchBox.setText("");
            searchPanel.setVisibility(View.GONE);
            omniBox.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public synchronized void removeAlbum(final AlbumController controller) {

        if (BrowserContainer.size() <= 1) {
            if (!sp.getBoolean("sp_reopenLastTab", false)) doubleTapsQuit();
            else {
                ninjaWebView.loadUrl(Objects.requireNonNull(sp.getString("favoriteURL", "about:blank")));
                hideOverview();
            }
        } else {
            closeTabConfirmation(() -> {
                AlbumController predecessor;
                if (controller == currentAlbumController)
                    predecessor = ((NinjaWebView) controller).getPredecessor();
                else predecessor = currentAlbumController;
                //if not the current TAB is being closed return to current TAB
                tab_container2.removeView(controller.getAlbumView());
                int index = BrowserContainer.indexOf(controller);
                BrowserContainer.remove(controller);
                if ((predecessor != null) && (BrowserContainer.indexOf(predecessor) != -1)) {
                    //if predecessor is stored and has not been closed in the meantime
                    showAlbum(predecessor);
                } else {
                    if (index >= BrowserContainer.size()) index = BrowserContainer.size() - 1;
                    showAlbum(BrowserContainer.get(index));
                }
            });
        }
    }

    //监听系统模式变化，通知工具类提示重启浏览器更换模式
    @SuppressWarnings("NullableProblems")
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!orientationChanged) {
            saveOpenedTabs();
            HelperUnit.triggerRebirth(context);
        } else orientationChanged = false;
    }

    @Override
    public synchronized void updateProgress(int progress) {
        progressBar.setProgress(progress);
        if (progress != BrowserUnit.LOADING_STOPPED) updateOmniBox();
        if (progress < BrowserUnit.PROGRESS_MAX) progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void showFileChooser(ValueCallback<Uri[]> filePathCallback) {
        if (mFilePathCallback != null) mFilePathCallback.onReceiveValue(null);
        mFilePathCallback = filePathCallback;

        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("*/*");
        Intent[] intentArray;
        intentArray = new Intent[0];

        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
        //noinspection deprecation
        startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);
    }

    @Override
    public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        if (view == null) return;
        if (customView != null && callback != null) {
            callback.onCustomViewHidden();
            return;
        }

        customView = view;
        fullscreenHolder = new FrameLayout(context);
        fullscreenHolder.addView(
                customView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                ));

        FrameLayout decorView = (FrameLayout) getWindow().getDecorView();
        decorView.addView(
                fullscreenHolder,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                ));

        customView.setKeepScreenOn(true);
        ((View) currentAlbumController).setVisibility(View.GONE);
        setCustomFullscreen(true);

        if (view instanceof FrameLayout) {
            if (((FrameLayout) view).getFocusedChild() instanceof VideoView) {
                videoView = (VideoView) ((FrameLayout) view).getFocusedChild();
                videoView.setOnErrorListener(new VideoCompletionListener());
                videoView.setOnCompletionListener(new VideoCompletionListener());
            }
        }
    }

    @Override
    public void onHideCustomView() {
        FrameLayout decorView = (FrameLayout) getWindow().getDecorView();
        decorView.removeView(fullscreenHolder);
        customView.setKeepScreenOn(false);
        ((View) currentAlbumController).setVisibility(View.VISIBLE);
        setCustomFullscreen(false);
        fullscreenHolder = null;
        customView = null;
        if (videoView != null) {
            videoView.setOnErrorListener(null);
            videoView.setOnCompletionListener(null);
            videoView = null;
        }
        contentFrame.requestFocus();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo
            menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        WebView.HitTestResult result = ninjaWebView.getHitTestResult();
        if (result.getExtra() != null) {
            if (result.getType() == SRC_ANCHOR_TYPE)
                showContextMenuLink(result.getExtra(), result.getExtra(), SRC_ANCHOR_TYPE, true);
            else if (result.getType() == SRC_IMAGE_ANCHOR_TYPE) {
                // Create a background thread that has a Looper
                HandlerThread handlerThread = new HandlerThread("HandlerThread");
                handlerThread.start();
                // Create a handler to execute tasks in the background thread.
                Handler backgroundHandler = new Handler(handlerThread.getLooper());
                Message msg = backgroundHandler.obtainMessage();
                ninjaWebView.requestFocusNodeHref(msg);
                String url = (String) msg.getData().get("url");
                showContextMenuLink(url, url, SRC_ANCHOR_TYPE, true);
            } else if (result.getType() == IMAGE_TYPE)
                showContextMenuLink(result.getExtra(), result.getExtra(), IMAGE_TYPE, true);
            else showContextMenuLink(result.getExtra(), result.getExtra(), 0, true);
        }
    }

    // Views

    @SuppressLint("ClickableViewAccessibility")
    private void initOverview() {
        bottomSheetDialog_OverView = findViewById(R.id.bottomSheetDialog_OverView);
        ListView listView = bottomSheetDialog_OverView.findViewById(R.id.list_overView);
        tab_container = bottomSheetDialog_OverView.findViewById(R.id.listOpenedTabs);
        tab_container2 = findViewById(R.id.listOpenedTabs2);

        AtomicInteger intPage = new AtomicInteger();

        NavigationBarView.OnItemSelectedListener navListener = menuItem -> {
            if (menuItem.getItemId() == R.id.page_0) {
                intPage.set(R.id.page_0);
                tab_container.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
            } else if (menuItem.getItemId() == R.id.page_2) {
                tab_container.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                overViewTab = getString(R.string.album_title_bookmarks);
                intPage.set(R.id.page_2);

                RecordAction action = new RecordAction(context);
                action.open(false);
                final List<Record> list;
                list = action.listBookmark(activity, filter, filterBy);
                action.close();
                adapter = new AdapterRecord(context, list);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                filter = false;
                listView.setOnItemClickListener((parent, view, position, id) -> {
                    if (list.get(position).getType() == BOOKMARK_ITEM || list.get(position).getType() == STARTSITE_ITEM) {
                        if (list.get(position).getDesktopMode() != ninjaWebView.isDesktopMode())
                            ninjaWebView.toggleDesktopMode(false);
                        if (list.get(position).getNightMode() == ninjaWebView.isNightMode() && !isNightMode) {
                            ninjaWebView.toggleNightMode();
                            isNightMode = ninjaWebView.isNightMode();
                        }
                    }
                    ninjaWebView.loadUrl(list.get(position).getURL());
                    hideOverview();
                });
                listView.setOnItemLongClickListener((parent, view, position, id) -> {
                    showContextMenuList(list.get(position).getTitle(), list.get(position).getURL(), adapter, list, position);
                    return true;
                });
            } else if (menuItem.getItemId() == R.id.page_3) {
                tab_container.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                overViewTab = getString(R.string.album_title_history);
                intPage.set(R.id.page_3);

                RecordAction action = new RecordAction(context);
                action.open(false);
                final List<Record> list;
                list = action.listHistory();
                action.close();
                //noinspection NullableProblems
                adapter = new AdapterRecord(context, list) {
                    @Override
                    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                        View v = super.getView(position, convertView, parent);
                        TextView record_item_time = v.findViewById(R.id.dateView);
                        record_item_time.setVisibility(View.VISIBLE);
                        TextView record_item_title = v.findViewById(R.id.titleView);
                        record_item_title.setPadding(0, 0, 150, 0);
                        return v;
                    }
                };

                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                listView.setOnItemClickListener((parent, view, position, id) -> {
                    if (list.get(position).getType() == BOOKMARK_ITEM || list.get(position).getType() == STARTSITE_ITEM) {
                        if (list.get(position).getDesktopMode() != ninjaWebView.isDesktopMode())
                            ninjaWebView.toggleDesktopMode(false);
                        if (list.get(position).getNightMode() == ninjaWebView.isNightMode() && !isNightMode) {
                            ninjaWebView.toggleNightMode();
                            isNightMode = ninjaWebView.isNightMode();
                        }
                    }
                    ninjaWebView.loadUrl(list.get(position).getURL());
                    hideOverview();
                });

                listView.setOnItemLongClickListener((parent, view, position, id) -> {
                    showContextMenuList(list.get(position).getTitle(), list.get(position).getURL(), adapter, list, position);
                    return true;
                });
            }
            return true;
        };

        bottom_navigation = bottomSheetDialog_OverView.findViewById(R.id.bottom_navigation);
        bottom_navigation.setOnItemSelectedListener(navListener);
        bottom_navigation.findViewById(R.id.page_2).setOnLongClickListener(v -> {
            showDialogFilter();
            return true;
        });
        setSelectedTab();
    }

    @SuppressLint({"ClickableViewAccessibility", "UnsafeOptInUsageError"})
    private void initOmniBox() {
        bt_home = findViewById(R.id.bt_home);
        omniBox = findViewById(R.id.omniBox);
        omniBox_text = findViewById(R.id.omniBox_input);
        listener = omniBox_text.getKeyListener(); // Save the default KeyListener!!!
        omniBox_text.setKeyListener(null); // Disable input
        omniBox_text.setEllipsize(TextUtils.TruncateAt.END);
        //Home键点击事件
        bt_home.setOnClickListener(view -> {
            ninjaWebView.loadUrl(Objects.requireNonNull(sp.getString("favoriteURL", "about:blank")));
        });
        multitab = findViewById(R.id.multitab);
        //多窗口layout
        LinearLayout multitabview = findViewById(R.id.multitabview);
        back = findViewById(R.id.back);
        ImageView add_bookmark = findViewById(R.id.add_bookmark);
        add_bookmark.setOnClickListener(view ->
                addAlbum(getString(R.string.app_name), Objects.requireNonNull(sp.getString("favoriteURL", "about:blank")), true, false, ""));
        back.setOnClickListener(view -> {
            if (ninjaWebView.canGoBack())
                ninjaWebView.goBack();
        });
        multitabview.setOnClickListener(v -> showTabView());
        multitabview.setOnLongClickListener(view -> {
            performGesture("setting_gesture_tabButton");
            return true;
        });

        list_search = findViewById(R.id.list_search);
        omnibox_close = findViewById(R.id.omnibox_close);
        searchBox_refresh = findViewById(R.id.searchBox_refresh);
        searchBox_refresh.setOnClickListener(view -> ninjaWebView.reload());
        omnibox_close.setOnClickListener(view -> {
            if (Objects.requireNonNull(omniBox_text.getText()).length() > 0)
                omniBox_text.setText("");
            else omniBox_text.clearFocus();
        });

        progressBar = findViewById(R.id.main_progress_bar);
        bottomAppBar = findViewById(R.id.bottomAppBar);
        bottom_op = findViewById(R.id.bottom_op);

//        多标签页上面的导航数字
//        badgeDrawable = BadgeDrawable.create(context);
//        badgeDrawable.setBadgeGravity(BadgeDrawable.TOP_END);
//        badgeDrawable.setVerticalOffset(20);
//        badgeDrawable.setHorizontalOffset(20);
//        badgeDrawable.setNumber(BrowserContainer.size());
//        badgeDrawable.setBackgroundColor(Color.parseColor("#ff0000"));
//        BadgeUtils.attachBadgeDrawable(badgeDrawable, multitab, findViewById(R.id.layout));


        //头部——》更多
        ImageView omnibox_overflow = findViewById(R.id.omnibox_overflow);
        omnibox_overflow.setOnClickListener(v -> showOverflow2(omnibox_overflow));

        multitab.setOnTouchListener(new SwipeTouchListener(context) {
            public void onSwipeTop() {
                performGesture("setting_gesture_nav_up");
            }

            public void onSwipeBottom() {
                performGesture("setting_gesture_nav_down");
            }

            public void onSwipeRight() {
                performGesture("setting_gesture_nav_right");
            }

            public void onSwipeLeft() {
                performGesture("setting_gesture_nav_left");
            }
        });

        omniBox_text.setOnEditorActionListener((v, actionId, event) -> {
            String query = Objects.requireNonNull(omniBox_text.getText()).toString().trim();
            ninjaWebView.loadUrl(query);
            return false;
        });

        //点击输入搜索栏 hasFocus有无焦点(输入)
        omniBox_text.setOnFocusChangeListener((v, hasFocus) -> {
            if (omniBox_text.hasFocus()) {
                omnibox_close.setVisibility(View.VISIBLE);
                searchBox_refresh.setVisibility(View.GONE);
                list_search.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                omnibox_overflow.setVisibility(View.INVISIBLE);
                String url = ninjaWebView.getUrl();
                ninjaWebView.setVisibility(View.GONE);

                InputMethodManager inputManager =
                        (InputMethodManager) omniBox_text.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(omniBox_text, 0);

                ninjaWebView.stopLoading();
                //暂时隐藏webview
                omniBox_text.setKeyListener(listener);
                if (url == null || url.isEmpty()) omniBox_text.setText("");
                else omniBox_text.setText(url);
                initSearch();
                omniBox_text.selectAll();
            } else {

                HelperUnit.hideSoftKeyboard(omniBox_text, context);
                omnibox_close.setVisibility(View.GONE);
                searchBox_refresh.setVisibility(View.VISIBLE);
                list_search.setVisibility(View.GONE);
                omnibox_overflow.setVisibility(View.VISIBLE);
                omniBox_text.setKeyListener(null);
                omniBox_text.setEllipsize(TextUtils.TruncateAt.END);
                omniBox_text.setText(ninjaWebView.getTitle());
                ninjaWebView.setVisibility(View.VISIBLE);
                updateOmniBox();
            }
        });

    }


    //新的下载任务
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void VideoDectedEvent(VideoDectedEvent videoDectedEvent) {
        if (VideoDected.getdetectedVideo() != null) {
            action_button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0099FF")));
        } else {
            action_button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#898989")));
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveMsg(LinkedBlockingQueue<DetectedVideoInfo> detectedTaskUrlQueue) {

    }


    //更新地址栏
    @SuppressLint({"UnsafeOptInUsageError"})
    private void updateOmniBox() {
        omniBox_text.clearFocus();
        ninjaWebView = (NinjaWebView) currentAlbumController;

        if (!ninjaWebView.canGoBack()) back.setImageResource(R.drawable.icon_backunselected);
        else back.setImageResource(R.drawable.icon_back);
        String url = ninjaWebView.getUrl();

        if (url != null) {
            progressBar.setVisibility(View.GONE);
            ninjaWebView.setProfileIcon(multitab);
            ninjaWebView.initCookieManager(url);
            listTrusted = new List_trusted(context);

            if (Objects.requireNonNull(ninjaWebView.getTitle()).isEmpty())
                omniBox_text.setText(url);
            else omniBox_text.setText(ninjaWebView.getTitle());

            if (url.contains("about:blank")) {
                sample_website.setVisibility(View.VISIBLE);
                bt_home.setVisibility(View.GONE);
                omniBox_text.setVisibility(View.GONE);
                searchBox_refresh.setVisibility(View.GONE);
            } else {
                sample_website.setVisibility(View.GONE);
                bt_home.setVisibility(View.VISIBLE);
                omniBox_text.setVisibility(View.VISIBLE);
                searchBox_refresh.setVisibility(View.VISIBLE);
            }

            if (url.startsWith("https://") || url.contains("about:blank") || listTrusted.isWhite(url)) {
                ninjaWebView.setProfileIcon(multitab);
            } else if (url.isEmpty()) {
                multitab.setOnClickListener(v -> showTabView());
                omniBox_text.setText("");
            } else {
                multitab.setImageResource(R.drawable.icon_alert);
                multitab.setOnClickListener(v -> {
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
                    builder.setIcon(R.drawable.icon_alert);
                    builder.setTitle(R.string.app_warning);
                    builder.setMessage(R.string.toast_unsecured);
                    builder.setPositiveButton(R.string.app_ok, (dialog, whichButton) -> ninjaWebView.loadUrl(url.replace("http://", "https://")));
                    builder.setNegativeButton(R.string.app_cancel, (dialog, whichButton) -> {
                        dialog.cancel();
                        multitab.setOnClickListener(v2 -> showTabView());
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    HelperUnit.setupDialog(context, dialog);
                });
            }
        }
    }

    private void initSearchPanel() {
        searchPanel = findViewById(R.id.searchBox);
        searchBox = findViewById(R.id.searchBox_input);
        Button searchUp = findViewById(R.id.searchBox_up);
        Button searchDown = findViewById(R.id.searchBox_down);
        Button searchCancel = findViewById(R.id.searchBox_cancel);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (currentAlbumController != null)
                    ((NinjaWebView) currentAlbumController).findAllAsync(s.toString());
            }
        });
        searchUp.setOnClickListener(v -> ((NinjaWebView) currentAlbumController).findNext(false));
        searchDown.setOnClickListener(v -> ((NinjaWebView) currentAlbumController).findNext(true));
        searchCancel.setOnClickListener(v -> {
            if (searchBox.getText().length() > 0) searchBox.setText("");
            else {
                HelperUnit.hideSoftKeyboard(searchBox, context);
                searchPanel.setVisibility(View.GONE);
                omniBox.setVisibility(View.VISIBLE);
            }
        });
    }

    public void initSearch() {
        RecordAction action = new RecordAction(this);
        List<Record> list = action.listEntries(activity);
        AdapterSearch adapter = new AdapterSearch(this, R.layout.item_list, list);
        list_search.setAdapter(adapter);
        list_search.setTextFilterEnabled(true);
        adapter.notifyDataSetChanged();
        list_search.setOnItemClickListener((parent, view, position, id) -> {
            omniBox_text.clearFocus();
            String url = ((TextView) view.findViewById(R.id.dateView)).getText().toString();
            for (Record record : list) {
                if (record.getURL().equals(url)) {
                    if ((record.getType() == BOOKMARK_ITEM) || (record.getType() == STARTSITE_ITEM)) {
                        if (record.getDesktopMode() != ninjaWebView.isDesktopMode())
                            ninjaWebView.toggleDesktopMode(false);
                        if (record.getNightMode() == ninjaWebView.isNightMode() && !isNightMode) {
                            ninjaWebView.toggleNightMode();
                            isNightMode = ninjaWebView.isNightMode();
                        }
                        break;
                    }
                }
            }
            ninjaWebView.loadUrl(url);
        });
        list_search.setOnItemLongClickListener((adapterView, view, i, l) -> {
            String title = ((TextView) view.findViewById(R.id.titleView)).getText().toString();
            String url = ((TextView) view.findViewById(R.id.dateView)).getText().toString();
            showContextMenuLink(title, url, SRC_ANCHOR_TYPE, false);
            omnibox_close.performClick();
            omnibox_close.performClick();
            return true;
        });
        omniBox_text.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }
        });
    }

    private void showOverview() {
        setSelectedTab();
        bottomSheetDialog_OverView.setVisibility(View.VISIBLE);
        ObjectAnimator animation = ObjectAnimator.ofFloat(bottomSheetDialog_OverView, "translationY", 0);
        animation.start();
        bottomAppBar.setVisibility(View.GONE);
        bottom_op.setVisibility(View.GONE);
    }

    public void hideOverview() {
        bottomSheetDialog_OverView.setVisibility(View.GONE);
        ObjectAnimator animation = ObjectAnimator.ofFloat(bottomSheetDialog_OverView, "translationY", bottomSheetDialog_OverView.getHeight());
        animation.start();
        bottom_op.setVisibility(View.VISIBLE);
        bottomAppBar.setVisibility(View.VISIBLE);
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(aBoolean -> {
            if (aBoolean) {
                //申请的权限全部允许
                File file = new File(MainApplication.appConfig.rootDataPath);
                File rootDownPath = new File(MainApplication.appConfig.rootDownPath);
                if (!rootDownPath.exists()) {
                    rootDownPath.mkdir();
                }
                if (!file.exists())
                    file.mkdir();
            } else {
                //只要有一个权限被拒绝，就会执行
                ViewUtil.openConfirmDialog(this,
                        "必需权限",
                        "没有该权限，此应用程序可能无法正常工作。打开应用设置屏幕以修改应用权限",
                        "去设置", (dialog, which) -> startActivity(
                                new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        .setData(Uri.fromParts("package", getPackageName(), null))),
                        "退出", (dialog, which) -> finish()
                );
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @SuppressLint("WrongConstant")
    public void showTabView() {
        drawerLayout.openDrawer(Gravity.START);
    }

    private void setSelectedTab() {
        if (overViewTab.equals(getString(R.string.album_title_bookmarks)))
            bottom_navigation.setSelectedItemId(R.id.page_2);
        else if (overViewTab.equals(getString(R.string.album_title_history)))
            bottom_navigation.setSelectedItemId(R.id.page_3);
    }

    //主页更多
    @SuppressLint({"RestrictedApi", "WrongConstant"})
    private void showOverflow2(ImageView omnibox_overflow) {
        //隐藏软键盘
        HelperUnit.hideSoftKeyboard(omniBox_text, context);
        final String url = ninjaWebView.getUrl();
        final String title = ninjaWebView.getTitle();
        // View当前PopupMenu显示的相对View的位置

        PopupMenu popupMenu = new PopupMenu(this, omnibox_overflow);
        @SuppressLint("RestrictedApi") MenuPopupHelper popupHelper = new MenuPopupHelper(context, (MenuBuilder) popupMenu.getMenu(), omnibox_overflow);
        popupHelper.setForceShowIcon(true);
        // menu布局
        popupMenu.getMenuInflater().inflate(R.menu.home_menu, popupMenu.getMenu());
        // menu的item点击事件

        popupMenu.setOnMenuItemClickListener(item -> {
            if ("分享链接".contentEquals(item.getTitle())) {
                shareLink(title, url);
            } else if (item.getTitle().equals("设置")) {
                Intent settings = new Intent(BrowserActivity.this, Settings_Activity.class);
                startActivity(settings);
            } else if (item.getTitle().equals("书签")) {
                current = 2;
                initlistview(current);
                drawerLayout.openDrawer(Gravity.END);
            } else if (item.getTitle().equals("历史记录")) {
//                current = 1;
//                initlistview(current);
//                drawerLayout.openDrawer(Gravity.END);
                Intent intent = new Intent(this, HistoryActivity.class);
                startActivity(intent);
            } else if (item.getTitle().equals("保存为书签")) {
                RecordAction action = new RecordAction(context);
                saveBookmark();
                action.close();
            } else if (item.getTitle().equals("保存为PDF")) {
                printPDF();
            } else if (item.getTitle().equals("页面内搜索")) {
                searchOnSite();
            } else if (item.getTitle().equals("新建标签页")) {
                addAlbum(getString(R.string.app_name), Objects.requireNonNull(sp.getString("favoriteURL", "about:blank")), true, false, "");
            }
            return false;
        });
        //popumenu添加图标
        try {
            Field field = popupMenu.getClass().getDeclaredField("mPopup");
            field.setAccessible(true);
            MenuPopupHelper mHelper = (MenuPopupHelper) field.get(popupMenu);
            assert mHelper != null;
            mHelper.setForceShowIcon(true);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        popupMenu.show();
    }

    // Menus
    public void showContextMenuLink(final String title, final String url, int type,
                                    boolean showAll) {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        View dialogView = View.inflate(context, R.layout.dialog_menu, null);

        TextView menuTitle = dialogView.findViewById(R.id.menuTitle);
        menuTitle.setText(title);
        ImageView menu_icon = dialogView.findViewById(R.id.menu_icon);

        if (type == SRC_ANCHOR_TYPE) {
            FaviconHelper faviconHelper = new FaviconHelper(context);
            Bitmap bitmap = faviconHelper.getFavicon(url);
            if (bitmap != null) menu_icon.setImageBitmap(bitmap);
            else menu_icon.setImageResource(R.drawable.icon_link);
        } else if (type == IMAGE_TYPE) menu_icon.setImageResource(R.drawable.icon_image);
        else menu_icon.setImageResource(R.drawable.icon_link);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();
        Objects.requireNonNull(dialog.getWindow()).setGravity(Gravity.BOTTOM);

        GridItem item_01 = new GridItem(getString(R.string.main_menu_new_tabOpen), 0);
        GridItem item_02 = new GridItem(getString(R.string.main_menu_new_tab), 0);
        GridItem item_03 = new GridItem(getString(R.string.main_menu_new_tabProfile), 0);
        GridItem item_04 = new GridItem(getString(R.string.menu_share_link), 0);
        GridItem item_05 = new GridItem(getString(R.string.menu_shareClipboard), 0);
        GridItem item_06 = new GridItem(getString(R.string.menu_open_with), 0);
        GridItem item_07 = new GridItem(getString(R.string.menu_save_as), 0);
        GridItem item_08 = new GridItem(getString(R.string.menu_save_home), 0);

        final List<GridItem> gridList = new LinkedList<>();

        gridList.add(gridList.size(), item_01);
        gridList.add(gridList.size(), item_02);
        gridList.add(gridList.size(), item_03);
        gridList.add(gridList.size(), item_04);
        gridList.add(gridList.size(), item_05);
        gridList.add(gridList.size(), item_06);

        if (showAll) {
            gridList.add(gridList.size(), item_07);
            gridList.add(gridList.size(), item_08);
        }

        GridView menu_grid = dialogView.findViewById(R.id.menu_grid);
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) menu_grid.setNumColumns(1);
        else menu_grid.setNumColumns(3);

        GridAdapter gridAdapter = new GridAdapter(context, gridList);
        menu_grid.setAdapter(gridAdapter);
        gridAdapter.notifyDataSetChanged();
        menu_grid.setOnItemClickListener((parent, view, position, id) -> {
            dialog.cancel();
            switch (position) {
                case 0:
                    addAlbum(getString(R.string.app_name), url, true, false, "");
                    break;
                case 1:
                    addAlbum(getString(R.string.app_name), url, false, false, "");
                    break;
                case 2:
                    addAlbum(getString(R.string.app_name), url, true, true, "");
                    break;
                case 3:
                    shareLink(HelperUnit.domain(url), url);
                    break;
                case 4:
                    copyLink(url);
                    break;
                case 5:
                    BrowserUnit.intentURL(context, Uri.parse(url));
                    break;
                case 6:
                    if (url.startsWith("data:")) {
                        DataURIParser dataURIParser = new DataURIParser(url);
                        HelperUnit.saveDataURI(activity, dataURIParser);
                    } else HelperUnit.saveAs(activity, url);
                    break;
                case 7:
                    save_atHome(title, url);
                    break;
            }
        });
    }

    private void showContextMenuList(final String title, final String url,
                                     final AdapterRecord adapterRecord, final List<Record> recordList, final int location) {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        View dialogView = View.inflate(context, R.layout.dialog_menu, null);

        TextView menuTitle = dialogView.findViewById(R.id.menuTitle);
        menuTitle.setText(title);
        FaviconHelper.setFavicon(context, dialogView, url, R.id.menu_icon, R.drawable.icon_image_broken);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();
        Objects.requireNonNull(dialog.getWindow()).setGravity(Gravity.BOTTOM);

        GridItem item_01 = new GridItem(getString(R.string.main_menu_new_tabOpen), 0);
        GridItem item_02 = new GridItem(getString(R.string.main_menu_new_tab), 0);
        GridItem item_03 = new GridItem(getString(R.string.main_menu_new_tabProfile), 0);
        GridItem item_04 = new GridItem(getString(R.string.menu_share_link), 0);
        GridItem item_05 = new GridItem(getString(R.string.menu_delete), 0);
        GridItem item_06 = new GridItem(getString(R.string.menu_edit), 0);

        final List<GridItem> gridList = new LinkedList<>();

        if (overViewTab.equals(getString(R.string.album_title_bookmarks)) || overViewTab.equals(getString(R.string.album_title_home))) {
            gridList.add(gridList.size(), item_01);
            gridList.add(gridList.size(), item_02);
            gridList.add(gridList.size(), item_03);
            gridList.add(gridList.size(), item_04);
            gridList.add(gridList.size(), item_05);
            gridList.add(gridList.size(), item_06);
        } else {
            gridList.add(gridList.size(), item_01);
            gridList.add(gridList.size(), item_02);
            gridList.add(gridList.size(), item_03);
            gridList.add(gridList.size(), item_04);
            gridList.add(gridList.size(), item_05);
        }

        GridView menu_grid = dialogView.findViewById(R.id.menu_grid);
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) menu_grid.setNumColumns(1);
        else menu_grid.setNumColumns(3);

        GridAdapter gridAdapter = new GridAdapter(context, gridList);
        menu_grid.setAdapter(gridAdapter);
        gridAdapter.notifyDataSetChanged();
        menu_grid.setOnItemClickListener((parent, view, position, id) -> {
            dialog.cancel();
            MaterialAlertDialogBuilder builderSubMenu;
            AlertDialog dialogSubMenu;
            switch (position) {
                case 0:
                    addAlbum(getString(R.string.app_name), url, true, false, "");
                    hideOverview();
                    break;
                case 1:
                    addAlbum(getString(R.string.app_name), url, false, false, "");
                    break;
                case 2:
                    addAlbum(getString(R.string.app_name), url, true, true, "");
                    hideOverview();
                    break;
                case 3:
                    shareLink(title, url);
                    break;
                case 4:
                    builderSubMenu = new MaterialAlertDialogBuilder(context);
                    builderSubMenu.setIcon(R.drawable.icon_alert);
                    builderSubMenu.setTitle(R.string.menu_delete);
                    builderSubMenu.setMessage(R.string.hint_database);
                    builderSubMenu.setPositiveButton(R.string.app_ok, (dialog2, whichButton) -> {
                        Record record = recordList.get(location);
                        RecordAction action = new RecordAction(context);
                        action.open(true);
                        if (overViewTab.equals(getString(R.string.album_title_home)))
                            action.deleteURL(record.getURL(), RecordUnit.TABLE_START);
                        else if (overViewTab.equals(getString(R.string.album_title_bookmarks)))
                            action.deleteURL(record.getURL(), RecordUnit.TABLE_BOOKMARK);
                        else if (overViewTab.equals(getString(R.string.album_title_history)))
                            action.deleteURL(record.getURL(), RecordUnit.TABLE_HISTORY);
                        action.close();
                        recordList.remove(location);
                        adapterRecord.notifyDataSetChanged();
                    });
                    builderSubMenu.setNegativeButton(R.string.app_cancel, (dialog2, whichButton) -> builderSubMenu.setCancelable(true));
                    dialogSubMenu = builderSubMenu.create();
                    dialogSubMenu.show();
                    HelperUnit.setupDialog(context, dialogSubMenu);
                    break;
                case 5:
                    builderSubMenu = new MaterialAlertDialogBuilder(context);

                    View dialogViewSubMenu = View.inflate(context, R.layout.dialog_edit, null);
                    LinearLayout editButtonsLayout = dialogViewSubMenu.findViewById(R.id.editButtonsLayout);
                    editButtonsLayout.setVisibility(View.VISIBLE);
                    TextInputLayout editTopLayout = dialogViewSubMenu.findViewById(R.id.editTopLayout);
                    editTopLayout.setHint(getString(R.string.dialog_title_hint));
                    TextInputLayout editBottomLayout = dialogViewSubMenu.findViewById(R.id.editBottomLayout);
                    editBottomLayout.setHint(getString(R.string.dialog_URL_hint));

                    EditText editTop = dialogViewSubMenu.findViewById(R.id.editTop);
                    EditText editBottom = dialogViewSubMenu.findViewById(R.id.editBottom);
                    editTop.setText(title);
                    editTop.setHint(getString(R.string.dialog_title_hint));
                    editBottom.setText(url);
                    editBottom.setHint(getString(R.string.dialog_URL_hint));

                    Chip chip_desktopMode = dialogViewSubMenu.findViewById(R.id.editDesktopMode);
                    chip_desktopMode.setChecked(recordList.get(location).getDesktopMode());
                    Chip chip_nightMode = dialogViewSubMenu.findViewById(R.id.editNightMode);
                    chip_nightMode.setChecked(!recordList.get(location).getNightMode());

                    MaterialCardView ib_icon = dialogViewSubMenu.findViewById(R.id.editIcon);
                    if (!overViewTab.equals(getString(R.string.album_title_bookmarks)))
                        ib_icon.setVisibility(View.GONE);
                    ib_icon.setOnClickListener(v -> {
                        MaterialAlertDialogBuilder builderFilter = new MaterialAlertDialogBuilder(context);
                        View dialogViewFilter = View.inflate(context, R.layout.dialog_menu, null);
                        builderFilter.setView(dialogViewFilter);
                        AlertDialog dialogFilter = builderFilter.create();
                        dialogFilter.show();
                        TextView menuTitleFilter = dialogViewFilter.findViewById(R.id.menuTitle);
                        menuTitleFilter.setText(R.string.menu_filter);
                        CardView cardView = dialogViewFilter.findViewById(R.id.cardView);
                        cardView.setVisibility(View.GONE);

                        Objects.requireNonNull(dialogFilter.getWindow()).setGravity(Gravity.BOTTOM);
                        GridView menuEditFilter = dialogViewFilter.findViewById(R.id.menu_grid);
                        final List<GridItem> menuEditFilterList = new LinkedList<>();
                        HelperUnit.addFilterItems(activity, menuEditFilterList);
                        GridAdapter menuEditFilterAdapter = new GridAdapter(context, menuEditFilterList);
                        menuEditFilter.setNumColumns(2);
                        menuEditFilter.setHorizontalSpacing(20);
                        menuEditFilter.setVerticalSpacing(20);
                        menuEditFilter.setAdapter(menuEditFilterAdapter);
                        menuEditFilterAdapter.notifyDataSetChanged();
                        menuEditFilter.setOnItemClickListener((parent2, view2, position2, id2) -> {
                            newIcon = menuEditFilterList.get(position2).getData();
                            HelperUnit.setFilterIcons(context, ib_icon, newIcon);
                            dialogFilter.cancel();
                        });
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(HelperUnit.convertDpToPixel(20f, context),
                                HelperUnit.convertDpToPixel(10f, context),
                                HelperUnit.convertDpToPixel(20f, context),
                                HelperUnit.convertDpToPixel(10f, context));
                        menuEditFilter.setLayoutParams(params);
                    });
                    newIcon = recordList.get(location).getIconColor();
                    HelperUnit.setFilterIcons(context, ib_icon, newIcon);

                    builderSubMenu.setView(dialogViewSubMenu);
                    builderSubMenu.setTitle(getString(R.string.menu_edit));
                    dialogSubMenu = builderSubMenu.create();
                    dialogSubMenu.show();
                    HelperUnit.setupDialog(context, dialogSubMenu);

                    Button ib_cancel = dialogViewSubMenu.findViewById(R.id.editCancel);
                    ib_cancel.setOnClickListener(v -> {
                        HelperUnit.hideSoftKeyboard(editBottom, context);
                        dialogSubMenu.cancel();
                    });
                    Button ib_ok = dialogViewSubMenu.findViewById(R.id.editOK);
                    ib_ok.setOnClickListener(v -> {
                        if (overViewTab.equals(getString(R.string.album_title_bookmarks))) {
                            RecordAction action = new RecordAction(context);
                            action.open(true);
                            action.deleteURL(url, RecordUnit.TABLE_BOOKMARK);
                            action.addBookmark(new Record(editTop.getText().toString(), editBottom.getText().toString(), 0, 0, BOOKMARK_ITEM, chip_desktopMode.isChecked(), chip_nightMode.isChecked(), newIcon));
                            action.close();
                            bottom_navigation.setSelectedItemId(R.id.page_2);
                        } else {
                            RecordAction action = new RecordAction(context);
                            action.open(true);
                            action.deleteURL(url, RecordUnit.TABLE_START);
                            int counter = sp.getInt("counter", 0);
                            counter = counter + 1;
                            sp.edit().putInt("counter", counter).apply();
                            action.addStartSite(new Record(editTop.getText().toString(), editBottom.getText().toString(), 0, counter, STARTSITE_ITEM, chip_desktopMode.isChecked(), chip_nightMode.isChecked(), 0));
                            action.close();
//                            bottom_navigation.setSelectedItemId(R.id.page_1);
                        }
                        HelperUnit.hideSoftKeyboard(editBottom, context);
                        dialogSubMenu.cancel();
                    });
                    break;
            }
        });
    }

    // Dialogs
    private void showDialogFastToggle() {
        listTrusted = new List_trusted(context);
        listStandard = new List_standard(context);
        listProtected = new List_protected(context);
        ninjaWebView = (NinjaWebView) currentAlbumController;
        String url = ninjaWebView.getUrl();

        if (url != null) {

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
            View dialogView = View.inflate(context, R.layout.dialog_toggle, null);
            builder.setView(dialogView);

            Chip chip_profile_standard = dialogView.findViewById(R.id.chip_profile_standard);
            Chip chip_profile_trusted = dialogView.findViewById(R.id.chip_profile_trusted);
            Chip chip_profile_changed = dialogView.findViewById(R.id.chip_profile_changed);
            Chip chip_profile_protected = dialogView.findViewById(R.id.chip_profile_protected);

            TextView dialog_warning = dialogView.findViewById(R.id.dialog_titleDomain);
            dialog_warning.setText(HelperUnit.domain(url));
            FaviconHelper.setFavicon(context, dialogView, url, R.id.menu_icon, R.drawable.icon_image_broken);

            TextView dialog_titleProfile = dialogView.findViewById(R.id.dialog_titleProfile);
            ninjaWebView.putProfileBoolean("", dialog_titleProfile, chip_profile_trusted, chip_profile_standard, chip_profile_protected, chip_profile_changed);

            AlertDialog dialog = builder.create();
            dialog.show();
            Objects.requireNonNull(dialog.getWindow()).setGravity(Gravity.BOTTOM);

            //ProfileControl

            Chip chip_setProfileTrusted = dialogView.findViewById(R.id.chip_setProfileTrusted);
            chip_setProfileTrusted.setChecked(listTrusted.isWhite(url));
            chip_setProfileTrusted.setOnClickListener(v -> {
                if (listTrusted.isWhite(ninjaWebView.getUrl()))
                    listTrusted.removeDomain(HelperUnit.domain(url));
                else {
                    listTrusted.addDomain(HelperUnit.domain(url));
                    listStandard.removeDomain(HelperUnit.domain(url));
                    listProtected.removeDomain(HelperUnit.domain(url));
                }
                ninjaWebView.reload();
                dialog.cancel();
            });
            chip_setProfileTrusted.setOnLongClickListener(view -> {
                Toast.makeText(context, getString(R.string.setting_title_profiles_trustedList), Toast.LENGTH_SHORT).show();
                return true;
            });

            Chip chip_setProfileProtected = dialogView.findViewById(R.id.chip_setProfileProtected);
            chip_setProfileProtected.setChecked(listProtected.isWhite(url));
            chip_setProfileProtected.setOnClickListener(v -> {
                if (listProtected.isWhite(ninjaWebView.getUrl()))
                    listProtected.removeDomain(HelperUnit.domain(url));
                else {
                    listProtected.addDomain(HelperUnit.domain(url));
                    listTrusted.removeDomain(HelperUnit.domain(url));
                    listStandard.removeDomain(HelperUnit.domain(url));
                }
                ninjaWebView.reload();
                dialog.cancel();
            });
            chip_setProfileProtected.setOnLongClickListener(view -> {
                Toast.makeText(context, getString(R.string.setting_title_profiles_protectedList), Toast.LENGTH_SHORT).show();
                return true;
            });

            Chip chip_setProfileStandard = dialogView.findViewById(R.id.chip_setProfileStandard);
            chip_setProfileStandard.setChecked(listStandard.isWhite(url));
            chip_setProfileStandard.setOnClickListener(v -> {
                if (listStandard.isWhite(ninjaWebView.getUrl()))
                    listStandard.removeDomain(HelperUnit.domain(url));
                else {
                    listStandard.addDomain(HelperUnit.domain(url));
                    listTrusted.removeDomain(HelperUnit.domain(url));
                    listProtected.removeDomain(HelperUnit.domain(url));
                }
                ninjaWebView.reload();
                dialog.cancel();
            });
            chip_setProfileStandard.setOnLongClickListener(view -> {
                Toast.makeText(context, getString(R.string.setting_title_profiles_standardList), Toast.LENGTH_SHORT).show();
                return true;
            });

            chip_profile_trusted.setChecked(Objects.equals(sp.getString("profile", "profileTrusted"), "profileTrusted"));
            chip_profile_trusted.setOnClickListener(v -> {
                sp.edit().putString("profile", "profileTrusted").apply();
                ninjaWebView.reload();
                dialog.cancel();
            });
            chip_profile_trusted.setOnLongClickListener(view -> {
                Toast.makeText(context, getString(R.string.setting_title_profiles_trusted), Toast.LENGTH_SHORT).show();
                return true;
            });

            chip_profile_standard.setChecked(Objects.equals(sp.getString("profile", "profileTrusted"), "profileStandard"));
            chip_profile_standard.setOnClickListener(v -> {
                sp.edit().putString("profile", "profileStandard").apply();
                ninjaWebView.reload();
                dialog.cancel();
            });
            chip_profile_standard.setOnLongClickListener(view -> {
                Toast.makeText(context, getString(R.string.setting_title_profiles_standard), Toast.LENGTH_SHORT).show();
                return true;
            });

            chip_profile_protected.setChecked(Objects.equals(sp.getString("profile", "profileTrusted"), "profileProtected"));
            chip_profile_protected.setOnClickListener(v -> {
                sp.edit().putString("profile", "profileProtected").apply();
                ninjaWebView.reload();
                dialog.cancel();
            });
            chip_profile_protected.setOnLongClickListener(view -> {
                Toast.makeText(context, getString(R.string.setting_title_profiles_protected), Toast.LENGTH_SHORT).show();
                return true;
            });

            chip_profile_changed.setChecked(Objects.equals(sp.getString("profile", "profileTrusted"), "profileChanged"));
            chip_profile_changed.setOnClickListener(v -> {
                sp.edit().putString("profile", "profileChanged").apply();
                ninjaWebView.reload();
                dialog.cancel();
            });
            chip_profile_changed.setOnLongClickListener(view -> {
                Toast.makeText(context, getString(R.string.setting_title_profiles_changed), Toast.LENGTH_SHORT).show();
                return true;
            });
            // CheckBox

            Chip chip_image = dialogView.findViewById(R.id.chip_image);
            chip_image.setChecked(ninjaWebView.getBoolean("_images"));
            chip_image.setOnLongClickListener(view -> {
                Toast.makeText(context, getString(R.string.setting_title_images), Toast.LENGTH_SHORT).show();
                return true;
            });
            chip_image.setOnClickListener(v -> {
                ninjaWebView.setProfileChanged();
                ninjaWebView.putProfileBoolean("_images", dialog_titleProfile, chip_profile_trusted, chip_profile_standard, chip_profile_protected, chip_profile_changed);
            });

            Chip chip_javaScript = dialogView.findViewById(R.id.chip_javaScript);
            chip_javaScript.setChecked(ninjaWebView.getBoolean("_javascript"));
            chip_javaScript.setOnLongClickListener(view -> {
                Toast.makeText(context, getString(R.string.setting_title_javascript), Toast.LENGTH_SHORT).show();
                return true;
            });
            chip_javaScript.setOnClickListener(v -> {
                ninjaWebView.setProfileChanged();
                ninjaWebView.putProfileBoolean("_javascript", dialog_titleProfile, chip_profile_trusted, chip_profile_standard, chip_profile_protected, chip_profile_changed);
            });

            Chip chip_javaScriptPopUp = dialogView.findViewById(R.id.chip_javaScriptPopUp);
            chip_javaScriptPopUp.setChecked(ninjaWebView.getBoolean("_javascriptPopUp"));
            chip_javaScriptPopUp.setOnLongClickListener(view -> {
                Toast.makeText(context, getString(R.string.setting_title_javascript_popUp), Toast.LENGTH_SHORT).show();
                return true;
            });
            chip_javaScriptPopUp.setOnClickListener(v -> {
                ninjaWebView.setProfileChanged();
                ninjaWebView.putProfileBoolean("_javascriptPopUp", dialog_titleProfile, chip_profile_trusted, chip_profile_standard, chip_profile_protected, chip_profile_changed);
            });

            Chip chip_cookie = dialogView.findViewById(R.id.chip_cookie);
            chip_cookie.setChecked(ninjaWebView.getBoolean("_cookies"));
            chip_cookie.setOnLongClickListener(view -> {
                Toast.makeText(context, getString(R.string.setting_title_cookie), Toast.LENGTH_SHORT).show();
                return true;
            });
            chip_cookie.setOnClickListener(v -> {
                ninjaWebView.setProfileChanged();
                ninjaWebView.putProfileBoolean("_cookies", dialog_titleProfile, chip_profile_trusted, chip_profile_standard, chip_profile_protected, chip_profile_changed);
            });

            Chip chip_fingerprint = dialogView.findViewById(R.id.chip_Fingerprint);
            chip_fingerprint.setChecked(ninjaWebView.getBoolean("_fingerPrintProtection"));
            chip_fingerprint.setOnLongClickListener(view -> {
                Toast.makeText(context, getString(R.string.setting_title_fingerPrint), Toast.LENGTH_SHORT).show();
                return true;
            });
            chip_fingerprint.setOnClickListener(v -> {
                ninjaWebView.setProfileChanged();
                ninjaWebView.putProfileBoolean("_fingerPrintProtection", dialog_titleProfile, chip_profile_trusted, chip_profile_standard, chip_profile_protected, chip_profile_changed);
            });

            Chip chip_adBlock = dialogView.findViewById(R.id.chip_adBlock);
            chip_adBlock.setChecked(ninjaWebView.getBoolean("_adBlock"));
            chip_adBlock.setOnLongClickListener(view -> {
                Toast.makeText(context, getString(R.string.setting_title_adblock), Toast.LENGTH_SHORT).show();
                return true;
            });
            chip_adBlock.setOnClickListener(v -> {
                ninjaWebView.setProfileChanged();
                ninjaWebView.putProfileBoolean("_adBlock", dialog_titleProfile, chip_profile_trusted, chip_profile_standard, chip_profile_protected, chip_profile_changed);
            });

            Chip chip_saveData = dialogView.findViewById(R.id.chip_saveData);
            chip_saveData.setChecked(ninjaWebView.getBoolean("_saveData"));
            chip_saveData.setOnLongClickListener(view -> {
                Toast.makeText(context, getString(R.string.setting_title_save_data), Toast.LENGTH_SHORT).show();
                return true;
            });
            chip_saveData.setOnClickListener(v -> {
                ninjaWebView.setProfileChanged();
                ninjaWebView.putProfileBoolean("_saveData", dialog_titleProfile, chip_profile_trusted, chip_profile_standard, chip_profile_protected, chip_profile_changed);
            });

            Chip chip_history = dialogView.findViewById(R.id.chip_history);
            chip_history.setChecked(ninjaWebView.getBoolean("_saveHistory"));
            chip_history.setOnLongClickListener(view -> {
                Toast.makeText(context, getString(R.string.album_title_history), Toast.LENGTH_SHORT).show();
                return true;
            });
            chip_history.setOnClickListener(v -> {
                ninjaWebView.setProfileChanged();
                ninjaWebView.putProfileBoolean("_saveHistory", dialog_titleProfile, chip_profile_trusted, chip_profile_standard, chip_profile_protected, chip_profile_changed);
            });

            Chip chip_location = dialogView.findViewById(R.id.chip_location);
            chip_location.setChecked(ninjaWebView.getBoolean("_location"));
            chip_location.setOnLongClickListener(view -> {
                Toast.makeText(context, getString(R.string.setting_title_location), Toast.LENGTH_SHORT).show();
                return true;
            });
            chip_location.setOnClickListener(v -> {
                ninjaWebView.setProfileChanged();
                ninjaWebView.putProfileBoolean("_location", dialog_titleProfile, chip_profile_trusted, chip_profile_standard, chip_profile_protected, chip_profile_changed);
            });

            Chip chip_microphone = dialogView.findViewById(R.id.chip_microphone);
            chip_microphone.setChecked(ninjaWebView.getBoolean("_microphone"));
            chip_microphone.setOnLongClickListener(view -> {
                Toast.makeText(context, getString(R.string.setting_title_microphone), Toast.LENGTH_SHORT).show();
                return true;
            });
            chip_microphone.setOnClickListener(v -> {
                ninjaWebView.setProfileChanged();
                ninjaWebView.putProfileBoolean("_microphone", dialog_titleProfile, chip_profile_trusted, chip_profile_standard, chip_profile_protected, chip_profile_changed);
            });

            Chip chip_camera = dialogView.findViewById(R.id.chip_camera);
            chip_camera.setChecked(ninjaWebView.getBoolean("_camera"));
            chip_camera.setOnLongClickListener(view -> {
                Toast.makeText(context, getString(R.string.setting_title_camera), Toast.LENGTH_SHORT).show();
                return true;
            });
            chip_camera.setOnClickListener(v -> {
                ninjaWebView.setProfileChanged();
                ninjaWebView.putProfileBoolean("_camera", dialog_titleProfile, chip_profile_trusted, chip_profile_standard, chip_profile_protected, chip_profile_changed);
            });

            Chip chip_dom = dialogView.findViewById(R.id.chip_dom);
            chip_dom.setChecked(ninjaWebView.getBoolean("_dom"));
            chip_dom.setOnLongClickListener(view -> {
                Toast.makeText(context, getString(R.string.setting_title_dom), Toast.LENGTH_SHORT).show();
                return true;
            });
            chip_dom.setOnClickListener(v -> {
                ninjaWebView.setProfileChanged();
                ninjaWebView.putProfileBoolean("_dom", dialog_titleProfile, chip_profile_trusted, chip_profile_standard, chip_profile_protected, chip_profile_changed);
            });

            if (listTrusted.isWhite(url) || listStandard.isWhite(url) || listProtected.isWhite(url)) {
                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = context.getTheme();
                theme.resolveAttribute(R.attr.colorError, typedValue, true);
                int color = typedValue.data;
                MaterialCardView cardView = dialogView.findViewById(R.id.editProfile);
                cardView.setVisibility(View.GONE);
                dialog_warning.setTextColor(color);
            }

            Chip chip_toggleNightView = dialogView.findViewById(R.id.chip_toggleNightView);
            chip_toggleNightView.setChecked(ninjaWebView.isNightMode());
            chip_toggleNightView.setOnLongClickListener(view -> {
                Toast.makeText(context, getString(R.string.menu_nightView), Toast.LENGTH_SHORT).show();
                return true;
            });
            chip_toggleNightView.setOnClickListener(v -> {
                ninjaWebView.toggleNightMode();
                isNightMode = ninjaWebView.isNightMode();
                dialog.cancel();
            });

            Chip chip_toggleDesktop = dialogView.findViewById(R.id.chip_toggleDesktop);
            chip_toggleDesktop.setChecked(ninjaWebView.isDesktopMode());
            chip_toggleDesktop.setOnLongClickListener(view -> {
                Toast.makeText(context, getString(R.string.menu_desktopView), Toast.LENGTH_SHORT).show();
                return true;
            });
            chip_toggleDesktop.setOnClickListener(v -> {
                ninjaWebView.toggleDesktopMode(true);
                dialog.cancel();
            });

            Chip chip_toggleScreenOn = dialogView.findViewById(R.id.chip_toggleScreenOn);
            chip_toggleScreenOn.setChecked(sp.getBoolean("sp_screenOn", false));
            chip_toggleScreenOn.setOnLongClickListener(view -> {
                Toast.makeText(context, getString(R.string.setting_title_screenOn), Toast.LENGTH_SHORT).show();
                return true;
            });
            chip_toggleScreenOn.setOnClickListener(v -> {
                sp.edit().putBoolean("sp_screenOn", !sp.getBoolean("sp_screenOn", false)).apply();
                saveOpenedTabs();
                HelperUnit.triggerRebirth(context);
                dialog.cancel();
            });

            Chip chip_toggleAudioBackground = dialogView.findViewById(R.id.chip_toggleAudioBackground);
            chip_toggleAudioBackground.setChecked(sp.getBoolean("sp_audioBackground", false));
            chip_toggleAudioBackground.setOnLongClickListener(view -> {
                Toast.makeText(context, getString(R.string.setting_title_audioBackground), Toast.LENGTH_SHORT).show();
                return true;
            });
            chip_toggleAudioBackground.setOnClickListener(v -> {
                sp.edit().putBoolean("sp_audioBackground", !sp.getBoolean("sp_audioBackground", false)).apply();
                dialog.cancel();
            });

            Chip chip_toggleRedirect = dialogView.findViewById(R.id.chip_toggleRedirect);
            chip_toggleRedirect.setChecked(sp.getBoolean("redirect", false));
            chip_toggleRedirect.setOnLongClickListener(view -> {
                Toast.makeText(context, getString(R.string.privacy_redirect), Toast.LENGTH_SHORT).show();
                return true;
            });
            chip_toggleRedirect.setOnClickListener(v -> {
                if (sp.getBoolean("redirect", false))
                    sp.edit().putBoolean("redirect", false).apply();
                else sp.edit().putBoolean("redirect", true).apply();
                dialog.cancel();
            });

            Button ib_reload = dialogView.findViewById(R.id.ib_reload);
            ib_reload.setOnClickListener(view -> {
                if (ninjaWebView != null) {
                    dialog.cancel();
                    ninjaWebView.reload();
                }
            });

            Button ib_settings = dialogView.findViewById(R.id.ib_settings);
            ib_settings.setOnClickListener(view -> {
                if (ninjaWebView != null) {
                    dialog.cancel();
                    Intent settings = new Intent(BrowserActivity.this, Settings_Activity.class);
                    startActivity(settings);
                }
            });

            Button button_help = dialogView.findViewById(R.id.button_help);
            button_help.setVisibility(View.VISIBLE);
            button_help.setOnClickListener(view -> {
                dialog.cancel();
                Uri webpage = Uri.parse("https://github.com/scoute-dich/browser/wiki/Fast-Toggle-Dialog");
                BrowserUnit.intentURL(this, webpage);
            });
        } else {
            NinjaToast.show(context, getString(R.string.app_error));
        }
    }

    private void showDialogFilter() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        View dialogView = View.inflate(context, R.layout.dialog_menu, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        TextView menuTitleFilter = dialogView.findViewById(R.id.menuTitle);
        menuTitleFilter.setText(R.string.menu_filter);
        CardView cardView = dialogView.findViewById(R.id.cardView);
        cardView.setVisibility(View.GONE);

        Button button_help = dialogView.findViewById(R.id.button_help);
        button_help.setVisibility(View.VISIBLE);
        button_help.setOnClickListener(view -> {
            dialog.cancel();
            Uri webpage = Uri.parse("https://github.com/scoute-dich/browser/wiki/Filter-Dialog");
            BrowserUnit.intentURL(this, webpage);
        });

        Objects.requireNonNull(dialog.getWindow()).setGravity(Gravity.BOTTOM);
        GridView menu_grid = dialogView.findViewById(R.id.menu_grid);
        final List<GridItem> gridList = new LinkedList<>();
        HelperUnit.addFilterItems(activity, gridList);

        GridAdapter gridAdapter = new GridAdapter(context, gridList);
        menu_grid.setNumColumns(2);
        menu_grid.setHorizontalSpacing(20);
        menu_grid.setVerticalSpacing(20);
        menu_grid.setAdapter(gridAdapter);
        gridAdapter.notifyDataSetChanged();
        menu_grid.setOnItemClickListener((parent, view, position, id) -> {
            filter = true;
            filterBy = gridList.get(position).getData();
            dialog.cancel();
            bottom_navigation.setSelectedItemId(R.id.page_2);
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(HelperUnit.convertDpToPixel(20f, context),
                HelperUnit.convertDpToPixel(10f, context),
                HelperUnit.convertDpToPixel(20f, context),
                HelperUnit.convertDpToPixel(10f, context));
        menu_grid.setLayoutParams(params);
    }

    // Voids
    private void doubleTapsQuit() {
        if (!sp.getBoolean("sp_close_browser_confirm", true)) finish();
        else {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
            builder.setTitle(R.string.setting_title_confirm_exit);
            builder.setIcon(R.drawable.icon_alert);
            builder.setMessage(R.string.toast_quit);
            builder.setPositiveButton(R.string.app_ok, (dialog, whichButton) -> finish());
            builder.setNegativeButton(R.string.app_cancel, (dialog, whichButton) -> dialog.cancel());
            AlertDialog dialog = builder.create();
            dialog.show();
            HelperUnit.setupDialog(context, dialog);
        }
    }

    private void saveOpenedTabs() {
        ArrayList<String> openTabs = new ArrayList<>();
        for (int i = 0; i < BrowserContainer.size(); i++) {
            if (currentAlbumController == BrowserContainer.get(i))
                openTabs.add(0, ((NinjaWebView) (BrowserContainer.get(i))).getUrl());
            else openTabs.add(((NinjaWebView) (BrowserContainer.get(i))).getUrl());
        }
        sp.edit().putString("openTabs", TextUtils.join("‚‗‚", openTabs)).apply();
        //Save profile of open Tabs in shared preferences
        ArrayList<String> openTabsProfile = new ArrayList<>();
        for (int i = 0; i < BrowserContainer.size(); i++) {
            if (currentAlbumController == BrowserContainer.get(i))
                openTabsProfile.add(0, ((NinjaWebView) (BrowserContainer.get(i))).getProfile());
            else openTabsProfile.add(((NinjaWebView) (BrowserContainer.get(i))).getProfile());
        }
        sp.edit().putString("openTabsProfile", TextUtils.join("‚‗‚", openTabsProfile)).apply();
    }

    private void setCustomFullscreen(boolean fullscreen) {
        if (fullscreen) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                final WindowInsetsController insetsController = getWindow().getInsetsController();
                if (insetsController != null) {
                    insetsController.hide(WindowInsets.Type.statusBars());
                    insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            } else
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                final WindowInsetsController insetsController = getWindow().getInsetsController();
                if (insetsController != null) {
                    insetsController.show(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                    insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            } else
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
    }

    public void goBack_skipRedirects() {
        if (ninjaWebView.canGoBack()) {
            ninjaWebView.setIsBackPressed(true);
            ninjaWebView.goBack();
        }
    }

    private void printPDF() {
        String title = HelperUnit.fileName(ninjaWebView.getUrl());
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        PrintDocumentAdapter printAdapter = ninjaWebView.createPrintDocumentAdapter(title);
        Objects.requireNonNull(printManager).print(title, printAdapter, new PrintAttributes.Builder().build());
        sp.edit().putBoolean("pdf_create", true).apply();
    }

    private void save_atHome(final String title, final String url) {
        FaviconHelper faviconHelper = new FaviconHelper(context);
        faviconHelper.addFavicon(context, ninjaWebView.getUrl(), ninjaWebView.getFavicon());

        RecordAction action = new RecordAction(context);
        action.open(true);
        if (action.checkUrl(url, RecordUnit.TABLE_START))
            NinjaToast.show(this, R.string.app_error);
        else {
            int counter = sp.getInt("counter", 0);
            counter = counter + 1;
            sp.edit().putInt("counter", counter).apply();
            if (action.addStartSite(new Record(title, url, 0, counter, 1, ninjaWebView.isDesktopMode(), ninjaWebView.isNightMode(), 0)))
                NinjaToast.show(this, R.string.app_done);
            else NinjaToast.show(this, R.string.app_error);
        }
        action.close();
    }

    private void copyLink(String url) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("text", url);
        Objects.requireNonNull(clipboard).setPrimaryClip(clip);
        String text = getString(R.string.toast_copy_successful) + ": " + url;
        NinjaToast.show(this, text);
    }

    private void shareLink(String title, String url) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        sharingIntent.putExtra(Intent.EXTRA_TEXT, url);
        context.startActivity(Intent.createChooser(sharingIntent, (context.getString(R.string.menu_share_link))));
    }

    private void postLink(String data) {
        String urlForPosting = sp.getString("urlForPosting", "");
        String message = getString(R.string.menu_shareClipboard) + ": " + data;
        assert urlForPosting != null;

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        View dialogViewSubMenu = View.inflate(context, R.layout.dialog_edit, null);

        TextInputLayout editTopLayout = dialogViewSubMenu.findViewById(R.id.editTopLayout);
        editTopLayout.setVisibility(View.GONE);
        TextInputLayout editBottomLayout = dialogViewSubMenu.findViewById(R.id.editBottomLayout);
        editBottomLayout.setHint(getString(R.string.dialog_URL_hint));
        editBottomLayout.setHelperText(getString(R.string.dialog_postOnWebsiteHint));

        EditText editTop = dialogViewSubMenu.findViewById(R.id.editBottom);
        if (urlForPosting.isEmpty()) editTop.setText("");
        else editTop.setText(urlForPosting);
        editTop.setHint(getString(R.string.dialog_URL_hint));

        builder.setView(dialogViewSubMenu);
        builder.setTitle(getString(R.string.dialog_postOnWebsite));
        builder.setMessage(message);

        Dialog dialog = builder.create();
        dialog.show();
        HelperUnit.setupDialog(context, dialog);

        Button ib_cancel = dialogViewSubMenu.findViewById(R.id.editCancel);
        ib_cancel.setOnClickListener(v -> {
            HelperUnit.hideSoftKeyboard(editTop, context);
            dialog.cancel();
        });
        Button ib_ok = dialogViewSubMenu.findViewById(R.id.editOK);
        ib_ok.setOnClickListener(v -> {
            String shareTop = editTop.getText().toString().trim();
            sp.edit().putString("urlForPosting", shareTop).apply();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("text", data);
            Objects.requireNonNull(clipboard).setPrimaryClip(clip);
            String text = getString(R.string.toast_copy_successful) + ": " + data;
            NinjaToast.show(this, text);
            addAlbum("", shareTop, true, false, "");
            HelperUnit.hideSoftKeyboard(editTop, context);
            dialog.cancel();
        });
    }

    private void searchOnSite() {
        omniBox.setVisibility(View.GONE);
        searchPanel.setVisibility(View.VISIBLE);
        HelperUnit.showSoftKeyboard(searchBox, activity);
    }

    private void saveBookmark() {
        FaviconHelper faviconHelper = new FaviconHelper(context);
        faviconHelper.addFavicon(context, ninjaWebView.getUrl(), ninjaWebView.getFavicon());
        RecordAction action = new RecordAction(context);
        action.open(true);
        if (action.checkUrl(ninjaWebView.getUrl(), RecordUnit.TABLE_BOOKMARK))
            NinjaToast.show(this, R.string.app_error);
        else {
            long value = 0;  //default red icon
            action.addBookmark(new Record(ninjaWebView.getTitle(), ninjaWebView.getUrl(), 0, 0, 2, ninjaWebView.isDesktopMode(), ninjaWebView.isNightMode(), value));
            NinjaToast.show(this, R.string.app_done);
        }
        action.close();
    }

    private void performGesture(String gesture) {
        String gestureAction = Objects.requireNonNull(sp.getString(gesture, "0"));
        switch (gestureAction) {
            case "01":
                break;
            case "02":
                if (ninjaWebView.canGoForward()) {
                    WebBackForwardList mWebBackForwardList = ninjaWebView.copyBackForwardList();
                    String historyUrl = mWebBackForwardList.getItemAtIndex(mWebBackForwardList.getCurrentIndex() + 1).getUrl();
                    ninjaWebView.initPreferences(historyUrl);
                    ninjaWebView.goForward();
                } else NinjaToast.show(this, R.string.toast_webview_forward);
                break;
            case "03":
                if (ninjaWebView.canGoBack()) {
                    WebBackForwardList mWebBackForwardList = ninjaWebView.copyBackForwardList();
                    String historyUrl = mWebBackForwardList.getItemAtIndex(mWebBackForwardList.getCurrentIndex() - 1).getUrl();
                    ninjaWebView.initPreferences(historyUrl);
                    goBack_skipRedirects();
                } else removeAlbum(currentAlbumController);
                break;
            case "04":
                ninjaWebView.pageUp(true);
                break;
            case "05":
                ninjaWebView.pageDown(true);
                break;
            case "06":
                showAlbum(nextAlbumController(false));
                break;
            case "07":
                showAlbum(nextAlbumController(true));
                break;
            case "08":
                showOverview();
                break;
            case "09":
                addAlbum(getString(R.string.app_name), Objects.requireNonNull(sp.getString("favoriteURL", "about:blank")), true, false, "");
                break;
            case "10":
                removeAlbum(currentAlbumController);
                break;
            case "11":
                showTabView();
                break;
            case "12":
                shareLink(ninjaWebView.getTitle(), ninjaWebView.getUrl());
                break;
            case "13":
                searchOnSite();
                break;
            case "14":
                saveBookmark();
                break;
            case "15":
                save_atHome(ninjaWebView.getTitle(), ninjaWebView.getUrl());
                break;
            case "16":
                ninjaWebView.reload();
                break;
            case "17":
                ninjaWebView.loadUrl(Objects.requireNonNull(sp.getString("favoriteURL", "about:blank")));
                break;
            case "18":
                bottom_navigation.setSelectedItemId(R.id.page_2);
                showOverview();
                showDialogFilter();
                break;
            case "19":
                showDialogFastToggle();
                break;
            case "20":
                ninjaWebView.toggleNightMode();
                isNightMode = ninjaWebView.isNightMode();
                break;
            case "21":
                ninjaWebView.toggleDesktopMode(true);
                break;
            case "22":
                sp.edit().putBoolean("sp_screenOn", !sp.getBoolean("sp_screenOn", false)).apply();
                saveOpenedTabs();
                HelperUnit.triggerRebirth(context);
                break;
            case "23":
                sp.edit().putBoolean("sp_audioBackground", !sp.getBoolean("sp_audioBackground", false)).apply();
                break;
            case "24":
                copyLink(ninjaWebView.getUrl());
                break;
        }
    }

    private void closeTabConfirmation(final Runnable okAction) {
        if (!sp.getBoolean("sp_close_tab_confirm", false)) okAction.run();
        else {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
            builder.setTitle(R.string.menu_closeTab);
            builder.setIcon(R.drawable.icon_alert);
            builder.setMessage(R.string.toast_quit_TAB);
            builder.setPositiveButton(R.string.app_ok, (dialog, whichButton) -> okAction.run());
            builder.setNegativeButton(R.string.app_cancel, (dialog, whichButton) -> dialog.cancel());
            AlertDialog dialog = builder.create();
            dialog.show();
            HelperUnit.setupDialog(context, dialog);
        }
    }

    private void dispatchIntent(Intent intent) {

        String action = intent.getAction();
        String url = intent.getStringExtra(Intent.EXTRA_TEXT);
        String data = "";

        if ("".equals(action)) {
            Log.i(TAG, "resumed FOSS browser");
            return;
        } else if (filePathCallback != null) {
            filePathCallback = null;
            getIntent().setAction("");
            return;
        } else if ("postLink".equals(action)) {
            getIntent().setAction("");
            hideOverview();
            postLink(url);
            return;
        } else if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_PROCESS_TEXT)) {
            CharSequence text = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
            assert text != null;
            data = text.toString();
        } else if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_WEB_SEARCH)) {
            data = Objects.requireNonNull(intent.getStringExtra(SearchManager.QUERY));
        } else if (Intent.ACTION_VIEW.equals(action)) {
            data = Objects.requireNonNull(getIntent().getData()).toString();
        } else if (url != null && Intent.ACTION_SEND.equals(action)) {
            data = url;
        }

        if (!data.isEmpty()) {
            addAlbum(null, data, true, false, "");
            getIntent().setAction("");
            hideOverview();
            BrowserUnit.openInBackground(activity, intent, data);
        }

        //if still no open Tab open default page
        if (BrowserContainer.size() < 1) {
            if (sp.getBoolean("start_tabStart", false)) showOverview();
            addAlbum(getString(R.string.app_name), Objects.requireNonNull(sp.getString("favoriteURL", "about:blank")), true, false, "");
            getIntent().setAction("");
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setWebView(String title, final String url, final boolean foreground) {
        ninjaWebView = new NinjaWebView(context, drawerLayout);
        if (Objects.requireNonNull(sp.getString("saved_key_ok", "no")).equals("no")) {
            sp.edit().putString("saved_key_ok", "yes")
                    .putString("setting_gesture_tb_up", "04")
                    .putString("setting_gesture_tb_down", "05")
                    .putString("setting_gesture_tb_left", "03")
                    .putString("setting_gesture_tb_right", "02")
                    .putString("setting_gesture_nav_up", "16")
                    .putString("setting_gesture_nav_down", "10")
                    .putString("setting_gesture_nav_left", "07")
                    .putString("setting_gesture_nav_right", "06")
                    .putString("setting_gesture_tabButton", "19")
                    .putString("setting_gesture_overViewButton", "18")
                    .putBoolean("sp_autofill", true)
                    .apply();
            ninjaWebView.setProfileDefaultValues();
        }
        if (isNightMode) {
            ninjaWebView.toggleNightMode();
            isNightMode = ninjaWebView.isNightMode();
        }
        ninjaWebView.setBrowserController(this);
        ninjaWebView.setAlbumTitle(title, url);
        activity.registerForContextMenu(ninjaWebView);

        if (url.isEmpty()) ninjaWebView.loadUrl("about:blank");
        else ninjaWebView.loadUrl(url);

        if (currentAlbumController != null) {
            ninjaWebView.setPredecessor(currentAlbumController);
            //save currentAlbumController and use when TAB is closed via Back button
            int index = BrowserContainer.indexOf(currentAlbumController) + 1;
            BrowserContainer.add(ninjaWebView, index);
        } else BrowserContainer.add(ninjaWebView);

        if (!foreground) ninjaWebView.deactivate();
        else {
            ninjaWebView.activate();
            showAlbum(ninjaWebView);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) ninjaWebView.reload();
        }
        View albumView = ninjaWebView.getAlbumView();
        tab_container2.addView(albumView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        updateOmniBox();
    }

    private synchronized void addAlbum(String title, final String url, final boolean foreground,
                                       final boolean profileDialog, String profile) {

        //restoreProfile from shared preferences if app got killed
        if (!profile.equals("")) sp.edit().putString("profile", profile).apply();
        if (profileDialog) {
            GridItem item_01 = new GridItem(getString(R.string.setting_title_profiles_trusted), 0);
            GridItem item_02 = new GridItem(getString(R.string.setting_title_profiles_standard), 0);
            GridItem item_03 = new GridItem(getString(R.string.setting_title_profiles_protected), 0);
            GridItem item_04 = new GridItem(getString(R.string.setting_title_profiles_changed), 0);

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
            View dialogView = View.inflate(context, R.layout.dialog_menu, null);
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();
            FaviconHelper.setFavicon(context, dialogView, url, R.id.menu_icon, R.drawable.icon_link);
            TextView dialog_title = dialogView.findViewById(R.id.menuTitle);
            dialog_title.setText(url);
            dialog.show();

            Objects.requireNonNull(dialog.getWindow()).setGravity(Gravity.BOTTOM);
            GridView menu_grid = dialogView.findViewById(R.id.menu_grid);
            int orientation = this.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) menu_grid.setNumColumns(1);
            else menu_grid.setNumColumns(2);
            final List<GridItem> gridList = new LinkedList<>();
            gridList.add(gridList.size(), item_01);
            gridList.add(gridList.size(), item_02);
            gridList.add(gridList.size(), item_03);
            gridList.add(gridList.size(), item_04);
            GridAdapter gridAdapter = new GridAdapter(context, gridList);
            menu_grid.setAdapter(gridAdapter);
            gridAdapter.notifyDataSetChanged();
            menu_grid.setOnItemClickListener((parent, view, position, id) -> {
                switch (position) {
                    case 0:
                        sp.edit().putString("profile", "profileTrusted").apply();
                        break;
                    case 1:
                        sp.edit().putString("profile", "profileStandard").apply();
                        break;
                    case 2:
                        sp.edit().putString("profile", "profileProtected").apply();
                        break;
                    case 3:
                        sp.edit().putString("profile", "profileChanged").apply();
                        break;
                }
                dialog.cancel();
                setWebView(title, url, foreground);
            });
        } else setWebView(title, url, foreground);
    }

}