package de.baumann.browser.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.VideoDecoder;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Hashtable;

import de.baumann.browser.R;

public class GlideUtil {
    /**
     * 获取网络视频第一帧
     *
     * @param videoUrl
     * @return
     */
    public static Bitmap loadFirst(String videoUrl) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            //根据url获取缩略图
            retriever.setDataSource(videoUrl, new HashMap());
            //获得第一帧图片
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException e) {
            //e.printStackTrace();
            Log.e("zhu", e.toString());
        } finally {
            retriever.release();
        }
        return bitmap;
    }


    /**
     * 有个0- 到1秒的延迟
     *
     * @param context         上下文
     * @param uri             视频地址
     * @param imageView       设置image
     * @param frameTimeMicros 获取某一时间帧
     */
    @SuppressLint("CheckResult")
    public static ImageView loadVideoScreenshot(final Context context, String uri, ImageView imageView, long frameTimeMicros) {
        RequestOptions requestOptions = RequestOptions.frameOf(frameTimeMicros);
        requestOptions.placeholder(R.drawable.icon_failed_videoframe);
        requestOptions.set(VideoDecoder.FRAME_OPTION, MediaMetadataRetriever.OPTION_CLOSEST);
        requestOptions.transform(new BitmapTransformation() {
            @Override
            protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
                return toTransform;
            }

            @Override
            public void updateDiskCacheKey(MessageDigest messageDigest) {
                try {
                    messageDigest.update((context.getPackageName() + "RotateTransform").getBytes("utf-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        Glide.with(context).load(uri)
                .apply(requestOptions).into(imageView);
        return imageView;
    }

    public static void LoadVideoThumbnail(Context context, ImageView imageView, String url) {
//        Glide.with(context).load(url).error(R.color.black).skipMemoryCache(false)
//                .diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(imageView);
        Glide.with(context)
                .load(url)
                .error(R.color.black)
                .into(new SimpleTarget() {
                    @Override
                    public void onResourceReady(@NonNull Object resource, @Nullable @org.jetbrains.annotations.Nullable Transition transition) {
                        imageView.setBackground((Drawable) resource);
                    }
                });
    }

    /**
     * 获取帧
     *
     * @param context
     * @param mmr
     * @param path
     * @return
     */
    public static Bitmap getVideoFrame(Context context, MediaMetadataRetriever mmr, String path) {
        long timeUs = 1;//取100毫秒得帧
        mmr.setDataSource(context, Uri.parse(path));
        Bitmap bitmap = mmr.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST);
        return bitmap;
    }

    public static void getFirstFrame(Context context, ImageView imageView, String url) {
        Glide.with(context)
                .setDefaultRequestOptions(
                        new RequestOptions()
                                .frame(0)
                                .centerCrop()
                )
                .load(url)
                .into(imageView);
    }


    /**
     * @param obj       这里obj 只加载 url bitmap  drawable
     * @param imageView 需要加载的图片
     * @describe 加载正常图片
     * @note 这里并没有加载错图，在有错图的时候设置 error()
     */
    public static void loadImage(Object obj, ImageView imageView) {
        if (obj instanceof String) {
            Glide.with(imageView.getContext()).load(obj).apply(initOptions())
                    .skipMemoryCache(isSkipMemoryCache()).error(getErrorImage())
                    .fallback(getErrorImage()).placeholder(getPlaceholder()).into(imageView);
        }
        if (obj instanceof Bitmap) {
            Glide.with(imageView.getContext()).load(obj).apply(initOptions())
                    .skipMemoryCache(isSkipMemoryCache()).error(getErrorImage())
                    .fallback(getErrorImage()).placeholder(getPlaceholder()).into(imageView);
        }
        if (obj instanceof Drawable) {
            Glide.with(imageView.getContext()).load(obj).apply(initOptions())
                    .skipMemoryCache(isSkipMemoryCache()).error(getErrorImage())
                    .fallback(getErrorImage()).placeholder(getPlaceholder()).into(imageView);
        }
    }

    /**
     * @param context   当前Activity的上下文对象
     * @param obj
     * @param imageView
     * @describe 与没有context的方法相比 不易导致 内存泄漏问题，原因 activity销毁的时候 imageView 的上下文对象自然不存在
     */
    public static void loadImage(Context context, Object obj, ImageView imageView) {
        if (obj instanceof String) {
            Glide.with(context).load(obj).apply(initOptions())
                    .skipMemoryCache(isSkipMemoryCache()).error(getErrorImage())
                    .fallback(getErrorImage()).placeholder(getPlaceholder()).into(imageView);
        }
        if (obj instanceof Bitmap) {
            Glide.with(context).load(obj).apply(initOptions())
                    .skipMemoryCache(isSkipMemoryCache()).error(getErrorImage())
                    .fallback(getErrorImage()).placeholder(getPlaceholder()).into(imageView);
        }
        if (obj instanceof Drawable) {
            Glide.with(context).load(obj).apply(initOptions())
                    .skipMemoryCache(isSkipMemoryCache()).error(getErrorImage())
                    .fallback(getErrorImage()).placeholder(getPlaceholder()).into(imageView);
        }
    }

    /**
     * @param context   当前Activity的上下文对象
     * @param imageView
     * @describe 加载圆形图
     */
    public static void loadCircleImage(Context context, String url, ImageView imageView) {
        Glide.with(context).load(url).apply(initOptions())
                .skipMemoryCache(isSkipMemoryCache()).error(getErrorImage()).placeholder(getPlaceholder())
                .fallback(getErrorImage()).circleCrop().into(imageView);
    }

    /**
     * @param context
     * @param url
     * @param imageView
     * @describe 加载动图gif
     */
    public static void loadGifImage(Context context, String url, ImageView imageView) {
        Glide.with(context).asGif().apply(initOptions())
                .skipMemoryCache(isSkipMemoryCache()).load(url).error(getErrorImage()).placeholder(getPlaceholder())
                .fallback(getErrorImage()).circleCrop().into(imageView);
    }

    /**
     * @param url
     * @param imageView
     * @describe 加载动图gif
     */
    public static void loadGifImage(String url, ImageView imageView) {
        Glide.with(imageView.getContext()).asGif().apply(initOptions())
                .skipMemoryCache(isSkipMemoryCache()).load(url).error(getErrorImage())
                .placeholder(getPlaceholder()).fallback(getErrorImage()).circleCrop().into(imageView);
    }


    /**
     * @param sizeMultiplier 如设置0.2f缩略
     * @describe 加载缩略图
     */
    public static void loadThumbnailImage(String url, ImageView imageView, float sizeMultiplier) {
        Glide.with(imageView.getContext()).load(url)
                .skipMemoryCache(isSkipMemoryCache())
                .thumbnail(sizeMultiplier)//缩略的参数
                .apply(initOptions())
                .into(imageView);
    }


    /**
     * @return 设置全局的错误图片 防止更改时多地方修改
     * @describe 当图片加载失败的时候显示
     */
    private static @DrawableRes
    int getErrorImage() {
        return R.mipmap.app_logo;
    }

    /**
     * @return 设置全局的占位图 防止更改时多地方修改
     * @describe 当图片没有加载出来的时候显示
     */
    private static @DrawableRes
    int getPlaceholder() {
        return R.mipmap.app_logo;
    }

    /**
     * @return 返回当前石头 跳过内存缓存
     * true 不缓存 false 缓存
     */
    private static boolean isSkipMemoryCache() {
        return false;
    }


    /**
     * @return 这里默认设置全部为禁止缓存
     * @describe 设置缓存
     * Glide有两种缓存机制，一个是内存缓存，一个是硬盘缓存。
     * 内存缓存的主要作用是防止应用重复将图片数据读取到内存当中，
     * 而硬盘缓存的主要作用是防止应用重复从网络或其他地方重复下载和读取数据
     * @diskCacheStrategy参数 DiskCacheStrategy.NONE： 表示不缓存任何内容
     * DiskCacheStrategy.DATA： 表示只缓存原始图片
     * DiskCacheStrategy.RESOURCE： 表示只缓存转换过后的图片
     * DiskCacheStrategy.ALL ： 表示既缓存原始图片，也缓存转换过后的图片
     * DiskCacheStrategy.AUTOMATIC： 表示让Glide根据图片资源智能地选择使用哪一种缓存策略（默认选项）
     */
    private static RequestOptions initOptions(BitmapTransformation transformation) {
        return new RequestOptions()
                .transform(transformation)
                .skipMemoryCache(isSkipMemoryCache())//是否允许内存缓存
                .onlyRetrieveFromCache(true)//是否只从缓存加载图片
                .diskCacheStrategy(DiskCacheStrategy.NONE);//禁止磁盘缓存
    }

    private static RequestOptions initOptions() {
        return new RequestOptions()
                .skipMemoryCache(isSkipMemoryCache())//是否允许内存缓存
                .onlyRetrieveFromCache(true)//是否只从缓存加载图片
                .diskCacheStrategy(DiskCacheStrategy.NONE);//禁止磁盘缓存
    }

    /**
     * @describe 清楚内容缓存
     */
    public static void clearMemory(Context context) {
        Glide.get(context).clearMemory();
    }

    /**
     * @describe 清除磁盘缓存
     */
    public static void clearDiskCache(Context context) {
        Glide.get(context).clearDiskCache();
    }

    /**
     * @param transformation
     * @return
     * @describe 设置加载的效果
     */
    private static RequestOptions bitmapTransform(BitmapTransformation transformation) {
        return new RequestOptions();
    }

    public static Bitmap createVideoThumbnail(String filePath, int kind) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            if (filePath.startsWith("http://")
                    || filePath.startsWith("https://")
                    || filePath.startsWith("widevine://")) {
                retriever.setDataSource(filePath, new Hashtable<String, String>());
            } else {
                retriever.setDataSource(filePath);
            }
            bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC); //retriever.getFrameAtTime(-1);
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
            ex.printStackTrace();
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
            ex.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
                ex.printStackTrace();
            }
        }

        if (bitmap == null) {
            return null;
        }

        if (kind == MediaStore.Images.Thumbnails.MINI_KIND) {//压缩图片 开始处
            // Scale down the bitmap if it's too large.
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int max = Math.max(width, height);
            if (max > 512) {
                float scale = 512f / max;
                int w = Math.round(scale * width);
                int h = Math.round(scale * height);
                bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
            }//压缩图片 结束处
        } else if (kind == MediaStore.Images.Thumbnails.MICRO_KIND) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap,
                    96,
                    96,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }
}
