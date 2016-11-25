package rs.luka.android.studygroup.io;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

import rs.luka.android.studygroup.misc.Utils;
import rs.luka.android.studygroup.model.ID;

/**
 * Created by luka on 19.9.15..
 */
public class LocalAudio {
    public static final File APP_AUDIO_DIR = new File(Environment.getExternalStorageDirectory(),
                                                      "recordings/Notekeeper/");

    public static void saveNoteAudio(ID id, String courseName, String lessonName, File audio) {
        saveAudio(audio, generateItemAudioFile(courseName, lessonName, id));
    }

    public static File getNoteAudio(ID id, String courseName, String lessonName) {
        return generateItemAudioFile(courseName, lessonName, id);
    }

    public static boolean noteHasAudio(ID id, String courseName, String lessonName) {
        return generateItemAudioFile(courseName, lessonName, id).exists();
    }

    public static File generateItemAudioFile(String courseName, String lessonName, ID itemId) {
        if (!APP_AUDIO_DIR.isDirectory()) APP_AUDIO_DIR.mkdir();
        File courseDir = new File(APP_AUDIO_DIR, courseName);
        if (!courseDir.isDirectory()) courseDir.mkdir();
        return new File(courseDir,
                        lessonName + "-REC_" + itemId.toString()
                        + ".mp3"); // TODO: 19.9.15. support i za druge formate sem mp3
        //note 18.11.16. drugi formati su podržani, iako ova klasa sve imenuje kao ".mp3", to ne znači da je tip
        //fajla mp3, tako da se ne bi trebalo oslanjati na ekstenziju
    }

    private static void saveAudio(File oldFile, File newFile) {
        try {
            if (!APP_AUDIO_DIR.isDirectory()) { APP_AUDIO_DIR.mkdir(); }
            Utils.copyFile(oldFile, newFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
