package kr.hyosang.musicplayer.data;

public class CategoryData {
    public String value;
    public String text;
    
    public CategoryData(String v, String t) {
        value = v;
        text = t;
    }
    
    @Override
    public String toString() {
        return text;
    }

}
