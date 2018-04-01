package com.panhongyuan.painter.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.panhongyuan.painter.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.jar.Manifest;

/**
 * Created by pan on 16-11-16.
 */
public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSION_REQUEST_CODE = 101;
    private static String[] PERMISSION_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    private static final String TAG = "HomeActivity";
    private Button bt_color;//颜料按钮
    private Button bt_painter;//画笔按钮
    private Button bt_cancel;//取消按钮
    private Button bt_confirm;//确定按钮
    private ImageView im_image;//最终保存的图片
    private Bitmap copyBitmap;//源画布图的复制品
    private Paint paint;//画笔
    private Canvas canvas;//画布
    private int checkedRadioButtonId = 0;//画笔的大小
    private RadioGroup rg_size_painter;//画笔按钮选择组
    private RadioButton rb_smallSize_painter;//小画笔
    private RadioButton rb_middleSize_painter;//中画笔
    private RadioButton rb_bigSize_painter;//大画笔
    private RadioButton rb_maximalSize_painter;//最大画笔
    private RadioButton rb_minimalSize_painter;//最小画笔
    int COLOR_RBG_R = 0, COLOR_RBG_B = 0, COLOR_RBG_G = 0;//RGB颜色变量
    String STRING_COLOR = null;//定义代表不同颜色的字符变量
    //不同画笔大小是否选中的状态
    boolean PAINTER_SMALL_IS_CHECKED = false, PAINTER_MIDDLE_IS_CHECKED = true, PAINTER_BIG_IS_CHECKED = false,
            PAINTER_MAX_IS_CHECKED = false, PAINTER_MIN_IS_CHECKED = false;
    private View view_color_show;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //初始化控件
        initUI();
        //根据不同的屏幕，获取不同的画布.再开始作画
        startPaint();
        //初始化权限
        initPermission();
    }

    private void initPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSION_STORAGE, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                Log.i("HomeActivity", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]);
            }
        }
    }

    /**
     * 图像复位
     */
    private void cancelImage() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("提示");
        builder.setMessage("选择继续将擦除当前画画，是否继续？");
        builder.setPositiveButton("继续", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
                //开启跳转动画
                overridePendingTransition(R.anim.pre_in_anim, R.anim.pre_out_anim);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    /**
     * 初始化控件
     */
    private void initUI() {
        //设置toolBar的支持
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolBar);

        //获取按钮
        bt_color = (Button) findViewById(R.id.bt_color);
        bt_painter = (Button) findViewById(R.id.bt_painter);
        bt_cancel = (Button) findViewById(R.id.bt_cancel);
        bt_confirm = (Button) findViewById(R.id.bt_confirm);
        //获取imageView
        im_image = (ImageView) findViewById(R.id.im_image);
        //获取Switch橡皮擦切换功能
        Switch switch_eraser = (Switch) findViewById(R.id.switch_eraser);
        //设置Switch橡皮擦的监听事件
        switch_eraser.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    //当Switch开启时，把画笔设置为橡皮擦
                    setEraser();
                } else {
                    //当Switch关闭时，取消橡皮擦，还原原来的画笔
                    cancelEraser();
                }
            }
        });

        //设置点击事件
        bt_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //设置画笔颜色
                setPaintColor();
            }
        });
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //复位按钮
                cancelImage();
            }
        });
        bt_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "按钮被点击");
                //保存画画
                saveImage();
            }
        });
        bt_painter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //设置画笔大小
                setPaint();
            }
        });
    }

    /**
     * 取消橡皮擦功能的实现
     */
    private void cancelEraser() {
        paint.setARGB(255, COLOR_RBG_R, COLOR_RBG_G, COLOR_RBG_B);
        paint.setStrokeWidth(10);
        rb_middleSize_painter.setChecked(true);
        Toast.makeText(getApplication(), "现在画笔大小为正常笔", Toast.LENGTH_SHORT).show();
    }

    /**
     * 设置橡皮擦的实现
     */
    private void setEraser() {
        paint.setARGB(255, 255, 255, 255);
        paint.setStrokeWidth(3 * paint.getStrokeWidth());
    }

    /**
     * 保存画画的实现
     */
    private void saveImage() {

        System.out.println("已经点击保存按钮");
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("是否要保存此图片？");
        builder.setMessage("保存后的图片在存储卡主目录，你也可以从相册中找到它");
        builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                try {
                    //如果“表情画板”路径不存在就创建路径
                    File imagePath = new File(Environment.getExternalStorageDirectory() + File.separator + "表情画板" + File.separator);
                    if (!imagePath.exists()) {
                        imagePath.mkdir();
                        //创建感谢文档
                        String fileName = imagePath + File.separator + "感谢.txt";
                        File createFile = new File(fileName);
                        boolean newFile = createFile.createNewFile();
                        if (newFile) {
                            //创建FileWriter对象，用来写入字符流
                            FileWriter fileWriter = new FileWriter(fileName);
                            //将缓冲对文件的输出
                            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                            //写入文件
                            String thanks = "\n\t\t\t\t\t感谢您的下载！\n\t\t\t\t《表情画板》：简单、免费、实用。\n" +
                                    "    \n\t\t\t\t：您的支持是我努力的动力\n" +
                                    "    \n\t\t\t\t邮箱：panghongyuandev@gmail.com\n" +
                                    "    \n\t\t\t\t再次感谢你的支持！祝你生活愉快！";
                            bufferedWriter.write(thanks);
                            bufferedWriter.newLine();
                            //刷新流的缓冲
                            bufferedWriter.flush();
                            //关闭流
                            fileWriter.close();
                            bufferedWriter.close();
                            //将图片保存到路径中
                            File file = new File(imagePath, System.currentTimeMillis() + "image.jpg");
                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            copyBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                            fileOutputStream.close();
                            Toast.makeText(getApplicationContext(), "已经保存画画", Toast.LENGTH_SHORT).show();
                            //通知图库更新图片
                            MediaScannerConnection.scanFile(getApplication(), new String[]{file.getAbsolutePath()}, null, null);
                        }
                    } else {
                        //将图片保存到路径中
                        File file = new File(imagePath, System.currentTimeMillis() + "image.jpg");
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        copyBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                        fileOutputStream.close();
                        Toast.makeText(getApplicationContext(), "已经保存画画", Toast.LENGTH_SHORT).show();
                        //通知图库更新图片
                        MediaScannerConnection.scanFile(getApplication(), new String[]{file.getAbsolutePath()}, null, null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.print("产生的异常为：" + e);
                    Toast.makeText(getApplicationContext(), "保存画画出错，请检查SD卡是否正常插入", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), "已经取消保存画画", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();

    }

    /**
     * 根据不同的屏幕尺寸，获取不同的画布，再开始作画
     */
    @SuppressLint("ClickableViewAccessibility")
    public void startPaint() {
        //或取手机的尺寸
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        int height = windowManager.getDefaultDisplay().getHeight();
        int width = windowManager.getDefaultDisplay().getWidth();
        //判断屏幕尺寸然后分配不同尺寸的画布
        int canvasHeight = height - height / 3;
        int canvasWidth = width - 30;

        //创建Bitmap源图片
        copyBitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
        //设置原图背景
        copyBitmap.eraseColor(Color.parseColor("#ffffff"));
        //得到画布
        canvas = new Canvas(copyBitmap);
        //得到画笔
        paint = new Paint();
        paint.setStrokeWidth(10);
        //画图
        canvas.drawBitmap(copyBitmap, new Matrix(), paint);
        //设置图像给最终图片
        im_image.setImageBitmap(copyBitmap);
        //跟踪手指画画
        im_image.setOnTouchListener(new View.OnTouchListener() {
            int startX;
            int startY;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        startX = (int) motionEvent.getX();
                        startY = (int) motionEvent.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int stopX = (int) motionEvent.getX();
                        int stopY = (int) motionEvent.getY();
                        canvas.drawLine(startX, startY, stopX, stopY, paint);
                        startX = stopX;
                        startY = stopY;
                        im_image.setImageBitmap(copyBitmap);
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return true;
            }
        });
    }

    /**
     * 按钮点击事件的实现
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.bt_color01:
                view_color_show.setBackgroundColor(Color.parseColor("#f44336"));
                STRING_COLOR = "#f44336";
                break;
            case R.id.bt_color02:
                view_color_show.setBackgroundColor(Color.parseColor("#e91e63"));
                STRING_COLOR = "#e91e63";
                break;
            case R.id.bt_color03:
                view_color_show.setBackgroundColor(Color.parseColor("#9c27b0"));
                STRING_COLOR = "#9c27b0";
                break;
            case R.id.bt_color04:
                view_color_show.setBackgroundColor(Color.parseColor("#673ab7"));
                STRING_COLOR = "#673ab7";
                break;
            case R.id.bt_color05:
                view_color_show.setBackgroundColor(Color.parseColor("#3f51b5"));
                STRING_COLOR = "#3f51b5";
                break;
            case R.id.bt_color06:
                view_color_show.setBackgroundColor(Color.parseColor("#2196f3"));
                STRING_COLOR = "#2196f3";
                break;
            case R.id.bt_color07:
                view_color_show.setBackgroundColor(Color.parseColor("#03a9f4"));
                STRING_COLOR = "#03a9f4";
                break;
            case R.id.bt_color08:
                view_color_show.setBackgroundColor(Color.parseColor("#00bcd4"));
                STRING_COLOR = "#00bcd4";
                break;
            case R.id.bt_color09:
                view_color_show.setBackgroundColor(Color.parseColor("#009688"));
                STRING_COLOR = "#009688";
                break;
            case R.id.bt_color10:
                view_color_show.setBackgroundColor(Color.parseColor("#4caf50"));
                STRING_COLOR = "#4caf50";
                break;
            case R.id.bt_color11:
                view_color_show.setBackgroundColor(Color.parseColor("#8bc34a"));
                STRING_COLOR = "#8bc34a";
                break;
            case R.id.bt_color12:
                view_color_show.setBackgroundColor(Color.parseColor("#cddc39"));
                STRING_COLOR = "#cddc39";
                break;
            case R.id.bt_color13:
                view_color_show.setBackgroundColor(Color.parseColor("#ffeb3b"));
                STRING_COLOR = "#ffeb3b";
                break;
            case R.id.bt_color14:
                view_color_show.setBackgroundColor(Color.parseColor("#ffc107"));
                STRING_COLOR = "#ffc107";
                break;
            case R.id.bt_color15:
                view_color_show.setBackgroundColor(Color.parseColor("#ff9800"));
                STRING_COLOR = "#ff9800";
                break;
            case R.id.bt_color16:
                view_color_show.setBackgroundColor(Color.parseColor("#ff5722"));
                STRING_COLOR = "#ff5722";
                break;
            case R.id.bt_color17:
                view_color_show.setBackgroundColor(Color.parseColor("#795548"));
                STRING_COLOR = "#795548";
                break;
            case R.id.bt_color18:
                view_color_show.setBackgroundColor(Color.parseColor("#9e9e9e"));
                STRING_COLOR = "#9e9e9e";
                break;
            case R.id.bt_color19:
                view_color_show.setBackgroundColor(Color.parseColor("#ff8a80"));
                STRING_COLOR = "#ff8a80";
                break;
            case R.id.bt_color20:
                view_color_show.setBackgroundColor(Color.parseColor("#ff80ab"));
                STRING_COLOR = "#ff80ab";
                break;
            case R.id.bt_color21:
                view_color_show.setBackgroundColor(Color.parseColor("#ea80fc"));
                STRING_COLOR = "#ea80fc";
                break;
            case R.id.bt_color22:
                view_color_show.setBackgroundColor(Color.parseColor("#b388ff"));
                STRING_COLOR = "#b388ff";
                break;
            case R.id.bt_color23:
                view_color_show.setBackgroundColor(Color.parseColor("#8c9eff"));
                STRING_COLOR = "#8c9eff";
                break;
            case R.id.bt_color24:
                view_color_show.setBackgroundColor(Color.parseColor("#82b1ff"));
                STRING_COLOR = "#82b1ff";
                break;
            case R.id.bt_color25:
                view_color_show.setBackgroundColor(Color.parseColor("#80d8ff"));
                STRING_COLOR = "#80d8ff";
                break;
            case R.id.bt_color26:
                view_color_show.setBackgroundColor(Color.parseColor("#84ffff"));
                STRING_COLOR = "#84ffff";
                break;
            case R.id.bt_color27:
                view_color_show.setBackgroundColor(Color.parseColor("#a7ffeb"));
                STRING_COLOR = "#a7ffeb";
                break;
            case R.id.bt_color28:
                view_color_show.setBackgroundColor(Color.parseColor("#b9f6ca"));
                STRING_COLOR = "#b9f6ca";
                break;
            case R.id.bt_color29:
                view_color_show.setBackgroundColor(Color.parseColor("#ccff90"));
                STRING_COLOR = "#ccff90";
                break;
            case R.id.bt_color30:
                view_color_show.setBackgroundColor(Color.parseColor("#f4ff81"));
                STRING_COLOR = "#f4ff81";
                break;
            case R.id.bt_color31:
                view_color_show.setBackgroundColor(Color.parseColor("#ffff8d"));
                STRING_COLOR = "#ffff8d";
                break;
            case R.id.bt_color32:
                view_color_show.setBackgroundColor(Color.parseColor("#ffe57f"));
                STRING_COLOR = "#ffe57f";
                break;
            case R.id.bt_color33:
                view_color_show.setBackgroundColor(Color.parseColor("#ffd180"));
                STRING_COLOR = "#ffd180";
                break;
            case R.id.bt_color34:
                view_color_show.setBackgroundColor(Color.parseColor("#ff9e80"));
                STRING_COLOR = "#ff9e80";
                break;
            case R.id.bt_color35:
                view_color_show.setBackgroundColor(Color.parseColor("#607d8b"));
                STRING_COLOR = "#607d8b";
                break;
            case R.id.bt_color36:
                view_color_show.setBackgroundColor(Color.parseColor("#000000"));
                STRING_COLOR = "#000000";
                break;

            default:
                break;
        }
    }

    /**
     * 设置画笔颜色的实现
     */
    private void setPaintColor() {
        //创建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog alertDialog = builder.create();
        //创建界面布局
        View view = View.inflate(getApplicationContext(), R.layout.vp_painter_color, null);
        alertDialog.setView(view);
        alertDialog.show();

        //获取view中的控件
        view_color_show = (View) view.findViewById(R.id.iv_color_show);
        view_color_show.setBackgroundColor(Color.argb(255, COLOR_RBG_R, COLOR_RBG_G, COLOR_RBG_B));
        final TextView tv_color_R = (TextView) view.findViewById(R.id.tv_color_R);
        tv_color_R.setText("R:" + COLOR_RBG_R);
        SeekBar sb_color_R = (SeekBar) view.findViewById(R.id.sb_color_R);
        sb_color_R.setProgress(COLOR_RBG_R);
        final TextView tv_color_G = (TextView) view.findViewById(R.id.tv_color_G);
        tv_color_G.setText("R:" + COLOR_RBG_G);
        SeekBar sb_color_G = (SeekBar) view.findViewById(R.id.sb_color_G);
        sb_color_G.setProgress(COLOR_RBG_G);
        final TextView tv_color_B = (TextView) view.findViewById(R.id.tv_color_B);
        tv_color_B.setText("R:" + COLOR_RBG_B);
        SeekBar sb_color_B = (SeekBar) view.findViewById(R.id.sb_color_B);
        sb_color_B.setProgress(COLOR_RBG_B);

        //获取不同颜色按钮控件，并设置点击事件
        view.findViewById(R.id.bt_color01).setOnClickListener(this);
        view.findViewById(R.id.bt_color02).setOnClickListener(this);
        view.findViewById(R.id.bt_color03).setOnClickListener(this);
        view.findViewById(R.id.bt_color04).setOnClickListener(this);
        view.findViewById(R.id.bt_color05).setOnClickListener(this);
        view.findViewById(R.id.bt_color06).setOnClickListener(this);
        view.findViewById(R.id.bt_color07).setOnClickListener(this);
        view.findViewById(R.id.bt_color08).setOnClickListener(this);
        view.findViewById(R.id.bt_color09).setOnClickListener(this);
        view.findViewById(R.id.bt_color10).setOnClickListener(this);
        view.findViewById(R.id.bt_color11).setOnClickListener(this);
        view.findViewById(R.id.bt_color12).setOnClickListener(this);
        view.findViewById(R.id.bt_color13).setOnClickListener(this);
        view.findViewById(R.id.bt_color14).setOnClickListener(this);
        view.findViewById(R.id.bt_color15).setOnClickListener(this);
        view.findViewById(R.id.bt_color16).setOnClickListener(this);
        view.findViewById(R.id.bt_color17).setOnClickListener(this);
        view.findViewById(R.id.bt_color18).setOnClickListener(this);
        view.findViewById(R.id.bt_color19).setOnClickListener(this);
        view.findViewById(R.id.bt_color20).setOnClickListener(this);
        view.findViewById(R.id.bt_color21).setOnClickListener(this);
        view.findViewById(R.id.bt_color22).setOnClickListener(this);
        view.findViewById(R.id.bt_color23).setOnClickListener(this);
        view.findViewById(R.id.bt_color24).setOnClickListener(this);
        view.findViewById(R.id.bt_color25).setOnClickListener(this);
        view.findViewById(R.id.bt_color26).setOnClickListener(this);
        view.findViewById(R.id.bt_color27).setOnClickListener(this);
        view.findViewById(R.id.bt_color28).setOnClickListener(this);
        view.findViewById(R.id.bt_color29).setOnClickListener(this);
        view.findViewById(R.id.bt_color30).setOnClickListener(this);
        view.findViewById(R.id.bt_color31).setOnClickListener(this);
        view.findViewById(R.id.bt_color32).setOnClickListener(this);
        view.findViewById(R.id.bt_color33).setOnClickListener(this);
        view.findViewById(R.id.bt_color34).setOnClickListener(this);
        view.findViewById(R.id.bt_color35).setOnClickListener(this);
        view.findViewById(R.id.bt_color36).setOnClickListener(this);

        //设置红色进度条的监听事件
        sb_color_R.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                COLOR_RBG_R = i;
                if (b) {
                    tv_color_R.setText("R:" + i);
                    view_color_show.setBackgroundColor(Color.argb(255, COLOR_RBG_R, COLOR_RBG_G, COLOR_RBG_B));
                    STRING_COLOR = null;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //设置绿色进度条的监听事件
        sb_color_G.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                COLOR_RBG_G = i;
                if (b) {
                    tv_color_G.setText("G:" + i);
                    view_color_show.setBackgroundColor(Color.argb(255, COLOR_RBG_R, COLOR_RBG_G, COLOR_RBG_B));
                    STRING_COLOR = null;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //设置蓝色进度条的监听事件
        sb_color_B.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                COLOR_RBG_B = i;
                if (b) {
                    tv_color_B.setText("B:" + i);
                    view_color_show.setBackgroundColor(Color.argb(255, COLOR_RBG_R, COLOR_RBG_G, COLOR_RBG_B));
                    STRING_COLOR = null;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //设置取消事件
        view.findViewById(R.id.bt_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        //设置确定事件
        view.findViewById(R.id.bt_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (STRING_COLOR != null) {
                    paint.setColor(Color.parseColor(STRING_COLOR));
                    bt_color.setBackgroundColor(Color.parseColor(STRING_COLOR));
                    alertDialog.dismiss();
                    Toast.makeText(getApplication(), "画笔颜色已更改", Toast.LENGTH_SHORT).show();
                } else {
                    paint.setColor(Color.argb(255, COLOR_RBG_R, COLOR_RBG_G, COLOR_RBG_B));
                    bt_color.setBackgroundColor(Color.argb(255, COLOR_RBG_R, COLOR_RBG_G, COLOR_RBG_B));
                    alertDialog.dismiss();
                    Toast.makeText(getApplication(), "画笔颜色已更改", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /**
     * s设置画笔大小
     */
    private void setPaint() {
        //创建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog alertDialog = builder.create();
        //创建一个界面布局
        View view = View.inflate(getApplicationContext(), R.layout.dialog_painter_size, null);
        //获取控件
        rg_size_painter = (RadioGroup) view.findViewById(R.id.rg_size_painter);
        //获取子控件，必须通过他的直接父类或者间接父类才能获取成功
        rb_smallSize_painter = (RadioButton) rg_size_painter.findViewById(R.id.rb_smallSize_painter);
        rb_middleSize_painter = (RadioButton) rg_size_painter.findViewById(R.id.rb_middleSize_painter);
        rb_bigSize_painter = (RadioButton) rg_size_painter.findViewById(R.id.rb_bigSize_painter);
        rb_maximalSize_painter = (RadioButton) rg_size_painter.findViewById(R.id.rb_maximalSize_painter);
        rb_minimalSize_painter = (RadioButton) rg_size_painter.findViewById(R.id.rb_minimalSize_painter);
        //初始化控件完毕后，设置对应的选中状态
        rb_smallSize_painter.setChecked(PAINTER_SMALL_IS_CHECKED);
        rb_middleSize_painter.setChecked(PAINTER_MIDDLE_IS_CHECKED);
        rb_bigSize_painter.setChecked(PAINTER_BIG_IS_CHECKED);
        rb_minimalSize_painter.setChecked(PAINTER_MIN_IS_CHECKED);
        rb_maximalSize_painter.setChecked(PAINTER_MAX_IS_CHECKED);
        //设置单选按钮状态变化监听事件
        rg_size_painter.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
            }
        });

        //取消的点击事件
        view.findViewById(R.id.bt_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        //确定的点击事件,获取画笔大小再设置相应的画笔
        view.findViewById(R.id.bt_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //根据选择的单选框来设置画笔大小
                switch (checkedRadioButtonId) {
                    case R.id.rb_smallSize_painter:
                        paint.setStrokeWidth(6);
                        PAINTER_SMALL_IS_CHECKED = true;
                        PAINTER_MIDDLE_IS_CHECKED = false;
                        PAINTER_BIG_IS_CHECKED = false;
                        PAINTER_MIN_IS_CHECKED = false;
                        PAINTER_MAX_IS_CHECKED = false;
                        Toast.makeText(getApplication(), "现在画笔大小为细笔", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.rb_middleSize_painter:
                        paint.setStrokeWidth(10);
                        PAINTER_MIDDLE_IS_CHECKED = true;
                        PAINTER_SMALL_IS_CHECKED = false;
                        PAINTER_BIG_IS_CHECKED = false;
                        PAINTER_MIN_IS_CHECKED = false;
                        PAINTER_MAX_IS_CHECKED = false;
                        Toast.makeText(getApplication(), "现在画笔大小为正常笔", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.rb_bigSize_painter:
                        paint.setStrokeWidth(14);
                        PAINTER_BIG_IS_CHECKED = true;
                        PAINTER_SMALL_IS_CHECKED = false;
                        PAINTER_MIDDLE_IS_CHECKED = false;
                        PAINTER_MIN_IS_CHECKED = false;
                        PAINTER_MAX_IS_CHECKED = false;
                        Toast.makeText(getApplication(), "现在画笔大小为粗笔", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.rb_minimalSize_painter:
                        paint.setStrokeWidth(2);
                        PAINTER_BIG_IS_CHECKED = false;
                        PAINTER_SMALL_IS_CHECKED = false;
                        PAINTER_MIDDLE_IS_CHECKED = false;
                        PAINTER_MIN_IS_CHECKED = true;
                        PAINTER_MAX_IS_CHECKED = false;
                        Toast.makeText(getApplication(), "现在画笔大小为最细笔", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.rb_maximalSize_painter:
                        paint.setStrokeWidth(18);
                        PAINTER_BIG_IS_CHECKED = false;
                        PAINTER_SMALL_IS_CHECKED = false;
                        PAINTER_MIDDLE_IS_CHECKED = false;
                        PAINTER_MIN_IS_CHECKED = false;
                        PAINTER_MAX_IS_CHECKED = true;
                        Toast.makeText(getApplication(), "现在画笔大小为最粗笔", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getApplication(), "现在画笔大小为中笔", Toast.LENGTH_SHORT).show();
                        break;
                }
                alertDialog.dismiss();
            }
        });
        //让对话框显示自己的界面布局
        alertDialog.setView(view);
        alertDialog.show();
    }

    /**
     * 构造菜单
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:
                startActivity(new Intent(getApplicationContext(), AboutActivity.class));
                break;
            case R.id.menu_helping:
                startActivity(new Intent(getApplicationContext(), HelpingActivity.class));
                break;
            case R.id.menu_feedback:
                startActivity(new Intent(getApplicationContext(), FeedBackActivity.class));
                break;
        }
        return true;
    }
}
