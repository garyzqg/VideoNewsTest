package com.example.administrator.videoplayer.full;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

import com.example.administrator.videoplayer.R;

import io.vov.vitamio.widget.MediaController;

/**
 * Created by Administrator on 2016/9/8.
 */
public class CustomMediaController extends MediaController{

    public CustomMediaController(Context context) {
        super(context);
    }
    // 通过重过此方法，来自定义layout
    @Override
    protected View makeControllerView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_custom_video_controller,this);
        initView(view);
        return view;
    }

    private MediaPlayerControl mediaPlayerControl;
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
    }
}
