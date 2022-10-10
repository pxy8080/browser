package de.baumann.browser;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.excellence.downloader.Downloader;

import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import de.baumann.browser.entity.AppConfig;

import de.baumann.browser.event.ShowToastMessageEvent;
import de.baumann.browser.event.getBadgeNumEvent;
import de.baumann.browser.util.DownloadForegroundService;


@SuppressLint("StaticFieldLeak")
public class MainApplication extends Application {
    public static MainApplication mainApplication = null;
    public static AppConfig appConfig = null;
    public static int DownTaskBadgeNum = 0;
    public static int DownloadedBadgeNum = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        mainApplication = this;
        appConfig = new AppConfig();
        //初始化解析模块YoutubeDL
        try {
            YoutubeDL.getInstance().init(this);
            Log.e(TAG, "success to initialize youtubedl-android");
        } catch (YoutubeDLException e) {
            Log.e(TAG, "failed to initialize youtubedl-android", e);
        }
        // 默认下载选项：任务数2，单任务单线程下载
        Downloader.init(this);
        EventBus.getDefault().register(this);

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onShowToastMessageEvent(ShowToastMessageEvent showToastMessageEvent) {
        Toast.makeText(MainApplication.mainApplication, showToastMessageEvent.getMessage(), Toast.LENGTH_SHORT).show();
    }

    public void startDownloadForegroundService() {
        Intent intent = new Intent(MainApplication.mainApplication, DownloadForegroundService.class);
        startService(intent);
    }

    public void stopDownloadForegroundService() {
        Intent intent = new Intent(MainApplication.mainApplication, DownloadForegroundService.class);
        stopService(intent);
    }

}
