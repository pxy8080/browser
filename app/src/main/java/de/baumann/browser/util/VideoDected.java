package de.baumann.browser.util;

import android.util.Log;

import com.yausername.youtubedl_android.mapper.VideoFormat;
import com.yausername.youtubedl_android.mapper.VideoInfo;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.baumann.browser.activity.BrowserActivity;
import de.baumann.browser.entity.DetectedVideoInfo;
import de.baumann.browser.entity.DowningTask;
import de.baumann.browser.event.VideoDectedEvent;

public class VideoDected {
    private static DetectedVideoInfo detectedVideoInfo = null;

    public synchronized static void add(VideoInfo streamInfo) {
        ArrayList<VideoFormat> list = streamInfo.getFormats();
        for (int i = 0; i < list.size(); i++) {
            VideoFormat a = list.get(i);
            Log.i("", "信息: " + a.getFormat() + "   " + a.getFileSize()+"    "+a.getExt() + "   " + a.getUrl());
        }
        list.removeIf(videoFormat -> !(videoFormat.getExt().equals("mp4") && videoFormat.getFormat().contains("x")));
        detectedVideoInfo = new DetectedVideoInfo(streamInfo.getUrl(), streamInfo.getWebpageUrl(),
                streamInfo.getTitle(), streamInfo.getThumbnail(), TimeUtil.formatTime(streamInfo.getDuration()), list);
        EventBus.getDefault().post(new VideoDectedEvent());
    }

    public static DetectedVideoInfo getdetectedVideo() {
        return detectedVideoInfo;
    }

    public static void ClearQueue() {
        detectedVideoInfo = null;
    }
}
