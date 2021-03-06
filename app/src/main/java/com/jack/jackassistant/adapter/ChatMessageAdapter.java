package com.jack.jackassistant.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jack.jackassistant.R;
import com.jack.jackassistant.bean.ChatMessage;
import com.jack.jackassistant.bean.Recorder;
import com.jack.jackassistant.util.Constants;
import com.jack.jackassistant.util.ScreenUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by xiaofeng on 2017/3/12.
 */

public class ChatMessageAdapter extends BaseAdapter {

    private static final String AUDIO_RECORDER_TIME_SUFFIX = "\"";
    private static final float MIN_ITEM_WIDTH_PERCENT = 0.15f;
    private static final float MAX_ITEM_WIDTH_PERCENT = 0.7f;

    private List<ChatMessage> mData;
    private LayoutInflater mInflater;
    private Context mContext;

    private int mMinItemWidth;
    private int mMaxItemWidth;

    private OnChatMessageItemClickListener mOnChatMessageItemClickListener;

    public interface OnChatMessageItemClickListener {
        void onAudioRecorderClick(List<ChatMessage> datas, View view, int position);
    }

    public void setOnChatMessageItemClickListener(OnChatMessageItemClickListener listener) {
        this.mOnChatMessageItemClickListener = listener;
    }

    public ChatMessageAdapter(Context context, List<ChatMessage> data) {
        this.mData = data;
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
        mMinItemWidth = (int) (ScreenUtil.getWindowsWidth((Activity) context) * MIN_ITEM_WIDTH_PERCENT);
        mMaxItemWidth = (int) (ScreenUtil.getWindowsWidth((Activity) context) * MAX_ITEM_WIDTH_PERCENT);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ChatMessage chatMessage = mData.get(position);
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            if (getItemViewType(position) == 0) {
                convertView = mInflater.inflate(R.layout.item_from_msg, parent, false);

            } else {
                convertView = mInflater.inflate(R.layout.item_to_msg, parent, false);
            }

            holder.sendDateTextView = (TextView) convertView.findViewById(R.id.sendDateTextView);

            holder.userAvatarImageView = (ImageView) convertView.findViewById(R.id.userAvatarImageView);
            holder.userNameTextView = (TextView) convertView.findViewById(R.id.userNameTextView);

            holder.sendContentTextView = (TextView) convertView.findViewById(R.id.sendContentTextView);
            holder.sendPhotoImageView = (ImageView) convertView.findViewById(R.id.sendPhotoImageView);
            holder.sendFaceImageView = (ImageView) convertView.findViewById(R.id.sendFaceImageView);

            holder.sendFailImageView = (ImageView) convertView.findViewById(R.id.sendFailImageView);
            holder.isSendingProgressBar = (ProgressBar) convertView.findViewById(R.id.isSendingProgressBar);

            holder.audioRecorderTime = (TextView) convertView.findViewById(R.id.tv_audio_recorder_time);
            holder.audioRecorderLength = convertView.findViewById(R.id.fl_audio_recorder_length);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateString = format.format(chatMessage.getDate());
            String[] date = dateString.split(" ");

            if (position == 0) {
                holder.sendDateTextView.setVisibility(View.VISIBLE);
                holder.sendDateTextView.setText(date[0]);
            } else {
                if (isSameDay(chatMessage.getDate(), mData.get(position - 1).getDate())) {
                    holder.sendDateTextView.setVisibility(View.GONE);

                } else {
                    holder.sendDateTextView.setVisibility(View.VISIBLE);
                    if (isSameDay(chatMessage.getDate(), new Date())) {
                        holder.sendDateTextView.setText(date[1]);
                    } else {
                        holder.sendDateTextView.setText(date[0]);
                    }
                }
            }



        } catch (Exception e) {
            e.printStackTrace();
        }

        if (getItemViewType(position) == 0) {
            String fromName = ( chatMessage.getFromName() == null || chatMessage.getFromName().isEmpty() ) ? mContext.getResources().getString(R.string.text_title) : chatMessage.getFromName();
            holder.userNameTextView.setText(fromName);
        } else {
            String toName = ( chatMessage.getFromName() == null || chatMessage.getToName().isEmpty() ) ? mContext.getResources().getString(R.string.text_me) : chatMessage.getToName();
            holder.userNameTextView.setText(toName);
        }

