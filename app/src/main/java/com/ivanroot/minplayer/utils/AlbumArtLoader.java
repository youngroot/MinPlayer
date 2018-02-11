package com.ivanroot.minplayer.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ivan Root on 07.07.2017.
 */

public class AlbumArtLoader {

    private Map<ImageView,BitmapWorkerTask> bitmapWorkers;
    private OnLoadCompleteListener onLoadCompleteListener;

    public AlbumArtLoader(OnLoadCompleteListener onLoadCompleteListener) {
        bitmapWorkers = new HashMap<>();
        this.onLoadCompleteListener = onLoadCompleteListener;
    }

    public void setAlbumArt(String data, ImageView imageView) {

        BitmapWorkerTask task;

        if(bitmapWorkers.containsKey(imageView)){
            task = bitmapWorkers.get(imageView);
            bitmapWorkers.remove(imageView);
            task.cancel(true);
        }

        task = new BitmapWorkerTask(imageView);
        bitmapWorkers.put(imageView,task);
        task.execute(data);
    }

    private class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {


        private MediaMetadataRetriever mmr;
        private ImageView imageView;

        public BitmapWorkerTask(ImageView imageView){
            mmr = new MediaMetadataRetriever();
            this.imageView = imageView;
        }
        @Override
        protected Bitmap doInBackground(String... params) {

            Bitmap bitmap = null;
            try {
                mmr.setDataSource(params[0]);
                byte[] data = mmr.getEmbeddedPicture();
                if (data != null) {

                    InputStream is = new ByteArrayInputStream(mmr.getEmbeddedPicture());
                    bitmap = BitmapFactory.decodeStream(is);

                }
            }catch (RuntimeException ex){
                Log.e(this.toString(),"Error!",ex);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if(bitmap != null) {
                imageView.setImageBitmap(bitmap);
                onLoadCompleteListener.onComplete(true);
            }else{
                onLoadCompleteListener.onComplete(false);
            }
        }

    }

    public interface OnLoadCompleteListener{
        void onComplete(boolean wasAlbumArt);
    }
}