package de.baumann.browser.unit;

public class RecordUnit {

    public static final String TABLE_START = "GRID";
    public static final String TABLE_BOOKMARK = "BOOKAMRK";
    public static final String TABLE_HISTORY = "HISTORY";
    public static final String TABLE_DOWNLOAD = "DOWNLOAD";

    public static final String TABLE_TRUSTED = "JAVASCRIPT";
    public static final String TABLE_PROTECTED = "COOKIE";
    public static final String TABLE_STANDARD = "REMOTE";

    public static final String COLUMN_TITLE = "TITLE";
    public static final String COLUMN_URL = "URL";
    public static final String COLUMN_TIME = "TIME";
    public static final String COLUMN_DOMAIN = "DOMAIN";
    public static final String COLUMN_FILENAME = "FILENAME";
    public static final String COLUMN_ORDINAL = "ORDINAL";

    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_SOURCEURL = "SOURCEURL";          //真实地址
    public static final String COLUMN_REALURL = "REALURL";             //源地址
    public static final String COLUMN_FILEPATH = "FILEPATH";             //文件地址
    public static final String COLUMN_VIDEOTHUMBNAIL = "VIDEOTHUMBNAIL";             //视频缩略图
    public static final String COLUMN_VIDEODURATION = "VIDEODURATION";             //视频时长

    public static final String COLUMN_FILESTATE = "FILESTATE";             //文件状态

    public static final String CREATE_BOOKMARK = "CREATE TABLE "
            + TABLE_BOOKMARK
            + " ("
            + " " + COLUMN_TITLE + " text,"
            + " " + COLUMN_URL + " text,"
            + " " + COLUMN_TIME + " integer"
            + ")";

    public static final String CREATE_HISTORY = "CREATE TABLE "
            + TABLE_HISTORY
            + " ("
            + " " + COLUMN_TITLE + " text,"
            + " " + COLUMN_URL + " text,"
            + " " + COLUMN_TIME + " integer"
            + ")";

    public static final String CREATE_TRUSTED = "CREATE TABLE "
            + TABLE_TRUSTED
            + " ("
            + " " + COLUMN_DOMAIN + " text"
            + ")";

    public static final String CREATE_PROTECTED = "CREATE TABLE "
            + TABLE_PROTECTED
            + " ("
            + " " + COLUMN_DOMAIN + " text"
            + ")";

    public static final String CREATE_STANDARD = "CREATE TABLE "
            + TABLE_STANDARD
            + " ("
            + " " + COLUMN_DOMAIN + " text"
            + ")";

    public static final String CREATE_START = "CREATE TABLE "
            + TABLE_START
            + " ("
            + " " + COLUMN_TITLE + " text,"
            + " " + COLUMN_URL + " text,"
            + " " + COLUMN_FILENAME + " text,"
            + " " + COLUMN_ORDINAL + " integer"
            + ")";

    public static final String CREATE_DOWNLOAD = "CREATE TABLE "
            + TABLE_DOWNLOAD
            + " ("
            + " " + COLUMN_ID + " text,"
            + " " + COLUMN_SOURCEURL + " text,"
            + " " + COLUMN_REALURL + " text,"
            + " " + COLUMN_FILENAME + " text,"
            + " " + COLUMN_FILEPATH + " text,"
            + " " + COLUMN_VIDEOTHUMBNAIL + " text,"
            + " " + COLUMN_VIDEODURATION + " text,"
            + " " + COLUMN_FILESTATE + " text"
            + ")";
}