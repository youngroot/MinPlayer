package com.ivanroot.minplayer.adapter.section;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public abstract class BaseItemSection<T> extends StatelessSection
        implements Filterable, com.ivanroot.minplayer.adapter.Disposable {
    protected final String TAG;
    protected String query;
    protected Context context;
    protected Disposable disposable;
    protected SectionedRecyclerViewAdapter adapter;

    protected List<T> data = new ArrayList<>();
    protected List<T> filteredData = new ArrayList<>();

    public BaseItemSection(Context context, SectionParameters sectionParameters, String tag, SectionedRecyclerViewAdapter adapter) {
        super(sectionParameters);
        this.context = context;
        this.TAG = tag;
        this.adapter = adapter;
        this.setVisible(false);
    }

    public void setData(@NonNull List<T> data) {
        this.data = data;
        this.filteredData = new ArrayList<>();
        this.setVisible(false);
        adapter.notifyDataSetChanged();
    }

    @Override
    public int getContentItemsTotal() {
        return filteredData.size();
    }

    @Override
    public void filter(@NonNull String query) {
        this.query = query;
        if (data.isEmpty())
            return;
//        for (int i = 0; i < data.size(); i++) {
//            T item1 = data.get(i);
//            if (isItemMatchingQuery(item1, query, i)) {
//                if (!filteredData.isEmpty()) {
//                    int insertInd = 0;
//                    for (int j = 0; j < filteredData.size(); j++) {
//                        T item2 = filteredData.get(j);
//
//                        if (item1.equals(item2)) {
//                            insertInd = -1;
//                            break;
//                        }
//
//                        if (isLowerThan(item1, item2)) {
//                            insertInd = j;
//                            break;
//                        }
//
//                        if (j == filteredData.size() - 1) {
//                            insertInd = -2;
//                        }
//                    }
//
//                    if (insertInd > -1) {
//                        filteredData.add(insertInd, item1);
//                        adapter.notifyItemInsertedInSection(TAG, insertInd);
//                    } else if(insertInd == -2){
//                        filteredData.add(item1);
//                        adapter.notifyItemInsertedInSection(TAG, filteredData.size() - 1);
//                    }
//
//                } else {
//                    filteredData.add(item1);
//                    adapter.notifyItemInsertedInSection(TAG, 0);
//                }
//
//            } else {
//                int startInd = filteredData.indexOf(item1);
//                if (startInd != -1) {
//                    int endInd = filteredData.lastIndexOf(item1);
//                    filteredData.removeAll(Collections.singleton(item1));
//                    adapter.notifyItemRangeRemovedFromSection(TAG, startInd, endInd);
//                }
//            }
//        }

        filteredData = new ArrayList<>();

        for (int i = 0; i < data.size(); i++)
            if (isItemMatchingQuery(data.get(i), query, i))
                filteredData.add(data.get(i));

        setVisible(!filteredData.isEmpty());
        adapter.notifyDataSetChanged();
        this.query = null;
    }

    public void subscribe(@NonNull Observable<List<T>> itemsSourceObservable) {
        dispose();
        disposable = itemsSourceObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    setData(data);
                    if (query != null)
                        filter(query);
                });

    }

    @Override
    public void dispose() {
        if (disposable != null)
            disposable.dispose();
    }

    public abstract boolean isItemMatchingQuery(T item, String query, int position);

    public abstract boolean isLowerThan(T item1, T item2);

    public String getTag() {
        return TAG;
    }
}
