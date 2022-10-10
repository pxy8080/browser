package de.baumann.browser.entity;

import android.os.Handler;

import com.excellence.downloader.Downloader;
import com.excellence.downloader.FileDownloader;
import com.excellence.downloader.exception.DownloadError;
import com.excellence.downloader.utils.IListener;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import de.baumann.browser.MainApplication;
import de.baumann.browser.database.RecordAction;
import de.baumann.browser.event.DownTaskStateChange;
import de.baumann.browser.util.BaseHandleMessage;

public class DowningTask {
    public static final int ID_DOWNNUM = 97;
    public static final int ID_NOTIFYDATASETCHANGED = 100;

    private Integer mUuid;
    private String url;
    private String mFileName;
    private String mSourceUrl;
    private String mFilePath;
    private String mVideoThumbnail;
    private String mDuration;
    private int mState;

    private long mfileSize;
    private long mdownloadedSize;
    private long mspeed;

    private int isSelect = 1;

    private FileDownloader.DownloadTask mDownloadTask = null;

    public DowningTask(Integer mUuid, String url, String mFileName, String mSourceUrl, String FilePath, String mVideoThumbnail, String mDuration, int mState) {
        this.mUuid = mUuid;
        this.url = url;
        this.mFileName = mFileName;
        this.mSourceUrl = mSourceUrl;
        this.mFilePath = FilePath;
        this.mVideoThumbnail = mVideoThumbnail;
        this.mDuration = mDuration;
        this.mState = mState;
        if (mState != 2) newDownTask();
    }

    public int getIsSelect() {
        return isSelect;
    }

    public void setIsSelect(int isSelect) {
        this.isSelect = isSelect;
    }

    public Integer getmUuid() {
        return mUuid;
    }

    public void setmUuid(Integer mUuid) {
        this.mUuid = mUuid;
    }

    public String geturl() {
        return url;
    }

    public void seturl(String url) {
        this.url = url;
    }

    public String getmFileName() {
        return mFileName;
    }

    public long getFileSize() {
        return mfileSize;
    }

    public long getDownloadedSize() {
        return mdownloadedSize;
    }

    public long getSpeed() {
        return mspeed;
    }

    public void setmFileName(String mFileName) {
        this.mFileName = mFileName;
    }

    public String getmSourceUrl() {
        return mSourceUrl;
    }

    public void setmFilePath(String mFilePath) {
        this.mFilePath = mFilePath;
    }

    public void setmSourceUrl(String mSourceUrl) {
        this.mSourceUrl = mSourceUrl;
    }

    public String getmFilePath() {
        return mFilePath;
    }

    public int getmState() {
        return mState;
    }

    public void setmState(int mState) {
        this.mState = mState;
    }

    public FileDownloader.DownloadTask getDownloadTask() {
        return mDownloadTask;
    }


    private void newDownTask() {

        File file = new File(mFilePath);
        MainApplication.DownTaskBadgeNum++;
        mDownloadTask = Downloader.addTask(file, url, new IListener() {
            @Override
            public void onPreExecute(long fileSize) {
                mfileSize = fileSize;
//                BaseHandleMessage.getInstance().setHandlerMessage(ID_DOWNBRAN, new downBean(mUuid, url, file.getName(), 0,
//                        fileSize, 0, 1));
                BaseHandleMessage.getInstance().setHandlerMessage(ID_NOTIFYDATASETCHANGED, mUuid);
                BaseHandleMessage.getInstance().setHandlerMessage(ID_DOWNNUM, MainApplication.DownTaskBadgeNum);
            }

            @Override
            public void onProgressChange(long fileSize, long downloadedSize) {

            }

            @Override
            public void onProgressChange(long fileSize, long downloadedSize, long speed) {
                boolean is_notifydatasetchanged = mfileSize != fileSize || mdownloadedSize != downloadedSize || mspeed != speed;

                mfileSize = fileSize;
                mdownloadedSize = downloadedSize;
                mspeed = speed;
                if (is_notifydatasetchanged)
                    BaseHandleMessage.getInstance().setHandlerMessage(ID_NOTIFYDATASETCHANGED, mUuid);

            }

            @Override
            public void onCancel() {
                EventBus.getDefault().post(new DownTaskStateChange(mUuid, "????"));
            }

            @Override
            public void onError(DownloadError error) {
                error.printStackTrace();
            }

            @Override
            public void onSuccess() {
                RecordAction action = new RecordAction(MainApplication.mainApplication);
                action.open(false);
                action.updateTaskState(mUuid, 2);
                action.close();
                mState = 2;

                BaseHandleMessage.getInstance().setHandlerMessage(ID_NOTIFYDATASETCHANGED, mUuid);
                MainApplication.DownTaskBadgeNum--;
                MainApplication.DownloadedBadgeNum++;
                BaseHandleMessage.getInstance().setHandlerMessage(ID_DOWNNUM, MainApplication.DownTaskBadgeNum);
            }
        });

    }

    public String getmVideoThumbnail() {
        return mVideoThumbnail;
    }

    public void setmVideoThumbnail(String mVideoThumbnail) {
        this.mVideoThumbnail = mVideoThumbnail;
    }

    public String getmDuration() {
        return mDuration;
    }

    public void setmDuration(String mDuration) {
        this.mDuration = mDuration;
    }
}
