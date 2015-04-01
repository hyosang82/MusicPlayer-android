package kr.hyosang.musicplayer.data;

import android.os.Parcel;
import android.os.Parcelable;

public class TrackListItem implements Parcelable {
    public int mAlbumId;
    public int mTrackId;
    public int mOrdering;
    public String mTrackNo;
    public String mTrackTitle;
    public String mTrackArtist;
    public String mLyrics;
    
    public static final Parcelable.Creator<TrackListItem> CREATOR = new Parcelable.Creator<TrackListItem>() {

        @Override
        public TrackListItem createFromParcel(Parcel source) {
            TrackListItem item = new TrackListItem();
            item.mAlbumId = source.readInt();
            item.mTrackId = source.readInt();
            item.mOrdering = source.readInt();
            item.mTrackNo = source.readString();
            item.mTrackTitle = source.readString();
            item.mTrackArtist = source.readString();
            item.mLyrics = source.readString();
            
            return item;
        }

        @Override
        public TrackListItem[] newArray(int size) {
            return new TrackListItem[size];
        }
    };
    
    
    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mAlbumId);
        dest.writeInt(mTrackId);
        dest.writeInt(mOrdering);
        dest.writeString(mTrackNo);
        dest.writeString(mTrackTitle);
        dest.writeString(mTrackArtist);
        dest.writeString(mLyrics);
    }
}
