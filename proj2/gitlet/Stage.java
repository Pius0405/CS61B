package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

import static gitlet.Utils.*;
import static gitlet.Repository.*;

public class Stage implements Serializable {
    private HashMap<String, String> stagedFiles;

    public Stage() {
        stagedFiles = new HashMap<>();
    }

    public void addRec(String filename, String blobID) {
        stagedFiles.put(filename, blobID);
    }

    public void deleteRec(String filename) {
        stagedFiles.remove(filename);
    }

    public static Stage getStagingArea() {
        return readObject(STAGING, Stage.class);
    }

    public Set<String> getStagedFiles() {
        return stagedFiles.keySet();
    }

    public String getStagedFileBlobID(String filename) {
        return stagedFiles.get(filename);
    }

    public static void clearStagingArea(){
        Stage stagingArea = readObject(STAGING, Stage.class);
        for (String filename : plainFilenamesIn(STAGED_FOR_ADD)) {
            join(STAGED_FOR_ADD, filename).delete();
        }
        for (String filename : plainFilenamesIn(STAGED_FOR_REMOVAL)) {
            join(STAGED_FOR_REMOVAL, filename).delete();
        }
        stagingArea.stagedFiles.clear();
        stagingArea.save();
    }

    public void save() {
        writeObject(STAGING, this);
    }
}
