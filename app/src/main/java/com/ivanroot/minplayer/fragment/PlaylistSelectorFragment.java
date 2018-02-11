package com.ivanroot.minplayer.fragment;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hwangjr.rxbus.Bus;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.player.RxBus;
import com.ivanroot.minplayer.playlist.PlaylistManager;
import com.ivanroot.minplayer.storio.PlaylistTable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by Ivan Root on 02.07.2017.
 */

public class PlaylistSelectorFragment extends ListFragment {

    public static final String NAME = "PlaylistSelectorFragment";
    private Cursor cursor;
    private SelectorAdapter adapter;
    private Bitmap bitmap;
    private Disposable disposable;
    private Observable<Cursor> observable;
    private int position = 0;
    private int top = 0;
    private PlaylistManager playlistManager = PlaylistManager.getInstance();
    private boolean addWasAdded = false;
    private Bus rxBus = RxBus.getInstance();


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rxBus.register(this);
        adapter = new SelectorAdapter(getActivity(), cursor);
        observable = playlistManager.getPlaylistNamesObservable(getActivity())
                .observeOn(AndroidSchedulers.mainThread());

    }

    @Override
    public void onStart() {
        super.onStart();
        if(!addWasAdded) {
            View child = getActivity().getLayoutInflater().inflate(R.layout.playlist_item, null);
            getListView().addHeaderView(child);
            setListAdapter(adapter);
            addWasAdded = true;
        }
        getListView().setOnItemLongClickListener((parent, view, position, id) -> {
            if(position == 0) {
                showPlaylistCreationDialog();
            }
            else {
                cursor.moveToPosition(position - 1);
                String playlistName = cursor.getString(cursor.getColumnIndex(PlaylistTable.ROW_PLAYLIST_NAME));
                playlistManager.removePlaylist(getActivity(),playlistName);
            }
            return true;
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        disposable = observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(c ->{
                    changeCursor(c);
                    Log.i("onResumeSubscription",String.valueOf(c.getCount()));
                });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onPause() {
        super.onPause();
        if(disposable != null){
            disposable.dispose();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        rxBus.unregister(this);
        if(disposable != null){
            disposable.dispose();
        }

    }

    private void changeCursor(Cursor cursor){
        this.cursor = cursor;
        adapter.changeCursor(cursor);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        if(position == 0) {
            showPlaylistCreationDialog();
        }
        else {
            cursor.moveToPosition(position - 1);
            String playlistName = cursor.getString(cursor.getColumnIndex(PlaylistTable.ROW_PLAYLIST_NAME));
            Toast.makeText(getActivity(), playlistName, Toast.LENGTH_LONG).show();
            PlaylistRecyclerFragment playlistRecyclerFragment = new PlaylistRecyclerFragment(playlistName);
            getActivity()
                    .getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentHolder,playlistRecyclerFragment,playlistRecyclerFragment.toString())
                    .commit();

        }

    }

    private void showPlaylistCreationDialog(){
        PlaylistAddDialog dialog = new PlaylistAddDialog();
        String tag = getResources().getString(R.string.add_playlist);
        dialog.show(getFragmentManager(),tag);
    }

    private class SelectorAdapter extends CursorAdapter{


        public SelectorAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }


        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return getActivity().getLayoutInflater().inflate(R.layout.playlist_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            ImageView playlistImage = (ImageView) view.findViewById(R.id.playlistImage);
            playlistImage.setImageResource(R.drawable.ic_playlist);
            TextView playlistTitle = (TextView) view.findViewById(R.id.playlistTitle);
            playlistTitle.setText(cursor.getString(cursor.getColumnIndex(PlaylistTable.ROW_PLAYLIST_NAME)));
        }

        @Override
        public void changeCursor(Cursor cursor) {
            super.changeCursor(cursor);
            notifyDataSetChanged();
        }
    }

    @Override
    public String toString() {
        return NAME;
    }
}
