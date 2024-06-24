package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import static gitlet.Utils.*;
import static gitlet.Commit.*;
import static gitlet.Stage.*;



/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author Pius
 */
public class Repository {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File OBJECTS = join(GITLET_DIR, "objects");
    public static final File COMMITS = join(OBJECTS, "commits");
    public static final File BLOBS = join(OBJECTS, "blobs");
    public static final File REFS = join(GITLET_DIR, "refs");
    public static final File HEADS = join(REFS, "heads");
    public static final File MASTER = join(HEADS, "master");
    public static final File HEAD = join(GITLET_DIR, "head");
    public static final File STAGED_FOR_ADD = join(GITLET_DIR, "add");
    public static final File STAGED_FOR_REMOVAL = join(GITLET_DIR, "removal");
    public static final File STAGING = join(GITLET_DIR, "staging");

    public static void init() {
        if (GITLET_DIR.exists()) {
            exit("A Gitlet version-control system already exists in the current directory");
        }
        try {
            GITLET_DIR.mkdir();
            OBJECTS.mkdir();
            COMMITS.mkdir();
            BLOBS.mkdir();
            REFS.mkdir();
            HEADS.mkdir();
            STAGED_FOR_ADD.mkdir();
            STAGED_FOR_REMOVAL.mkdir();
            MASTER.createNewFile();
            HEAD.createNewFile();
            STAGING.createNewFile();
        } catch(IOException e) {
            throw error("IOException: Cannot create file or directory");
        }
        Commit initialCommit = new Commit(new Date(0), new String[] {"", ""}, "initial commit", new HashMap<String, String>());
        initialCommit.setID();
        initialCommit.save();
        writeContents(MASTER, initialCommit.getID());
        writeContents(HEAD, "master");
        Stage staging_area = new Stage();
        staging_area.save();
    }

    public static void add(String filename) {
        if (! join(CWD, filename).exists()) {
            exit("File does not exists.");
        }
        Commit currentCommit = getCurrentCommit();
        Blob fileBlob = new Blob(readContentsAsString(join(CWD, filename)), filename);
        Stage staging_area = getStagingArea();
        if (fileBlob.getID().equals(currentCommit.getTrackedFileBlobID(filename))){
            join(STAGED_FOR_REMOVAL, filename).delete();
            if (staging_area.getStagedFileBlobID(filename) != null){
                join(STAGED_FOR_ADD, staging_area.getStagedFileBlobID(filename)).delete();
                staging_area.deleteRec(filename);
            }
        } else {
            if (staging_area.getStagedFileBlobID(filename) != null){
                join(STAGED_FOR_ADD, staging_area.getStagedFileBlobID(filename)).delete();
            }
            writeObject(join(STAGED_FOR_ADD, fileBlob.getID()), fileBlob);
            staging_area.addRec(filename, fileBlob.getID());
        }
        staging_area.save();
    }

    public static void commit(String message) {
        if (STAGED_FOR_ADD.listFiles().length + STAGED_FOR_REMOVAL.listFiles().length == 0){
            exit("No changes added to the commit.");
        }
        Commit currentCommit = getCurrentCommit();
        Commit newCommit = new Commit(new Date(), new String[] {currentCommit.getID(), ""}, message, currentCommit.getTrackedFiles());
        List<String> filenames = plainFilenamesIn(STAGED_FOR_REMOVAL);
        for (String filename : filenames){
            newCommit.removeTrackRec(filename);
            join(STAGED_FOR_REMOVAL, filename).delete();
        }
        Stage staging_area = getStagingArea();
        for (String filename: staging_area.getStagedFiles()){
            newCommit.renewTrackRec(filename, staging_area.getStagedFileBlobID(filename));
        }
        moveAllFiles(STAGED_FOR_ADD, BLOBS);
        staging_area.clear();
        newCommit.setID();
        newCommit.save();
        writeContents(join(HEADS, readContentsAsString(HEAD)), newCommit.getID());
    }

    public static void rm(String filename) {
        Commit currentCommit = getCurrentCommit();
        Stage staging_area = getStagingArea();
        String trackedFileBlobID = currentCommit.getTrackedFileBlobID(filename);
        String stagedFileBlobID = staging_area.getStagedFileBlobID(filename);
        if (trackedFileBlobID == null && stagedFileBlobID == null){
            exit("No reason to remove the file.");
        }
        if (trackedFileBlobID != null) {
            if (join(CWD, filename).exists()) {
                File destination = join(STAGED_FOR_REMOVAL, filename);
                join(CWD, filename).renameTo(destination);
            } else {
                try {
                    join(STAGED_FOR_REMOVAL, filename).createNewFile();
                } catch (IOException e) {
                    throw error("IOException: Cannot create file or directory");
                }
            }
        }
        if (stagedFileBlobID != null) {
            staging_area.deleteRec(filename);
            join(STAGED_FOR_ADD, stagedFileBlobID).delete();
        }
        staging_area.save();
    }

