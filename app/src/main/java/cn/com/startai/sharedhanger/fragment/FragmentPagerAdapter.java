package cn.com.startai.sharedhanger.fragment;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.ArrayList;

/**
 * author: Guoqiang_Sun
 * date : 2018/5/16 0016
 * desc :
 */

public class FragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {

    private ArrayList<Fragment> fragmentsList;

    public FragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public FragmentPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
        super(fm);
        this.fragmentsList = fragments;
    }

    @Override
    public int getCount() {
        return fragmentsList.size();
    }

    @Override
    public Fragment getItem(int arg0) {
        return fragmentsList.get(arg0);
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }
}
