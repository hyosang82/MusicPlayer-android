package kr.hyosang.musicplayer.service;

import android.os.Parcel;
import android.os.Parcelable;
import kr.hyosang.musicplayer.data.TrackListItem;

public class PlaylistTrack extends TrackListItem implements Parcelable {
    public boolean mbPlayed = false;
    
    public static PlaylistTrack from(TrackListItem track) {
        PlaylistTrack t = new PlaylistTrack();
        t.mAlbumId = track.mAlbumId;
        t.mLyrics = track.mLyrics;
        t.mOrdering = track.mOrdering;
        t.mTrackArtist = track.mTrackArtist;
        t.mTrackId = track.mTrackId;
        t.mTrackNo = track.mTrackNo;
        t.mTrackTitle = track.mTrackTitle;
        
        return t;
    }
    
    public static final Parcelable.Creator<PlaylistTrack> CREATOR = new Parcelable.Creator<PlaylistTrack>() {

        @Override
        public PlaylistTrack createFromParcel(Parcel source) {
            PlaylistTrack item = PlaylistTrack.from(TrackListItem.CREATOR.createFromParcel(source));
            item.mbPlayed = (source.readInt() == 1);
            
            return item;
        }

        @Override
        public PlaylistTrack[] newArray(int size) {
            return new PlaylistTrack[size];
        }
    };
    
    
    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(mbPlayed ? 1 : 0);
    }

}
