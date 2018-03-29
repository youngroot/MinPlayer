package com.ivanroot.minplayer.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.adapter.viewholder.BaseItemViewHolder;
import com.ivanroot.minplayer.adapter.viewholder.PlaylistViewHolder;
import com.ivanroot.minplayer.playlist.PlaylistItem;
import com.ivanroot.minplayer.playlist.PlaylistManager;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class PlaylistSelectorDialog extends DialogFragment {

    private PlaylistSelectorAdapter adapter;
    private PlaylistSelectorAdapter.OnPlaylistItemClickListener playlistItemClickListener;
    private DialogInterface.OnDismissListener dismissListener;

    public void setPlaylistItemClickListener(PlaylistSelectorAdapter.OnPlaylistItemClickListener playlistItemClickListener){
        this.playlistItemClickListener = playlistItem -> {
            playlistItemClickListener.onPlaylistItemClick(playlistItem);
            Activity activity = getActivity();
            String message = activity.getResources().getString(R.string.audio_was_added_to) + " " + playlistItem.getName();
            Toast.makeText(activity,message,Toast.LENGTH_SHORT).show();
            dismiss();
        };
    }

    public void setDialogDismissListener(DialogInterface.OnDismissListener dismissListener){
        this.dismissListener = dismissListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.playlist_selector_dialog_layout, null);
        FastScrollRecyclerView recyclerView = (FastScrollRecyclerView)dialogView.findViewById(R.id.playlist_recycler);
        adapter = new PlaylistSelectorAdapter(getActivity(),playlistItemClickListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        builder.setView(dialogView);
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        try {
            dismissListener.onDismiss(dialog);
        }catch (NullPointerException ex){
            ex.printStackTrace();
            Log.d(toString(),ex.getMessage());
        }
        adapter.dispose();
        super.onDismiss(dialog);
    }


    public static class PlaylistSelectorAdapter extends RecyclerView.Adapter<PlaylistViewHolder>
    implements FastScrollRecyclerView.SectionedAdapter{

        private PlaylistManager playlistManager;
        private Context context;
        private Disposable disposable;
        private OnPlaylistItemClickListener playlistItemClickListener;
        private List<PlaylistItem> playlistItems;

        public PlaylistSelectorAdapter(Context context, OnPlaylistItemClickListener playlistItemClickListener){
            playlistManager = PlaylistManager.getInstance();
            this.context = context;
            this.playlistItemClickListener = playlistItemClickListener;
            disposable = playlistManager.getPlaylistItemsObservable(context)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::setPlaylistItems);
        }

        private void setPlaylistItems(List<PlaylistItem> playlistItems) {
            Log.i(this.toString(), "new List!");
            this.playlistItems = playlistItems;
            notifyDataSetChanged();
        }

        @Override
        public PlaylistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.playlist_selector_dialog_item, parent, false);
            return new PlaylistViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PlaylistViewHolder playlistViewHolder, int i) {

            PlaylistItem playlistItem = playlistItems.get(i);
            playlistViewHolder.representItem(context,playlistItem);
            playlistViewHolder.itemView.setOnClickListener(v -> playlistItemClickListener.onPlaylistItemClick(playlistItem));
        }

        @Override
        public int getItemCount() {
            try {
                return playlistItems.size();
            } catch (NullPointerException ex) {
                return 0;
            }

        }

        public void dispose(){
            if(disposable != null)
                disposable.dispose();
        }

        @NonNull
        @Override
        public String getSectionName(int i) {
            return playlistItems
                    .get(i)
                    .getName()
                    .substring(0, 1);
        }

        public interface OnPlaylistItemClickListener{
            void onPlaylistItemClick(PlaylistItem playlistItem);
        }
    }
}
