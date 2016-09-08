package com.example.administrator.videoplayer.full;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.videoplayer.R;

import java.util.Locale;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.VideoView;

/**
 * 直接用系统自带的VideoView(封装了mediaplayer和surfaceview)实现
 */
public class VideoViewActivity extends AppCompatActivity {
    private static final String VIDEO_PATH = "video_path";

    // 相关视图start
    private VideoView videoView;
    private ImageView ivLoading; // 缓冲信息(图像)
    private TextView tvBufferInfo; // 缓冲信息(78kb/s, 35%)
    // 相关视图end
    private MediaPlayer mediaPlayer;
    private int bufferPercent; // 缓冲百分比
    private int downloadSpeed; // 下载速度

    /**
     *公开open方法传入context和播放地址,然后跳转至本activity
     *也可在在跳转处携带地址跳转,但更麻烦:一方面有可能地址忘记传递,另一方面,VIDEO_PATH这个键要在两个类中写
     */
    //启动当前Activity
    public static void open(Context context, String videoPath) {
        Intent intent = new Intent(context, VideoViewActivity.class);
        intent.putExtra(VIDEO_PATH, videoPath);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 取消状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 设置窗口的背景色
        getWindow().setBackgroundDrawableResource(android.R.color.black);
        // 设置当前内容视图
        setContentView(R.layout.activity_video_view);
        initBufferView();
        initVideoView();
    }

    @Override protected void onResume() {
        super.onResume();
        videoView.setVideoPath(getIntent().getStringExtra(VIDEO_PATH));
    }

    @Override protected void onPause() {
        super.onPause();
        videoView.stopPlayback();
    }
    //初始化缓冲视图
    private void initBufferView() {
        tvBufferInfo = (TextView)findViewById(R.id.tvBufferInfo);
        ivLoading = (ImageView) findViewById(R.id.ivLoading);
        //先默认隐藏
        tvBufferInfo.setVisibility(View.INVISIBLE);
        ivLoading.setVisibility(View.INVISIBLE);
    }
    // 初始化VideoView,设置各种监听
    private void initVideoView() {
        //一定记住Vitamio先初始化!!!!!!
        Vitamio.isInitialized(this);
        videoView = (VideoView) findViewById(R.id.videoView);
        // videoView.setMediaController();部分播放时不是直接用VideoView,控制器全部是自己定义的视图initControllerViews();,此时用VideoView可以直接设置
        videoView.setMediaController(new CustomMediaController(this));
        //设置屏幕常亮
        videoView.setKeepScreenOn(true);
        //设置获取焦点
        videoView.requestFocus();

        // 资源准备的监听
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer = mp;
                // 在prepared后，设置缓冲区大小(缓冲区填充完后，才会播放),默认值是1M
                mediaPlayer.setBufferSize(512 * 1024);
            }
        });
        // 缓冲更新的监听(得到缓冲percent!!!!)
        videoView.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                bufferPercent = percent;
                // 更新缓冲UI
                updateBufferView();
            }
        });

        // 播放信息监听
        videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                switch (what) {
                    // 开始缓冲
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                            // 显示缓冲视图
                            showBufferView();
                        if (videoView.isPlaying()) {
                            videoView.pause();
                        }
                        //处理完此状态就返回true
                        return true;
                    // 结束缓冲
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        // 隐藏缓冲视图
                        hideBufferView();
                        videoView.start();
                        return true;
                    // 缓冲时，下载速率
                    case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
                        //得到缓冲速率!!!!缓冲百分比在缓冲监听中获得!!!
                        downloadSpeed = extra;
                        //再次更新缓冲ui
                        updateBufferView();
                        return true;
                }
                return false;
            }
        });
    }
    // 显示缓冲视图
    private void showBufferView() {
        tvBufferInfo.setVisibility(View.VISIBLE);
        ivLoading.setVisibility(View.VISIBLE);
        downloadSpeed = 0;
        bufferPercent = 0;
    }

    // 隐藏缓冲视图
    private void hideBufferView() {
        tvBufferInfo.setVisibility(View.INVISIBLE);
        ivLoading.setVisibility(View.INVISIBLE);
    }

    // 更新缓冲UI
    private void updateBufferView() {
        String info = String.format(Locale.CHINA,"%d%%dkb/s",bufferPercent,downloadSpeed);
        tvBufferInfo.setText(info);
    }
}
