package com.seakun.photopicker.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.seakun.photopicker.R;
import com.seakun.photopicker.adapter.PhotoPagerAdapter;
import com.seakun.photopicker.adapter.PhotoPickerAdapter;

public class PhotoPreviewActivity extends AppCompatActivity {
    private ViewPager pickerPager;
    private PhotoPagerAdapter adapter;
    private ActionBar supportActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_preview);
        initView();
        initData();
        initEvent();
    }

    private void initView() {
        pickerPager = (ViewPager) findViewById(R.id.preview_pager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.pickerToolbar);
        setSupportActionBar(toolbar);
        supportActionBar = getSupportActionBar();
        supportActionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void initData(){
        supportActionBar.setTitle(getResources().getString(R.string.toolbar_preview, 1, PhotoPickerAdapter.mSelectedImage.size()));
        adapter = new PhotoPagerAdapter(this, PhotoPickerAdapter.mSelectedImage);
        pickerPager.setAdapter(adapter);
    }

    private void initEvent(){
        pickerPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                supportActionBar.setTitle(getResources().getString(R.string.toolbar_preview, position + 1, PhotoPickerAdapter.mSelectedImage.size()));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()== R.id.action_discard){
            if(PhotoPickerAdapter.mSelectedImage.size()>1){
                PhotoPickerAdapter.mSelectedImage.remove(pickerPager.getCurrentItem());
                adapter.notifyDataSetChanged();
                supportActionBar.setTitle(getResources().getString(R.string.toolbar_preview, pickerPager.getCurrentItem()+1, PhotoPickerAdapter.mSelectedImage.size()));
            }else{
                PhotoPickerAdapter.mSelectedImage.clear();
                finish();
            }
        }else {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            super.dispatchTouchEvent(ev);
        }catch (Exception e){}
        return true;
    }
}
