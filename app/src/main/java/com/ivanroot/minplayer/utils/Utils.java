package com.ivanroot.minplayer.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.BiPredicate;

import static android.content.ContentValues.TAG;
import static hu.akarnokd.rxjava.interop.RxJavaInterop.toV2Observable;

/**
 * Created by Ivan Root on 07.07.2017.
 */
public class Utils {
    public static void storeImage(Context context,Bitmap image, String filename, String directory) {
        File pictureFile = getOutputMediaFile(context,filename,directory);
        if (pictureFile == null) {
            Log.e("store Image error", "file == null !");
            return;
        }
        else{
            Log.i("store Image info", pictureFile.getName());
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    /** Create a File for saving an image or video */
    public static  File getOutputMediaFile(Context context, String filename, String directory){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(directory);

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        File mediaFile;
        String mImageName = filename + ".png";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        Log.i("File",mediaFile.getAbsolutePath());
        return mediaFile;
    }

    public static String MD5_Hash(String s) {
        MessageDigest m = null;

        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        m.update(s.getBytes(),0,s.length());
        String hash = new BigInteger(1, m.digest()).toString(16);
        return hash;
    }

    public static <T> Observable<T> v2(rx.Observable<T> source) {
        return toV2Observable(source);
    }

    @NonNull
    public static BiPredicate<Cursor, Cursor> cursorSizeComparator() {
        return (cursor1, cursor2) -> cursor1.getCount() == cursor2.getCount();
    }

    @NonNull
    public static <T>BiPredicate<List<T>, List<T>> listSizeComparator() {
        return (list1, list2) -> list1.size() == list2.size();
    }

    public static byte[] getByteArrayFromBitmap(Bitmap bitmap) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }catch (NullPointerException ex){
            return null;
        }
    }

    public static Bitmap getBitmapFromByteArray(byte[] bytes){
        try {
            InputStream is = new ByteArrayInputStream(bytes);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            return bitmap;
        }catch (NullPointerException ex){
            return null;
        }
    }

    public static Bitmap extractAudioAlbumArt(String data){
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(data);
        byte[] bytes = mmr.getEmbeddedPicture();
        if(bytes != null)
            return BitmapFactory.decodeStream(new ByteArrayInputStream(bytes));
        else return null;
    }

    public static Bitmap getAudioAlbumArt(String path){
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(path);
        }catch (NullPointerException ex){}
        return bitmap;
    }
    public  static Bitmap getAudioAlbumArt(String path, Bitmap defaultImage){
        Bitmap bitmap = getAudioAlbumArt(path);
        if(bitmap == null) return defaultImage;
        return bitmap;
    }

    public static Bitmap combineBitmapsIntoOne(List<Bitmap> bitmaps){

        return null;
    }
}
