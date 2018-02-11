package com.ivanroot.minplayer.visualization;

import android.graphics.Canvas;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivan Root on 22.10.2017.
 */

public abstract class BaseVisualization {

    protected byte [] fftData;
    protected int width = 0;
    protected int height = 0;
    protected int samplingRate = 44100;


    protected List<Float> getFftMagnitudes(){

        List<Float> fftMag = new ArrayList<>();

        float magnitude;
        byte rfk, ifk;

        magnitude = (float) (Math.sqrt(fftData[0] * fftData[0]));
        fftMag.add(magnitude);

        for (int i = 1; i < (fftData.length/2); i++) {

            rfk = fftData[2 * i];
            ifk = fftData[2 * i + 1];
            magnitude = (float) Math.sqrt(rfk * rfk + ifk * ifk);
            fftMag.add(magnitude);

        }

        magnitude = (float)(Math.sqrt(fftData[1] * fftData[1]));
        fftMag.add(magnitude);

        /*float freqPerSample = samplingRate / fftData.length;
        Log.i("Freq_per_sample", String.valueOf(freqPerSample));
        for(int i = 0; i < fftMag.size(); i++){

            Log.i("Freq",String.valueOf(freqPerSample * i));
        }
        */

        return fftMag;
    }


    public BaseVisualization setFftData(byte[] fftData){
        this.fftData = fftData;
        return this;
    }

    public BaseVisualization setSamplingRate(int samplingRate){
        this.samplingRate = samplingRate;
        return this;
    }

    public BaseVisualization setWidth(int width){
        this.width = width;
        return this;
    }

    public BaseVisualization setHeight(int height) {
        this.height = height;
        return this;
    }

    public abstract Canvas visualize(Canvas canvas) throws Exception;

}
