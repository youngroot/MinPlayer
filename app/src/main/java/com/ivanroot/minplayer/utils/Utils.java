package com.ivanroot.minplayer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by Ivan Root on 07.07.2017.
 */
public class Utils {

    public static String saveImage(@NonNull Context context, @NonNull  Bitmap bitmap, @NonNull String dir, @NonNull String name){
        String path = "";
        try {
            name += ".png";
            File file = new File(context.getExternalFilesDir(dir),name);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,100,fos);
            fos.close();
            path = file.getPath();
            Log.i("Utils",path);
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
            Log.e("Utils",e.getMessage());
        }

        return path;
    }

    public static void deleteFile(String path){
        try {
            File file = new File(path);
            file.delete();
        }catch (NullPointerException e){
            Log.e("Utils",e.getMessage());
            e.printStackTrace();
        }
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


    public static Bitmap getAudioAlbumArt(String path){
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(path);
        }catch (NullPointerException ex){}
        return bitmap;
    }
    public static Bitmap getAudioAlbumArt(String path, Bitmap defaultImage){
        Bitmap bitmap = getAudioAlbumArt(path);
        if(bitmap == null) return defaultImage;
        return bitmap;
    }

    public static Bitmap combineFourBitmapsIntoOne(List<Bitmap> bitmaps){

        int width = 750;
        int height = 750;
        int left = 0;
        int right = width / 2;
        int top = 0;
        int bottom = height / 2;

        Bitmap bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        if(bitmaps.size() <= 4){
            for(int i = 0; i < bitmaps.size(); i++){
                if(i == 4) break;
                Rect dst = new Rect(left,top,right,bottom);
                canvas.drawBitmap(bitmaps.get(i),null,dst,null);
                if(right < width) {
                    left = right;
                    right += width / 2;
                }
                else{
                    left = 0;
                    right = width / 2;
                    top = bottom;
                    bottom += height / 2;
                }
            }
        }

        return bitmap;
    }

    public static File getFileFromPath(String path){
        try {
            return new File(path);
        }catch (NullPointerException e) {
            e.printStackTrace();
            return new File("");
        }
    }
}
