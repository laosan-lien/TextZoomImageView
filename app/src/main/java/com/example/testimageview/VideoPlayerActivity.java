package com.example.testimageview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextRenderer;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.oplus.microapp.eventbus.HttpEventNotifier;
import com.songwenju.useexoplayer.utils.LogUtil;
import com.songwenju.useexoplayer.utils.NetUtil;
import com.songwenju.useexoplayer.utils.TaskUtils;
import com.songwenju.useexoplayer.utils.WsManager;
import com.songwenju.useexoplayer.utils.listener.WsStatusListener;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Response;
import okio.ByteString;

public class VideoPlayerActivity extends AppCompatActivity {
    public static final String CHANNEL_NAME = "videoplayer_1000";
    public static final String CHANNEL_ID = "1";
    public static final String VIDEO_URI = "video_uri";
    public static final String VIDEO_CONTENT_URI = "video_content_uri";
    public static final String VIDEO_NAME = "video_name";
    public static final String COMMAND = "command";
    public static final String DEVICE = "device";
    public static final String KEY_SHOW_MSG = "key_show_msg";
    public static final String VIDEO_SOURCE = "source";
    public static final String VIDEO_PROGRESS = "progress";
    private static final String ARRAY_COMMANDS[] = {"playVideo", "makeToast", "playVideoFromOneLauncher"};
    private static final HashMap<String, String> mDevices = new HashMap();
    private static final String TAG = "VideoPlayerActivity";
    private LinearLayout mLltShowMsg;
    private TextView mTvShowMsg;
    private SimpleExoPlayerView mExoPlayerView;
    private SimpleExoPlayer mSimpleExoPlayer;
    private Context mContext;
    private String state = NO_STATE;
    private boolean mPlaying = false;

    private static final String APP_NAME = "com.songwenju.useexoplayer";
    private static final String ACTIVITY_NAME = "VideoPlayerActivity";
    private static final String CANCEL_STATE = "cancel";
    private static final String USER_CONFIRM_STATE = "confirm";
    private static final String NO_STATE = "no_state";
    private static final String HAND_OFF_CLEAR = "no_handoff";


    Uri playerUri = null;
    String fileName = "";
    long mProgress = -1;
    boolean isFromOneLauncher = false;

    static {
        mDevices.put("TV", "电视");
        mDevices.put("PC", "电脑");
        mDevices.put("CAR", "车机");
        mDevices.put("PHONE", "手机");
        mDevices.put("PREV_DEVICE", "之前设备");
    }

    private Thread mthread = null;
    private ProgressBar mProgressBar;
    private boolean getRestoreValue = false;
    private HandOffThread handOffThread;
    private static boolean runningFlag = true;

    private WsManager wsManager;
    private WsStatusListener wsStatusListener = new WsStatusListener() {
        @Override
        public void onOpen(Response response) {
            Log.d(TAG, "open");
        }

        @Override
        public void onMessage(String text) {
            Log.d(TAG, "onMessageString: " + text);
        }

        @Override
        public void onMessage(ByteString text) {
            byte[] bytes = text.toByteArray();
            String msg = new String(bytes);
            Log.d(TAG, "onMessageByteString: " + msg);
        }

        @Override
        public void onClose(int code, String reason) {
            Log.d(TAG, "onClose");
        }

        @Override
        public void onValue(Map<String, ByteString> valueMap) {
            Log.d(TAG, "onValue");
            if (valueMap.containsKey("quit")) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);

                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        }

        @Override
        public void onRestore(Map<String, ByteString> valueMap) {
            String quit_msg = "quit";
            wsManager.setValue("quit", ByteString.encodeString(quit_msg, Charset.forName("UTF-8")));

            if (valueMap.containsKey("playUri")) {
                String t = valueMap.get("playUri").toString();
                byte[] bytes = valueMap.get("playUri").toByteArray();
                String msg = new String(bytes);
                playerUri = Uri.parse(msg);
            }
            if (valueMap.containsKey("progress")) {
                byte[] bytes = valueMap.get("progress").toByteArray();
                String msg = new String(bytes);
                mProgress = Long.parseLong(msg);
            }
            if (playerUri == null) {
                Intent intent = getIntent();
                Bundle bundle = intent.getExtras();
                String uri = bundle.getString(VIDEO_URI);
                playerUri = Uri.parse(uri);
                playVideo();
            } else {
                //showToast("PREV_DEVICE");
                replayVideo();
            }
            wsManager.deleteValueByKey("quit");
        }

