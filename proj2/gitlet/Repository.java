package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Utils.*;
import static gitlet.Commit.*;
import static gitlet.Stage.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
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

    public static void init(){
        if (GITLET_DIR.exists()){
            exit("A Gitlet version-control system already exists in the current directory");
        }
        try{
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
        }catch(IOException e){
            throw error("IOException: Cannot create file or directory");
        }
        Commit initialCommit = new Commit(new Date(0), new String[] {"", ""}, "initial commit.", new HashMap<String, String>());
        initialCommit.setID();
        initialCommit.store();
        writeContents(MASTER, initialCommit.getID());
        writeContents(HEAD, "master");
        createStagingArea();
    }

    public static void add(String filename){
        if (! join(CWD, filename).exists()){
            exit("File does not exists.");
        }
        Commit currentCommit = getCurrentCommit();
        Blob fileBlob = new Blob(readContentsAsString(join(CWD, filename)), filename);
        Stage staging_area = getStagingArea();
        if (fileBlob.getID().equals(currentCommit.getTrackedFileBlobID(filename))){
            join(STAGED_FOR_ADD, fileBlob.getID()).delete();
            staging_area.deleteRec(filename);

        } else {
            writeObject(join(STAGED_FOR_ADD, fileBlob.getID()), fileBlob);
            staging_area.addRec(filename, fileBlob.getID());
        }
    }

    public static void commit(String message){
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
            newCommit.resetTrackRec(filename, staging_area.getStagedFileBlobID(filename));
        }
        moveAllFiles(STAGED_FOR_ADD, BLOBS);
        staging_area.clear();
        newCommit.setID();
        newCommit.store();
        writeContents(join(HEADS, readContentsAsString(HEAD)), newCommit.getID());
    }

    public static void rm(String filename){
        Commit currentCommit = getCurrentCommit();
        Stage staging_area = getStagingArea();
        String trackedFileBlobID = currentCommit.getTrackedFileBlobID(filename);
        String stagedFileBlobID = staging_area.getStagedFileBlobID(filename);
        if (trackedFileBlobID == null && stagedFileBlobID == null){
            exit("No reason to remove the file.");
        }
        if (trackedFileBlobID != null){
            if (join(CWD, filename).exists()){
                File destination = join(STAGED_FOR_REMOVAL, filename);
                join(CWD, filename).renameTo(destination);
            } else {
                try{
                    join(STAGED_FOR_REMOVAL, filename).createNewFile();
                } catch (IOException e){
                    throw error("IOException: Cannot create file or directory");
                }
            }
        }
        if (stagedFileBlobID != null){
            staging_area.deleteRec(filename);
            join(STAGED_FOR_ADD, stagedFileBlobID).delete();
        }
    }

    //Helper method for log and global-log to print out a commit
    private static void displayCommit(Commit currentCommit){
        System.out.println("===");
        System.out.println("commit " + currentCommit.getID());
        System.out.println("Date: " + currentCommit.timestampInString());
        System.out.println(currentCommit.getMessage());
        if (! currentCommit.getParentID(1).equals("")){
            System.out.println("Merged development into master.");
        }
        System.out.println();
    }

    public static void log(){
        Commit currentCommit = getCurrentCommit();
        while (true){
            displayCommit(currentCommit);
            if (currentCommit.getParentID(0).equals("")){
                break;
            }
            currentCommit = readObject(join(COMMITS, currentCommit.getParentID(0)), Commit.class);
        }
    }

    public static void global_log(){
        for (File file: COMMITS.listFiles()){
            Commit currentCommit = readObject(file, Commit.class);
            displayCommit(currentCommit);
        }
    }

    public static void find(String searchMessage){
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
        if (! found){
            System.out.println("Found no commit with that message.");
        }
    }

    public static void status(){
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
        for (String name: info){
            System.out.println(name);
        }
        System.out.println();
        info.clear();

        System.out.println("=== Staged Files ===");
        for (File blobFile : STAGED_FOR_ADD.listFiles()){
             info.add(readObject(blobFile, Blob.class).getFilename());
        }
        Collections.sort(info);
        for (String name: info){
            System.out.println(name);
        }
        System.out.println();
        info.clear();

        System.out.println("=== Removed Files ===");
        for (String filename : plainFilenamesIn(STAGED_FOR_REMOVAL)){
            info.add(filename);
        }
        Collections.sort(info);
        for (String name: info){
            System.out.println(name);
        }
        System.out.println();
        info.clear();

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }



    //Utility methods

    public static void exit(String message){
        System.out.println(message);
        System.exit(0);
    }

    private static void moveAllFiles(File sourceDir, File destinationDir){
        File[] files = sourceDir.listFiles();
        // Move each file from source directory to destination directory
        for (File file : files) {
            if (file.isFile()) { // Ensure we only move files, not directories
                File newFile = new File(destinationDir, file.getName());
                if (!file.renameTo(newFile)) {
                    System.out.println("Failed to move file: " + file.getName());
                }
            }
        }
    }

}
