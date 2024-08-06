package com.rk.xededitor.MainActivity.fragment;

import static com.rk.xededitor.MainActivity.StaticData.fragments;
import static com.rk.xededitor.MainActivity.StaticData.mTabLayout;
import static com.rk.xededitor.MainActivity.StaticData.menu;

import android.os.Parcelable;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;

import com.rk.xededitor.BaseActivity;
import com.rk.xededitor.MainActivity.MainActivity;
import com.rk.xededitor.R;
import com.rk.xededitor.rkUtils;

import java.io.File;

import io.github.rosemoe.sora.widget.CodeEditor;

public class TabAdapter extends FragmentStatePagerAdapter {

    private final FragmentManager fragmentManager;
    private boolean removing = false;

    public TabAdapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        fragmentManager = fm;
    }

    public static CodeEditor getCurrentEditor() {
        return fragments.get(mTabLayout.getSelectedTabPosition()).editor;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {

        if (fragments == null) {
            rkUtils.toast(BaseActivity.Companion.getActivity(MainActivity.class),"Error : fragment array is null");
            return 0;

        }
        return fragments.size();
    }

    @Override
    public Parcelable saveState() {
        // Prevent saving state
        return null;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        // Do not restore state
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        if (removing) {
            return PagerAdapter.POSITION_NONE;
        } else {
            final int index = fragments.indexOf(object);
            if (index == -1) {
                return POSITION_NONE;
            } else {
                return index;
            }
        }
    }

    public void addFragment(DynamicFragment frag, File file) {
        if (fragments.contains(frag)) {
            return;
        }
        else {
            var uri = file.getPath();
            for (DynamicFragment f : fragments) {
                if (f.getFile().getPath().equals(uri)) {
                    return;
                }
            }
        }

        fragments.add(frag);
        notifyDataSetChanged();
        if (fragments.size() > 1) TabLayout.getTabAt(fragments.size() - 1).select();
    }

    public void onEditorRemove(DynamicFragment fragment) {
        fragment.releaseEditor();
        if (fragments.size() <= 1) {
            MenuItem undo = menu.findItem(R.id.undo);
            MenuItem redo = menu.findItem(R.id.redo);
            undo.setVisible(false);
            redo.setVisible(false);
        }
    }

    public void removeFragment(int position) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        DynamicFragment fragment = fragments.get(position);
        onEditorRemove(fragment);
        fragmentTransaction.remove(fragment);
        fragmentTransaction.commitNow();
        fragments.remove(position);

        removing = true;
        notifyDataSetChanged();
        removing = false;
    }

    public void closeOthers(int index) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        DynamicFragment selectedObj = fragments.get(index);
        for (DynamicFragment fragment : fragments) {
            if (!fragment.equals(selectedObj)) {
                onEditorRemove(fragment);
                fragmentTransaction.remove(fragment);
            }
        }
        fragmentTransaction.commitNow();

        fragments.clear();
        fragments.add(selectedObj);

        notifyDataSetChanged();
    }

    public void clear() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        for (DynamicFragment fragment : fragments) {
            onEditorRemove(fragment);
            fragmentTransaction.remove(fragment);
        }
        fragmentTransaction.commitNow();

        fragments.clear();
        notifyDataSetChanged();
    }
}
