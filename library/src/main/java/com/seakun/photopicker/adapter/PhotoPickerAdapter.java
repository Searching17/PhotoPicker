package com.seakun.photopicker.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.seakun.photopicker.R;

import java.util.ArrayList;
import java.util.List;

public class PhotoPickerAdapter extends CommonAdapter<String> {
	/**
	 * 用户选择的图片，存储为图片的完整路径
	 */
	public static ArrayList<String> mSelectedImage = new ArrayList<>();
    private OnEventListener listener;
    public static int MAX_COUNT = 9;

    public PhotoPickerAdapter(Context context, List<String> mDatas, int itemLayoutId) {
		super(context, mDatas, itemLayoutId);
	}

	@Override
	public void convert(final com.seakun.photopicker.utils.ViewHolder helper, final String item) {
		//设置图片
		helper.setImageByUrl(R.id.id_item_image, item);
		
		final ImageView mImageView = helper.getView(R.id.id_item_image);
		final ImageView mSelect = helper.getView(R.id.id_item_select);
		
		mImageView.setColorFilter(null);
		//设置ImageView的点击事件
		mImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onPhotoClicked(item, mSelect, mImageView);
                if(listener!=null){
                    listener.onClicked(item);
                }
			}
		});
		
		 //已经选择过的图片，显示出选择过的效果
		if (mSelectedImage.contains(item)) {
			mSelect.setImageResource(R.drawable.btn_selected);
			mImageView.setColorFilter(Color.parseColor("#77000000"));
		}else {
			mSelect.setImageResource(R.drawable.btn_unselected);
		}
	}

	private void onPhotoClicked(String item, ImageView mSelect, ImageView mImageView) {
		// 已经选择过该图片
		if (mSelectedImage.contains(item)) {
            mSelectedImage.remove(item);
            mSelect.setImageResource(R.drawable.btn_unselected);
            mImageView.setColorFilter(null);
        } else {
            // 未选择该图片
			if(mSelectedImage.size()>=MAX_COUNT){
				Toast.makeText(mContext, mContext.getString(R.string.count_limit, MAX_COUNT), Toast.LENGTH_SHORT).show();
				return;
			}
            mSelectedImage.add(item);
            mSelect.setImageResource(R.drawable.btn_selected);
            mImageView.setColorFilter(Color.parseColor("#77000000"));
        }
	}

	public void setEventListener(OnEventListener listener){
        this.listener = listener;
    }

	public interface OnEventListener{
        void onClicked(String imgPic);
    }
}
