package kr.hyosang.musicplayer.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import kr.hyosang.android.common.HttpRequest;
import kr.hyosang.android.common.HttpRequest.RequestType;
import kr.hyosang.android.common.HttpResponse;
import kr.hyosang.android.common.HttpThread.IHttpListener;
import kr.hyosang.android.common.Logger;
import kr.hyosang.musicplayer.R;
import kr.hyosang.musicplayer.adapter.AlbumListAdapter;
import kr.hyosang.musicplayer.adapter.CategorySpinnerAdapter;
import kr.hyosang.musicplayer.common.Define;
import kr.hyosang.musicplayer.data.AlbumListItem;
import kr.hyosang.musicplayer.data.CategoryData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.Spinner;

public class AlbumListFragment extends FragmentBase {
    private static final int MSG_CATEGORY_DATA_SET = 0x02;
    
    private static final String KEY_LEVEL1_VALUE = "key_lv1";
    private static final String KEY_LEVEL2_VALUE = "key_lv2";
    
    private ListView mAlbumList = null;
    private AlbumListAdapter mAdapter = null;
    private IAlbumListListener mListener = null;
    private Spinner mLevel1 = null;
    private Spinner mLevel2 = null;
    private CategorySpinnerAdapter mLevel1Adapter = null;
    private CategorySpinnerAdapter mLevel2Adapter = null;
    private boolean bLastReached = false;
    private HashMap<String, List<CategoryData>> mCate2Map = new HashMap<String, List<CategoryData>>();
    private String mRestoreLevel1 = null;
    private String mRestoreLevel2 = null;
    
    public static interface IAlbumListListener {
        public void onAlbumSelected(AlbumListItem item);
    }
    
