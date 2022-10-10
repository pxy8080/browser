package de.baumann.browser.entity;
import android.graphics.Bitmap;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.excellence.downloader.FileDownloader.DownloadTask;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : https://veizhang.github.io/
 *     time   : 2017/2/17
 *     desc   : 下载任务
 * </pre>
 */

public class Task {
    private String SourceUrl;                       //真实地址
    private String RealUrl;                         //源地址
    private Bitmap bitmap;                          //视频关键帧
    private String FilePath;                            //文件地址
    private String FileState;
    private String mFileName = null;                //文件名
    private long mDownloadLength;                   //已下载大小
    private long mFileSize;                         //文件总大小
    private String mspeed;                          //文件速度

    private DownloadTask mDownloadTask = null;

    private TextView SpeedView = null;
    private Button mDeleteBtn = null;
    private ProgressBar mProgressBar = null;


    public Task(String sourceUrl, String realUrl, String mFileName, long mFileSize, String filePath, String fileState) {
        this.SourceUrl = sourceUrl;
        this.RealUrl = realUrl;
        this.mFileName = mFileName;
        this.mFileSize = mFileSize;
        this.FilePath = filePath;
        this.FileState = fileState;
    }

    public String getSourceUrl() {
        return SourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        SourceUrl = sourceUrl;
    }

    public String getRealUrl() {
        return RealUrl;
    }

    public void setRealUrl(String realUrl) {
        RealUrl = realUrl;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getFilePath() {
        return FilePath;
    }

    public void setFilePath(String filePath) {
        FilePath = filePath;
    }

    public String getFileState() {
        return FileState;
    }

    public void setFileState(String fileState) {
        FileState = fileState;
    }

    public String getmFileName() {
        return mFileName;
    }

    public void setmFileName(String mFileName) {
        this.mFileName = mFileName;
    }

    public long getmDownloadLength() {
        return mDownloadLength;
    }

    public void setmDownloadLength(long mDownloadLength) {
        this.mDownloadLength = mDownloadLength;
    }

    public long getmFileSize() {
        return mFileSize;
    }

    public void setmFileSize(long mFileSize) {
        this.mFileSize = mFileSize;
    }

    public String getMspeed() {
        return mspeed;
    }

    public void setMspeed(String mspeed) {
        this.mspeed = mspeed;
    }

    public DownloadTask getmDownloadTask() {
        return mDownloadTask;
    }

    public void setmDownloadTask(DownloadTask mDownloadTask) {
        this.mDownloadTask = mDownloadTask;
    }

    public TextView getSpeedView() {
        return SpeedView;
    }

    public void setSpeedView(TextView speedView) {
        SpeedView = speedView;
    }

    public Button getmDeleteBtn() {
        return mDeleteBtn;
    }

    public void setmDeleteBtn(Button mDeleteBtn) {
        this.mDeleteBtn = mDeleteBtn;
    }

    public ProgressBar getmProgressBar() {
        return mProgressBar;
    }

    public void setmProgressBar(ProgressBar mProgressBar) {
        this.mProgressBar = mProgressBar;
    }
}
