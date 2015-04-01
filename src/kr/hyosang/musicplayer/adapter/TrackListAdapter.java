package kr.hyosang.musicplayer.adapter;

import java.util.ArrayList;

import kr.hyosang.musicplayer.R;
import kr.hyosang.musicplayer.R.id;
import kr.hyosang.musicplayer.R.layout;
import kr.hyosang.musicplayer.data.AlbumListItem;
import kr.hyosang.musicplayer.data.TrackListItem;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TrackListAdapter extends BaseAdapter {
    private Context mContext = null;
    
    private ArrayList<TrackListItem> mList = new ArrayList<TrackListItem>();

    public TrackListAdapter(Context context) {
        super();
        
        mContext = context;
    }
    
    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public TrackListItem getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
    
    public void add(TrackListItem item) {
        mList.add(item);
    }
    
    public void clear() {
        mList.clear();
    }

    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_track, parent, false);
            
            holder = new ViewHolder();
            holder.trackTitle = (TextView) convertView.findViewById(R.id.track_title);
            
            convertView.setTag(holder);
            
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        TrackListItem item = getItem(position);
        
        holder.trackTitle.setText(item.mTrackTitle);
        
        
        return convertView;
    }
    
    private class ViewHolder {
        TextView trackTitle;
    }


}
