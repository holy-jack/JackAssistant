package com.jack.jackassistant.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

import com.jack.jackassistant.R;
import com.jack.jackassistant.adapter.ChatMessageAdapter;
import com.jack.jackassistant.adapter.ImageLoaderGridViewAdapter;
import com.jack.jackassistant.app.OnOperationListener;
import com.jack.jackassistant.bean.ChatMessage;
import com.jack.jackassistant.bean.Function;
import com.jack.jackassistant.bean.Recorder;
import com.jack.jackassistant.manager.MediaManager;
import com.jack.jackassistant.util.Constants;
import com.jack.jackassistant.util.HttpUtils;
import com.jack.jackassistant.util.MyLog;
import com.jack.jackassistant.util.ToastUtil;
import com.jack.jackassistant.view.AudioRecorderButton;
import com.jack.jackassistant.view.MessageSendToolLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements ChatMessageAdapter.OnChatMessageItemClickListener {

    private static final String TAG = "MainActivity";

    private ListView mMessageListView;
    private MessageSendToolLayout mBottomMessageSendToolLayout;
    private AudioRecorderButton mAudioRecorderButton;
    private View mViewAnim;

    private ChatMessageAdapter mChatMessageAdapter;
    private List<ChatMessage> mData = new ArrayList<ChatMessage>();

    private long mExitTime;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            ChatMessage result = (ChatMessage) msg.obj;
            mData.add(result);
            MyLog.e(TAG, "handler Thread.getId():" + Thread.currentThread().getId());
            mChatMessageAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();

    }

    private void initView() {
        mMessageListView = (ListView) findViewById(R.id.messageListView);
        mBottomMessageSendToolLayout = (MessageSendToolLayout) findViewById(R.id.bottomMessageSendToolLayout);
        mAudioRecorderButton = (AudioRecorderButton) mBottomMessageSendToolLayout.findViewById(R.id.btn_audio_recorder);

    }

    private void initData() {

//        httpTest();
//        mData = testData();
        mData = getInitData();
        mChatMessageAdapter = new ChatMessageAdapter(this, mData);
        mMessageListView.setAdapter(mChatMessageAdapter);

        mBottomMessageSendToolLayout.setOnOperationListener(new OnOperationListener() {
            @Override
            public void sendMessages(final String content) {
                //send message
                ChatMessage toChatMessage =
                        new ChatMessage(getResources().getString(R.string.text_title),
                                "",
                                getResources().getString(R.string.text_me),
                                "",
                                ChatMessage.SendType.OUTCOMING,
                                ChatMessage.SendStatus.SUCCESS,
                                new Date(),
                                content,
                                ChatMessage.ContentType.TEXT);

                mData.add(toChatMessage);
                mChatMessageAdapter.notifyDataSetChanged();

                //receive message
                new Thread() {
                    @Override
                    public void run() {
                        ChatMessage result = HttpUtils.getChatMessage(content);
                        Message message = Message.obtain();
                        message.obj = result;
                        handler.sendMessage(message);
                    }
                }.start();
            }

            @Override
            public void selectedFace(String resId) {
                //send face
                ChatMessage toChatFace =
                        new ChatMessage(getResources().getString(R.string.text_title),
                                "",
                                getResources().getString(R.string.text_me),
                                "",
                                ChatMessage.SendType.OUTCOMING,
                                ChatMessage.SendStatus.SUCCESS,
                                new Date(),
                                resId,
                                ChatMessage.ContentType.FACE);

                mData.add(toChatFace);
                mChatMessageAdapter.notifyDataSetChanged();
            }

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void selectedFunction(Function function) {
                mBottomMessageSendToolLayout.hideMessageProviderLayout();

                Intent intent = new Intent(MainActivity.this, ImageLoaderActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivityForResult(intent, Constants.REQUEST_CODE_SELECTED_IMAGE, null);
            }


        });

        mMessageListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mBottomMessageSendToolLayout.hideMessageProviderLayout();
                return false;
            }
        });


        mAudioRecorderButton.setOnAudioFinishRecorderListener(new AudioRecorderButton.OnAudioFinishRecorderListener() {
            @Override
            public void audioFinishRecorder(float time, String filePath) {
                Recorder recorder = new Recorder();
                recorder.setTime(time);
                recorder.setFilePath(filePath);

                //send audio recorder
                ChatMessage toChatAudioRecorder =
                        new ChatMessage(getResources().getString(R.string.text_title),
                                "",
                                getResources().getString(R.string.text_me),
                                "",
                                ChatMessage.SendType.OUTCOMING,
                                ChatMessage.SendStatus.SUCCESS,
                                new Date(),
                                recorder,
                                ChatMessage.ContentType.RECORDER);

                mData.add(toChatAudioRecorder);
                mChatMessageAdapter.notifyDataSetChanged();
            }
        });

        mChatMessageAdapter.setOnChatMessageItemClickListener(this);
    }

    private List<ChatMessage> getInitData() {

        List<ChatMessage> initData = new ArrayList<ChatMessage>();
        ChatMessage fromChatMessage =
                new ChatMessage(getResources().getString(R.string.text_title),
                        "",
                        getResources().getString(R.string.text_me),
                        "",
                        ChatMessage.SendType.INCOMING,
                        ChatMessage.SendStatus.SUCCESS,
                        new Date(),
                        getString(R.string.default_incoming_msg),
                        ChatMessage.ContentType.TEXT);

        initData.add(fromChatMessage);

        return initData;
    }

    //use message_send_tool_layout
