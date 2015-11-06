package be.ac.ulb.iridia.empaticadatastream;

import android.os.Environment;
import android.util.Log;
import android.util.Pair;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

public class SaveDataUtils {

    private static final String TAG = "SaveDataUtils";

    static public void writePhysioArrayToFlash(String user, String session, String dataType, ArrayList<PhysioData> data) {
        String filename = user + "_" + session + "_" + dataType + ".txt";
        File d = getUserSessionStorageDir(user, session);
        File file = new File(d, filename);
        try {
            Writer writer = new BufferedWriter(new FileWriter(file));
            for (PhysioData physioData : data) {
                String s =  physioData.getValue() +  '\t' + physioData.getTimestamp();
                if (physioData.isTagged())
                    s +=  " - TAGGED";
                s+="\n";
               writer.write(s);
            }
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, "Error writing to flash...");
        }
    }

    static public void writeStringArrayToFlash(String user, String session, String dataType,
                                               ArrayList<Pair<Long, String>> data) {
        String filename = user + "_" + session + "_" + dataType + ".txt";
        File d = getUserSessionStorageDir(user, session);
        File file = new File(d, filename);
        try {
            Writer writer = new BufferedWriter(new FileWriter(file));

            for (Pair p : data) {
                writer.write(p.first.toString() + " " + p.second+"\n");
            }

            writer.close();
        } catch (IOException e) {
            Log.e(TAG, "Error writing to flash...");
        }
    }

    static private File getUserSessionStorageDir(String user, String session) {
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "PhysioExperiment/"+user+"/"+session);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created, probably it already exists.");
        }
        return file;
    }


    static private File getPhysioStorageDir() {
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "PhysioExperiment");
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created, probably it already exists.");
        }


        return file;
    }



    public static void eraseData(String user, String session, String dataType) {
        String filename = user + "_" + session + "_" + dataType + ".txt";
        File d =getUserSessionStorageDir(user, session);
        File file = new File(d, filename);

        if (!file.delete()) {
            Log.e(TAG, "Error while deleting file");
        }


    }
}
