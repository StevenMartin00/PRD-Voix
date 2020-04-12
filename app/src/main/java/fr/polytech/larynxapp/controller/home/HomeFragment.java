package fr.polytech.larynxapp.controller.home;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.writer.WriterProcessor;
import fr.polytech.larynxapp.R;
import fr.polytech.larynxapp.model.Record;
import fr.polytech.larynxapp.model.analysis.FeaturesCalculator;
import fr.polytech.larynxapp.model.analysis.PitchProcessor;
import fr.polytech.larynxapp.model.analysis.Yin;
import fr.polytech.larynxapp.model.audio.AudioData;
import fr.polytech.larynxapp.model.database.DBManager;

public class HomeFragment extends Fragment {

    /**
     * The status of the mic button, can have the 3 status listed in Status_mic
     */
    private Status_mic status_mic_button;

    /**
     *  The reset button, only appear after a full record
     */
    private Button button_restart;

    /**
     *  The font of the mic button, hold the button
     */
    private ImageView button_mic;

    /**
     *  The icon of the mic button
     */
    private ImageView icon_mic;

    /**
     *  Show the advancement of the recording
     */
    private ProgressBar progressBar;

    /**
     * The default file name.
     */
    public static final String FILE_NAME = "New Record.wav";

    /**
     * The storage path of the records.
     */
    public static final String FILE_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + "voiceRecords";

    /**
     * The permission request's code.
     */
    private static final int    MY_PERMISSIONS_REQUEST_NUMBER = 1;

    /**
     * The shimmer value.
     */
    private double shimmer;

    /**
     * The jitter value.
     */
    private double jitter;

    /**
     * The record's fundamental frequency.
     */
    private double f0;

    /**
     * The permissions list needed for the recording.
     */
    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * The permissions that still need to be added.
     */
    private List<String> mPermissionList = new ArrayList<>();

    /**
     * Boolean that verify the permissions.
     */
    private boolean granted = false;

    /**
     * The data base manager.
     */
    private DBManager manager;

    /**
     * The path used to save the file.
     */
    private String finalPath = FILE_PATH + File.separator + FILE_NAME;

    /**
     * File name corresponding to record's name
     */
    private String fileName;

    /**
     * List of pitch captured during the record (frequencies used to make the mean frequency and get the mean period)
     */
    private List<Float> pitches;

    /**
     * AudioDispatcher managing the pitch detection and the wav file writing
     */
    private AudioDispatcher dispatcher;

    /**
     * TarsosDSPAudioFormat created to work with AudioDispatcher (see TarsosDSP)
     */
    private TarsosDSPAudioFormat AUDIO_FORMAT;

    /**
     * Manages the notifications
     */
    private NotificationManager mNotificationManager;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        initPermissions();
        manager = new DBManager( this.getContext() );
        button_restart = root.findViewById(R.id.reset_button);
        button_mic = root.findViewById(R.id.mic_button);
        icon_mic = root.findViewById(R.id.mic_icon);
        status_mic_button = Status_mic.DEFAULT;
        progressBar = root.findViewById(R.id.progressBar_mic);
        progressBar.getProgressDrawable().setColorFilter(
                Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        AUDIO_FORMAT = new TarsosDSPAudioFormat(
                44100,
                16,
                1,
                true,
                ByteOrder.LITTLE_ENDIAN.equals(ByteOrder.nativeOrder()));

        updateView();
        return root;
    }

