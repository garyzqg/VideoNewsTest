package com.example.administrator.videonewstest.ui.local;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2016/9/9.
 */
public class LocalVideoAdapter extends CursorAdapter {
    // 用来加载视频预览图的线程池
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);
    // 用来缓存已加载过的预览图像(缓存大小5M,缓存个数图片的个数value.getByteCount())
    private LruCache<String,Bitmap> lruCache = new LruCache<String,Bitmap>(5 * 1024 * 1024){
        @Override protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };

    public LocalVideoAdapter(Context context) {
        super(context, null, true);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //用一个自定义控件实现Item,去完成数据的展示(关联id,设置监听,设置视图等)
        //自定义控件适合较复杂的布局,比如listview的item很复杂,且需要重复利用
        return new LocalVideoItem(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final LocalVideoItem localVideoItem = (LocalVideoItem) view;
        localVideoItem.bind(cursor);

        //通过item里的公开的set方法,获取本地视频地址
        final String filePath = localVideoItem.getFilePath();

        //如果缓存中存在这张预览图,直接在缓存中获取并展示在视图上
        if (lruCache.get(filePath) != null){
            localVideoItem.setIvPreView(lruCache.get(filePath));
            //return为直接结束此方法,不在进行以下操作,浪费时间
            return;
        }
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                // 加载视频的预览图像
                Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MINI_KIND);
                // 缓存当前预览图像,文件路径做为key
                lruCache.put(filePath,bitmap);
                // 将图像设置到控件上
                // 注意：当前是在后台线程内
                localVideoItem.setIvPreView(filePath, bitmap);
            }
        });
    }

    //用于释放线程池,fragmnet销毁时调用!!!!!!!!!!
    public void release(){
        executorService.shutdown();
    }
}
