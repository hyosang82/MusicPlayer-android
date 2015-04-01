package kr.hyosang.musicplayer.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class MainViewPagerAdapter extends FragmentStatePagerAdapter {
    private Fragment [] mPages;
    
    public MainViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }
    
    public void setFragments(Fragment [] pages) {
        mPages = pages;
    }
    
    
    @Override
    public int getCount() {
        return mPages.length;
    }
    
    @Override
    public Fragment getItem(int pageIndex) {
        return mPages[pageIndex];
    }

}
