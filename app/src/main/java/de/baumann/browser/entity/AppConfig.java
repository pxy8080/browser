package de.baumann.browser.entity;

import android.os.Environment;
import android.util.Log;

import java.io.File;

import de.baumann.browser.MainApplication;
import de.baumann.browser.util.PreferencesUtils;

/**
 * Created by xm on 17/8/18.
 */
public class AppConfig {
    public static final String ROOT_DIR = "BrowserData";//默认目录
    public String rootDataPath = "";//完整的程序主目录路径, 初次初始化的时候进行设置
    public String rootDownPath = "";//保存的下载路径
    public int videoSnifferThreadNum;
    public int videoSnifferRetryCountOnFail;
    public int maxConcurrentTask;
    public int m3U8DownloadThreadNum;
    public int m3U8DownloadSizeDetectRetryCountOnFail;
    public int downloadSubFileRetryCountOnFail;
    public int normalFileHeaderCheckRetryCountOnFail;
    public long normalFileSplitSize;
    public int normalFileDownloadThreadNum;
    public int webServerPort;


    public AppConfig() {
        loadConfig();
    }

    private void loadConfig() {
//        rootDataPath = PreferencesUtils.getString(MainApplication.mainApplication, "rootDataPath", Environment.getExternalStorageDirectory()+File.separator+ROOT_DIR);
        rootDataPath = PreferencesUtils.getString(MainApplication.mainApplication, "rootDataPath", MainApplication.mainApplication.getExternalFilesDir("") + "");
        rootDownPath = PreferencesUtils.getString(MainApplication.mainApplication, "rootDownPath", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator + ROOT_DIR + File.separator);

        videoSnifferThreadNum = PreferencesUtils.getInt(MainApplication.mainApplication, "videoSnifferThreadNum", 5);
        videoSnifferRetryCountOnFail = PreferencesUtils.getInt(MainApplication.mainApplication, "videoSnifferRetryCountOnFail", 1);
        videoSnifferRetryCountOnFail = 1;
        maxConcurrentTask = PreferencesUtils.getInt(MainApplication.mainApplication, "maxConcurrentTask", 2);
        m3U8DownloadThreadNum = PreferencesUtils.getInt(MainApplication.mainApplication, "m3U8DownloadThreadNum", 20);
        m3U8DownloadSizeDetectRetryCountOnFail = PreferencesUtils.getInt(MainApplication.mainApplication, "m3U8DownloadSizeDetectRetryCountOnFail", 20);
        downloadSubFileRetryCountOnFail = PreferencesUtils.getInt(MainApplication.mainApplication, "downloadSubFileRetryCountOnFail", 50);
        normalFileHeaderCheckRetryCountOnFail = PreferencesUtils.getInt(MainApplication.mainApplication, "normalFileHeaderCheckRetryCountOnFail", 20);
        normalFileSplitSize = PreferencesUtils.getLong(MainApplication.mainApplication, "normalFileSplitSize", 2000000);
        normalFileDownloadThreadNum = PreferencesUtils.getInt(MainApplication.mainApplication, "normalFileDownloadThreadNum", 5);
        webServerPort = PreferencesUtils.getInt(MainApplication.mainApplication, "webServerPort", 10625);

        saveConfig();
    }

    public void saveConfig() {
        PreferencesUtils.putString(MainApplication.mainApplication, "rootDataPath", rootDataPath);
        PreferencesUtils.putString(MainApplication.mainApplication, "rootDownPath", rootDownPath);
        PreferencesUtils.putInt(MainApplication.mainApplication, "videoSnifferThreadNum", videoSnifferThreadNum);
        PreferencesUtils.putInt(MainApplication.mainApplication, "videoSnifferRetryCountOnFail", videoSnifferRetryCountOnFail);
        PreferencesUtils.putInt(MainApplication.mainApplication, "maxConcurrentTask", maxConcurrentTask);
        PreferencesUtils.putInt(MainApplication.mainApplication, "m3U8DownloadThreadNum", m3U8DownloadThreadNum);
        PreferencesUtils.putInt(MainApplication.mainApplication, "m3U8DownloadSizeDetectRetryCountOnFail", m3U8DownloadSizeDetectRetryCountOnFail);
        PreferencesUtils.putInt(MainApplication.mainApplication, "downloadSubFileRetryCountOnFail", downloadSubFileRetryCountOnFail);
        PreferencesUtils.putInt(MainApplication.mainApplication, "normalFileHeaderCheckRetryCountOnFail", normalFileHeaderCheckRetryCountOnFail);
        PreferencesUtils.putLong(MainApplication.mainApplication, "normalFileSplitSize", normalFileSplitSize);
        PreferencesUtils.putInt(MainApplication.mainApplication, "normalFileDownloadThreadNum", normalFileDownloadThreadNum);
        PreferencesUtils.putInt(MainApplication.mainApplication, "webServerPort", webServerPort);
    }
}
