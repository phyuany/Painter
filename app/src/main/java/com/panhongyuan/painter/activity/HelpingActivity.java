package com.panhongyuan.painter.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.panhongyuan.painter.R;


/**
 * Created by pan on 17-5-6.
 */

public class HelpingActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_helping);

        //初始化控件
        initUI();
    }

    private void initUI() {
        /*
        * 1.设置ToolBar支持
        * */
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolBar);
        /*
        * 2.获取折叠控件中的子控件
        * */
        CollapsingToolbarLayout collapsing_toolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        ImageView iv_background_view = (ImageView) findViewById(R.id.iv_background_view);
        /*
        * 3.设置ToolBar的左侧点击按钮支持
        * */
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        /*
        * 4.设置可折叠控件的标题
        * */
        collapsing_toolbar.setTitle("帮助");

        /*
        * 5.设置加载的资源内容
        * */
        //Glide.with(this).load(R.drawable.hone_anshun).into(iv_background_view);//展示可折叠图片
        Glide.with(this).load(R.drawable.wallpaper).into(iv_background_view);//展示可折叠图片
        //tv_share_content_des.setText("分享给好朋友");//设置文字描述
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}