    /**
     * Updates the view depending of the status of the mic button
     */
    private void updateView() {
        switch (status_mic_button) {
            case DEFAULT:
                progressBar.setVisibility(View.INVISIBLE);
                icon_mic.setBackgroundResource(R.drawable.ic_mic);
                button_mic.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        updateView(Status_mic.RECORDING);
                    }
                });
                button_restart.setVisibility(View.GONE);
                break;

            case RECORDING:
                progressBar.setProgress(0);
                startRecording();
                progressBar.setVisibility(View.VISIBLE);
                icon_mic.setBackgroundResource(R.drawable.ic_stop_black_24dp);
                button_mic.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        updateView(Status_mic.CANCELED);
                    }
                });

                button_restart.setVisibility(View.GONE);
                break;

            case CANCELED:
                progressBar.setVisibility(View.VISIBLE);
                stopRecording();
                icon_mic.setBackgroundResource(R.drawable.ic_replay_black_24dp);
                button_mic.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        updateView(Status_mic.DEFAULT);
                    }
                });

                button_restart.setVisibility(View.GONE);
                break;

            case FINISH:
                progressBar.setVisibility(View.INVISIBLE);
                icon_mic.setBackgroundResource(R.drawable.ic_save_black_24dp);

                button_mic.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        save();
                        createRecordingNotification();
                        analyseData();
                        manager.updateRecordVoiceFeatures(fileName, jitter, shimmer, f0);

                        updateView(Status_mic.DEFAULT);
                    }
                });

                button_restart.setVisibility(View.VISIBLE);
                button_restart.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        updateView(Status_mic.DEFAULT);
                    }
                });
                break;
        }
    }

    /**
     * Starts the recording
     */
    private void startRecording() {
        if ( granted ) {
            File folder = new File( FILE_PATH );
            if ( !folder.exists() ) {
                folder.mkdirs();
            }
            pitches = new ArrayList<>();
            dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(44100, 2048, 0);

            File file = new File(FILE_PATH + File.separator + FILE_NAME);
            RandomAccessFile randomAccessFile = null;
            try {
                randomAccessFile = new RandomAccessFile(file,"rw");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            AudioProcessor recordProcessor = new WriterProcessor(AUDIO_FORMAT, randomAccessFile);
            dispatcher.addAudioProcessor(recordProcessor);

            PitchDetectionHandler pitchDetectionHandler = new PitchDetectionHandler() {
                @Override
                public void handlePitch(PitchDetectionResult res, AudioEvent e){
                    float pitchInHz = res.getPitch();
                    if(pitchInHz != -1 && pitchInHz < 400)
                        pitches.add(pitchInHz);
                }
            };

            AudioProcessor pitchProcessor = new PitchProcessor(new Yin(44100, 2048), 44100, 2048, pitchDetectionHandler);
            dispatcher.addAudioProcessor(pitchProcessor);


            new Thread(new Runnable() {
                @Override
                public void run() {
                    long startTime = System.nanoTime();
                    long endTime = System.nanoTime();// start dispatcher
                    Thread thread = new Thread(dispatcher, "Audio Dispatcher");
                    thread.start();
                    while (endTime - startTime < 5000000000L && status_mic_button == Status_mic.RECORDING) {
                        progressBar.setProgress(Math.round((endTime - startTime) / 50000000f));
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        endTime = System.nanoTime();
                    }
                    if (endTime - startTime >= 5000000000L) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                stopRecording();
                                updateView(Status_mic.FINISH);
                            }
                        });
                    }
                }
            }).start();
        }
    }

    /**
     * Stops the recording
     */
    private void stopRecording()
    {
        releaseDispatcher();
    }

    /**
     * Releases the audio dispatcher processing the pitch and creating the wav file associated
     */
    private void releaseDispatcher()
    {
        if(dispatcher != null)
        {
            if(!dispatcher.isStopped())
                dispatcher.stop();
            dispatcher = null;
        }
    }

    /**
     * Changing the status of the mic button
     * @param newStatus the new status of the mic button
     */
    private void updateView(Status_mic newStatus) {
        setStatus_mic_button(newStatus);
        if(newStatus == Status_mic.RECORDING)
            getActivity().findViewById(R.id.nav_view).setActivated(false);
        updateView();
    }

    public Status_mic getStatus_mic_button() {
        return status_mic_button;
    }

    public void setStatus_mic_button(Status_mic status_mic_button) {
        this.status_mic_button = status_mic_button;
    }

    /**
     * OnPause method called when the phone goes in sleep mode
     */
    @Override
    public void onPause()
    {
        super.onPause();
    }

    /**
     * OnStop method called once the app is hidden (back to home or something like that)
     */
    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * OnDestroy method called after app termination
     */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if(mNotificationManager != null)
            mNotificationManager.cancelAll();
        releaseDispatcher();
        manager.closeDB();
    }

    /**
     * OnDetach method called after leaving the app in background task
     */
    @Override
    public void onDetach()
    {
        super.onDetach();
        if(getStatus_mic_button() == Status_mic.RECORDING)
            stopRecording();
    }

    /**
     * Check the permissions of the application and request the permissions of the missing ones.
     */
    private void initPermissions() {
        this.mPermissionList.clear();
        for ( String permission : permissions ) {
            if ( ContextCompat.checkSelfPermission( getActivity(), permission ) != PackageManager.PERMISSION_GRANTED ) {
                mPermissionList.add( permission );
            }
        }

        if ( mPermissionList.isEmpty() ) {
            granted = true;
        }
        else
        {
            requestPermissions(permissions, MY_PERMISSIONS_REQUEST_NUMBER );
        }
    }

    /**
     * OnRequestPermissionsResult override that initializes granted.
     *
     * @param requestCode the request code
     * @param permissions the permissions list that you want to granted
     * @param grantResults the permissions list that has been granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults ) {
        if ( requestCode == 1 && grantResults.length > 0 ) {
            granted = true;
            for ( int grantResult : grantResults ) {
                if ( grantResult != PackageManager.PERMISSION_GRANTED ) {
                    granted = false;
                }
            }
        }
    }

    /**
     * FinalPath getter.
     *
     * @return the finalPath value
     */
    public String getFinalPath() {
        return this.finalPath;
    }

    /**
     * FinalPath setter.
     *
     * @param path the new finalPath
     */
    public void setFinalPath( String path ) {
        this.finalPath = path;
    }

    /**
     * Change file's name.
     *
     * @param oldPath the path with the old name
     * @param newPath the path with the new name
     */
    public boolean renameFile(String oldPath, String newPath ) {
        File oldFile = new File( oldPath );
        File newFile = new File( newPath );

        return oldFile.renameTo( newFile );
    }

    /**
     * Adds a Record in the dataBase
     *
     * @param name
     * @param filePath
     */
    public void addRecordDB(String name, String filePath) {
        Record record = new Record( name, filePath);
        manager.add( record );
    }

    /**
     * Saves files.
     */
    public void save() {
        DateFormat dateFormat  = new SimpleDateFormat( "dd-MM-yyyy HH-mm-ss" );
        Date currentDate = new Date( System.currentTimeMillis() );
        fileName = dateFormat.format( currentDate );
        String newPath = FILE_PATH + File.separator + fileName + ".wav";

        if ( renameFile( finalPath, newPath ) ) {
            setFinalPath( newPath );
            addRecordDB( fileName, newPath );
        }
    }

    /**
     * Runs the analyse of the data.
     */
    public void analyseData() {

        AudioData audioData = new AudioData();
        boolean fileOK;

        File file = new File(finalPath);
        try {
            if (!file.exists())
                //noinspection ResultOfMethodCallIgnored we don't need the result because we try to create only if the file doesn't exist.
                file.createNewFile();

            fileOK = true;
        }
        catch (IOException e) {
            Log.e("AnalyseData", e.getMessage(), e);
            fileOK = false;
        }

        if (!fileOK) {
            return;
        }

        try {
            FileInputStream inputStream = new FileInputStream(file);
            byte[] b = new byte[inputStream.available()];
            inputStream.read(b);
            short[] s = new short[(b.length - 44) / 2];
            ByteBuffer.wrap(b)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .asShortBuffer()
                    .get(s);

            for (short ss : s) {
                audioData.addData(ss);
            }

            audioData.setMaxAmplitudeAbs();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        audioData.processData();
        FeaturesCalculator featuresCalculator = new FeaturesCalculator(audioData, pitches);

        shimmer = featuresCalculator.getShimmer() * 100;
        jitter = featuresCalculator.getJitter() * 100;
        f0 = featuresCalculator.getfundamentalFreq();
    }

    /**
     * Creates a notification about the recording's path
     */
    public void createRecordingNotification()
    {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("LarynxChannel", name, importance);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        String pathForNotification = finalPath.substring(finalPath.indexOf("voiceRecords/"));
        Intent intent = new Intent();
        File recordFile = new File(finalPath);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(FileProvider.getUriForFile(this.getActivity(), this.getActivity().getPackageName() + ".provider", recordFile), "audio/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        PendingIntent contentIntent = PendingIntent.getActivity(this.getActivity(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = new NotificationCompat.Builder(this.getActivity(), "LarynxChannel")
                .setSmallIcon(R.drawable.bouton_micro)
                .setContentTitle("Fichier enregistré sur : ")
                .setContentText(pathForNotification)
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(false)
                .build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        mNotificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, notification);
    }
}