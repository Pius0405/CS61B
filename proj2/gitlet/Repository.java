package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import static gitlet.Utils.*;
import static gitlet.Commit.*;

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
            error("A Gitlet version-control system already exists in the current directory");
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
            error("IOException: Cannot create file or directory");
        }
        Commit initialCommit = new Commit(new Date(0), new String[] {"", ""}, "initial commit");
        initialCommit.setID();
        initialCommit.store();
        writeContents(MASTER, initialCommit.getID());
        writeContents(HEAD, MASTER.getPath());
    }

    public static void add(String filename){
        if (! join(CWD, filename).exists()){
            error("File does not exists.");
        }
        Commit currentCommit = getCurrentCommit();
        Blob fileBlob = new Blob(readContentsAsString(join(CWD, filename)), filename);
        if (fileBlob.getID().equals(currentCommit.getTrackedFileBlobID(filename))){
            join(STAGED_FOR_ADD, fileBlob.getID()).delete();
        } else {
            writeObject(join(STAGED_FOR_ADD, fileBlob.getID()), fileBlob);
        }
    }
}
