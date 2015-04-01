package kr.hyosang.musicplayer.common;


public class Define {
    public static final String BUNDLE_DEFAULT_OBJECT = "_object";
    
    public static final String SERVER_URL = "http://hyosang.asuscomm.com:8001/musicdb";
    
    public static final String URI_LIST_DATA = "/data/listData.php";
    public static final String URI_ALBUM_DETAIL = "/data/albumDetail.php";
    public static final String URI_PLAYLIST_DETAIL = "/data/playlistDetail.php";
    public static final String URI_PLAYLIST = "/data/playlist.php";
    public static final String URI_ALBUM_ART = "/data/albumart.php?albumId=";
    public static final String URI_INFO_DATA = "/data/info.php";
    public static final String URI_ALBUM_CATEGORY = "/data/category.php";
    
    public static class Page {
        public static final int ALBUM_LIST = 0;
        public static final int ALBUM_DETAIL = 1;
    }
    
    public static class Message {
        public static final int REGISTER_CLIENT = 0x01;
        public static final int PLAYER_STATE_CHANGED = 0x02;
        public static final int ADD_PLAYITEM = 0x03;
        public static final int PLAY = 0x04;
        public static final int PAUSE = 0x05;
        public static final int PREV = 0x06;
        public static final int NEXT = 0x07;
        public static final int REQUEST_CURRENT_TRACK = 0x08;
        public static final int RESPONSE_CURRENT_TRACK = 0x09;
        public static final int NOTIFY_TRACK_ID = 0x0A;
        public static final int REQUEST_PLAYLIST = 0x0B;
        public static final int RESPONSE_PLAYLIST = 0x0C;
        

    }

}
