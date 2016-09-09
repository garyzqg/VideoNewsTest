package com.example.administrator.videonewstest.ui.local;


import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.example.administrator.videonewstest.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * A simple {@link Fragment} subclass.
 */
public class LocalVideoFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "LocalVideoFragment";

    private Unbinder unbinder;
    @BindView(R.id.gridView)GridView gridView;
    LocalVideoAdapter adapter;
    public LocalVideoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始当前页面的Loader（加载器，去loader视频数据）
        // 个唯一ID来标志加载器
        // 可选的参数，用于加载器初始化时
        // 加载器事件回调接口
        adapter = new LocalVideoAdapter(getContext());
        getLoaderManager().initLoader(0,null,this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_local_video, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        gridView.setAdapter(adapter);
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.release();
    }

    // loadercallback start ----------- -----
    //泛型改成Cursor
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                MediaStore.Video.Media._ID, // 视频ID
                MediaStore.Video.Media.DATA, // 视频文件路径
                MediaStore.Video.Media.DISPLAY_NAME,// 视频名称
        };
        //CursorLoader继承自AsyncTaskLoader<Cursor>,后者继承自Loader<Cursor>!!!!!!!!
        return new CursorLoader(getContext(),
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,// 视频provider URI
                projection,
                null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished: " + data.getCount());
        adapter.swapCursor(data);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
    // loadercallback end ----------------
}