        if (chatMessage.getContentType() == null || chatMessage.getContentType() == ChatMessage.ContentType.TEXT) {
            holder.sendContentTextView.setVisibility(View.VISIBLE);
            holder.sendPhotoImageView.setVisibility(View.GONE);
            holder.sendFaceImageView.setVisibility(View.GONE);
            holder.audioRecorderLength.setVisibility(View.GONE);

            holder.sendContentTextView.setText((String) chatMessage.getContent());

        } else if (chatMessage.getContentType() == ChatMessage.ContentType.PHOTO) {
            holder.sendContentTextView.setVisibility(View.GONE);
            holder.sendPhotoImageView.setVisibility(View.VISIBLE);
            holder.sendFaceImageView.setVisibility(View.GONE);
            holder.audioRecorderLength.setVisibility(View.GONE);

            Glide.with(mContext)
                    .load(chatMessage.getContent())
                    .into(holder.sendPhotoImageView);

        } else if (chatMessage.getContentType() == ChatMessage.ContentType.FACE) {
            holder.sendContentTextView.setVisibility(View.GONE);
            holder.sendPhotoImageView.setVisibility(View.GONE);
            holder.sendFaceImageView.setVisibility(View.VISIBLE);
            holder.audioRecorderLength.setVisibility(View.GONE);

            int resId = mContext.getResources().getIdentifier((String) chatMessage.getContent(), Constants.TYPE_DRAWABLE, mContext.getPackageName());
            holder.sendFaceImageView.setImageResource(resId);
        } else if (chatMessage.getContentType() == ChatMessage.ContentType.RECORDER) {
            holder.sendContentTextView.setVisibility(View.GONE);
            holder.sendPhotoImageView.setVisibility(View.GONE);
            holder.sendFaceImageView.setVisibility(View.GONE);
            holder.audioRecorderLength.setVisibility(View.VISIBLE);

            holder.audioRecorderLength.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnChatMessageItemClickListener != null) {
                        mOnChatMessageItemClickListener.onAudioRecorderClick(mData, view, position);
                    }
                }
            });

            ViewGroup.LayoutParams layoutParams = holder.audioRecorderLength.getLayoutParams();
            layoutParams.width = (int) (mMinItemWidth + ((Recorder)(chatMessage.getContent())).getTime() / 60f * mMaxItemWidth);
        }

        if (chatMessage.getSendStatus() == null || chatMessage.getSendStatus() == ChatMessage.SendStatus.SUCCESS) {
            holder.sendFailImageView.setVisibility(View.GONE);
            holder.isSendingProgressBar.setVisibility(View.GONE);
            if (chatMessage.getContentType() == ChatMessage.ContentType.RECORDER) {
                holder.audioRecorderTime.setVisibility(View.VISIBLE);
                holder.audioRecorderTime.setText(Math.round(((Recorder)(chatMessage.getContent())).getTime()) + AUDIO_RECORDER_TIME_SUFFIX);
            } else {
                holder.audioRecorderTime.setVisibility(View.GONE);
            }

        } else if (chatMessage.getSendStatus() == ChatMessage.SendStatus.FAIL) {
            holder.sendFailImageView.setVisibility(View.VISIBLE);
            holder.isSendingProgressBar.setVisibility(View.GONE);

        } else if (chatMessage.getSendStatus() == ChatMessage.SendStatus.ONGOING) {
            holder.sendFailImageView.setVisibility(View.GONE);
            holder.isSendingProgressBar.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return ChatMessage.SendType.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage chatMessage = mData.get(position);
        if (chatMessage.getSendType() == ChatMessage.SendType.INCOMING) {
            return 0;
        }
        return 1;
    }

    public class ViewHolder {
        TextView sendDateTextView;

        ImageView userAvatarImageView;
        TextView userNameTextView;

        TextView sendContentTextView;
        ImageView sendPhotoImageView;
        ImageView sendFaceImageView;

        ImageView sendFailImageView;
        ProgressBar isSendingProgressBar;

        TextView audioRecorderTime;
        View audioRecorderLength;

    }

    public boolean isSameDay(Date preDate, Date date) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(preDate);
        int preYear = calendar.get(Calendar.YEAR);
        int preDay = calendar.get(Calendar.DAY_OF_YEAR);

        calendar.setTime(date);
        int currentYear = calendar.get(Calendar.YEAR);
        int currentDay = calendar.get(Calendar.DAY_OF_YEAR);

        if (preYear == currentYear && preDay == currentDay) {
            return true;
        }

        return false;
    }

}
