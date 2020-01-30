package fr.polytech.larynxapp.model.audio;

import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;

import java.io.File;

/**
 * @author Tianxue WANG and Wenli YAN
 * @version 2018.0115
 * @date 13/11/2017
 */


/**
 * Class for playing the audio records.
 */
public class AudioPlayer {
	
	/**
	 * Default file name
	 */
	private static final String FILE_NAME = "New Record.wav";
	
	/**
	 * The path to the file to record to play
	 */
	private String pathFinal;
	
	/**
	 * The mediaPlayer playing the record
	 */
	private MediaPlayer mediaPlayer;
	
	/**
	 * Whether the playing is paused
	 */
	private boolean     isPaused;
	
	/**
	 * Whether the playing is stopped
	 */
	private boolean     isStopped;

	/**
	 * Speed of the player
	 */
	private float speed = 1.0f;

	/**
	 * AudioPlayer sole and default builder.
	 */
	public AudioPlayer() {
		String filePath = Environment.getExternalStorageDirectory()
									 .getPath();
		
		mediaPlayer = new MediaPlayer();
		isPaused = false;
		isStopped = false;
		pathFinal = filePath + File.separator + FILE_NAME;
	}

	/**
	 * Sets the OnCompletionListener of the MediaPlayer
	 *
	 * @see MediaPlayer.OnCompletionListener
	 * @param listener the OnCompletionListener
	 */
	public void setMediaPlayerOnCompletionListener( MediaPlayer.OnCompletionListener listener ) {
		mediaPlayer.setOnCompletionListener( listener );
	}
	
	/**
	 * Sets the file's path to play.
	 *
	 * @param path the path to the file
	 */
	public void setFilePath( String path ) {
		this.pathFinal = path;
	}
	
	/**
	 * Plays the record.
	 */
	@RequiresApi(api = Build.VERSION_CODES.M)
	public void play() {
		try {
			if ( !isPaused && !isStopped ) { // if it's the first time to play this audio file
				if ( !setData() ) {
					throw ( new Exception( "Fail to set data of AudioPlayer ! " ) );
				}
				else {
					mediaPlayer.prepare();
					//mediaPlayer.getPlaybackParams().setSpeed(speed);
					PlaybackParams pbp = mediaPlayer.getPlaybackParams();
					pbp.setSpeed(speed);
					pbp.setPitch(1);
					pbp.setAudioFallbackMode(
							PlaybackParams.AUDIO_FALLBACK_MODE_DEFAULT);
					mediaPlayer.setPlaybackParams(pbp);
					mediaPlayer.start();
					mediaPlayer.setLooping( false );
				}
			}
			else if ( isStopped ) { // if the state is STOP
				mediaPlayer.prepare();
				mediaPlayer.seekTo( 0 ); // return to beginning
				mediaPlayer.start();
			}
			else { // if the state is PAUSE
				mediaPlayer.start();
			}
			isPaused = false;
			isStopped = false;
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the audio data to play
	 *
	 * @return true if it's successful
	 */
	private boolean setData() {
		try {
			if ( !Environment.MEDIA_MOUNTED.equals( Environment.getExternalStorageState() ) ) {
				throw ( new Exception( "Fail to set data of AudioPlayer ! " ) );
			}
			else {
				//mediaPlayer.setDataSource(filePath + File.separator + FILE_NAME);
				mediaPlayer.setDataSource( pathFinal );
				mediaPlayer.setLooping( true );
				//duration = mediaPlayer.getDuration();
			}
		}
		catch ( Exception e ) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Pauses the sound
	 */
	public void pause() {
		if ( mediaPlayer.isPlaying() && !isPaused && !isStopped ) {
			mediaPlayer.pause();
		}
		isPaused = true;
	}
	
	/**
	 * Stops the sound
	 */
	public void stop() {
		mediaPlayer.stop();
		isStopped = true;
	}
	
	/**
	 * Releases the resources
	 */
	public void destroy() {
		if ( mediaPlayer != null ) {
			mediaPlayer.release();
		}
	}
}
