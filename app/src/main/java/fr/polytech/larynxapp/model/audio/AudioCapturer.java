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

import static fr.polytech.larynxapp.ui.home.HomeFragment.FILE_NAME;
import static fr.polytech.larynxapp.ui.home.HomeFragment.FILE_PATH;

/**
 * @author Tianxue WANG and Wenli YAN
 * @version 2018.0115
 * @date 30/09/2017
 */

/**
 * The class getting the audio data form the micro.
 */
public class AudioCapturer {
	
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


	
	/* No builder */
	
	
	/**
	 * The method starting the recording.
	 *
	 * The parameters for the record will be the default ones.
	 *
	 * @return true if the record is successful
	 */
	public boolean startCapture() {
		return startCapture( AUDIO_SOURCE, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL_CONFIG, AUDIO_FORMAT );
	}
	
	/**
	 * The method starting the recording with the given parameters.
	 *
	 * @return true if the record is successful
	 */
	public boolean startCapture( int audioSource, int sampleRateInHz, int channelConfig, int audioFormat ) {
		
		AudioData audioData = new AudioData();
		
		if ( createAudioFile() ) { // make sure that the file is created
			// set minBufferSize
			minBufferSize = AudioRecord.getMinBufferSize( sampleRateInHz, channelConfig, audioFormat );
			
			if ( minBufferSize == AudioRecord.ERROR_BAD_VALUE ) { // if there is an error
				return false;
			}
			
			// instantiate an AudioRecord object
			audioRecord = new AudioRecord( audioSource, sampleRateInHz, channelConfig, audioFormat, minBufferSize );
			
			
			if ( audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED ) { //if there is an error
				return false;
			}
			
			// start recording
			audioRecord.startRecording();
			
			// start a new thread
			Thread captureThread = new Thread( new AudioCaptureThread( audioData ) );
			
			captureThread.start();
			
		}
		else {
			return false;
		}
		return true;
	}
	
	/**
	 * Creates a new audio file.
	 *
	 * The file will have the default FILE_NAME constant.
	 *
	 * @return true if the creation is successful
	 */
	private boolean createAudioFile() {
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
		waveFileConverter = new WaveFileConverter( randomAccessFile );
		waveFileConverter.addWaveHeader( AUDIO_SAMPLE_RATE, AUDIO_BITS_PER_SECOND );
		
		return true;
	}
	
	/**
	 * Stops the audio capture
	 */
	public void stopCapture() {
		// audioRecord is null if the SD card was not accessible when the record started (@Author Aurélien)
		if ( audioRecord == null )
			return;
		
		if ( audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING ) {
			// stop recording
			audioRecord.stop();
		}
	}

	/**
	 * Class used as thread for capturing the data
	 */
	private class AudioCaptureThread implements Runnable {

		/**
		 * The AudioData where the capture will be saved.
		 */
		private AudioData audioData;

		/**
		 * AudioCaptureThread sole builder.
		 *
		 * @param audioData the AudioData where to save the capture
		 */
		AudioCaptureThread( AudioData audioData ) {
			this.audioData = audioData;
		}

		/**
		 * Run override saving the record into the AudioData.
		 */
		@Override
		public void run() {

			short[] data = new short[ minBufferSize / 2 ];
			int     readSize;

			try {
				while ( audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING ) {
					// write the audio data to audioData
					readSize = audioRecord.read( data, 0, data.length );

					if ( readSize != AudioRecord.ERROR_INVALID_OPERATION &&
						 readSize != AudioRecord.ERROR_BAD_VALUE ) {

						for ( int i = 0; i < readSize; i++ ) {
							// write the content of audioData into file
							randomAccessFile.writeShort( Short.reverseBytes( data[ i ] ) );

							// copy all the data into audioData.data
							audioData.addData( data[ i ] );
						}

					}
					else {
						throw ( new Exception( "Fail to read audio data." ) );
					}
				}
			}
			catch ( Exception e ) {
				Log.e( "Audio Data", e.getMessage() );
			}
			finally {
				audioRecord.release();
				audioRecord = null;

				waveFileConverter.setWaveHeaderChunkSize();
				audioData.setMaxAmplitudeAbs();

				if ( randomAccessFile != null ) {
					try {
						randomAccessFile.close();
					}
					catch ( IOException e ) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
