package fr.polytech.larynxapp.model.analysis;

import java.util.List;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;

public class PitchDetection implements PitchDetectionHandler
{

    private List<String> pitches;
    private List<String> timestamps;

    @Override
    public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent)
    {
        if (pitchDetectionResult.getPitch() != -1)
        {
            double timeStamp = audioEvent.getTimeStamp();
            float pitch = pitchDetectionResult.getPitch();

            float probability = pitchDetectionResult.getProbability();
            String addMe1;
            if (probability < 0.5 && pitches.size() > 2) {
                addMe1 = pitches.get(pitches.size() - 1);
            } else {
                addMe1 = String.valueOf(pitch);
            }
            String addMe2 = String.valueOf(timeStamp);
            pitches.add(addMe1);
            timestamps.add(addMe2);
        }
    }

    public List<String> getTimeStamps()
    {
        return this.timestamps;
    }

    public void setTimestamps(List<String> timestamps)
    {
        this.timestamps = timestamps;
    }

    public List<String> getPitches()
    {
        return this.pitches;
    }

    public void setPitches(List<String> pitches)
    {
        this.pitches = pitches;
    }
}
