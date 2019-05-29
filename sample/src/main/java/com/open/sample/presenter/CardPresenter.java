package com.open.sample.presenter;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.open.leanback.widget.Presenter;
import com.open.sample.R;
import com.open.sample.entity.*;
import com.open.sample.player.VideoPlayerPageActivity;
import com.open.sample.ui.ImageBrowserActivity;
import com.open.sample.view.CardView;

import java.util.ArrayList;

/**
 * Created by hailongqiu on 2016/12/16.
 */
public class CardPresenter extends Presenter {

    private static final int CARD_WIDTH = 313;
    private static final int CARD_HEIGHT = 176;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        CardView cardView = new CardView(parent.getContext()) {
            @Override
            public void setSelected(boolean selected) {
                super.setSelected(selected);
            }
        };
        ViewGroup.LayoutParams lp = cardView.getLayoutParams();
//        lp.width = CARD_WIDTH;
//        lp.height = CARD_HEIGHT;
//        cardView.setLayoutParams(lp);
//        cardView.setLayoutParams(new ViewGroup.LayoutParams(CARD_WIDTH, CARD_HEIGHT));
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final Object item) {
        final CardView cardView = (CardView) viewHolder.view;
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (item instanceof Movie) {
                        Movie movie = (Movie) item;
                        Intent intent = new Intent(cardView.getmContext(), VideoPlayerPageActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(VideoPlayerPageActivity.MOVIE, movie.getVideoFile().getFileUrl());
                        cardView.getmContext().startActivity(intent);
                    } else if (item instanceof Image) {
                        Image image = (Image) item;
                        ArrayList<String> imgs = new ArrayList<>();
                        imgs.add(image.getImgFile().getFileUrl());
                        ImageBrowserActivity.startImageBrowserActivity(imgs, 0, cardView.getmContext());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        if (item instanceof Movie) {
            Movie movie = (Movie) item;
            Glide.with(viewHolder.view.getContext())
                    .load(movie.getImageFile().getFileUrl())
                    .error(R.mipmap.loading)
                    .into(cardView.getImg_cover());
            cardView.getTv_name().setText(movie.getTitle());
        } else if (item instanceof Image) {
            Image image = (Image) item;
            Glide.with(viewHolder.view.getContext())
                    .load(image.getImgFile().getFileUrl())
                    .error(R.mipmap.loading)
                    .into(cardView.getImg_cover());
            cardView.getTv_name().setText(image.getTitle());
        }
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        CardView cardView = (CardView) viewHolder.view;
    }
}
