package kr.hyosang.musicplayer.adapter;

import java.util.ArrayList;

import kr.hyosang.musicplayer.R;
import kr.hyosang.musicplayer.common.Define;
import kr.hyosang.musicplayer.common.ImageDecoder;
import kr.hyosang.musicplayer.data.AlbumListItem;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AlbumListAdapter extends BaseAdapter {
    private Context mContext = null;
    private ImageDecoder mDecoder = null;
    
    private ArrayList<AlbumListItem> mList = new ArrayList<AlbumListItem>();

    public AlbumListAdapter(Context context) {
        super();
        
        mContext = context;
        mDecoder = ImageDecoder.getInstance();
    }
    
    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public AlbumListItem getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
    
    public void add(AlbumListItem item) {
        mList.add(item);
    }
    
    public void clear() {
        mList.clear();
    }

    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_album, parent, false);
            
            holder = new ViewHolder();
            holder.albumart = (ImageView) convertView.findViewById(R.id.album_art);
            holder.albumArtist = (TextView) convertView.findViewById(R.id.album_artist);
            holder.albumTitle = (TextView) convertView.findViewById(R.id.album_title);
            
            convertView.setTag(holder);
            
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        AlbumListItem item = getItem(position);
        
        holder.albumTitle.setText(item.mAlbumTitle);
        holder.albumArtist.setText(item.mAlbumArtist);
        
        mDecoder.add(Define.SERVER_URL + Define.URI_ALBUM_ART + item.mAlbumId, holder.albumart);
        
        return convertView;
    }
    
    private class ViewHolder {
        ImageView albumart;
        TextView albumTitle;
        TextView albumArtist;
    }


}
