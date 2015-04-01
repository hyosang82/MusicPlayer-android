package kr.hyosang.musicplayer.data;

public class AlbumListItem {
    public static enum Type {
        ALBUM("ALB"),
        PLAYLIST("PLS");
        
        private String tp = "";
        
        Type(String t) {
            tp = t;
        }
        
        public static Type make(String id) {
            if("ALB".equals(id)) {
                return Type.ALBUM;
            }else if("PLS".equals(id)) {
                return Type.PLAYLIST;
            }
            
            return null;
        }
        
        public String getId() {
            return tp;
        }
    };
    
    public int mAlbumId;
    public String mAlbumTitle;
    public String mAlbumArtist;
    public String mYear;
    public String mDate;
    public int mTagCount;
    public Type mType;

}