    public AlbumListFragment(IAlbumListListener l) {
        mListener = l;
    }
        
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_albumlist, container, false);
        
        mAlbumList = (ListView) v.findViewById(R.id.album_list);
        mLevel1 = (Spinner) v.findViewById(R.id.album_cate_1);
        mLevel2 = (Spinner) v.findViewById(R.id.album_cate_2);
        mAdapter = new AlbumListAdapter(getActivity());
        mAlbumList.setAdapter(mAdapter);
        
        mLevel1Adapter = new CategorySpinnerAdapter(getActivity());
        mLevel1.setAdapter(mLevel1Adapter);
        
        mLevel2Adapter = new CategorySpinnerAdapter(getActivity());
        mLevel2.setAdapter(mLevel2Adapter);
        
        mAlbumList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlbumListItem item = mAdapter.getItem(position);
                
                if(mListener != null) {
                    mListener.onAlbumSelected(item);
                }
            }
        });
        mAlbumList.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(!bLastReached) {
                    if(totalItemCount > 10) {
                        if((totalItemCount - (firstVisibleItem + visibleItemCount)) < 10) {
                            if(!isLoading()) {
                                loadData();
                            }
                        }
                    }
                }
                
            }
        });
        
        mLevel1.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cate1Selected();
                
                if(mLevel2Adapter.getCount() == 0) {
                    //바로 로드
                    mAdapter.clear();
                    bLastReached = false;
                    
                    mLevel2.setSelection(-1);
                    
                    loadData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        
        mLevel2.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mAdapter.clear();
                bLastReached = false;
                
                loadData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }            
        });
        
        
        
        return v;
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if(savedInstanceState != null) {
            mRestoreLevel1 = savedInstanceState.getString(KEY_LEVEL1_VALUE);
            mRestoreLevel2 = savedInstanceState.getString(KEY_LEVEL2_VALUE);
        }
        
        //카테고리 목록 로드
        String url = Define.SERVER_URL + Define.URI_ALBUM_CATEGORY;
        
        HttpRequest req = new HttpRequest(url, RequestType.GET);
        req.mListener = new IHttpListener() {
            @Override
            public void onError(int errorCode, HttpRequest req) {
            }

            @Override
            public void onError(int errorCode, HttpResponse resp) {
            }

            @Override
            public void onComplete(HttpResponse resp) {
                try {
                    JSONArray json = new JSONArray(resp.mResponseData);
                    
                    Message.obtain(mHandler, MSG_CATEGORY_DATA_SET, json).sendToTarget();
                    
                    
                    /*
                    
                    JSONArray years = json.optJSONArray("year");
                    
                    for(int i=0;i<years.length();i++) {
                        String year = years.getString(i);
                        
                        Message.obtain(mHandler, MSG_ADD_CATEGORY_ITEM, new CategoryData(year, year)).sendToTarget();
                    }
                    
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(!isLoading() && mAdapter.getCount() == 0) {
                                loadData();
                            }
                        }
                    }, 500);
                    
                    */
                    
                }catch(JSONException e) {
                    Logger.w(e);
                }
            }
        };
        mHttpThread.addRequest(req);
    }
    
    @Override
    public void onResume() {
        super.onResume();

    }
    
    private void setCategoryList(JSONArray json) {
        if(json != null) {
            int nsel = 0;
            for(int i=0;i<json.length();i++) {
                JSONObject cate1 = json.optJSONObject(i);
                
                mLevel1Adapter.add(new CategoryData(cate1.optString("key"), cate1.optString("name")));
                
                if(mRestoreLevel1 != null && mRestoreLevel1.equals(cate1.optString("key"))) {
                    nsel = mLevel1Adapter.getCount() - 1;
                    Logger.d("SELECTION = " + nsel);
                }
                
                JSONArray cate2 = cate1.optJSONArray("cate2");
                
                if(cate2 != null) {
                    ArrayList<CategoryData> list = new ArrayList<CategoryData>();
                    
                    for(int j=0;j<cate2.length();j++) {
                        JSONObject cate2Obj = cate2.optJSONObject(j);
                        
                        list.add(new CategoryData(cate2Obj.optString("key"), cate2Obj.optString("name")));
                    }
                    
                    mCate2Map.put(cate1.optString("key"), list);
                }       
            }
            
            mLevel1.setSelection(nsel);
        }
    }
    
    private void cate1Selected() {
        CategoryData cate1 = mLevel1Adapter.getItem(mLevel1.getSelectedItemPosition());
        
        List<CategoryData> cate2 = mCate2Map.get(cate1.value);
        
        String currYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        int selectedIndex = 0;
        
        if(cate2 != null && cate2.size() > 0) {
            mLevel2Adapter.clear();
            
            for(CategoryData itm : cate2) {
                mLevel2Adapter.add(itm);
                
                if(mRestoreLevel2 != null && mRestoreLevel2.equals(itm.value)) {
                    selectedIndex = mLevel2Adapter.getCount() - 1;
                }else if(selectedIndex == 0 && currYear.equals(itm.value)) {
                    selectedIndex = mLevel2Adapter.getCount() - 1;
                }
            }
            
            mLevel2.setEnabled(true);
            
            mLevel2.setSelection(selectedIndex);
        }else {
            mLevel2Adapter.clear();
            mLevel2.setEnabled(false);
        }
    }
    
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch(msg.what) {
            case MSG_CATEGORY_DATA_SET:
            {
                setCategoryList((JSONArray) msg.obj);
            }
            break;
                
            }
        }
    };
    
    public void onSaveInstanceState(Bundle outState) {
        int idx = mLevel1.getSelectedItemPosition();
        if(idx != -1) {
            CategoryData cate = mLevel1Adapter.getItem(idx);
            outState.putString(KEY_LEVEL1_VALUE, cate.value);
        }
        
        idx = mLevel2.getSelectedItemPosition();
        if(idx != -1) {
            CategoryData cate = mLevel2Adapter.getItem(idx);
            outState.putString(KEY_LEVEL2_VALUE, cate.value);
        }
    }
    
    
    
    
    private void loadData() {
        int idx1 = mLevel1.getSelectedItemPosition();
        int idx2 = mLevel2.getSelectedItemPosition();
        
        
        if(idx1 <= -1) return;
        CategoryData cate1 = mLevel1Adapter.getItem(idx1);
        
        CategoryData cate2 = null;
        if(idx2 >= 0) {
            cate2 = mLevel2Adapter.getItem(idx2);
        }    
        
        if(cate1 == null) return;
        
        String url = Define.SERVER_URL + Define.URI_LIST_DATA;
        String param = String.format("type=%s&count=%d&year=%s", cate1.value, mAdapter.getCount(), ((cate2 == null) ? "" : cate2.value)); 
        url = url + "?" + param;
                
        HttpRequest req = new HttpRequest(url, RequestType.GET);
        req.mListener = new IHttpListener() {
            @Override
            public void onError(int errorCode, HttpRequest req) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onError(int errorCode, HttpResponse resp) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onComplete(HttpResponse resp) {
                try {
                    JSONArray json = new JSONArray(resp.mResponseData);
                    
                    if(json.length() == 0) {
                        bLastReached = true;
                    }else {
                        for(int i=0;i<json.length();i++) {
                            JSONObject obj = json.optJSONObject(i);
                            
                            AlbumListItem item = new AlbumListItem();
                            item.mAlbumId = obj.optInt("id");
                            item.mAlbumTitle = obj.optString("title");
                            item.mAlbumArtist = obj.optString("artist");
                            item.mYear = obj.optString("year");
                            item.mDate = obj.optString("date");
                            item.mTagCount = obj.optInt("tag_count");
                            item.mType = AlbumListItem.Type.make(obj.optString("type"));
                            
                            mAdapter.add(item);
                        }
                    }
                }catch(JSONException e) {
                    Logger.w(e);
                }
                
                requestListNotify(mAdapter);
                
                setLoadingFlag(false);
            }
        };
        
        mHttpThread.addRequest(req);
        setLoadingFlag(true);
    }
}
