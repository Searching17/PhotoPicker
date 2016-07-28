package com.seakun.photopicker.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.seakun.photopicker.R;
import com.seakun.photopicker.adapter.PhotoPagerAdapter;

import java.util.ArrayList;

/**
 * Created by Seakun on 2016/3/31.
 */
public class GalleryActivity extends AppCompatActivity {
    private TextView title;
    private ViewPager vp;
    private PhotoPagerAdapter adapter;
    private ArrayList<String> photoList;
    private int currPhoto;
    public static final String PHOTO_LIST = "PHOTO_LIST";
    public static final String CURR_PHOTO = "CURR_PHOTO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
        initEvent();
    }

    private void initView() {
        setContentView(R.layout.activity_gallery);
        title = (TextView) findViewById(R.id.gallery_title);
        vp = (ViewPager) findViewById(R.id.gallery_pager);
    }

    private void initData() {
        photoList = getIntent().getStringArrayListExtra(PHOTO_LIST);
        currPhoto = getIntent().getIntExtra(CURR_PHOTO, 0);
        title.setText(getString(R.string.gallery_title, currPhoto+1, photoList.size()));
        adapter = new PhotoPagerAdapter(this, photoList);
        vp.setAdapter(adapter);
        vp.setCurrentItem(currPhoto);
    }

    private void initEvent() {
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                title.setText(getString(R.string.gallery_title, position+1, photoList.size()));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        adapter.setOnItemClickedListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                finish();
            }
        });

        vp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            super.dispatchTouchEvent(ev);
        }catch (Exception e){}
        return true;
    }
}
