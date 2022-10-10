package de.baumann.browser.util;

import android.media.MediaMetadataRetriever;

import java.util.HashMap;
import java.util.Objects;

public class VideoUtil {
    public static String getLocalVideoDuration(String filePath) {
        int duration = 0;
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(filePath);
            duration = Integer.parseInt(Objects.requireNonNull(mmr.extractMetadata
                    (MediaMetadataRetriever.METADATA_KEY_DURATION))) / 1000;//除以 1000 返回是:
            //时长(毫:)
//            String duration = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
//            //宽
//            String width = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
//            //高
//            String height = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);

        } catch (Exception e) {
            e.printStackTrace();
            return formatDateTime(duration);
        }
        return formatDateTime(duration);
    }


    /**
     * 根据url查询视频时长和宽高
     *
     * @param url
     * @return
     */
    public static long getPlayTime(String url) {
        android.media.MediaMetadataRetriever metadataRetriever = new android.media.MediaMetadataRetriever();
        Long durationTime = null;
        try {
            if (url != null) {
                HashMap<String, String> headers;
                headers = new HashMap<>();
                headers.put("User-Agent", "Mozilla/5.0 (Linux; U; Android 4.4.2; zh-CN; MW-KW-001 Build/JRO03C) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 UCBrowser/1.0.0.001 U4/0.8.0 Mobile Safari/533.1");
                metadataRetriever.setDataSource(url, headers);
            } else {
                //mmr.setDataSource(mFD, mOffset, mLength);
            }
            durationTime = Long.valueOf(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));//时长(毫:)
            String width = metadataRetriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);//宽
            String height = metadataRetriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);//高
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            metadataRetriever.release();
        }
        return durationTime;
    }

    public static String formatDateTime(long mss) {
        String DateTimes;
        long hours = (mss % (60 * 60 * 24)) / (60 * 60);
        long minutes = (mss % (60 * 60)) / 60;
        long seconds = mss % 60;
        String second;
        String hour;
        if (seconds < 10)
            second = "0" + seconds;
        else second = String.valueOf(seconds);
        if (hours < 10)
            hour = "0" + hours;
        else hour = String.valueOf(hours);
        if (hours > 0) {
            DateTimes = hour + ":" + minutes + ":"
                    + second;
        } else if (minutes > 0) {
            DateTimes = minutes + ":"
                    + second;
        } else {
            DateTimes = "00" + ":" + second;
        }
        return DateTimes;
    }

}
