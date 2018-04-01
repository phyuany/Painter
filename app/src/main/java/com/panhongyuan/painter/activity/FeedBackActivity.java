package com.panhongyuan.painter.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;


import com.panhongyuan.painter.R;



/**
 * Created by pan on 17-5-15.
 */

public class FeedBackActivity extends AppCompatActivity {

    private EditText content;
    //private DatabaseReference mDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_feedback);

        initUI();

        initData();
    }

    private void initData() {
        //mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    private void initUI() {

        //获取并初始化ToolBar
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolBar);
        //设置ActionBar的返回菜单支持
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowTitleEnabled(false);
        }

        //获取控件
        //内容
        content = (EditText) findViewById(R.id.et_feedback_content);

      /*  //确定
        findViewById(R.id.bt_feedback_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取时间
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                String time = df.format(new Date());

                //获取反馈内容
                String content = FeedBackActivity.this.content.getText().toString();

                Content feed_content = new Content(content);

                //提交数据
                mDatabase.child("feedback").child(time).setValue(feed_content);

                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Toast.makeText(getApplicationContext(), "提交成功，感谢您的反馈", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), "提交失败，请检查您的网络连接", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });*/
    }

    /**
     *
     */
    public class Content {

        public String content;

        public Content(String content) {
            this.content = content;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
