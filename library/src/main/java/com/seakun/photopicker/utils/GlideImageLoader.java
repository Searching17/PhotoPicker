package com.seakun.photopicker.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.seakun.photopicker.R;

/**
 * 图片加载工具类
 * Update:  2015/11/25
 * Version: 1.0
 * Created by Seakun on 2015/11/25 10:02.
 */
public class GlideImageLoader {

    public static void load(Context context, String url, ImageView view, boolean isTransform){
        Glide.clear(view);
        DrawableRequestBuilder<String> builder = Glide.with(context).load(url).error(R.drawable.image_error).fitCenter().crossFade()//
                .diskCacheStrategy(DiskCacheStrategy.SOURCE);
        if (isTransform){
            builder.into(view);
        }else {
            builder.dontTransform().into(view);
        }
    }

    public static void load(Context context, int resId, ImageView view){
        Glide.clear(view);
        Glide.with(context).load(resId).error(R.drawable.image_error).fitCenter().crossFade()//
                .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(view);
    }

    public static void load(Context context, String url, ImageView view, int width, int height){
        Glide.clear(view);
        Glide.with(context).load(url).error(R.drawable.image_error).fitCenter().crossFade()//
                .override(width, height).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(view);
    }

    public static void load(Context context, int resId, ImageView view, int width, int height){
        Glide.clear(view);
        Glide.with(context).load(resId).error(R.drawable.image_error).fitCenter().crossFade()//
                .override(width, height).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(view);
    }

}
