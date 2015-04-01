package kr.hyosang.musicplayer.service;

import java.io.IOException;
import java.util.ArrayList;

import kr.hyosang.android.common.Logger;
import kr.hyosang.musicplayer.R;
import kr.hyosang.musicplayer.common.Define;
import kr.hyosang.musicplayer.data.MediaPlayerState;
import kr.hyosang.musicplayer.data.TrackListItem;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.widget.Toast;

public class PlayerService extends Service {
    private static final int ID_NOTIFY = 0x01;
    
    private NotificationManager mNotiManager = null;
    private MediaPlayer mPlayer = null;
    private ArrayList<PlaylistTrack> mPlaylist = new ArrayList<PlaylistTrack>();
    private MediaPlayerState mPlayerState = MediaPlayerState.IDLE;
    private PlaylistTrack mCurrentPlaying = null;
    private Messenger mClient = null;
    
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        mNotiManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        Notification notification = (new Notification.Builder(this))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentText("Service started")
                .getNotification();
        
        mNotiManager.notify(ID_NOTIFY, notification);
        
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                setPlayerState(MediaPlayerState.PLAYING);
            }
        });
        mPlayer.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Logger.d("Player completed");
                
                mCurrentPlaying.mbPlayed = true;
                mPlayer.reset();
                setPlayerState(MediaPlayerState.IDLE);
                
                //다음 트랙
                PlaylistTrack next = getNextPlayItem();
                if(next != null) {
                    onPlay();
                }else {
                    mCurrentPlaying = null;
                }
            }
        });
        
        setPlayerState(MediaPlayerState.IDLE);
    }
    
    @Override
    public void onDestroy() {
        mNotiManager.cancel(ID_NOTIFY);
        
        Toast.makeText(this, "Player Service Stopped.", Toast.LENGTH_SHORT).show();

        super.onDestroy();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
    
    private void setPlayerState(MediaPlayerState state) {
        mPlayerState = state;
        
        sendClient(Define.Message.PLAYER_STATE_CHANGED, state, 0, 0);
        
        Logger.d("MediaPlayer state = " + state);
    }
    
    private void sendClient(int what, Parcelable obj, int arg1, int arg2) {
        Logger.d("CLIENT = " + mClient);
        if(mClient != null) {
            Message msg = Message.obtain();
            msg.what = what;
            msg.arg1 = arg1;
            msg.arg2 = arg2;
            msg.replyTo = mMessenger;
            
            if(obj != null) {
                Bundle b = msg.getData();
                
                if(obj instanceof Bundle) {
                    b.putAll((Bundle) obj);
                }else {
                    b.putParcelable(Define.BUNDLE_DEFAULT_OBJECT, obj);
                }
                msg.setData(b);
            }
            
            try {
                mClient.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void onPlay() {
        switch(mPlayerState) {
        case IDLE:
        case STOPPED:
        {
            //새 트랙 재생
            PlaylistTrack item = getNextPlayItem();
            changeTrack(item);
        }
        break;
        
        case PREPARING:     //MediaPlayer 준비중
        case PLAYING:       //재생중
            break;
            
        case PAUSED:
            //일시중지. 재생
            mPlayer.start();
            setPlayerState(MediaPlayerState.PLAYING);
            break;
        }
    }
    
    public void onNext() {
        if(mCurrentPlaying != null) {
            mCurrentPlaying.mbPlayed = true;
        }
        
        changeTrack(getNextPlayItem());
    }
    
    public void onPrev() {
        changeTrack(getPrevPlayItem());
        
    }
    
    private void changeTrack(PlaylistTrack item) {
        if(item != null) {
            if(mPlayer.isPlaying()) {
                mPlayer.stop();
            }
            
            mPlayer.reset();

            try {
                mPlayer.setDataSource(Define.SERVER_URL + "/getmedia.php?trackId=" + item.mTrackId);
                mPlayer.prepareAsync();
                
                setPlayerState(MediaPlayerState.PREPARING);
                mCurrentPlaying = item;
                
                sendClient(Define.Message.NOTIFY_TRACK_ID, null, item.mTrackId, 0);
            }catch(IOException e) {
                Logger.w(e);
            }
        }
    }
        
    private PlaylistTrack getNextPlayItem() {
        if(mPlaylist.size() > 0) {
            for(int i=0;i<mPlaylist.size();i++) {
                PlaylistTrack t = mPlaylist.get(i);
                if(!t.mbPlayed) {
                    return t;
                }
            }
        }
        
        return null;
    }
    
    private PlaylistTrack getPrevPlayItem() {
        if(mCurrentPlaying != null) {
            if(mPlaylist.size() > 0) {
                for(int i=0;i<mPlaylist.size();i++) {
                    if(mCurrentPlaying.equals(mPlaylist.get(i))) {
                        if(i == 0) return null;
                        else return mPlaylist.get(i-1);
                    }
                }
            }
        }
        
        return null;
    }
                
                
    
    private Object getDataFromBundle(Bundle b, Class<?> T) {
        b.setClassLoader(T.getClassLoader());
        return b.get(Define.BUNDLE_DEFAULT_OBJECT);
    }
    
    private Handler mIncomingHandler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            
            switch(msg.what) {
            case Define.Message.REGISTER_CLIENT:
            {
                mClient = (Messenger) getDataFromBundle(b, Messenger.class);
            }
            break;
            
            case Define.Message.ADD_PLAYITEM:
            {
                mPlaylist.add(PlaylistTrack.from((TrackListItem) getDataFromBundle(b, TrackListItem.class)));
                responsePlaylist();
            }
            break;
            
            case Define.Message.PLAY:
            {
                onPlay();
            }
            break;
            
            case Define.Message.PAUSE:
            {
                mPlayer.pause();
                setPlayerState(MediaPlayerState.PAUSED);
            }
            break;
            
            case Define.Message.NEXT:
            {
                onNext();
            }
            break;
            
            case Define.Message.PREV:
            {
                onPrev();
            }
            break;
            
            case Define.Message.REQUEST_PLAYLIST:
            {
                responsePlaylist();
            }
            break;
            
            case Define.Message.REQUEST_CURRENT_TRACK:
            {
                responseCurrentTrack();
            }
            break;
            
            }
            
        }
        
    };
    private Messenger mMessenger = new Messenger(mIncomingHandler);
    
    
    private void responsePlaylist() {
        Bundle b = new Bundle();
        b.putParcelableArrayList(Define.BUNDLE_DEFAULT_OBJECT, mPlaylist);
        sendClient(Define.Message.RESPONSE_PLAYLIST, b, 0, 0);
    }
    
    private void responseCurrentTrack() {
        sendClient(Define.Message.RESPONSE_CURRENT_TRACK, mCurrentPlaying, 0, 0);
    }
}
