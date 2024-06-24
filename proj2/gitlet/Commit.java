package gitlet;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.io.Serializable;
import java.util.Locale;
import static gitlet.Repository.*;
import static gitlet.Utils.*;


/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author Pius
 */
public class Commit implements Serializable{

    private String ID;
    private final String message;
    // Gitlet allows at most two parents for each commit
    private final String[] parents;
    private HashMap<String,String> trackedFiles;
    private final Date timestamp;

    public Commit(Date currentTime, String[] parents, String message, HashMap<String, String> trackedFiles) {
        this.message = message;
        this.parents = parents;
        this.timestamp = currentTime;
        this.trackedFiles = trackedFiles;
    }

    public String timestampInString() {
        DateFormat dateFormatter = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        return dateFormatter.format(timestamp);
    }

    public void setID() {
        ID = sha1(message, parents.toString(), timestampInString(), trackedFiles.toString());
    }

    public String getID() {
        return ID;
    }

    public String getMessage() {
        return message;
    }

    public String getParentID(int parentNum) {
        return parents[parentNum];
    }

    public HashMap<String, String> getTrackedFiles() {
        return trackedFiles;
    }

    public void removeTrackRec(String filename) {
        trackedFiles.remove(filename);
    }

    public void resetTrackRec(String filename, String blobID) {
        trackedFiles.put(filename, blobID);
    }

    public void store() {
        try{
            File f = join(COMMITS, ID);
            f.createNewFile();
            writeObject(f, this);
        } catch(IOException e) {
            throw error("IOException: Cannot create file or directory");
        }
    }

    public static Commit getCurrentCommit() {
        String currentBranch = readContentsAsString(HEAD);
        String curCommitID = readContentsAsString(join(HEADS, currentBranch));
        return readObject(join(COMMITS, curCommitID), Commit.class);
    }

    public String getTrackedFileBlobID(String filename) {
        return trackedFiles.get(filename);
    }
}
