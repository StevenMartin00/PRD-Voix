package fr.polytech.larynxapp.model.analysis;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import fr.polytech.larynxapp.model.audio.AudioData;

public class Temp
{
    private List<Integer> pitches = new ArrayList<>();
    private List<Integer> harmonic_rates = new ArrayList<>();
    private List<Integer> argmins = new ArrayList<>();
    private List<Float> times = new ArrayList<>();
    private int sampling_rate = 44100;
    private int w_len = 512;
    private int w_step = 216;
    private int f0_min = 40;
    private int f0_max = 400;
    private int tau_min = sampling_rate / f0_max;
    private int tau_max = sampling_rate / f0_min;


    public Temp()
    {

    }

    public void differenceFunction(AudioData x, int N, int tau_max)
    {
        int w = x.getDataSize();
        if(this.tau_max > w)
            this.tau_max = w;
        List<Short> x_cumsum = new ArrayList<>();
        for(Short value : x.getData())
        {
            x_cumsum.add((short) (value + (value * value)));
        }
        int size = w + tau_max;
        int p2 = new BigInteger(String.valueOf(Math.floor(size / 32))).bitLength();
        List<Integer> nice_numbers = new ArrayList<>();
        for(int i = 16; i <= 32; i += 2)
            nice_numbers.add(i);
        double size_pad = 0;
        for(Integer number : nice_numbers)
        {
            if(number * Math.pow(2, p2) >= size)
            {
                double size_pad_loop = number * Math.pow(2, p2);
                if(size_pad < size_pad_loop)
                    size_pad = size_pad_loop;
            }
        }


    }

    public void computeYin(AudioData audioData, int sampling_rate, int w_len, int w_step, int f0_min, int f0_max, int harmo_tresh)
    {
        List<Integer> timeScale = new ArrayList<>();
        List<Short> frames = new ArrayList<>();

        for(int i = 0; i < audioData.getDataSize() - w_len; i += w_step)
        {
            timeScale.add(i);
        }
        for(Integer t : timeScale)
        {
            times.add(t/Float.valueOf(String.valueOf(sampling_rate)));
            if(t != t + w_len)
                frames.add(audioData.getDataElement(t));
        }
        for(int i = 0; i < frames.size(); i++)
        {
            /*df = differenceFunction(frames.get(i), w_len, tau_max);
            cmdf = cumulativeMeanNormalizedDifferenceFunction(df, tau_max);
            p = getPitch(cmdf, tau_min, tau_max, harmo_tresh);

            //find min of cmdf
            if(cmdf_min > tau_min)
            {
                argmins.add(i, sampling_rate/cmdf_min);
            }
            if(p != 0)
            {
                pitches.add(i, sampling_rate/p);
                harmonic_rates.add(i, cmdf.get(p));
            }
            else
            {
                harmonic_rates.add(i, cmdf_min);
            }*/
        }
    }

}