//    @Override
//    public void onClick(View v) {
//        final String msg = inputEditText.getText().toString();
//        if (msg.trim().isEmpty()) {
//            ToastUtil.showShortToast(this, R.string.empty_msg);
//            return;
//        }
//
//        ChatMessage toChatMessage =
//                new ChatMessage(getResources().getString(R.string.text_title),
//                        "",
//                        getResources().getString(R.string.text_me),
//                        "",
//                        ChatMessage.SendType.OUTCOMING,
//                        ChatMessage.SendStatus.SUCCESS,
//                        new Date(),
//                        msg,
//                        ChatMessage.ContentType.TEXT);
//
//        mData.add(toChatMessage);
//        mChatMessageAdapter.notifyDataSetChanged();
//        MyLog.e(TAG, "onClick Thread.getId():" + Thread.currentThread().getId());
//
//        inputEditText.setText("");
//
//        new Thread() {
//            @Override
//            public void run() {
//                MyLog.e(TAG, "new Thread.getId():" + Thread.currentThread().getId());
//                ChatMessage result = HttpUtils.getChatMessage(msg);
//                Message message = Message.obtain();
//                message.obj = result;
//                handler.sendMessage(message);
//            }
//        }.start();
//    }



    /**
     * just for test connection
     */
    private void httpTest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MyLog.e(TAG, "run: ");

                String result = HttpUtils.doGet("hi");
                MyLog.e(TAG, "hi->result=" + result);

                result = HttpUtils.doGet("说个笑话");
                MyLog.e(TAG, "说个笑话->result=" + result);

                result = HttpUtils.doGet("天气");
                MyLog.e(TAG, "天气->result=" + result);
            }
        }).start();
    }

    /**
     * just for test datas
     */
    private List<ChatMessage> testData() {
        List<ChatMessage> testData = new ArrayList<ChatMessage>();

        ChatMessage chatMessage1 =
                new ChatMessage("from robot",
                        "",
                        "me",
                        "",
                        ChatMessage.SendType.INCOMING,
                        ChatMessage.SendStatus.SUCCESS,
                        new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24) * 8),
                        "hello",
                        ChatMessage.ContentType.TEXT);


        ChatMessage chatMessage2 =
                new ChatMessage("robot",
                        "",
                        "me reply",
                        "",
                        ChatMessage.SendType.OUTCOMING,
                        ChatMessage.SendStatus.SUCCESS,
                        new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 20) * 8),
                        "hello world",
                        ChatMessage.ContentType.TEXT);


        ChatMessage chatMessage3 =
                new ChatMessage("",
                        "",
                        "",
                        "",
                        ChatMessage.SendType.INCOMING,
                        ChatMessage.SendStatus.SUCCESS,
                        new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24) * 7),
                        "device_2014_08_21_215311",
                        ChatMessage.ContentType.PHOTO);


        ChatMessage chatMessage4 =
                new ChatMessage("",
                        "",
                        "",
                        "",
                        ChatMessage.SendType.OUTCOMING,
                        ChatMessage.SendStatus.SUCCESS,
                        new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 20) * 7),
                        "device_2014_08_21_215311",
                        ChatMessage.ContentType.PHOTO);


        ChatMessage chatMessage5 =
                new ChatMessage("",
                        "",
                        "",
                        "",
                        ChatMessage.SendType.OUTCOMING,
                        ChatMessage.SendStatus.FAIL,
                        new Date(System.currentTimeMillis()),
                        "test send fail",
                        ChatMessage.ContentType.TEXT);

        ChatMessage chatMessage6 =
                new ChatMessage("",
                        "",
                        "",
                        "",
                        ChatMessage.SendType.OUTCOMING,
                        ChatMessage.SendStatus.ONGOING,
                        new Date(System.currentTimeMillis()),
                        "test sending",
                        ChatMessage.ContentType.TEXT);

        testData.add(chatMessage1);
        testData.add(chatMessage2);
        testData.add(chatMessage3);
        testData.add(chatMessage4);
        testData.add(chatMessage5);
        testData.add(chatMessage6);

        return testData;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE_SELECTED_IMAGE
                && resultCode == Constants.RESULT_CODE_SELECTED_IMAGE) {
            Set<String> mSelectedImages = (Set<String>) data.getSerializableExtra(Constants.KEY_SELECTED_IMAGE);

            for (String selectedImgPath : mSelectedImages) {
                ChatMessage toChatFace =
                        new ChatMessage(getResources().getString(R.string.text_title),
                                "",
                                getResources().getString(R.string.text_me),
                                "",
                                ChatMessage.SendType.OUTCOMING,
                                ChatMessage.SendStatus.SUCCESS,
                                new Date(),
                                selectedImgPath,
                                ChatMessage.ContentType.PHOTO);
                MyLog.e(TAG, "selectedImgPath path:" + selectedImgPath);
                mData.add(toChatFace);
            }

            mChatMessageAdapter.notifyDataSetChanged();
            ImageLoaderGridViewAdapter.selectedImages.clear();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - mExitTime > Constants.EXIT_INTERVAL_TIME) {
                ToastUtil.showLongToast(this, R.string.click_back_to_exit);
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onAudioRecorderClick(List<ChatMessage> datas, View view, int position) {
        //停止播放的动画
        if (mViewAnim != null) {
            mViewAnim.setBackgroundResource(R.drawable.audio_recorder_adj);
            mViewAnim = null;
        }

        //播放动画
        mViewAnim = view.findViewById(R.id.view_audio_recorder_anim);
        mViewAnim.setBackgroundResource(R.drawable.anim_audio_recoder_play);
        AnimationDrawable animationDrawable = (AnimationDrawable) mViewAnim.getBackground();
        animationDrawable.start();

        //播放音频
        MediaManager.playSound(((Recorder)datas.get(position).getContent()).getFilePath(), new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mViewAnim.setBackgroundResource(R.drawable.audio_recorder_adj);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        MediaManager.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MediaManager.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaManager.release();
    }
}
