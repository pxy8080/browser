package de.baumann.browser.util;

import android.util.Log;

import java.util.regex.Pattern;

/**
 * @Param:
 * @return: 检测视频连接是否是国外加密网站
 * @Author: PengXinYu
 * @date: 14:37
 */
public class DecideUtil {
    public static boolean isYoutuBe(String url) {
        return url.contains("https://m.youtube.com/watch?v=") || url.contains("https://m.youtube.com/shorts/");
    }

    public static boolean isVimeo(String url) {
        //匹配vimeo搜索到的
        String pattern1 = "https://vimeo.com/\\d+";
        boolean isMatch1 = Pattern.matches(pattern1, url);
        //匹配vimeo直接播放的视频（watch模块）
        String pattern2 = "https://vimeo.com/\\d+\\?autoplay=1";
        boolean isMatch2 = Pattern.matches(pattern2, url);
        return isMatch1 || isMatch2;
    }

    //匹配twitter
    public static boolean isTwitter(String url) {

        String pattern = "https://mobile.twitter.com/(.*)/status/\\d{19}";
        return Pattern.matches(pattern, url);
    }

    //匹配instagram
    public static boolean isInsTagram(String url) {
        String pattern = "https://www.instagram.com/.{1,5}/.{11}/.{0,30}";
        boolean isMatch = Pattern.matches(pattern, url);

        return isMatch;
    }

    public static String ChangeUrlIsFaceBook(String url) {
        String pattern = "https://m.facebook.com/story.php\\?story_fbid=\\d{15,16}&id=\\d{15,16}&m_entstream_source=video_home&player_suborigin=feed&player_format=permalink";
        boolean isMatch = Pattern.matches(pattern, url);

        String pattern2 = "https://m.youtube.com/watch\\?v=.{11}&list=.{20,}";
        boolean isMatch2 = Pattern.matches(pattern2, url);

        String pattern3 = "https://m.facebook.com/story.php\\?story_fbid=\\d{15,16}&id=\\d{15,16}";
        boolean isMatch3 = Pattern.matches(pattern3, url);

        if (isMatch || isMatch3) {
            String[] s = url.split("=");
            String suffix = s[1].substring(0, s[1].length() - 3);
            Log.i("TAG", "ChangeUrlIsFaceBook: " + "https://m.facebook.com/watch/?v=" + suffix);
            return "https://m.facebook.com/watch/?v=" + suffix;
        } else if (isMatch2) {
            String[] s = url.split("&");
            return s[0];
        }
        return url;
    }

    public static boolean isFacebook(String url) {
        //匹配facebook
        String pattern = "https://m.facebook.com/watch/\\?v=\\d{15,16}";
        boolean isMatch = Pattern.matches(pattern, url);
        return isMatch;
    }
}
