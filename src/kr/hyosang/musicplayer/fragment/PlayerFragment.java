package kr.hyosang.musicplayer.fragment;

import java.util.List;

import kr.hyosang.android.common.HttpRequest;
import kr.hyosang.android.common.HttpRequest.RequestType;
import kr.hyosang.android.common.HttpResponse;
import kr.hyosang.android.common.HttpThread.IHttpListener;
import kr.hyosang.android.common.Logger;
import kr.hyosang.musicplayer.R;
import kr.hyosang.musicplayer.adapter.PlaylistListAdapter;
import kr.hyosang.musicplayer.common.Define;
import kr.hyosang.musicplayer.common.ImageDecoder;
import kr.hyosang.musicplayer.common.ServiceWrapper;
import kr.hyosang.musicplayer.data.MediaPlayerState;
import kr.hyosang.musicplayer.service.PlaylistTrack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PlayerFragment extends FragmentBase {
    private static final int MSG_INFO_RECEIVED = 0x01;
    private static final int MSG_SHOW_TOAST = 0x02;
    
    private Button mBtnPrev;
    private Button mBtnNext;
    private Button mBtnPlayPause;
    private TextView mArtist;
    private TextView mTitle;
    private LinearLayout mPlaylistCheckLayout;
    private ImageView mAlbumart;
    private ListView mPlaylist;
    
    private PlaylistListAdapter mListAdapter;
    private ImageDecoder mDecoder;
    
    private MediaPlayerState mCurrentState = MediaPlayerState.IDLE;
    
    private int mCheckboxTrackId = 0;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_player, container, false);
        
        mBtnPrev = (Button) v.findViewById(R.id.btn_prev);
        mBtnNext = (Button) v.findViewById(R.id.btn_next);
        mBtnPlayPause = (Button) v.findViewById(R.id.btn_playpause);
        mArtist = (TextView) v.findViewById(R.id.tv_artist);
        mTitle = (TextView) v.findViewById(R.id.tv_title);
        mPlaylistCheckLayout = (LinearLayout) v.findViewById(R.id.playlist_check_layout);
        mAlbumart = (ImageView) v.findViewById(R.id.albumart);
        mPlaylist = (ListView) v.findViewById(R.id.lv_playlist);
        
        updatePlayPauseButton();
        
        mDecoder = ImageDecoder.getInstance();
        mListAdapter = new PlaylistListAdapter(mActivity);
        mPlaylist.setAdapter(mListAdapter);
        
        mBtnPlayPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(mCurrentState) {
                case IDLE:
                case PAUSED:
                case STOPPED:
                    ServiceWrapper.getInstance().play();
                    break;
                    
                case PREPARING:
                    //do nothing
                    break;
                    
                case PLAYING:
                    //pause
                    ServiceWrapper.getInstance().pause();
                    break;
                }
            }
        });
        
        mBtnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ServiceWrapper.getInstance().next();
            }
        });
        
        mBtnPrev.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ServiceWrapper.getInstance().prev();
            }
        });
        
        return v;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        ServiceWrapper.getInstance().requestPlaylist();
        ServiceWrapper.getInstance().requestCurrentTrackId();
    }
    
    private void requestInfo(int trackId) {
        String url = Define.SERVER_URL + Define.URI_INFO_DATA;
        String param = String.format("act=track_info&trackId=%d", trackId);
        url = url + "?" + param;
        
        HttpRequest req = new HttpRequest(url, RequestType.GET);
        req.mListener = new IHttpListener() {
            @Override
            public void onError(int errorCode, HttpRequest req) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onError(int errorCode, HttpResponse resp) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onComplete(HttpResponse resp) {
                try {
                    Logger.d("RESP :  " + resp.mResponseData);
                    JSONObject json = new JSONObject(resp.mResponseData);
                    Message.obtain(mHandler, MSG_INFO_RECEIVED, json).sendToTarget();
                }catch(JSONException e) {
                    Logger.w(e);
                }
            }
        };
        mHttpThread.addRequest(req);
    }
    
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if(msg.what == MSG_INFO_RECEIVED) {
                JSONObject json = (JSONObject) msg.obj;
                
                JSONObject track = json.optJSONObject("track_info");
                if(track != null) {
                    mArtist.setText(track.optString("title"));
                    mTitle.setText(track.optString("artist"));
                    mCheckboxTrackId = track.optInt("trackId");
                    mDecoder.add(Define.SERVER_URL + Define.URI_ALBUM_ART + track.optString("albumId"), mAlbumart);
                }
                
                JSONArray playlist = json.optJSONArray("playlist");
                //clear checkbox
                mPlaylistCheckLayout.removeAllViews();
                if(playlist != null) {
                    for(int i=0;i<playlist.length();i++) {
                        JSONObject obj = playlist.optJSONObject(i);
                        String id = obj.optString("id");
                        String name = obj.optString("name");
                        boolean added = (obj.optInt("added") == 1);
                        
                        addCheckbox(id, name, added);
                    }
                }
            }else if(msg.what == MSG_SHOW_TOAST) {
                Toast.makeText(mActivity, (String)msg.obj, Toast.LENGTH_SHORT).show();
            }
        }
    };
    
    private void addCheckbox(String plid,  String name, boolean added) {
        CheckBox chk = new CheckBox(mActivity);
        chk.setText(name);
        chk.setTag(plid);
        chk.setChecked(added);
        chk.setOnCheckedChangeListener(mCheckChanged);
        
        mPlaylistCheckLayout.addView(chk, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }
    
    public void onPlaylistUpdate(List<PlaylistTrack> playlist) {
        mListAdapter.setList(playlist);
        mListAdapter.notifyDataSetChanged();
    }
    
    public void onNewTrack(int trackId) {
        requestInfo(trackId);
    }
    
    public void playerStateUpdated(MediaPlayerState state) {
        mCurrentState = state;
        
        Logger.d("Player state updated = " + state);
        
        updatePlayPauseButton();
    }
    
    private void updatePlayPauseButton() {
        if(mBtnPlayPause == null) return;
        
        String btnStr = "PLAY";
                
        switch(mCurrentState) {
        case IDLE:
            btnStr = "PLAY";
            break;
            
        case PREPARING:
            btnStr = "PAUSE";
            break;
            
        case PLAYING:
            btnStr = "PAUSE";
            break;
            
        case PAUSED:
            btnStr = "PLAY";
            break;
            
        case STOPPED:
            btnStr = "PLAY";
            break;
        
        }
        
        mBtnPlayPause.setText(btnStr);

    }
    
    private OnCheckedChangeListener mCheckChanged = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton v, boolean isChecked) {
            if(isChecked) {
                if(v.getTag() instanceof String) {
                    String playlistId = (String) v.getTag();
                    
                    String url = Define.SERVER_URL + Define.URI_PLAYLIST;
                    
                    
                    HttpRequest req = new HttpRequest(url, RequestType.POST);
                    req.addPostData("mode", "add");
                    req.addPostData("playlistId", playlistId);
                    req.addPostData("trackId", String.valueOf(mCheckboxTrackId));
                    req.mListener = new IHttpListener() {
                        @Override
                        public void onError(int errorCode, HttpRequest req) {
                        }
    
                        @Override
                        public void onError(int errorCode, HttpResponse resp) {
                        }
    
                        @Override
                        public void onComplete(HttpResponse resp) {
                            Message.obtain(mHandler, MSG_SHOW_TOAST, resp.mResponseData).sendToTarget();
                            requestInfo(mCheckboxTrackId);
                        }
                    };
                    
                    mHttpThread.addRequest(req);
                }
            }
            
        }
    };

}
