package kr.hyosang.musicplayer.data;

import android.os.Parcel;
import android.os.Parcelable;

public enum MediaPlayerState implements Parcelable {
    IDLE, 
    PREPARING, 
    PLAYING, 
    PAUSED, 
    STOPPED
    ;
    
    public static final int STATE_IDLE = 0x01;
    public static final int STATE_PREPARING = 0x02;
    public static final int STATE_PLAYING = 0x03;
    public static final int STATE_PAUSED = 0x04;
    public static final int STATE_STOPPED = 0x05;
    
    
    public static MediaPlayerState from(int val) {
        switch(val) {
        case STATE_IDLE: return IDLE;
        case STATE_PREPARING: return PREPARING;
        case STATE_PLAYING: return PLAYING;
        case STATE_PAUSED: return PAUSED;
        case STATE_STOPPED: return STOPPED;
        }
        
        return IDLE;
    }
    
    public int toInt() {
        switch(this) {
        case IDLE: return STATE_IDLE;
        case PREPARING: return STATE_PREPARING;
        case PLAYING: return STATE_PLAYING;
        case PAUSED: return STATE_PAUSED;
        case STOPPED: return STATE_STOPPED;
        }
        
        return STATE_IDLE;
    }
    

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(toInt());
    }
    
    public static final Parcelable.Creator<MediaPlayerState> CREATOR = new Parcelable.Creator<MediaPlayerState>() {

        @Override
        public MediaPlayerState createFromParcel(Parcel source) {
            int v = source.readInt();
            return from(v);
        }


        @Override
        public MediaPlayerState[] newArray(int size) {
            return new MediaPlayerState[size];
        }

        
    };

    
};
