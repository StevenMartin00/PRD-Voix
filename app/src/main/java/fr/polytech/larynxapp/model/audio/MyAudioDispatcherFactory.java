package fr.polytech.larynxapp.model.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.TarsosDSPAudioInputStream;
import be.tarsos.dsp.io.android.AndroidAudioInputStream;

import static fr.polytech.larynxapp.ui.home.HomeFragment.FILE_NAME;
import static fr.polytech.larynxapp.ui.home.HomeFragment.FILE_PATH;

public class MyAudioDispatcherFactory
{
    /* the parameters of the audio */

    /**
     * The source of the audio to record.
     */
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;

    /**
     * The sample rate to record.
     */
    private static final int AUDIO_SAMPLE_RATE = 44100; //Hz

    /**
     * The bits per second value to record.
     */
    private static final int AUDIO_BITS_PER_SECOND = 16; //bits

    /**
     * The channel configuration.
     */
    private static final int AUDIO_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO; //TODO check stereo error

    /**
     * The format in which to record.
     */
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    /**
     * The audioRecord use to record.
     */
    private AudioRecord audioRecord;

    /**
     * AudioRecord's minBufferSize.
     */
    private int minBufferSize = 0;

    /**
     * The file in which the record is to be saved.
     */
    private RandomAccessFile randomAccessFile  = null;

    /**
     * Converter use to create the final .wav file.
     */
    private WaveFileConverter waveFileConverter = null;

    /**
     * The audio data
     */
    private AudioData audioData;

    /**
     * Create a new AudioDispatcher connected to the default microphone.
     *
     * @param sampleRate
     *            The requested sample rate.
     * @param audioBufferSize
     *            The size of the audio buffer (in samples).
     *
     * @param bufferOverlap
     *            The size of the overlap (in samples).
     * @return A new AudioDispatcher
     */
    public AudioDispatcher fromDefaultMicrophone(final int sampleRate,
                                                        final int audioBufferSize, final int bufferOverlap) {
        //new AudioCapturer().startCapture(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        audioData = new AudioData();

        try {
            if (createAudioFile(sampleRate, 16)) { // make sure that the file is created
                // set minBufferSize
                minBufferSize = AudioRecord.getMinBufferSize(sampleRate, AUDIO_CHANNEL_CONFIG, AUDIO_FORMAT);

                if (minBufferSize == AudioRecord.ERROR_BAD_VALUE) { // if there is an error
                    throw new IllegalArgumentException("Buffer size too small should be at least " + (minBufferSize * 2));
                }

                // instantiate an AudioRecord object
                audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize);


                if (audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) { //if there is an error
                    throw new IllegalArgumentException("Unable to create AudioRecord");
                }

                TarsosDSPAudioFormat format = new TarsosDSPAudioFormat(sampleRate, 16, 1, true, false);

                TarsosDSPAudioInputStream audioStream = new AndroidAudioInputStream(audioRecord, format);

                // start recording
                audioRecord.startRecording();

                // start a new thread
                Thread captureThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        short[] data = new short[minBufferSize / 2];
                        int readSize;

                        try {
                            while (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                                // write the audio data to audioData
                                readSize = audioRecord.read(data, 0, data.length);

                                if (readSize != AudioRecord.ERROR_INVALID_OPERATION &&
                                        readSize != AudioRecord.ERROR_BAD_VALUE) {

                                    for (int i = 0; i < readSize; i++) {
                                        // write the content of audioData into file
                                        randomAccessFile.writeShort(Short.reverseBytes(data[i]));

                                        // copy all the data into audioData.data
                                        audioData.addData(data[i]);
                                    }

                                } else {
                                    throw (new Exception("Fail to read audio data."));
                                }
                            }
                        } catch (Exception e) {
                            Log.e("Audio Data", e.getMessage());
                        } finally {
                            audioRecord.release();
                            audioRecord = null;

                            waveFileConverter.setWaveHeaderChunkSize();
                            audioData.setMaxAmplitudeAbs();

                            if (randomAccessFile != null) {
                                try {
                                    randomAccessFile.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });

                captureThread.start();

                return new AudioDispatcher(audioStream, audioBufferSize, bufferOverlap);

//            int minAudioBufferSize = AudioRecord.getMinBufferSize(sampleRate,
//                    android.media.AudioFormat.CHANNEL_IN_MONO,
//                    android.media.AudioFormat.ENCODING_PCM_16BIT);
//            int minAudioBufferSizeInSamples = minAudioBufferSize / 2;
//            if (minAudioBufferSizeInSamples <= audioBufferSize) {
//                AudioRecord audioInputStream = new AudioRecord(
//                        MediaRecorder.AudioSource.MIC, sampleRate,
//                        android.media.AudioFormat.CHANNEL_IN_MONO,
//                        android.media.AudioFormat.ENCODING_PCM_16BIT,
//                        audioBufferSize * 2);
//
//                TarsosDSPAudioFormat format = new TarsosDSPAudioFormat(sampleRate, 16, 1, true, false);
//
//                TarsosDSPAudioInputStream audioStream = new AndroidAudioInputStream(audioInputStream, format);
//                //start recording ! Opens the stream.
//                audioInputStream.startRecording();
//                return new AudioDispatcher(audioStream, audioBufferSize, bufferOverlap);
            } else {
                throw new Exception("Can't create new audio file");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Creates a new audio file.
     *
     * The file will have the default FILE_NAME constant.
     *
     * @return true if the creation is successful
     */
    private boolean createAudioFile(int sampleRate, int bitspersecond)
    {
        File audioFile;

        try {
            if ( !Environment.MEDIA_MOUNTED.equals( Environment.getExternalStorageState() ) ) {
                throw ( new Exception( "SDCard not found ! " ) );
            }
            else {
                // create a new file to store the audio data
                audioFile = new File( FILE_PATH + File.separator + FILE_NAME );
                if ( audioFile.exists() ) { // if this file already exists, delete it
                    if ( !audioFile.delete() ) { // if fail to delete this file
                        return false;
                    }
                }
                if ( !audioFile.createNewFile() ) { // if fail to create the new file
                    return false;
                }
            }
        }
        catch ( Exception e ) {
            e.printStackTrace();
            return false;
        }

        // create a new file to store the audio data
        try {
            randomAccessFile = new RandomAccessFile( audioFile, "rw" );
        }
        catch ( FileNotFoundException e ) {
            e.printStackTrace();
        }

        // make this file to the type WAVE
        WaveFileConverter waveFileConverter = new WaveFileConverter( randomAccessFile );
        waveFileConverter.addWaveHeader( sampleRate, bitspersecond );

        return true;
    }
}
