package com.ivanroot.minplayer.adapter.section;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ivanroot.minplayer.adapter.ItemRemoveInsertListObservableTransformer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public abstract class BaseItemSection<T> extends StatelessSection
        implements Filterable, com.ivanroot.minplayer.adapter.Disposable,
        ItemRemoveInsertListObservableTransformer.OnItemRemovedListener<T>,
        ItemRemoveInsertListObservableTransformer.OnItemInsertedListener<T>,
        ObservableOnSubscribe<String> {

    protected final String TAG;
    protected String query;
    protected Context context;
    protected Disposable disposable;
    protected SectionedRecyclerViewAdapter adapter;
    protected List<T> filteredData = new ArrayList<>();
    protected ObservableEmitter<String> queryEmitter;
    protected ItemRemoveInsertListObservableTransformer<T> transformer;

    public BaseItemSection(Context context, SectionParameters sectionParameters, String tag, SectionedRecyclerViewAdapter adapter) {
        super(sectionParameters);
        this.context = context;
        this.TAG = tag;
        this.adapter = adapter;
        this.setVisible(false);

        transformer = new ItemRemoveInsertListObservableTransformer<>(filteredData);
        transformer.setOnItemRemovedListener(this);
        transformer.setOnItemInsertedListener(this);
    }

    @Override
    public int getContentItemsTotal() {
        return filteredData.size();
    }

    @Override
    public void filter(@NonNull String query) {
        if (queryEmitter != null) {
            queryEmitter.onNext(query);
            this.query = null;
        } else {
            this.query = query;
        }
    }

    @Override
    public void subscribe(ObservableEmitter<String> emitter) {
        queryEmitter = emitter;

        if (query != null)
            emitter.onNext(query);

    }

    public void subscribe(@NonNull Observable<List<T>> itemsSourceObservable) {
        dispose();
        disposable = Observable.combineLatest(itemsSourceObservable, Observable.create(this), (updatedData, query) -> {
            List<T> updatedFilteredData = new ArrayList<>();

            for (int i = 0; i < updatedData.size(); i++)
                if (isItemMatchingQuery(updatedData.get(i), query, i))
                    updatedFilteredData.add(updatedData.get(i));

            return updatedFilteredData;
        }).observeOn(AndroidSchedulers.mainThread())
                .compose(transformer)
                .subscribe(filteredData -> {}, throwable -> Log.e(toString(), throwable.toString()));

    }

    @Override
    public void dispose() {
        if (queryEmitter != null)
            queryEmitter.onComplete();

        if (disposable != null)
            disposable.dispose();
    }

    public abstract boolean isItemMatchingQuery(T item, String query, int position);

    public String getTag() {
        return TAG;
    }

    @Override
    public void onItemRemoved(int position, T item) {
        if (filteredData.isEmpty()) {
            setVisible(false);
            adapter.notifyDataSetChanged();
            return;
        }

        adapter.notifyItemRemovedFromSection(TAG, position);
    }

    @Override
    public void onItemInserted(int position, T item) {
        if (filteredData.size() == 1)
            setVisible(true);

        adapter.notifyItemInsertedInSection(TAG, position);
    }

}
