
package com.open.sample.player;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.blankj.utilcode.utils.ConvertUtils;
import com.blankj.utilcode.utils.Utils;
import com.open.sample.MPlayerUtil.IMPlayListener;
import com.open.sample.MPlayerUtil.IMPlayer;
import com.open.sample.MPlayerUtil.MPlayer;
import com.open.sample.MPlayerUtil.MPlayerException;
import com.open.sample.MPlayerUtil.MinimalDisplay;
import com.open.sample.MPlayerUtil.StringUtils;
import com.open.sample.R;

import java.util.Timer;
import java.util.TimerTask;

public class VideoPlayerPageActivity extends Activity implements SeekBar.OnSeekBarChangeListener {

    public static final String MOVIE = "movieParmas";
    private SurfaceView mPlayerView;
    private MPlayer player;
    private SeekBar seekBar;
    private Timer timer;
    TimerTask timerTask;
    String url1="file:///sdcard/XCZ001.ts.ts";
//    String url2="http://221.228.226.23/11/t/j/v/b/tjvbwspwhqdmgouolposcsfafpedmb/sh.yinyuetai.com/691201536EE4912BF7E4F1E2C67B8119.mp4";
    String url2 = "http://bmob-cdn-24828.b0.upaiyun.com/2019/05/23/13bc609f40317eec8081152fe69ef404.mp4";
    private ImageButton btn_play_pause;
    private TextView tv_time_total,tv_time_current,tv_file_name,tvMoveTime;
    private static final int HIDE = 1,SHOW=2,FINISH=3,ENABLE_SEEK=4,ENABLE_PLAY=5;
    int timeout=1000;//超时时间，毫秒
    boolean flag_enable_seek=false;//是否允许快进，默认打开播放器后5s内不允许快进

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            long pos;
            switch (msg.what) {
                case HIDE:
                    hideSeekbar();
                    break;
                case SHOW:
                    showSeekbar();
                    break;
                case FINISH:
                    finish();
                    break;
                case ENABLE_SEEK:
                    flag_enable_seek=true;
                    break;
                case ENABLE_PLAY:
                    try { player.play(); }
                    catch (MPlayerException e) { e.printStackTrace(); }
                    break;
            }
        }
    };


    private RelativeLayout llSeekbar;
    private int videoDuration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_myplayer2);
        url2 = getIntent().getStringExtra(MOVIE);
        initView();
        initData();
        initPlayer();
    }

    private void initView(){
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        mPlayerView= (SurfaceView) findViewById(R.id.mPlayerView);
        btn_play_pause = (ImageButton)findViewById(R.id.btn_play_pause);
        tv_time_total = (TextView) findViewById(R.id.tv_time_total);
        tv_time_current = (TextView) findViewById(R.id.tv_time_current);
        llSeekbar = (RelativeLayout) findViewById(R.id.llSeekbar);
        tv_file_name = (TextView) findViewById(R.id.tv_file_name);
        tvMoveTime = (TextView) findViewById(R.id.tvMoveTime);
        seekBar.setOnSeekBarChangeListener(this);
    }

    private void initData() {
        Utils.init(VideoPlayerPageActivity.this);
    }
    private void initPlayer(){
        player=new MPlayer();
        player.setDisplay(new MinimalDisplay(mPlayerView));
        playerOnPrepared();
        playerSurfaceDestroyed();
        playerOnBufferUpdateListen();
        seedCompletedListen();
        starPlayUrl(url2);


    }

    private void playerOnBufferUpdateListen() {
        player.setImPlayerBufferUpdate(new MPlayer.IMPlayerBufferUpdate() {
            @Override
            public void onBufferUpdate(final int percent) {
                VideoPlayerPageActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (videoDuration>0)
                        seekBar.setSecondaryProgress((int) ((percent/100.0)*videoDuration));
                    }
                });
            }
        });
    }

    private void playerSurfaceDestroyed() {
        player.setiSurfaceDestroyed(new MPlayer.IMPlayerSurfaceDestroyed() {
            @Override
            public void onSurfaceDestroyed() {
                mHandler.removeMessages(HIDE);
                mHandler.removeMessages(SHOW);//快进到最后，特别处理
                mHandler.removeMessages(FINISH);
                mHandler.removeMessages(ENABLE_SEEK);
                mHandler.removeMessages(ENABLE_PLAY);
                if (timer!=null) timer.cancel();
                if (timerTask!=null) timerTask.cancel();
            }
        });
    }

    private void seedCompletedListen() {
        player.setiSeekCompleted(new MPlayer.ISeekCompleted() {
            @Override
            public void onSeekCompleted() {
                mHandler.removeMessages(HIDE);
                mHandler.sendMessageDelayed(mHandler.obtainMessage(HIDE), timeout);
            }
        });
    }

    private void playerSetListen() {
        player.setPlayListener(new IMPlayListener() {
            @Override
            public void onStart(IMPlayer player) {

            }
            @Override
            public void onPause(IMPlayer player) {
            }
            @Override
            public void onResume(IMPlayer player) {
            }
            @Override
            public void onComplete(IMPlayer player) {
                mHandler.sendMessageDelayed(mHandler.obtainMessage(FINISH), 0);
            }
        });
    }

    private void hideSeekbar() {
        if (player.isPlaying()){
            llSeekbar.setVisibility(View.GONE);
            btn_play_pause.setVisibility(View.GONE);
            tv_file_name.setVisibility(View.GONE);
        }
    }

    private void showSeekbar() {
        llSeekbar.setVisibility(View.VISIBLE);
        tv_file_name.setVisibility(View.VISIBLE);
        if (!player.isPlaying()) {
            btn_play_pause.setVisibility(View.VISIBLE);
        }
    }
    private void starPlayUrl(String mUrl) {
        if(mUrl.length()>0){
            try {
                player.setSource(mUrl);
                player.play();

            } catch (MPlayerException e) {
                e.printStackTrace();
            }
        }
    }

    private void playerOnPrepared() {
        player.setOnPrepared(new MPlayer.IMPlayerPrepared() {
            @Override
            public void onMPlayerPrepare() {

                mHandler.sendMessageDelayed(mHandler.obtainMessage(ENABLE_SEEK), timeout);//播放器准备好后开始计时5秒内不允许快进
                playerSetListen();
                videoDuration = player.getDuration();
                seekBar.setMax(videoDuration);
                seekBar.setProgress(player.getCurrentPosition());
                String time = StringUtils.generateTime(videoDuration);
                tv_time_total.setText(" / "+time);
                tv_time_current.setText("00:00");

                mHandler.sendMessageDelayed(mHandler.obtainMessage(HIDE), timeout);
                timer = new Timer();
                timerTask=new TimerTask() {
                    @Override
                    public void run() {
                        final int currentPosition=player.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                        VideoPlayerPageActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String time = StringUtils.generateTime(currentPosition);
                                tv_time_current.setText(time);
                                tvMoveTime.setText(time);
                            }
                        });
                    }
                };
                timer.schedule(timerTask,0,100);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        player.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.onDestroy();

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
         int seekbarRealWidth=seekBar.getWidth()-2* ConvertUtils.dp2px(15);//15在xml中写死了
        float average= (float) (seekbarRealWidth*1.0/seekBar.getMax());
        int left=(int) (  progress * average )+tvMoveTime.getWidth()/4;
        tvMoveTime.setX(left);//当前播放时间的位置跟随seekbar
    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (flag_enable_seek) player.seekto(seekBar.getProgress());
        String time = StringUtils.generateTime(player.getCurrentPosition());
        tv_time_current.setText(time);
        tvMoveTime.setText(time);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(HIDE),0);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(HIDE), timeout);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        mHandler.sendMessageDelayed(mHandler.obtainMessage(SHOW), 0);
        switch (keyCode){
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if ( player.isPlaying())
                {
                    player.pause();
                    btn_play_pause.setImageResource(R.drawable.mediacontroller_pause);
                    btn_play_pause.setVisibility(View.VISIBLE);
                }
                else
                {
                    try {
                        player.play();
                        btn_play_pause.setImageResource(R.drawable.mediacontroller_play);
                        mHandler.removeMessages(HIDE);
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(HIDE), 0);
                    } catch (MPlayerException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (flag_enable_seek) player.seekto(player.getCurrentPosition()+5000);
                break;
            case KeyEvent.KEYCODE_SHIFT_LEFT:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (flag_enable_seek) player.seekto(player.getCurrentPosition()-5000);
                break;
            case KeyEvent.KEYCODE_BACK:

                finish();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }



}
