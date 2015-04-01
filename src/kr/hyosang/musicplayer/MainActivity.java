package kr.hyosang.musicplayer;

import java.util.ArrayList;

import kr.hyosang.android.common.Logger;
import kr.hyosang.musicplayer.adapter.MainViewPagerAdapter;
import kr.hyosang.musicplayer.common.Define;
import kr.hyosang.musicplayer.common.ServiceWrapper;
import kr.hyosang.musicplayer.data.AlbumListItem;
import kr.hyosang.musicplayer.data.MediaPlayerState;
import kr.hyosang.musicplayer.fragment.AlbumDetailFragment;
import kr.hyosang.musicplayer.fragment.AlbumListFragment;
import kr.hyosang.musicplayer.fragment.AlbumListFragment.IAlbumListListener;
import kr.hyosang.musicplayer.fragment.PlayerFragment;
import kr.hyosang.musicplayer.service.PlaylistTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

public class MainActivity extends FragmentActivity implements IAlbumListListener {
    private ViewPager mPager;
    private MainViewPagerAdapter mPagerAdapter;
    private AlbumListFragment mFragAlbumlist;
    private AlbumDetailFragment mFragAlbumdetail;
    private PlayerFragment mFragPlayer;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mPager = (ViewPager) findViewById(R.id.main_pager);
        mPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager());
        
        mFragAlbumlist = new AlbumListFragment(this);
        mFragAlbumdetail = new AlbumDetailFragment();
        mFragPlayer = new PlayerFragment();
        
        Fragment [] pages = new Fragment[]{
                mFragAlbumlist, 
                mFragAlbumdetail,
                mFragPlayer
                };
        
        mPagerAdapter.setFragments(pages);
        
        mPager.setAdapter(mPagerAdapter);
    }
    

    @Override
    protected void onResume() {
        super.onResume();
        
        ServiceWrapper.getInstance().addReceiver(mHandler);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        
        ServiceWrapper.getInstance().removeReceiver(mHandler);
    }
    
    @Override
    public void onAlbumSelected(AlbumListItem item) {
        
        mFragAlbumdetail.setAlbumId(item.mType, item.mAlbumId);
        mFragAlbumdetail.reload();
        
        mPager.setCurrentItem(Define.Page.ALBUM_DETAIL);
    }
    
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            
            switch(msg.what) {
            case Define.Message.PLAYER_STATE_CHANGED:
            {
                b.setClassLoader(MediaPlayerState.class.getClassLoader());
                MediaPlayerState st = (MediaPlayerState) b.get(Define.BUNDLE_DEFAULT_OBJECT);
                
                Logger.d("Player state changed = " + st);
                
                mFragPlayer.playerStateUpdated(st);
            }
            break;
            
            case Define.Message.NOTIFY_TRACK_ID:
                mFragPlayer.onNewTrack(msg.arg1);
                break;
                
            case Define.Message.RESPONSE_PLAYLIST:
            {
                b.setClassLoader(PlaylistTrack.class.getClassLoader());
                ArrayList<PlaylistTrack> playlist = b.getParcelableArrayList(Define.BUNDLE_DEFAULT_OBJECT);
                mFragPlayer.onPlaylistUpdate(playlist);
            }
            break;
            
            case Define.Message.RESPONSE_CURRENT_TRACK:
            {
                b.setClassLoader(PlaylistTrack.class.getClassLoader());
                PlaylistTrack track = (PlaylistTrack) b.get(Define.BUNDLE_DEFAULT_OBJECT);
                if(track != null) {
                    mFragPlayer.onNewTrack(track.mTrackId);
                }
            }
            break;
               
            }
        }
    };
    
}
