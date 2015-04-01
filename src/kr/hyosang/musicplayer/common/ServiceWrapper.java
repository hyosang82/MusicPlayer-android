package kr.hyosang.musicplayer.common;

import java.util.ArrayList;

import kr.hyosang.android.common.Logger;
import kr.hyosang.musicplayer.data.TrackListItem;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;

public class ServiceWrapper {
    private static ServiceWrapper mInstance = null;
    private ArrayList<Handler> mReceivers = new ArrayList<Handler>();
    private Messenger mService = null;
    private Messenger mMessenger = new Messenger(new Handler() {
        public void handleMessage(Message msg) {
            //각 핸들러로 전달
            for(Handler h : mReceivers) {
                Message newmsg = Message.obtain(msg);
                newmsg.setTarget(h);
                newmsg.sendToTarget();
            }
        }
    });
    
    private ServiceWrapper() {
        //block default constructor
    }

    public static ServiceWrapper createInstance(Messenger service) {
        if(mInstance == null) {
            mInstance = new ServiceWrapper();
        }
        
        mInstance.mService = service;
        mInstance.send(Define.Message.REGISTER_CLIENT, mInstance.mMessenger, 0, 0);
        
        return mInstance;
    }
    
    public static ServiceWrapper getInstance() {
        if(mInstance == null) {
            mInstance = new ServiceWrapper();
        }
        
        return mInstance;
    }
    
    private void send(int what, Parcelable obj, int arg1, int arg2) {
        if(mService != null) {
            Message msg = Message.obtain();
            msg.what = what;
            msg.arg1 = arg1;
            msg.arg2 = arg2;
            msg.replyTo = mMessenger;
            
            if(obj != null) {
                Bundle b = msg.getData();
                b.putParcelable(Define.BUNDLE_DEFAULT_OBJECT, obj);
                msg.setData(b);
            }
            
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                Logger.w(e);
            }
        }
    }
    
    public void requestCurrentTrackId() {
        send(Define.Message.REQUEST_CURRENT_TRACK, null, 0, 0);
    }
    
    public void requestPlaylist() {
        send(Define.Message.REQUEST_PLAYLIST, null, 0, 0);
    }
        
    public void addPlaylist(TrackListItem track) {
        send(Define.Message.ADD_PLAYITEM, track, 0, 0);
    }
    
    public void play() {
        send(Define.Message.PLAY, null, 0, 0);
    }
    
    public void pause() {
        send(Define.Message.PAUSE, null, 0, 0);
    }
    
    public void next() {
        send(Define.Message.NEXT, null, 0, 0);
    }
    
    public void prev() {
        send(Define.Message.PREV, null, 0, 0);
    }
   
    
    public void addReceiver(Handler h) {
        if(!mReceivers.contains(h)) {
            mReceivers.add(h);
        }
    }
    
    public void removeReceiver(Handler h) {
        mReceivers.remove(h);
    }

}
