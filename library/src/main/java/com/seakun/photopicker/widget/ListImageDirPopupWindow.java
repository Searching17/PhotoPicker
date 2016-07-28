package com.seakun.photopicker.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.seakun.photopicker.R;
import com.seakun.photopicker.adapter.CommonAdapter;
import com.seakun.photopicker.bean.ImageFolder;
import com.seakun.photopicker.utils.ViewHolder;

import java.util.List;

public class ListImageDirPopupWindow extends PopupWindow {
	protected View mContentView;
	protected Context context;
	protected List<ImageFolder> mDatas;
	private ListView mListDir;
    private CommonAdapter mAdapter;
    private ImageFolder selectedFolder;
	private OnImageDirSelected mImageDirSelected;

	public ListImageDirPopupWindow(int width, int height, List<ImageFolder> datas, View convertView) {
		super(convertView, width, height, true);
        this.mContentView = convertView;
        context = convertView.getContext();
        if (datas != null)
            this.mDatas = datas;

        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setTouchable(true);
        setOutsideTouchable(true);
        setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });
        initViews();
        initEvents();
	}

	public void initViews() {
		mListDir = (ListView) mContentView.findViewById(R.id.id_list_dir);
        mAdapter = new CommonAdapter<ImageFolder>(context, mDatas, R.layout.list_dir_item) {
            @Override
            public void convert(ViewHolder helper, ImageFolder item)
            {
                if(ImageFolder.FOLDER_ALL.equals(item.getDir())){
                    helper.setText(R.id.id_dir_item_name, mContext.getString(R.string.all_photos));
                    helper.setSelected(R.id.selected_icon, (selectedFolder ==null || selectedFolder ==item)?View.VISIBLE:View.GONE);
                }else{
                    helper.setText(R.id.id_dir_item_name, item.getName());
                    helper.setSelected(R.id.selected_icon, (selectedFolder != null && selectedFolder == item) ? View.VISIBLE : View.GONE);
                }
                helper.setImageByUrl(R.id.id_dir_item_image, item.getAllPicPath().get(0))
                    .setText(R.id.id_dir_item_count, item.getCount() + mContext.getString(R.string.piece));
                if(selectedFolder ==null){

                }else {

                }
            }
        };
        mListDir.setAdapter(mAdapter);
	}

	public interface OnImageDirSelected {
		void selected(ImageFolder folder);
	}

	public void setOnImageDirSelected(OnImageDirSelected mImageDirSelected) {
		this.mImageDirSelected = mImageDirSelected;
	}

	public void initEvents() {
		mListDir.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (mImageDirSelected != null) {
                    selectedFolder = mDatas.get(position);
					mImageDirSelected.selected(selectedFolder);
                    mAdapter.notifyDataSetChanged();
				}
			}
		});
	}
}
