package kr.hyosang.musicplayer.adapter;

import kr.hyosang.musicplayer.data.CategoryData;
import android.content.Context;
import android.widget.ArrayAdapter;

public class CategorySpinnerAdapter extends ArrayAdapter<CategoryData> {

    public CategorySpinnerAdapter(Context context) {
        super(context, android.R.layout.select_dialog_item);
    }

}

