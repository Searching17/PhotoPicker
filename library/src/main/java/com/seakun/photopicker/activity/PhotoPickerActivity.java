package com.seakun.photopicker.activity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.seakun.photopicker.R;
import com.seakun.photopicker.adapter.PhotoPickerAdapter;
import com.seakun.photopicker.bean.ImageFolder;
import com.seakun.photopicker.widget.ListImageDirPopupWindow;
import com.seakun.photopicker.widget.ListImageDirPopupWindow.OnImageDirSelected;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PhotoPickerActivity extends AppCompatActivity implements OnImageDirSelected {
    private MenuItem menuDoneItem;
    private ProgressDialog mProgressDialog;
    public static final String RESULT  = "result";

	/**
	 * all photos
	 */
	private static List<String> mImgs = new ArrayList<>();

	private List<String> cImgs = new ArrayList<>();

	private GridView mGirdView;
	private PhotoPickerAdapter mAdapter;

	/**
	 * all photo folders
	 */
	private static List<ImageFolder> mImageFolders = new ArrayList<>();

	private RelativeLayout mBottomLy;

	private TextView mChooseDir;
	private TextView mPreview;
	int totalCount = 0;

	private int mScreenHeight;

	private ListImageDirPopupWindow mListImageDirPopupWindow;

	/**
	 * bind data
	 */
	private void data2View() {
		if (mImgs.size() == 0) {
			Toast.makeText(getApplicationContext(), R.string.has_no_photo, Toast.LENGTH_SHORT).show();
			return;
		}

        cImgs = mImgs;
        createAdapter();
		mGirdView.setAdapter(mAdapter);
        modifyPreviewText();
    }

    private void modifyPreviewText() {
        if(PhotoPickerAdapter.mSelectedImage.size()>0){
            mPreview.setTextColor(getResources().getColor(android.R.color.white));
            mPreview.setText(getString(R.string.preview_count, PhotoPickerAdapter.mSelectedImage.size()));
        }else{
            mPreview.setTextColor(getResources().getColor(android.R.color.darker_gray));
            mPreview.setText(R.string.preview);
        }
    }

    private void createAdapter() {
        mAdapter = new PhotoPickerAdapter(getApplicationContext(), cImgs, R.layout.photopicker_grid_item);
        mAdapter.setEventListener(new PhotoPickerAdapter.OnEventListener() {
            @Override
            public void onClicked(String imgPic) {
                modifyDoneItem();
                modifyPreviewText();
            }
        });
    }

    /**
	 * 初始化展示文件夹的popupWindw
	 */
	private void initListDirPopupWindow() {
		mListImageDirPopupWindow = new ListImageDirPopupWindow(
				LayoutParams.MATCH_PARENT, (int) (mScreenHeight * 0.7),
				mImageFolders, LayoutInflater.from(getApplicationContext())
						.inflate(R.layout.list_dir, null));

		mListImageDirPopupWindow.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss() {
                // 设置背景颜色变暗
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1.0f;
                getWindow().setAttributes(lp);
            }
        });
		// 设置选择文件夹的回调
		mListImageDirPopupWindow.setOnImageDirSelected(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState==null){
			PhotoPickerAdapter.mSelectedImage.clear();
		}
		setContentView(R.layout.photo_picker);

		DisplayMetrics outMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		mScreenHeight = outMetrics.heightPixels;

		initView();
		getImages();
		initEvent();
	}

	/**
	 * 利用ContentProvider扫描手机中的图片，此方法在运行在子线程中 完成图片的扫描，最终获得jpg最多的那个文件夹
	 */
	private void getImages() {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			Toast.makeText(this, R.string.has_no_external_storage, Toast.LENGTH_SHORT).show();
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				ContentResolver mContentResolver = PhotoPickerActivity.this
						.getContentResolver();

				// 只查询jpeg和png的图片
				Cursor mCursor = mContentResolver.query(mImageUri, null,
						MediaStore.Images.Media.MIME_TYPE + "=? or "
								+ MediaStore.Images.Media.MIME_TYPE + "=? or "
								+ MediaStore.Images.Media.MIME_TYPE + "=?",
						new String[] { "image/jpeg", "image/png", "image/gif" },
						MediaStore.Images.Media.DATE_MODIFIED + " desc");
				ImageFolder all = new ImageFolder();
				all.setDir(ImageFolder.FOLDER_ALL);
                totalCount = mCursor.getCount();
				ImageFolder imageFolder;
				File parentFile;
				String dirPath, path;
                if(totalCount>0){
                    mCursor.moveToNext();
                    path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    if(totalCount!=mImgs.size()){
                        mImgs.clear();
                        cImgs.clear();
                        mImageFolders.clear();
                    }
                    if(TextUtils.isEmpty(path) || !(mImgs.size()>0 && path.equals(mImgs.get(0)))){
				        mImageFolders.add(all);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog = ProgressDialog.show(PhotoPickerActivity.this, null, getString(R.string.loading));
                            }
                        });
                        mCursor.moveToPrevious();
                        while (mCursor.moveToNext()) {
                            // 获取图片的路径
                            path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                            if(TextUtils.isEmpty(path)){
                                continue;
                            }
                            // 获取该图片的父路径名
                            parentFile = new File(path).getParentFile();
                            dirPath = parentFile.getAbsolutePath();
                            mImgs.add(path);
                            // 初始化imageFloder
                            imageFolder = new ImageFolder();
                            imageFolder.setDir(dirPath);
                            imageFolder.getAllPicPath().add(path);
                            if (mImageFolders.contains(imageFolder)) {
                                imageFolder = mImageFolders.get(mImageFolders.indexOf(imageFolder));
                                imageFolder.getAllPicPath().add(path);
                            } else {
                                mImageFolders.add(imageFolder);
                            }
                        }
                        mCursor.close();
                    }
                }else{
                    mImgs.clear();
                    cImgs.clear();
                    mImageFolders.clear();
                }
                all.setAllPicPath(mImgs);

				runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mProgressDialog!=null && mProgressDialog.isShowing()){
                            mProgressDialog.dismiss();
                        }
                        // 为View绑定数据
                        data2View();
                        // 初始化展示文件夹的popupWindow
                        initListDirPopupWindow();
                    }
                });

			}
		}).start();
	}

	private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.pickerToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.toolbar_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mGirdView = (GridView) findViewById(R.id.id_gridView);
		mChooseDir = (TextView) findViewById(R.id.id_choose_dir);
		mPreview = (TextView) findViewById(R.id.id_preview);

		mBottomLy = (RelativeLayout) findViewById(R.id.id_bottom_ly);
	}

    @Override
    protected void onResume() {
        super.onResume();
		modifyDoneItem();
		modifyPreviewText();
        if(mAdapter!=null){
            mAdapter.notifyDataSetChanged();
        }
    }

	private void modifyDoneItem(){
		if(menuDoneItem!=null){
			if( PhotoPickerAdapter.mSelectedImage.size()>0){
				String text = getString(R.string.select_done, PhotoPickerAdapter.mSelectedImage.size(), PhotoPickerAdapter.MAX_COUNT);
				menuDoneItem.setTitle(text);
				menuDoneItem.setVisible(true);
			}else{
				menuDoneItem.setVisible(false);
			}
		}
	}

    private void initEvent() {
		//为底部的布局设置点击事件，弹出popupWindow
		mChooseDir.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mListImageDirPopupWindow
						.setAnimationStyle(R.style.anim_popup_dir);
				mListImageDirPopupWindow.showAsDropDown(mBottomLy, 0, 0);

				// 设置背景颜色变暗
				WindowManager.LayoutParams lp = getWindow().getAttributes();
				lp.alpha = .3f;
				getWindow().setAttributes(lp);
			}
		});

		mPreview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                if(PhotoPickerAdapter.mSelectedImage.size()>0){
                    Intent intent = new Intent(PhotoPickerActivity.this, PhotoPreviewActivity.class);
                    startActivityForResult(intent, 0);
                }
			}
        });
	}

    @Override
	public void selected(ImageFolder folder) {
		if(ImageFolder.FOLDER_ALL.equals(folder.getDir())){
			cImgs = mImgs;
			mChooseDir.setText(R.string.all_photos);
		}else{
			mChooseDir.setText(folder.getName());
			cImgs = folder.getAllPicPath();
		}

        createAdapter();
		mGirdView.setAdapter(mAdapter);
        modifyPreviewText();

		mListImageDirPopupWindow.dismiss();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_picker, menu);
        menuDoneItem = menu.findItem(R.id.action_picker_done);
        modifyDoneItem();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()== R.id.action_picker_done){
            Intent data = new Intent();
            data.putStringArrayListExtra(RESULT, PhotoPickerAdapter.mSelectedImage);
            setResult(0, data);
            finish();
        }else {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
