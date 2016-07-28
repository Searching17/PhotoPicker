package com.seakun.photopicker.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.seakun.photopicker.R;
import com.seakun.photopicker.utils.GlideImageLoader;
import com.seakun.photopicker.widget.ZoomImageView;

import java.util.ArrayList;

/**
 * Created by Seakun on 2016/3/31.
 */
public class PhotoPagerAdapter extends PagerAdapter {
    private ArrayList<String> mSelectedImage;
    private Context context;
    private AdapterView.OnItemClickListener listener;

    public PhotoPagerAdapter(Context context, ArrayList<String> mSelectedImage) {
        this.context = context;
        this.mSelectedImage = mSelectedImage;
    }

    @Override
    public int getCount() {
        return mSelectedImage.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View root = LayoutInflater.from(context).inflate(R.layout.item_preview, null);
        final ZoomImageView imageView = (ZoomImageView) root.findViewById(R.id.preview_iv);
        GlideImageLoader.load(context, mSelectedImage.get(position), imageView, true);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener!=null){
                    listener.onItemClick(null, imageView, position, R.id.preview_iv);
                }
            }
        });
        container.addView(root);
        return root;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void setOnItemClickedListener(AdapterView.OnItemClickListener listener){
        this.listener = listener;
    }

}