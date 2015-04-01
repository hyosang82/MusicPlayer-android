package kr.hyosang.musicplayer.fragment;

import java.util.ArrayList;
import java.util.List;

import kr.hyosang.android.common.HttpRequest;
import kr.hyosang.android.common.HttpRequest.RequestType;
import kr.hyosang.android.common.HttpResponse;
import kr.hyosang.android.common.HttpThread.IHttpListener;
import kr.hyosang.android.common.Logger;
import kr.hyosang.musicplayer.R;
import kr.hyosang.musicplayer.common.Define;
import kr.hyosang.musicplayer.common.ImageDecoder;
import kr.hyosang.musicplayer.common.ServiceWrapper;
import kr.hyosang.musicplayer.data.AlbumListItem;
import kr.hyosang.musicplayer.data.TrackListItem;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AlbumDetailFragment extends FragmentBase {
    private static final int MSG_UPDATE_INFO = 0x01;
    private static final int MSG_ADD_TRACKS = 0x02;
    private static final int MSG_SHOW_INFO_AREA = 0x03;
    
    private ImageView mAlbumArt;
    private TextView mAlbumTitle;
    private TextView mAlbumArtist;
    private TextView mAlbumReleased;
    private LinearLayout mTrackView;
    private LinearLayout mInfoArea;
    private Button mBtnAddAll;
    private int mAlbumId = 0;
    private AlbumListItem.Type mAlbumType;
    private int mCurrentAlbumId = 0;
    
    private ImageDecoder mDecoder;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_albumdetail, container, false);
        
        mAlbumArt = (ImageView) v.findViewById(R.id.album_art);
        mAlbumTitle = (TextView) v.findViewById(R.id.album_title);
        mAlbumArtist = (TextView) v.findViewById(R.id.album_artist);
        mAlbumReleased = (TextView) v.findViewById(R.id.album_released);
        mTrackView = (LinearLayout) v.findViewById(R.id.tracks);
        mInfoArea = (LinearLayout) v.findViewById(R.id.album_info_area);
        mBtnAddAll = (Button) v.findViewById(R.id.btn_add_all);
        
        mDecoder = ImageDecoder.getInstance();
        
        mBtnAddAll.setOnClickListener(mBtnClicked);
        
        return v;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        boolean bLoad = false;
        
        if(mCurrentAlbumId != mAlbumId) {
            bLoad = true;
        }
        
        bLoad = !isLoading() && bLoad;
        
        if(bLoad) {
            loadData();
        }
    }
    
    public void setAlbumId(AlbumListItem.Type type, int albumId) {
        mAlbumType = type;
        mAlbumId = albumId;
    }
    
    
    public void reload() {
        loadData();
    }
    
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case MSG_UPDATE_INFO:
            {
                JSONObject obj = (JSONObject) msg.obj;
                mAlbumTitle.setText(obj.optString("title"));
                mAlbumArtist.setText(obj.optString("artist"));
                mAlbumReleased.setText(obj.optString("year") + "/" + obj.optString("date"));
            }
            break;
            
            case MSG_ADD_TRACKS:
            {
                //화면에 트랙 추가
                @SuppressWarnings("unchecked")
                ArrayList<TrackListItem> tracks = (ArrayList<TrackListItem>) msg.obj;
                addTracks(tracks);
            }
            break;
            
            case MSG_SHOW_INFO_AREA:
            {
                mInfoArea.setVisibility(msg.arg1==1 ? View.VISIBLE : View.GONE);
            }
            break;
            }
        }
    };
    
    private OnClickListener mBtnClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if(id == R.id.btn_add_all) {
                int trackCount = mTrackView.getChildCount();
                
                for(int i=0;i<trackCount;i++) {
                    View subView = mTrackView.getChildAt(i);
                    if(subView != null) {
                        View addBtn = subView.findViewById(R.id.btn_play_add);
                        if(addBtn != null) {
                            Object tag = addBtn.getTag();
                            if(tag != null) {
                                TrackListItem track = (TrackListItem) tag;
                                ServiceWrapper.getInstance().addPlaylist(track);
                            }
                        }
                    }
                }
                
                ServiceWrapper.getInstance().play();
            }
        }
    };
    
    private void addTracks(List<TrackListItem> tracks) {
        mTrackView.removeAllViewsInLayout();
        
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        
        for(TrackListItem t : tracks) {
            View v = inflater.inflate(R.layout.item_track, mTrackView, false);
            
            TextView title = (TextView) v.findViewById(R.id.track_title);
            title.setText(t.mTrackTitle);
            
            TextView artist = (TextView) v.findViewById(R.id.track_artist);
            artist.setText(t.mTrackArtist);
            
            Button btn = (Button) v.findViewById(R.id.btn_play_add);
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Object tag = v.getTag();
                    if(tag != null) {
                        if(tag instanceof TrackListItem) {
                            TrackListItem item = (TrackListItem) tag;
                            ServiceWrapper.getInstance().addPlaylist(item);
                            ServiceWrapper.getInstance().play();
                            
                            Toast.makeText(getActivity(), "Playlist added", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
            btn.setTag(t);
            
            v.setTag(t);
            
            mTrackView.addView(v, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
    }
    
    
    private void loadData() {
        if(mAlbumType == AlbumListItem.Type.ALBUM) {
            String url = Define.SERVER_URL + Define.URI_ALBUM_DETAIL;
            
            HttpRequest req = new HttpRequest(url, RequestType.POST);
            req.addPostData("albumId", String.valueOf(mAlbumId));
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
                        JSONObject jsonObj = new JSONObject(resp.mResponseData);
                        
                        JSONObject alb = jsonObj.getJSONObject("album");
                        int albId = alb.getInt("aid");
                        mCurrentAlbumId = albId;
                        
                        mDecoder.add(Define.SERVER_URL + Define.URI_ALBUM_ART + alb.optString("aid"), mAlbumArt);
                        
                        Message.obtain(mHandler, MSG_UPDATE_INFO, alb).sendToTarget();
                        
                        JSONArray tracks = jsonObj.getJSONArray("track_list");
                        ArrayList<TrackListItem> tracklist = new ArrayList<TrackListItem>();
                        for(int i=0;i<tracks.length();i++) {
                            JSONObject obj = tracks.optJSONObject(i);
                            
                            TrackListItem item = new TrackListItem();
                            item.mAlbumId = albId;
                            item.mTrackId = obj.getInt("tid");
                            item.mTrackNo = obj.getString("track");
                            item.mTrackTitle = obj.getString("title");
                            item.mTrackArtist = obj.getString("artist");
                            item.mLyrics = obj.getString("lyrics");
                            
                            tracklist.add(item);
                        }
                        
                        Message.obtain(mHandler, MSG_ADD_TRACKS, tracklist).sendToTarget();
                        Message.obtain(mHandler, MSG_SHOW_INFO_AREA, 1, 0).sendToTarget();
                    }catch(JSONException e) {
                        Logger.w(e);
                    }
                    
                    //requestListNotify(mAdapter);
                    
                    setLoadingFlag(false);
                }
            };
            mHttpThread.addRequest(req);
        }else if(mAlbumType == AlbumListItem.Type.PLAYLIST) {
            String url = Define.SERVER_URL + Define.URI_PLAYLIST_DETAIL;
            
            HttpRequest req = new HttpRequest(url, RequestType.POST);
            req.addPostData("playlistId", String.valueOf(mAlbumId));
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
                        JSONObject jsonObj = new JSONObject(resp.mResponseData);
                        JSONObject plsInfo = jsonObj.optJSONObject("playlist");
                        if(plsInfo != null) {
                            String plsId = plsInfo.optString("id");
                            String plsName = plsInfo.optString("name");
                        }
                        
                        JSONArray tracks = jsonObj.getJSONArray("track_list");
                        ArrayList<TrackListItem> tracklist = new ArrayList<TrackListItem>();
                        for(int i=0;i<tracks.length();i++) {
                            JSONObject obj = tracks.optJSONObject(i);
                            
                            TrackListItem item = new TrackListItem();
                            item.mAlbumId = 0;
                            item.mTrackId = obj.getInt("tid");
                            item.mTrackNo = obj.getString("track");
                            item.mTrackTitle = obj.getString("title");
                            item.mTrackArtist = obj.getString("artist");
                            item.mLyrics = obj.getString("lyrics");
                            
                            tracklist.add(item);
                        }
                        
                        Message.obtain(mHandler, MSG_ADD_TRACKS, tracklist).sendToTarget();
                        Message.obtain(mHandler, MSG_SHOW_INFO_AREA, 0, 0).sendToTarget();
                    }catch(JSONException e) {
                        Logger.w(e);
                    }
                        
                        
                        
                }
                
            };
            mHttpThread.addRequest(req);
        }else {
            return;
        }
        
        setLoadingFlag(true);
    }
}
