package com.seakun.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.seakun.photopicker.activity.GalleryActivity;
import com.seakun.photopicker.activity.PhotoPickerActivity;
import com.seakun.photopicker.utils.GlideImageLoader;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    GridView lv;
    MyAdapter adapter;
    ArrayList<String> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = (GridView) findViewById(R.id.lv);
        adapter = new MyAdapter(list);
        lv.setAdapter(adapter);
    }

    public void get(View view){
        Intent intent = new Intent(this, PhotoPickerActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data!=null){
            ArrayList<String> result = data.getStringArrayListExtra(PhotoPickerActivity.RESULT);
            if(result!=null){
                list.addAll(result);
                adapter.notifyDataSetChanged();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    class MyAdapter extends BaseAdapter{
        List<String> paths;

        public MyAdapter(List<String> paths) {
            this.paths = paths;
        }

        @Override
        public int getCount() {
            return paths.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ImageView imageView = new ImageView(MainActivity.this);
            GlideImageLoader.load(MainActivity.this, list.get(position), imageView, 200, 200);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onPicClicked(position);
                }
            });
            return imageView;
        }
    }

    private void onPicClicked(int position){
        Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
        intent.putStringArrayListExtra(GalleryActivity.PHOTO_LIST, list);
        intent.putExtra(GalleryActivity.CURR_PHOTO, position);
        startActivityForResult(intent, 0);
    }
}
