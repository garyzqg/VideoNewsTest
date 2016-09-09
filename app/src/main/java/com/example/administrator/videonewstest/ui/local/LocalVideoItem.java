package com.example.administrator.videonewstest.ui.local;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.UiThread;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.videonewstest.R;
import com.example.administrator.videoplayer.full.VideoViewActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 自定义控件实现GridView的item
 * Created by Administrator on 2016/9/9.
 */
public class LocalVideoItem extends FrameLayout {
    public LocalVideoItem(Context context) {
        this(context,null);
    }

    public LocalVideoItem(Context context, AttributeSet attrs) {
        this(context, attrs , 0);
    }

    public LocalVideoItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    public String getFilePath() {
        return filePath;
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.item_local_video,this,true);
        ButterKnife.bind(this);
    }

    @BindView(R.id.ivPreview) ImageView ivPreView;
    @BindView(R.id.tvVideoName)TextView tvVideoName;
    private String filePath; // 文件路径


    /** 数据绑定(将cursor内容,设置到对应控件上)*/
    public void bind(Cursor cursor){
        // 取出文件路径
        filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
        // 取出视频名称
        String videoName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
        tvVideoName.setText(videoName);
        // 获取视频的预览图，是一个很费时的操作
        // ------ 到后台线程执行

        // 同时会去获取多张预览图
        // ------ 线程池处理

        // 已获取过的图像要做缓存
        // ------ LruCache

        //这样直接在这直接获取会ANR异常
        // Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MINI_KIND);
        // ivPreView.setImageBitmap(bitmap);
    }


    //自身点击事件(相当于设置GridView的item的点击事件),携带数据跳转至全屏播放页面
    @OnClick
    public void click(){
        VideoViewActivity.open(getContext(),filePath);
    }

    //设置预览图像，在adapter的后台线程调用此方法
    //更改视图必须在主线程,view的post方法，运行在主线程中。
    public void setIvPreView(final String filePath, final Bitmap bitmap) {
        //如果传过来的视频预览图路径与item不符,直接停止
        if (!filePath.equals(this.filePath)) return;
        post(new Runnable() {
            @Override
            public void run() {
                if (!filePath.equals(LocalVideoItem.this.filePath)) return;

                ivPreView.setImageBitmap(bitmap);
            }
        });

    }

    //此方法用于缓存中存在的预览图直接展示,因为若缓存中存在,不需在后台线程加载,所以不需要post方法返回主线程
    @UiThread
    public void setIvPreView(Bitmap bitmap){
        ivPreView.setImageBitmap(bitmap);
    }
}
