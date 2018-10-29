package com.ivanroot.minplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.ivanroot.minplayer.R;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

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


    public static byte[] getFileBytes(File file){
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bytes;
    }

    public static String getMd5Hash(final byte[] bytes) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(bytes);
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                StringBuilder h = new StringBuilder(Integer.toHexString(0xFF & aMessageDigest));
                while (h.length() < 2)
                    h.insert(0, "0");
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void writeToFile(Context context, File file, String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(file.getAbsolutePath(), Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public static String readFromFile(Context context, File file) {

        String data = "";

        try {
            InputStream inputStream = context.openFileInput(file.getAbsolutePath());

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                data = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return data;
    }

    public static int getLoadedPercentage(long loaded, long total) {
        return (int) (((double) loaded) / total * 100);
    }
}
