package com.ivanroot.minplayer.adapter;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;

public class ItemRemoveInsertListObservableTransformer<T> implements ObservableTransformer<List<T>, List<T>> {

    private List<T> currentList;
    private OnItemRemovedListener<T> onItemRemovedListener;
    private OnItemInsertedListener<T> onItemInsertedListener;
    private boolean paused = false;

    public ItemRemoveInsertListObservableTransformer(@NonNull List<T> currentList) {
        this.currentList = currentList;
    }

    public void setCurrentList(@NonNull List<T> currentList) {
        this.currentList = currentList;
    }

    public void setOnItemRemovedListener(OnItemRemovedListener<T> onItemRemovedListener) {
        this.onItemRemovedListener = onItemRemovedListener;
    }

    public void setOnItemInsertedListener(OnItemInsertedListener<T> onItemInsertedListener) {
        this.onItemInsertedListener = onItemInsertedListener;
    }

    @Override
    public ObservableSource<List<T>> apply(Observable<List<T>> upstream) {
        return upstream
                .filter(updatedList -> !paused)
                .doOnNext(updatedList -> {
            Set<T> updatedListSet = new HashSet<>(updatedList);
            List<T> copyList = new ArrayList<>(currentList);
            int delta = 0;


            for (int i = 0; i < copyList.size(); i++)
                if (!updatedListSet.contains(copyList.get(i))) {
                    currentList.remove(i - delta);
                    if (onItemRemovedListener != null)
                        onItemRemovedListener.onItemRemoved(i - delta, copyList.get(i));
                    delta++;
                }


            for (int i = 0; i < updatedList.size(); i++) {
                T item = updatedList.get(i);

                if (i < currentList.size() && !Objects.equals(item, currentList.get(i))) {
                    currentList.add(i, item);
                    if (onItemInsertedListener != null)
                        onItemInsertedListener.onItemInserted(i, item);

                } else if (i >= currentList.size()) {
                    currentList.add(item);
                    if (onItemInsertedListener != null)
                        onItemInsertedListener.onItemInserted(currentList.size() - 1, item);
                }
            }

        });
    }

    public void setPaused(boolean paused){
        this.paused = paused;
    }

    public interface OnItemRemovedListener<T> {
        void onItemRemoved(int position, T item);
    }

    public interface OnItemInsertedListener<T> {
        void onItemInserted(int position, T item);
    }
}
