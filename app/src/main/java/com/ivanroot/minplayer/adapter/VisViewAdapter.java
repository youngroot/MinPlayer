package com.ivanroot.minplayer.adapter;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cleveroad.audiovisualization.AudioVisualization;
import com.cleveroad.audiovisualization.DbmHandler;
import com.cleveroad.audiovisualization.GLAudioVisualizationView;
import com.cleveroad.audiovisualization.VisualizerDbmHandler;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.adapter.viewholder.VisViewHolder;
import com.ivanroot.minplayer.visualization.surfaceview.HorizonGLSurfaceView;

import java.util.ArrayList;
import java.util.List;

public class VisViewAdapter extends RecyclerView.Adapter<VisViewHolder> {
    private int visCount = 2;
    private List<GLSurfaceView> views = new ArrayList<>(visCount);
    private int audioSesisonId = 0;
    private Context context;

    public VisViewAdapter(Context context) {
        this.context = context;
        setAudioSessionId(0);
    }

    public VisViewAdapter(Context context, int audioSessionId) {
        this.context = context;
        setAudioSessionId(audioSessionId);
    }

    public void setAudioSessionId(int audioSessionId) {
        this.audioSesisonId = audioSessionId;

        GLAudioVisualizationView waveIn = new GLAudioVisualizationView.Builder(context)
                .setBubblesSize(R.dimen.bubble_size)
                .setBubblesRandomizeSize(true)
                .setWavesHeight(R.dimen.wave_height)
                .setWavesFooterHeight(R.dimen.footer_height)
                .setWavesCount(7)
                .setLayersCount(3)
                .setBackgroundColorRes(R.color.av_color_bg)
                .setLayerColors(R.array.av_colors)
                .setBubblesPerLayer(16)
                .build();


        VisualizerDbmHandler dbmHandler = DbmHandler.Factory.newVisualizerHandler(context, audioSessionId);
        waveIn.linkTo(dbmHandler);

        views.add(0, waveIn);

        HorizonGLSurfaceView horizon = new HorizonGLSurfaceView(context, audioSessionId, context.getResources().getColor(R.color.colorDarkBlue));
        views.add(1, horizon);

        notifyDataSetChanged();

    }

    @Override
    public VisViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.vis_fragment_card_item, parent, false);
        return new VisViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VisViewHolder holder, int position) {
        holder.bindVisualization(views.get(position));
    }

    @Override
    public int getItemCount() {
        return views.size();
    }


}
