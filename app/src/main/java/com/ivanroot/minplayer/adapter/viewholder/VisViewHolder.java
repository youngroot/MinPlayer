package com.ivanroot.minplayer.adapter.viewholder;

import android.opengl.GLSurfaceView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.ivanroot.minplayer.R;

public class VisViewHolder extends RecyclerView.ViewHolder {
    public VisViewHolder(View itemView) {
        super(itemView);
    }

    public void bindVisualization(GLSurfaceView visView){
        ViewGroup visHolder = (ViewGroup)itemView.findViewById(R.id.visHolder);
        visHolder.removeAllViews();
        visView.onResume();
        visHolder.addView(visView);
    }
}
