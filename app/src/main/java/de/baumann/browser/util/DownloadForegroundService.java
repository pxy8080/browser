package de.baumann.browser.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import de.baumann.browser.MainApplication;
import de.baumann.browser.R;

public class DownloadForegroundService extends Service {
    private static final int ONGOING_NOTIFICATION_ID = 1;

    public DownloadForegroundService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationManager service = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel();
        }

        Notification notification = new NotificationCompat.Builder(MainApplication.mainApplication, channelId)
                .setContentTitle("前台任务")
                .setContentText("正在下载")
                .setSmallIcon(R.drawable.icon_download)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon_download)).build();
        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(){
        String channelId = "VBrowserNotification";
        String channelName = "前台下载通知";
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_HIGH);
        chan.setLightColor(Color.BLUE);
        chan.setImportance(NotificationManager.IMPORTANCE_NONE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
