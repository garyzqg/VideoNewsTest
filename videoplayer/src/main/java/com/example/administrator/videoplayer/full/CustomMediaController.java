package com.example.administrator.videoplayer.full;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.example.administrator.videoplayer.R;

import io.vov.vitamio.widget.MediaController;

/**
 * Created by Administrator on 2016/9/8.
 */
public class CustomMediaController extends MediaController{
    private MediaPlayerControl mediaPlayerControl;
    private final AudioManager audioManager;
    private Window window;

    private final int maxVolume;
    private int currentVolume;
    private float currentBrightness;
    public CustomMediaController(Context context) {
        super(context);
        //获取音频管理者
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        //获取最大音量
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        //获取屏幕,只能activity去get,所以强转
        window = ((Activity) context).getWindow();
    }
    // 通过重过此方法，来自定义layout
    @Override
    protected View makeControllerView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_custom_video_controller,this);
        initView(view);
        return view;
    }



    //因为父类mediaPlayerControl变量私有,且没有get方法,只有set方法.为了获得mediaPlayerControl,需要重写set方法.
    //重写的方法自动执行
    @Override public void setMediaPlayer(MediaPlayerControl player) {
        super.setMediaPlayer(player);
        mediaPlayerControl = player;
    }


    private void initView(View view) {
        //快进
        ImageButton btnFastForward = (ImageButton)view.findViewById(R.id.btnFastForward);
        btnFastForward.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //通过mediaPlayerControl获取当前播放进度
                long position = mediaPlayerControl.getCurrentPosition();
                position += 10000;
                if(position >= mediaPlayerControl.getDuration()){
                    position = mediaPlayerControl.getDuration();
                }
                //设置播放进度
                mediaPlayerControl.seekTo(position);
            }
        });
        //快退
        ImageButton btnFastRewide = (ImageButton)view.findViewById(R.id.btnFastRewind);
        btnFastRewide.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                long postion = mediaPlayerControl.getCurrentPosition();
                postion -= 10000;
                if(postion < 0)postion = 0;
                mediaPlayerControl.seekTo(postion);
            }
        });

        // 调整视图(左边调整亮度,右边是音量)
        final View adjustView = view.findViewById(R.id.adjustView);

        //手势监听(用OnGestureListener的实现类SimpleOnGestureListener)
        final GestureDetector gestureDetector = new GestureDetector(getContext(),new GestureDetector.SimpleOnGestureListener(){
            //只监控滑动动作
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                float startX = e1.getX();
                float startY = e1.getY();
                float endX = e2.getX();
                float endY = e2.getY();

                float width = adjustView.getWidth();
                float height = adjustView.getHeight();
                //获取纵向滑动距离占view高度的比例percentage
                float percentage = (startY - endY) / height;
                //判断是左侧还是右侧
                // 左侧: 亮度
                if (startX < width / 5) {
                    adjustbRrightness(percentage);
                }
                // 右侧: 音量
                else if (startX > width * 4 / 5) {
                    adjustVolume(percentage);
                }
                return super.onScroll(e1, e2, distanceX, distanceY);
            }


        });
        //对adjustView获取touch监听
        adjustView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //监听手势之前先获取当前音量和屏幕亮度(只识别一次点击时)
                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
                    currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    currentBrightness = window.getAttributes().screenBrightness;
                }
                // 但是，我们自己不去判读处理各种touch动用了,我们交给手势操作gesture去做
                gestureDetector.onTouchEvent(event);
                // 在调整过程中，一直显示
                show();
                //处理完成return true
                return true;
            }
        });
    }

    private void adjustVolume(float percentage) {
        int volume = (int) (currentVolume + percentage * maxVolume);
        volume = volume > maxVolume? maxVolume:volume;
        volume = volume < 0 ? 0 : volume;
        //设置音量,并展示到ui上
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,volume,AudioManager.FLAG_SHOW_UI);
    }

    private void adjustbRrightness(float percentage) {
        float brightness = percentage + currentBrightness;
        brightness = brightness > 1.0f ? 1.0f : brightness;
        brightness = brightness < 0 ? 0 : brightness;
        //设置亮度
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.screenBrightness = brightness;
        window.setAttributes(layoutParams);
    }
}
