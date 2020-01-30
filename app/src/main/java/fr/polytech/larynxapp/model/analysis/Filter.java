package fr.polytech.larynxapp.model.analysis;


/**
 * The class crating a filter that's used to filter a signal.
 */
public class Filter {
    
    /**
     * Calculation's input ratios.
     */
    private float a1;
    private float a2;
    private float a3;

    /**
     *  Calculation's output ratios.
     */
    private float b1;
    private float b2;

    /**
     * Array of unfiltered values, latest are in front.
     */
    private float[] inputHistory = new float[2];

    /**
     * Array of filtered values, latest are in front.
     */
    private float[] outputHistory = new float[3];

	/// resonance amount, from sqrt(2) to ~ 0.1
    
    /**
     * Filter sole builder.
     *
     * It initialize all the calculation's ratios with the given parameters.
     *
     * @param frequency the cutoff frequency of the filter.
     * @param sampleRate the sampling rate of the signal, used to the cut the signal at the right frequency.
     * @param passType the type of filter wanted (high or low pass).
     * @see PassType
     * @param resonance the resonance of the filter
     */
    public Filter(float frequency, int sampleRate, PassType passType, float resonance)
    {

	    float c;

        switch (passType)
        {
            case LowPass:
                c = 1.0f / (float) Math.tan( Math.PI * frequency / sampleRate );
                a1 = 1.0f / ( 1.0f + resonance * c + c * c );
                a2 = 2f * a1;
                a3 = a1;
                b1 = 2.0f * ( 1.0f - c * c ) * a1;
                b2 = (1.0f - resonance * c + c * c ) * a1;
                break;

            case HighPass:
                c = (float) Math.tan( Math.PI * frequency / sampleRate );
                a1 = 1.0f / ( 1.0f + resonance * c + c * c );
                a2 = -2f * a1;
                a3 = a1;
                b1 = 2.0f * ( c * c - 1.0f) * a1;
                b2 = (1.0f - resonance * c + c * c ) * a1;
                break;
        }
    }

    /**
     * Enum class representing the filter type.
     */
    public enum PassType
    {
        /**
         * Represent the high pass filter type.
         */
	    HighPass,

        /**
         * Represent the low pass filter type.
         */
        LowPass,
    }

    /**
     * Filter a value of the signal thanks to the last filtered and unfiltered values of it.
     *
     * After filtering the value, it update and save the last values.
     * @param newInput a value of the signal to filter
     */
    public void Update(float newInput)
    {
        float newOutput = a1 * newInput + a2 * this.inputHistory[0] + a3 * this.inputHistory[1] - b1 * this.outputHistory[0] - b2 * this.outputHistory[1];

        this.inputHistory[1] = this.inputHistory[0];
        this.inputHistory[0] = newInput;

        this.outputHistory[2] = this.outputHistory[1];
        this.outputHistory[1] = this.outputHistory[0];
        this.outputHistory[0] = newOutput;
    }

    /**
     * return the last filtered value.
     *
     * @return a filtered value
     */
    public float getValue()
    {
        return this.outputHistory[0];
    }

}