package com.ivanroot.minplayer.adapter.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;

public abstract class BaseItemViewHolder<T> extends RecyclerView.ViewHolder {

    protected ImageButton moreBtn;

    public BaseItemViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void representItem(Context context, T object);

    public void setMoreBtnOnClickListener(View.OnClickListener onClickListener){
        moreBtn.setOnClickListener(onClickListener);
    }

}
