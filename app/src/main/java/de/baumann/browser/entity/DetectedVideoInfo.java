package de.baumann.browser.entity;

import com.yausername.youtubedl_android.mapper.VideoFormat;

import java.util.ArrayList;

/**
 * @Param:
 * @return:
 * @Author: PengXinYu
 * @date: 10:52
 */
public class DetectedVideoInfo {
    private String url;
    private String sourcePageUrl;//原网页url
    private String sourcePageTitle;//原网页标题
    private String VideoThumbnail;//视频缩略图地址
    private String mDuration;//视频时长
    private ArrayList<VideoFormat> VideoFormatlist;

    public DetectedVideoInfo(String url, String sourcePageUrl, String sourcePageTitle, String VideoThumbnail, String mDuration, ArrayList<VideoFormat> VideoFormatlist) {
        this.url = url;
        this.sourcePageUrl = sourcePageUrl;
        this.sourcePageTitle = sourcePageTitle;
        this.VideoThumbnail = VideoThumbnail;
        this.mDuration = mDuration;
        this.VideoFormatlist = VideoFormatlist;
    }

    public String getVideoThumbnail() {
        return VideoThumbnail;
    }

    public void setVideoThumbnail(String videoThumbnail) {
        VideoThumbnail = videoThumbnail;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSourcePageUrl() {
        return sourcePageUrl;
    }

    public void setSourcePageUrl(String sourcePageUrl) {
        this.sourcePageUrl = sourcePageUrl;
    }

    public String getSourcePageTitle() {
        return sourcePageTitle;
    }

    public void setSourcePageTitle(String sourcePageTitle) {
        this.sourcePageTitle = sourcePageTitle;
    }

    public String getmDuration() {
        return mDuration;
    }

    public void setmDuration(String mDuration) {
        this.mDuration = mDuration;
    }

    public ArrayList<VideoFormat> getVideoFormatlist() {
        return VideoFormatlist;
    }

    public void setVideoFormatlist(ArrayList<VideoFormat> videoFormatlist) {
        VideoFormatlist = videoFormatlist;
    }
}
