package kr.hyosang.musicplayer.adapter;

import java.util.ArrayList;
import java.util.List;

import kr.hyosang.musicplayer.R;
import kr.hyosang.musicplayer.service.PlaylistTrack;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PlaylistListAdapter extends BaseAdapter {
    private Context mContext = null;
    
    private ArrayList<PlaylistTrack> mList = new ArrayList<PlaylistTrack>();

    public PlaylistListAdapter(Context context) {
        super();
        
        mContext = context;
    }
    
    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public PlaylistTrack getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
    
    public void setList(List<PlaylistTrack> list) {
        mList.clear();
        
        if(list != null) {
            if(list.size() > 0) {
                mList.addAll(list);
            }
        }
    }
    
    public void add(PlaylistTrack item) {
        mList.add(item);
    }
    
    public void clear() {
        mList.clear();
    }

    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_play_track, parent, false);
            
            holder = new ViewHolder();
            holder.trackTitle = (TextView) convertView.findViewById(R.id.track_title);
            holder.trackArtist = (TextView) convertView.findViewById(R.id.track_artist);
            
            convertView.setTag(holder);
            
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        PlaylistTrack item = getItem(position);
        
        holder.trackTitle.setText(item.mTrackTitle);
        holder.trackArtist.setText(item.mTrackArtist);
        
        return convertView;
    }
    
    private class ViewHolder {
        TextView trackTitle;
        TextView trackArtist;
    }


}
