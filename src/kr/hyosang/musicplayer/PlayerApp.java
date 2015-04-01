package kr.hyosang.musicplayer;

import kr.hyosang.android.common.Logger;
import kr.hyosang.musicplayer.common.ImageDecoder;
import kr.hyosang.musicplayer.common.ServiceWrapper;
import kr.hyosang.musicplayer.service.PlayerService;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;

public class PlayerApp extends Application {
    private Messenger mService = null;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        //Set logger tag
        Logger.init(null, "MusicPlayer");
        ImageDecoder.createInstance(getCacheDir().getAbsolutePath());

        Intent service = new Intent(this, PlayerService.class);
        bindService(service, mConnection, Context.BIND_AUTO_CREATE);
        
        Logger.d("Application created");
    }
    
    @Override
    public void onTerminate() {
        unbindService(mConnection);
        
        super.onTerminate();
    }
    
    
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            
            ServiceWrapper.createInstance(mService);
            
            Logger.d("Service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logger.d("Service disconnected");
        }
    };
    
}