        @Override
        public void onValueDeleteByKeys(List<String> keys) {
            Log.d(TAG, "onValueDeleteByKeys");
        }
    };

    private String getApiProxyVisitPath(String uri) {
        if (uri.startsWith("http")) {
            return uri;
        }
        String wlanIP = NetUtil.getWlanInfo(mContext).getIp();
        String visitPath = "http://" + wlanIP + ":8000/v2/file/download?path=";
        String base64Uri = Base64.getEncoder().encodeToString(uri.getBytes(StandardCharsets.UTF_8));
        visitPath = visitPath + base64Uri;
        return visitPath;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, String.format("video player start"));
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        mContext = this;
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        initNotification();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        wsManager = new WsManager(getBaseContext(), "datasync3.wanyol.com", 80, "videohandoff", "tanke");
        wsManager.setWsStatusListener(wsStatusListener);
        wsManager.start();
        initPlayer();

        findViewById(R.id.closeplaying).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    new Thread(()->{
                        HttpEventNotifier.notifyEvent(11112, new HashMap<>());
                    }).start();
                }
        });

        // OPPO test 启动监听线程  监听视频播放音量、进度
        runningFlag = true;
        if (mthread == null) {
            handOffThread = new HandOffThread();
            mthread = new Thread(handOffThread);
            mthread.start();
            Log.d(TAG, String.format("video player mthread start"));
        }

        Log.d(TAG, String.format("VideoPlayerActivity Started......"));
    }

    private void initNotification() {
        mLltShowMsg = findViewById(R.id.llt_notification);
        mLltShowMsg.setVisibility(View.GONE);
        mTvShowMsg = findViewById(R.id.tv_title);

        findViewById(R.id.bt_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLltShowMsg.setVisibility(View.GONE);
                TaskUtils.stopTask(KEY_SHOW_MSG);
                //TODO:切换到TV/PC
                state = USER_CONFIRM_STATE;
            }
        });

        findViewById(R.id.bt_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLltShowMsg.setVisibility(View.GONE);
                TaskUtils.stopTask(KEY_SHOW_MSG);
                state = CANCEL_STATE;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mthread == null && !runningFlag) {
            replayVideo();
            mSimpleExoPlayer.seekTo(mProgress);
        } else {
            setPlayPause(true);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.v(TAG, "onNewIntent");
        AlertDialog.Builder alertdialogbuilder = new AlertDialog.Builder(this);
        alertdialogbuilder.setMessage("是否进行视频续播？");
        alertdialogbuilder.setPositiveButton("确定", null);
        alertdialogbuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
        @Override
         public void onClick(DialogInterface dialog, int which) {
             new Thread(()->{
                 HttpEventNotifier.notifyEvent(11111, new HashMap<>());
             }).start();
                dialog.dismiss();
            }
        });
        alertdialogbuilder.setNeutralButton("取消", null);
        final AlertDialog alertdialog1 = alertdialogbuilder.create();
        alertdialog1.show();
    }

    private void showToast(String device) {
        String name = mDevices.get(device);
        if (!TextUtils.isEmpty(name)) {
            if (mLltShowMsg.getVisibility() == View.GONE) {
                mLltShowMsg.setVisibility(View.VISIBLE);
                mTvShowMsg.setText(mContext.getString(R.string.msg_toast, name));
                TaskUtils.startTask(KEY_SHOW_MSG, 10000, () -> {
                    VideoPlayerActivity.this.runOnUiThread(() -> {
                                mLltShowMsg.setVisibility(View.GONE);
                            }
                    );
                });
            }
        }
    }

    public class HandOffThread implements Runnable {
        @Override
        public void run() {
            Log.d(TAG, "hand off thread start run");
            while (runningFlag) {
                try {
                    Thread.sleep(500);
                    Log.d(TAG, "hand off thread run in period");
                    startHandOffListener();
                } catch (Exception e) {
                    Log.e(TAG, String.format("hand off thread run fail, exception[%s].", e.toString()));
                }
            }
            stopHandOff();

            Log.d(TAG, String.format("video player mthread stop"));
        }

        private void startHandOffListener() {
            // step1. 获取播放进度
            if (mPlaying == false) {
                return;
            }
            long progress = mSimpleExoPlayer.getCurrentPosition();

            // step2. 获取播放音量
            float volume = mSimpleExoPlayer.getVolume();

            Log.d(TAG, String.format("hand off listener get progress[%d] volume[%f]", progress, volume));
            Map<String, ByteString> info = new HashMap<>();
            //info.put("playUri", ByteString.encodeString(playerUri.toString(), Charset.forName("UTF-8")));
            String uri = playerUri.toString();
            String apiProxyVisitPath = getApiProxyVisitPath(uri);
            //String apiProxyVisitPath = "http://172.23.72.250:8888/2.mov";
            info.put("playUri", ByteString.encodeString(apiProxyVisitPath, Charset.forName("UTF-8")));
            info.put("progress", ByteString.encodeString(Long.toString(progress), Charset.forName("UTF-8")));
            wsManager.setValues(info);
            //SDK.storeLocaleInfo(json);
        }

        private void stopHandOff() {
            Log.d(TAG, String.format("hand off stop"));
        }
    }

    /**
     * 初始化player
     */
    private void initPlayer() {
        //1. 创建一个默认的 TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();
        //2.创建ExoPlayer
        mSimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
        //3.创建SimpleExoPlayerView
        mExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.exoView);

        //4.为SimpleExoPlayer设置播放器
        mExoPlayerView.setPlayer(mSimpleExoPlayer);
    }

    private void playVideo() {
        //测量播放过程中的带宽。 如果不需要，可以为null。
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        // 生成加载媒体数据的DataSource实例。
        DataSource.Factory dataSourceFactory
                = new DefaultDataSourceFactory(VideoPlayerActivity.this,
                Util.getUserAgent(VideoPlayerActivity.this, "useExoplayer"), bandwidthMeter);
        // 生成用于解析媒体数据的Extractor实例。
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();


        // MediaSource代表要播放的媒体。
        MediaSource videoSource = new ExtractorMediaSource(playerUri, dataSourceFactory, extractorsFactory,
                null, null);
        //Prepare the player with the source.
        mSimpleExoPlayer.prepare(videoSource);
        //添加监听的listener
//        mSimpleExoPlayer.setVideoListener(mVideoListener);
        mSimpleExoPlayer.addListener(eventListener);
//        mSimpleExoPlayer.setTextOutput(mOutput);
        mSimpleExoPlayer.setPlayWhenReady(true);
        mSimpleExoPlayer.seekTo(mProgress);
        mPlaying = true;
    }

    private void replayVideo() {
        if (mSimpleExoPlayer != null) {
            setPlayPause(false);
            mSimpleExoPlayer.stop();
            mSimpleExoPlayer.release();
            initPlayer();
            playVideo();
        }
    }

    TextRenderer.Output mOutput = new TextRenderer.Output() {
        @Override
        public void onCues(List<Cue> cues) {
            LogUtil.i(TAG, "MainActivity.onCues.");
        }
    };

    private SimpleExoPlayer.VideoListener mVideoListener = new SimpleExoPlayer.VideoListener() {
        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            LogUtil.i(TAG, "MainActivity.onVideoSizeChanged.width:" + width + ", height:" + height);

        }

        @Override
        public void onRenderedFirstFrame() {
            LogUtil.i(TAG, "MainActivity.onRenderedFirstFrame.");
        }
    };


    private ExoPlayer.EventListener eventListener = new ExoPlayer.EventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
            LogUtil.i(TAG, "onTimelineChanged");
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            LogUtil.i(TAG, "hello onTracksChanged");
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            LogUtil.i(TAG, "onLoadingChanged");
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            LogUtil.i(TAG, "onPlayerStateChanged: playWhenReady = " + String.valueOf(playWhenReady)
                    + " playbackState = " + playbackState);
            switch (playbackState) {
                case ExoPlayer.STATE_ENDED:
                    LogUtil.i(TAG, "Playback ended!");
                    //Stop playback and return to start position
                    setPlayPause(false);
                    mSimpleExoPlayer.seekTo(0);
                    break;
                case ExoPlayer.STATE_READY:
                    mProgressBar.setVisibility(View.GONE);
                    LogUtil.i(TAG, "ExoPlayer ready! pos: " + mSimpleExoPlayer.getCurrentPosition()
                            + " max: " + stringForTime((int) mSimpleExoPlayer.getDuration()));
                    setProgress(0);

                    // OPPO test 启动监听线程  监听视频播放音量、进度
                    if (mthread == null) {
                        handOffThread = new HandOffThread();
                        mthread = new Thread(handOffThread);
                        mthread.start();
                    }

                    break;
                case ExoPlayer.STATE_BUFFERING:
                    LogUtil.i(TAG, "Playback buffering!");
                    mProgressBar.setVisibility(View.VISIBLE);
                    break;
                case ExoPlayer.STATE_IDLE:
                    LogUtil.i(TAG, "ExoPlayer idle!");
                    break;
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            LogUtil.i(TAG, "onPlaybackError: " + error.getMessage());
        }

        @Override
        public void onPositionDiscontinuity() {
            LogUtil.i(TAG, "onPositionDiscontinuity");
        }

