package com.fusionx.lightirc.fragments.actions;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;
import com.fusionx.common.PreferenceKeys;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.SelectionAdapter;
import com.fusionx.lightirc.misc.FragmentUtils;
import com.fusionx.lightirc.promptdialogs.IgnoreNickPromptDialogBuilder;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.util.TreeSet;

public class IgnoreListFragment extends ListFragment implements ActionMode.Callback,
        SlidingMenu.OnCloseListener, ListView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private ActionMode mMode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final IgnoreListCallback callback = FragmentUtils.getParent(this,
                IgnoreListCallback.class);

        final TreeSet<String> arrayList = new TreeSet<>(getActivity().getSharedPreferences
                (callback.getServerTitle().toLowerCase(), Context.MODE_PRIVATE).getStringSet
                (PreferenceKeys.IgnoreList, new TreeSet<String>()));
        final View serverHeader = inflater.inflate(R.layout.sliding_menu_header, null);
        final TextView textView = (TextView) serverHeader.findViewById(R.id
                .sliding_menu_heading_textview);
        textView.setText("Ignore List");

        final MergeAdapter adapter = new MergeAdapter();
        final SelectionAdapter ignoreAdapter = new SelectionAdapter<>(getActivity(),
                arrayList);

        adapter.addView(serverHeader);
        adapter.addAdapter(ignoreAdapter);

        setListAdapter(adapter);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().setLongClickable(true);
        getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        getListView().setOnItemClickListener(this);
        getListView().setOnItemLongClickListener(this);
    }

    @Override
    public void onClose() {
        if (mMode != null) {
            mMode.finish();
        }
    }

    @Override
    public MergeAdapter getListAdapter() {
        return (MergeAdapter) super.getListAdapter();
    }

    private SelectionAdapter<String> getIgnoreAdapter() {
        return (SelectionAdapter) getListAdapter().getAdapter(1);
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        // Inflate a menu resource providing context menu items
        final MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.ignore_list_cab, menu);

        mMode = actionMode;

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        final int checkedItemCount = getIgnoreAdapter().getSelectedItemCount();

        if (checkedItemCount != 0) {
            actionMode.setTitle(checkedItemCount + " items checked");
        } else {
            actionMode.setTitle("");
        }
        menu.getItem(0).setVisible(checkedItemCount == 0);

        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.ignore_list_cab_add:
                final IgnoreNickPromptDialogBuilder builder = new IgnoreNickPromptDialogBuilder
                        (getActivity(), "") {
                    @Override
                    public void onOkClicked(final String input) {
                        getIgnoreAdapter().add(input);
                    }
                };
                builder.show();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        final IgnoreListCallback callback = FragmentUtils.getParent(this,
                IgnoreListCallback.class);
        getIgnoreAdapter().clearSelection();

        callback.switchToIRCActionFragment();

        final SharedPreferences.Editor preferences = getActivity().getSharedPreferences
                (callback.getServerTitle().toLowerCase(), Context.MODE_PRIVATE).edit();
        preferences.putStringSet(PreferenceKeys.IgnoreList,
                getIgnoreAdapter().getCopyOfItems());
        preferences.commit();

        mMode = null;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        final boolean checked = getIgnoreAdapter().isItemAtPositionChecked(i - 1);
        if (checked) {
            getIgnoreAdapter().removeSelection(i - 1);
        } else {
            getIgnoreAdapter().addSelection(i - 1);
        }

        mMode.invalidate();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        onItemClick(adapterView, view, i, l);
        return true;
    }

    public interface IgnoreListCallback {
        public void switchToIRCActionFragment();

        public String getServerTitle();
    }
}