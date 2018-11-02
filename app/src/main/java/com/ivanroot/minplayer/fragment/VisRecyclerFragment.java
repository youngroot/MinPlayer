package com.ivanroot.minplayer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.adapter.VisViewAdapter;

public class VisRecyclerFragment extends NavFragmentBase {
    public static final String NAME = "VisRecyclerFragment";
    private RecyclerView visRecycler;
    private VisViewAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.vis_recycler_fragment, container, false);
        setupDrawer(view);
        setupRecycler(view);
        return view;
    }

    @Override
    protected String getActionBarTitle() {
        return getResources().getString(R.string.visualizer);
    }

    private void setupRecycler(View view) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL,false);
        adapter = new VisViewAdapter(getActivity());
        visRecycler = (RecyclerView) view.findViewById(R.id.vis_recycler);

        visRecycler.setHasFixedSize(true);
        visRecycler.setLayoutManager(layoutManager);
        visRecycler.setAdapter(adapter);
    }
}
