package fr.polytech.larynxapp.ui.home;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import fr.polytech.larynxapp.R;
import fr.polytech.larynxapp.model.Record;
import fr.polytech.larynxapp.model.audio.AudioCapturer;
import fr.polytech.larynxapp.model.audio.AudioData;
import fr.polytech.larynxapp.model.audio.MyAudioDispatcherFactory;
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
     * The small text introducing how to use the app on start
     */
    private TextView hint_tv;

    /**
     *  Show the advancment of the recording
     */
    private ProgressBar progressBar;

    /**
     * The default file name.
     */
    public static final String FILE_NAME                     = "New Record.wav";

    /**
     * The storage path of the records.
     */
    public static final String FILE_PATH                     = Environment.getExternalStorageDirectory()
            .getPath() + File.separator + "voiceRecords";

    /**
     * The permission request's code.
     */
    private static final int    MY_PERMISSIONS_REQUEST_NUMBER = 1;

    /**
     * The record's data to analysis.
     */
    private AudioData audioData;

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

    String[]     permissions     = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * The permissions that still need to be added.
     */
    List<String> mPermissionList = new ArrayList<>();

    /**
     * The record thread that runs the recording.
     */
    private RecordThread recordThread;

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
        hint_tv = root.findViewById(R.id.hint_tv);
        updateView();

        return root;
    }

    private void updateView() {
        switch (status_mic_button) {
            case DEFAULT:
                progressBar.setVisibility(View.INVISIBLE);
                icon_mic.setBackgroundResource(R.drawable.ic_mic);
                button_mic.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        List<Record> records = manager.query();
                        for(Record record : records)
                            System.out.println(record.getName() + " " + record.getPath() + " " + record.getJitter() + " " + record.getShimmer() + " " + record.getF0());
                        System.out.println(manager.deleteByName("Record Test"));
                        //updateView(Status_mic.RECORDING);
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
                        //analyseData();
                        //manager.updateRecordVoiceFeatures(fileName, jitter, shimmer, f0);
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

    private void startRecording() {
        if ( granted ) {
            File folder = new File(FILE_PATH);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            MyAudioDispatcherFactory myAudioDispatcherFactory = new MyAudioDispatcherFactory();
            final AudioDispatcher dispatcher = myAudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);
            PitchDetectionHandler pdh = new PitchDetectionHandler() {
                @Override
                public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
                    final float pitchInHz = pitchDetectionResult.getPitch();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hint_tv.setText(String.valueOf(pitchInHz));
                        }
                    });

                }
            };
            AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
            dispatcher.addAudioProcessor(pitchProcessor);
            //dispatcher.run();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long startTime = System.nanoTime();
                    long endTime = System.nanoTime();
                    final Thread audioThread = new Thread(dispatcher, "Audio Thread");
                    audioThread.start();
                    while (endTime - startTime < 5000000000L && status_mic_button == Status_mic.RECORDING) {
                        progressBar.setProgress( Math.round((endTime - startTime)/50000000f));
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        endTime = System.nanoTime();
                    }
                    if(endTime - startTime >= 5000000000L) {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                updateView(Status_mic.FINISH);
                            }
                        });
                    }
                }
            }).start();

            /*new Thread(new Runnable() {
                @Override
                public void run() {
                    long startTime = System.nanoTime();
                    long endTime = System.nanoTime();
                    recordThread = new RecordThread();
                    recordThread.run();
                    while (endTime - startTime < 5000000000L && status_mic_button == Status_mic.RECORDING) {
                        progressBar.setProgress( Math.round((endTime - startTime)/50000000f));
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        endTime = System.nanoTime();
                    }
                    if(endTime - startTime >= 5000000000L) {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                recordThread.stop();
                                updateView(Status_mic.FINISH);
                            }
                        });
                    }
                }
            }).start();*/
        }

    }

    private void stopRecording() {
    }

    private void updateView(Status_mic newStatus) {
        setStatus_mic_button(newStatus);
        updateView();
    }

    public Status_mic getStatus_mic_button() {
        return status_mic_button;
    }

    public void setStatus_mic_button(Status_mic status_mic_button) {
        this.status_mic_button = status_mic_button;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        manager.closeDB();
    }

    /**
     * Check the permissions of the application and request the permissions of the missing ones.
     */
    public void initPermissions() {
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
        System.out.println( "oldPath = " + oldPath );
        System.out.println( "newPath = " + newPath );
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
    private void save() {
        DateFormat dateFormat  = new SimpleDateFormat( "YYYY-MM-DD HH:MM:SS" );
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
    private void analyseData() {

        /*new Thread(new Runnable() {
            @Override
            public void run() {
                audioData = new AudioData();
                boolean fileOK;

                File file = new File(finalPath);
                System.out.println("finalPath = " + finalPath);
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

                //TEST DATA
                shimmer = 14.6;
                jitter = 5.2;
                f0 = 250;

                FeaturesCalculator featuresCalculator = new FeaturesCalculator(audioData);

                shimmer = featuresCalculator.getShimmer();
                jitter = featuresCalculator.getJitter();
                f0 = featuresCalculator.getF0();
            }
        }).start();*/
    }

    /**
     * Creates a notification about the recording's path
     */
    private void createRecordingNotification()
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

        Notification notification = new NotificationCompat.Builder(this.getActivity(), "LarynxChannel")
                .setSmallIcon(R.drawable.bouton_micro) //TODO: à changer
                .setContentTitle("Fichier enregistré sur : ")
                .setContentText(pathForNotification)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager mNotificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, notification);
    }
}