    private static void printCommit(Commit currentCommit) {
        System.out.println("===");
        System.out.println("commit " + currentCommit.getID());
        if (! currentCommit.getParentID(1).equals("")){
            System.out.println("Merge: " + currentCommit.getParentID(0).substring(0,7) + " " + currentCommit.getParentID(1).substring(0,7));
            System.out.println("Date: " + currentCommit.timestampInString());
            System.out.println("Merged development into master.");
        } else{
            System.out.println("Date: " + currentCommit.timestampInString());
            System.out.println(currentCommit.getMessage());
        }
        System.out.println();
    }

    public static void log() {
        Commit currentCommit = getCurrentCommit();
        while (true) {
            printCommit(currentCommit);
            if (currentCommit.getParentID(0).isEmpty()){
                break;
            }
            currentCommit = readObject(join(COMMITS, currentCommit.getParentID(0)), Commit.class);
        }
    }

    public static void global_log() {
        for (File file: COMMITS.listFiles()){
            Commit currentCommit = readObject(file, Commit.class);
            printCommit(currentCommit);
        }
    }

    public static void find(String searchMessage) {
        boolean found = false;
        for (File file: COMMITS.listFiles()){
            Commit currentCommit = readObject(file, Commit.class);
            if (currentCommit.getMessage().equals(searchMessage)){
                System.out.println(currentCommit.getID());
                if (! found) {
                    found = true;
                }
            }
        }
        if (! found) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void status() {
        ArrayList<String> info = new ArrayList<>();
        System.out.println("=== Branches ===");
        String currentBranch = readContentsAsString(HEAD);
        for (String branch: plainFilenamesIn(HEADS)){
            if (currentBranch.equals(branch)){
                branch = "*" + branch;
            }
            info.add(branch);
        }
        Collections.sort(info);
        for (String name: info) {
            System.out.println(name);
        }
        System.out.println();
        info.clear();

        System.out.println("=== Staged Files ===");
        for (File blobFile : STAGED_FOR_ADD.listFiles()) {
             info.add(readObject(blobFile, Blob.class).getFilename());
        }
        Collections.sort(info);
        for (String name: info) {
            System.out.println(name);
        }
        System.out.println();
        info.clear();

        System.out.println("=== Removed Files ===");
        for (String filename : plainFilenamesIn(STAGED_FOR_REMOVAL)) {
            info.add(filename);
        }
        Collections.sort(info);
        for (String name: info) {
            System.out.println(name);
        }
        System.out.println();
        info.clear();

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    public static void checkout(String[] args){
        if (args.length == 3 && args[1].equals("--")){
            if (join(COMMITS, args[0]).exists()){
                Commit targetCommit = readObject(join(COMMITS, args[0]), Commit.class);
                String targetBlobID = targetCommit.getTrackedFileBlobID(args[2]);
                if (targetBlobID != null){
                    Blob targetBlob = readObject(join(BLOBS, targetBlobID), Blob.class);
                    writeContents(join(CWD, args[2]), targetBlob.getContents());
                    return;
                }
                exit("File does not exists in that commit.");
            }
            exit("No commit with that id exists.");
        }

        if (args.length == 2 && args[0].equals("--")){
            Commit currentCommit = getCurrentCommit();
            String oldVersionBlobID = currentCommit.getTrackedFileBlobID(args[1]);
            if (oldVersionBlobID != null){
                Blob oldBlob = readObject(join(BLOBS, oldVersionBlobID), Blob.class);
                writeContents(join(CWD, args[1]), oldBlob.getContents());
                return;
            } else {
                exit("File does not exists in that commit.");
            }
        }

        if (join(HEADS, args[0]).exists()){
            if (args[0].equals(readContentsAsString(HEAD))){
                exit("No need to checkout the current branch");
            } else {
                String targetCommitID = readContentsAsString(join(HEADS, args[0]));
                Commit targetCommit = readObject(join(COMMITS, args[0]), Commit.class);
                Commit currentCommit = getCurrentCommit();
                Set<String> trackedInCurrentBranch = currentCommit.getTrackedFiles().keySet();
                Set<String> trackedInTargetBranch = targetCommit.getTrackedFiles().keySet();
                for (String filename: plainFilenamesIn(CWD)){
                    if (! trackedInCurrentBranch.contains(filename) && trackedInTargetBranch.contains(filename)){
                        exit("There is an untracked file in the way; delete it, or add and commit it first");
                    }
                }
                for (String filename: trackedInTargetBranch){
                    String newBlobID = targetCommit.getTrackedFileBlobID(filename);
                    Blob newBlob = readObject(join(BLOBS, newBlobID), Blob.class);
                    File newFile = new File(CWD.getPath(), filename);
                    writeContents(newFile, newBlob.getContents());
                }
            }
        } else {
            exit("No such branch exists.");
        }
        exit("Incorrect operands");
    }

    //Utility methods

    public static void exit(String message) {
        System.out.println(message);
        System.exit(0);
    }

    private static void moveAllFiles(File sourceDir, File destinationDir) {
        File[] files = sourceDir.listFiles();
        // Move each file from source directory to destination directory
        for (File file : files) {
            if (file.isFile()) { // Ensure we only move files, not directories
                file.renameTo(join(destinationDir, file.getName()));
            }
        }
    }

}
