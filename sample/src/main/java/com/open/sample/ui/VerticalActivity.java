package com.open.sample.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;

import com.open.leanback.widget.ArrayObjectAdapter;
import com.open.leanback.widget.HeaderItem;
import com.open.leanback.widget.ItemBridgeAdapter;
import com.open.leanback.widget.ListRow;
import com.open.leanback.widget.OnChildViewHolderSelectedListener;
import com.open.leanback.widget.Presenter;
import com.open.leanback.widget.RowPresenter;
import com.open.leanback.widget.VerticalGridView;
import com.open.sample.R;
import com.open.sample.entity.Image;
import com.open.sample.entity.Movie;
import com.open.sample.presenter.CardPresenter;
import com.open.sample.presenter.NewPresenterSelector;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class VerticalActivity extends Activity {

    private static final String TAG = "hailongqiu";

    VerticalGridView mRecyclerView;
    ArrayObjectAdapter mRowsAdapter;
    ItemBridgeAdapter mItemBridgeAdapter;
    int mSubPosition;
    ItemBridgeAdapter.ViewHolder mSelectedViewHolder;

    private static final int NUM_ROWS = 3;
    private static final int NUM_COLS = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vertical);
        mRecyclerView = findViewById(R.id.recyclerView);
        mRowsAdapter = new ArrayObjectAdapter(new NewPresenterSelector()); // 填入Presenter选择器.
        movieList = new ArrayList<>();
        imageList = new ArrayList<>();
//        loadData();
        queryNetData();
    }

    private void setUI() {
        mItemBridgeAdapter = new ItemBridgeAdapter(mRowsAdapter);
        mItemBridgeAdapter.setAdapterListener(mBridgeAdapterListener); // 测试一行选中颜色的改变.
        mRecyclerView.setAdapter(mItemBridgeAdapter);
        mRecyclerView.setOnChildViewHolderSelectedListener(new OnChildViewHolderSelectedListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onChildViewHolderSelected(RecyclerView parent, RecyclerView.ViewHolder viewHolder, int position, int subposition) {
                Log.d("hailongqiu", "选择一行");
                // 测试一行选中颜色的改变.
                if(mSelectedViewHolder != viewHolder || mSubPosition != subposition) {
                    mSubPosition = subposition;
                    if(mSelectedViewHolder != null) {
                        setRowViewSelected(mSelectedViewHolder, false);
                    }
                    mSelectedViewHolder = (ItemBridgeAdapter.ViewHolder)viewHolder;
                    if(mSelectedViewHolder != null) {
                        setRowViewSelected(mSelectedViewHolder, true);
                    }
                }
            }
        });
        // 不然有一些item放大被挡住了. (注意)
        mRecyclerView.setClipChildren(false);
        mRecyclerView.setClipToPadding(false);
        // 设置间隔.
        mRecyclerView.setPadding(50, 10, 50, 10);
        // 设置垂直item的间隔.
        mRecyclerView.setVerticalMargin(0);
        // 设置缓存.
        mRecyclerView.getRecycledViewPool().setMaxRecycledViews(0, 100);
    }

    private List<Movie> movieList;
    private List<Image> imageList;

    /**
     * 请求视频数据源
     */
    private void queryNetData() {
        BmobQuery<Movie> query = new BmobQuery<>();
        query.setLimit(500);
        query.order("createdAt");
        //v3.5.0版本提供`findObjectsByTable`方法查询自定义表名的数据
        query.findObjects(new FindListener<Movie>() {
            @Override
            public void done(List<Movie> list, BmobException e) {
                if (list != null) {
                    movieList.addAll(list);
                    queryImageData();
                }
            }
        });
    }

    /**
     * 请求图片数据源
     */
    private void queryImageData() {
        BmobQuery<Image> query = new BmobQuery<>();
        query.setLimit(500);
        query.order("createdAt");
        query.findObjects(new FindListener<Image>() {
            @Override
            public void done(List<Image> list, BmobException e) {
                if (list != null) imageList.addAll(list);
                loadData();
            }
        });
    }

    private void loadData() {
        CardPresenter cardPresenter = new CardPresenter();
        int limit = 5;

        int cloumCount2 = movieList.size() / limit + (movieList.size() % limit > 0 ? 1 : 0);
        for (int i = 0, j = 0; j < cloumCount2; i = i + 5, j++) {
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
            if (cloumCount2 - 1 == j) {
                listRowAdapter.addAll(0, movieList.subList(i, movieList.size()));
            } else {
                listRowAdapter.addAll(0, movieList.subList(i, i + 5));
            }
            //只有第一行才会有标题头
            if (i == 0) {
                HeaderItem gridHeader = new HeaderItem(0, "企业风采");
                mRowsAdapter.add(new ListRow(gridHeader, listRowAdapter));
            } else {
                mRowsAdapter.add(new ListRow(listRowAdapter));
            }
        }

        int cloumCount = imageList.size() / limit + (imageList.size() % limit > 0 ? 1 : 0);
        for (int i = 0, j = 0; j < cloumCount; i = i + 5, j++) {
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
            if (cloumCount - 1 == j) {
                listRowAdapter.addAll(0, imageList.subList(i, imageList.size()));
            } else {
                listRowAdapter.addAll(0, imageList.subList(i, i + 5));
            }
            //只有第一行才会有标题头
            if (i == 0) {
                HeaderItem gridHeader = new HeaderItem(0, "会员专区");
                mRowsAdapter.add(new ListRow(gridHeader, listRowAdapter));
            } else {
                mRowsAdapter.add(new ListRow(listRowAdapter));
            }
        }

        setUI();
    }

    ItemBridgeAdapter.AdapterListener mBridgeAdapterListener = new ItemBridgeAdapter.AdapterListener() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onCreate(ItemBridgeAdapter.ViewHolder viewHolder) {
            setRowViewSelected(viewHolder, false);
        }

        @Override
        public void onDetachedFromWindow(ItemBridgeAdapter.ViewHolder vh) {
            if (mSelectedViewHolder == vh) {
                setRowViewSelected(mSelectedViewHolder, false);
                mSelectedViewHolder = null;
            }
        }

        public void onUnbind(ItemBridgeAdapter.ViewHolder vh) {
            setRowViewSelected(vh, false);
        }
    };

    /**
     *  测试一行选中的颜色改变.
     */
    @TargetApi(Build.VERSION_CODES.M)
    void setRowViewSelected(ItemBridgeAdapter.ViewHolder vh, boolean selected) {
//        vh.itemView.setBackground(new ColorDrawable(selected ? Color.TRANSPARENT : Color.RED));
    }

    private static final int GRID_ITEM_WIDTH = 200;
    private static final int GRID_ITEM_HEIGHT = 200;



    // 测试其它数据.
//        HeaderItem gridHeader = new HeaderItem(i, "系统设置");
//        GridItemPresenter mGridPresenter = new GridItemPresenter();
//        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
//        gridRowAdapter.add("音频");
//        gridRowAdapter.add("投影设置");
//        gridRowAdapter.add("明天是否");
//        mRowsAdapter.add(new ButtonListRow(gridHeader, gridRowAdapter));
    public static class GridItemPresenter extends Presenter {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            Button view = new Button(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT));
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setTextColor(Color.WHITE);
            view.setGravity(Gravity.CENTER);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
            ((Button) viewHolder.view).setText((String) item);
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {
        }
    }

}
