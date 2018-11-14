package com.ivanroot.minplayer.adapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private ItemTouchHelperAdapter adapter;
    private boolean longPressDragEnabled = false;

    public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter){
        this.adapter = adapter;
    }

    public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter, boolean longPressDragEnabled){
        this.adapter = adapter;
        this.longPressDragEnabled = longPressDragEnabled;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        adapter.onItemDismiss(viewHolder.getAdapterPosition());
    }

    public void setLongPressDragEnabled(boolean longPressDragEnabled) {
        this.longPressDragEnabled = longPressDragEnabled;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return longPressDragEnabled;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

}