//        @Override
//        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
//            LogUtil.i(this,"MainActivity.onPlaybackParametersChanged."+playbackParameters.toString());
//        }
    };

    /**
     * Starts or stops playback. Also takes care of the Play/Pause button toggling
     *
     * @param play True if playback should be started
     */
    private void setPlayPause(boolean play) {
        mSimpleExoPlayer.setPlayWhenReady(play);
    }

    private String stringForTime(int timeMs) {
        StringBuilder mFormatBuilder;
        Formatter mFormatter;
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }


    @Override
    protected void onPause() {
        LogUtil.i(TAG, "MainActivity.onPause.");
        super.onPause();
        setPlayPause(false);
        mProgress = mSimpleExoPlayer.getCurrentPosition();
    }

    @Override
    protected void onStop() {
        LogUtil.e(TAG, "MainActivity.onStop.");
        super.onStop();
        mSimpleExoPlayer.stop();
        mSimpleExoPlayer.release();
        runningFlag = false;
        mthread = null;
        Log.d(TAG, String.format("video player stop"));
    }

    /*
    private String toHttpFilePath(Uri playerUri) {
        return "http://" + NetUtil.getLocalIP() + ":" + "8080/file/download?path="
                + Base64.getUrlEncoder().encodeToString(playerUri.getPath().getBytes());
    }
     */
}
