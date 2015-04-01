package kr.hyosang.musicplayer.fragment;

import kr.hyosang.android.common.HttpThread;
import kr.hyosang.musicplayer.MainActivity;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.widget.BaseAdapter;

public abstract class FragmentBase extends Fragment {
    private static final int MSG_LIST_NOTIFY = 0x01;
    
    protected MainActivity mActivity = null;
    protected boolean mbLoading = false;
    protected HttpThread mHttpThread = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mHttpThread = HttpThread.getInstance();
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        mActivity = (MainActivity) activity;
    }
    
    protected void setLoadingFlag(boolean loading) {
        mbLoading = loading;
    }
    
    protected boolean isLoading() {
        return mbLoading;
    }
    
    protected void requestListNotify(BaseAdapter adapter) {
        Message.obtain(mHandler, MSG_LIST_NOTIFY, adapter).sendToTarget();
    }
    
    
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case MSG_LIST_NOTIFY:
            {
                ((BaseAdapter) msg.obj).notifyDataSetChanged();
            }
            break;
            }
            
            
        }
    };

}
