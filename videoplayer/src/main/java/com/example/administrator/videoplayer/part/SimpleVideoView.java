package com.example.administrator.videoplayer.part;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.administrator.videoplayer.R;
import com.example.administrator.videoplayer.full.VideoViewActivity;

import java.io.IOException;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;

/**
 * Created by Administrator on 2016/9/7.
 * /**
 * 一个自定义的VideoView,使用MediaPlayer+SurfaceView来实现视频的播放
 * <p/>
 * MediaPlayer来做视频播放的控制，SurfaceView来显示视频
 * <p/>
 * 视图方面(initView方法中进行初始化)将简单实现:放一个播放/暂停按钮，一个进度条,一个全屏按钮,和一个SurfaceView
 * <p/>
 * 本API实现结构：
 * <ul>
 * <li/>提供setVideoPath方法(要在onResume方法调用前来调用): 设置播放谁
 * <li/>提供onResume方法(在activity的onResume来调用): 初始化MediaPlayer,准备MediaPlayer
 * <li/>提供onPause方法 (在activity的onPause来调用): 释放MediaPlayer,暂停mediaPlayer
 * </ul>
 * <p/>
 * 作者：yuanchao on 2016/9/7 0007 10:17
 * 邮箱：yuanchao@feicuiedu.com
 */
public class SimpleVideoView extends FrameLayout {
    private static final int PROGRESS_MAX = 1000;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    private String videoPath; // 视频播放URL
    private MediaPlayer mMediaPlayer;
    private ImageView ivPreview;
    private ImageButton btnToggle;
    private ProgressBar progressBar;

    private boolean isPrepared; // 是否已准备好
    private boolean isPlaying; // 是否正在播放

    public SimpleVideoView(Context context) {
        this(context, null);
    }

    public SimpleVideoView(Context context, AttributeSet attrs) {
        this(context, attrs , 0);
    }

    public SimpleVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (isPlaying){
                // 如果正在播放,每200毫秒更新一次播放进度
                int progress = (int) (mMediaPlayer.getCurrentPosition() * PROGRESS_MAX / mMediaPlayer.getDuration());
                progressBar.setProgress(progress);
                handler.sendEmptyMessageDelayed(0,200);
            }
        }
    };

    private void init() {
        // Vitamio的初始化
        Vitamio.isInitialized(getContext());
        //inflate
        LayoutInflater.from(getContext()).inflate(R.layout.view_simple_video_player,this,true);
        // 初始化SurfaceView
        initSurfaceView();
        // 初始化视频播放控制视图
        initControllerViews();
    }

    private void initSurfaceView() {
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        // 注意：vitamio使用SurfaceView播放时要设置fixelFormat
        surfaceHolder.setFormat(PixelFormat.RGBA_8888);
    }
    private void initControllerViews() {
        // 预览图
        ivPreview = (ImageView) findViewById(R.id.ivPreview);
        // 播放、暂停
        btnToggle = (ImageButton) findViewById(R.id.btnToggle);
        btnToggle.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                if (mMediaPlayer.isPlaying()) {
                    pauseMediaPlayer();
                } else if (isPrepared) {
                    //如果没有准备好就开始,会报错,所以加此判断,在准备好的情况下点击开始才进行播放
                    startMediaPlayer();
                } else {
                    Toast.makeText(getContext(), "Can't play now!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // 设置进度条
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(PROGRESS_MAX);
        // 全屏播放按钮
        ImageButton btnFullScreen = (ImageButton) findViewById(R.id.btnFullScreen);
        btnFullScreen.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                VideoViewActivity.open(getContext(), videoPath);
            }
        });
    }
    public void onResume(){
        initMediaPlayer(); // 初始化MediaPlayer，设置一系列监听器
        prepareMediaPlayer(); // 准备MediaPlayer，同时更新UI状态
    }

    // 初始化MediaPlayer，设置一系列监听器
    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer(getContext());
        //在surfaceview上展示
        mMediaPlayer.setDisplay(surfaceHolder);
        //监听(3个)
        //准备状态的监听 - 主要为了在准备好了后进行缓冲空间的设置
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //准备好了之后开始播放mediaplay,同时isPrepared设置为true
                isPrepared = true;
                startMediaPlayer();
            }
        });
        //信息的监听，类似状态的一个监听 - 主要是为了知道什么时候开始及结束缓冲
        mMediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                // vitamio5.0，要进行audio处理,才能对在线视频进行播放
                if (what == MediaPlayer.MEDIA_INFO_FILE_OPEN_OK) {
                    mMediaPlayer.audioInitedOk(mMediaPlayer.audioTrackInit());
                    return true;
                }
                return false;
            }
        });
        mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                //按比例展示surfaceView
                int layoutWidth = surfaceView.getWidth();
                int layoutHeigth = layoutWidth * height / width;
                // 更新surfaceView的size
                ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
                layoutParams.width = layoutWidth;
                layoutParams.height = layoutHeigth;
                surfaceView.setLayoutParams(layoutParams);
            }
        });

    }


    private void prepareMediaPlayer() {
        try {
            //重置
            mMediaPlayer.reset();
            //activity的Resume之前必须先传递videoPath
            mMediaPlayer.setDataSource(videoPath);
            //循环播放
            mMediaPlayer.setLooping(true);
            //异步,触发准备状态的监听setOnPreparedListener,执行startMediaPlayer()方法开始播放
            mMediaPlayer.prepareAsync();
            //加载预览图
            ivPreview.setVisibility(View.VISIBLE);
        } catch (IOException e) {

            Log.d("SimpleVideoView", " prepare MediaPlayer " + e.getMessage());
        }
    }
    //开始播放,同时更新UI状态
    private void startMediaPlayer() {
        //隐藏预览图
        ivPreview.setVisibility(View.INVISIBLE);
        //按键变成暂停图片
        btnToggle.setImageResource(R.drawable.ic_pause);
        mMediaPlayer.start();
        isPlaying = true;
        //开始播放时传递handler
        handler.sendEmptyMessage(0);
    }

    public void onPause() {
        pauseMediaPlayer();   // 暂停播放，同时更新UI状态
        releaseMediaPlayer(); // 释放MediaPlayer，同时更新UI状态
    }
    // 释放MediaPlayer，同时更新UI状态
    private void releaseMediaPlayer() {
        mMediaPlayer.release();
        mMediaPlayer = null;
        //是否准备好设置为false
        isPrepared = false;
        //播放进度重置为0
        progressBar.setProgress(0);
    }
    // 暂停播放，同时更新UI状态
    private void pauseMediaPlayer() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }

        isPlaying = false;
        btnToggle.setImageResource(R.drawable.ic_play_arrow);
        handler.removeMessages(0);
    }
    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

}
