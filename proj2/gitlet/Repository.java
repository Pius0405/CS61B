package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;
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
        } catch (IOException e) {
            throw error("IOException: Cannot create file or directory");
        }
        Commit initialCommit = new Commit(new Date(0), new String[] {"", ""},
                "initial commit", new HashMap<String, String>());
        initialCommit.setID();
        initialCommit.save();
        writeContents(MASTER, initialCommit.getID());
        writeContents(HEAD, "master");
        Stage stagingArea = new Stage();
        stagingArea.save();
    }

    public static void add(String filename) {
        if (!join(CWD, filename).exists()) {
            exit("File does not exists.");
        }
        Commit currentCommit = getCurrentCommit();
        Blob fileBlob = new Blob(readContentsAsString(join(CWD, filename)), filename);
        Stage stagingArea = getStagingArea();
        if (fileBlob.getID().equals(currentCommit.getTrackedFileBlobID(filename))) {
            join(STAGED_FOR_REMOVAL, filename).delete();
            if (stagingArea.getStagedFileBlobID(filename) != null) {
                join(STAGED_FOR_ADD, stagingArea.getStagedFileBlobID(filename)).delete();
                stagingArea.deleteRec(filename);
            }
        } else {
            if (stagingArea.getStagedFileBlobID(filename) != null) {
                join(STAGED_FOR_ADD, stagingArea.getStagedFileBlobID(filename)).delete();
            }
            writeObject(join(STAGED_FOR_ADD, fileBlob.getID()), fileBlob);
            stagingArea.addRec(filename, fileBlob.getID());
        }
        stagingArea.save();
    }

    public static void commit(String message, String parent2ID) {
        if (STAGED_FOR_ADD.listFiles().length + STAGED_FOR_REMOVAL.listFiles().length == 0) {
            exit("No changes added to the commit.");
        }
        Commit currentCommit = getCurrentCommit();
        Commit newCommit = new Commit(new Date(), new String[] {currentCommit.getID(), parent2ID},
                message, currentCommit.getTrackedFiles());
        List<String> filenames = plainFilenamesIn(STAGED_FOR_REMOVAL);
        for (String filename : filenames) {
            newCommit.removeTrackRec(filename);
            join(STAGED_FOR_REMOVAL, filename).delete();
        }
        Stage stagingArea = getStagingArea();
        for (String filename: stagingArea.getStagedFiles()) {
            newCommit.renewTrackRec(filename, stagingArea.getStagedFileBlobID(filename));
        }
        moveAllFiles(STAGED_FOR_ADD, BLOBS);
        clearStagingArea();
        newCommit.setID();
        newCommit.save();
        writeContents(join(HEADS, readContentsAsString(HEAD)), newCommit.getID());
    }

    public static void rm(String filename) {
        Commit currentCommit = getCurrentCommit();
        Stage stagingArea = getStagingArea();
        String trackedFileBlobID = currentCommit.getTrackedFileBlobID(filename);
        String stagedFileBlobID = stagingArea.getStagedFileBlobID(filename);
        if (trackedFileBlobID == null && stagedFileBlobID == null) {
            exit("No reason to remove the file.");
        }
        if (trackedFileBlobID != null) {
            if (join(CWD, filename).exists()) {
                File destination = join(STAGED_FOR_REMOVAL, filename);
                join(CWD, filename).renameTo(destination);
            } else {
                writeContents(join(STAGED_FOR_REMOVAL, filename), "");
            }
        }
        if (stagedFileBlobID != null) {
            stagingArea.deleteRec(filename);
            join(STAGED_FOR_ADD, stagedFileBlobID).delete();
        }
        stagingArea.save();
    }

    private static void printCommit(Commit currentCommit) {
        System.out.println("===");
        System.out.println("commit " + currentCommit.getID());
        if (!currentCommit.getParentID(1).equals("")) {
            System.out.println("Merge: " + currentCommit.getParentID(0).substring(0, 7)
                   + " " + currentCommit.getParentID(1).substring(0, 7));
            System.out.println("Date: " + currentCommit.timestampInString());
            System.out.println(currentCommit.getMessage());
        } else {
            System.out.println("Date: " + currentCommit.timestampInString());
            System.out.println(currentCommit.getMessage());
        }
        System.out.println();
    }

    public static void log() {
        Commit currentCommit = getCurrentCommit();
        while (true) {
            printCommit(currentCommit);
            if (currentCommit.getParentID(0).isEmpty()) {
                break;
            }
            currentCommit = readObject(join(COMMITS, currentCommit.getParentID(0)), Commit.class);
        }
    }

    public static void globalLog() {
        for (File file: COMMITS.listFiles()) {
            Commit currentCommit = readObject(file, Commit.class);
            printCommit(currentCommit);
        }
    }

    public static void find(String searchMessage) {
        boolean found = false;
        for (File file: COMMITS.listFiles()) {
            Commit currentCommit = readObject(file, Commit.class);
            if (currentCommit.getMessage().equals(searchMessage)) {
                System.out.println(currentCommit.getID());
                if (!found) {
                    found = true;
                }
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    //Helper method for status
    private static void statusPrinter(String topic, ArrayList<String> info) {
        Collections.sort(info);
        System.out.println("=== " + topic + " ===");
        for (String name: info) {
            System.out.println(name);
        }
        System.out.println();
    }

    public static void status() {
        ArrayList<String> info = new ArrayList<>();
        String currentBranch = readContentsAsString(HEAD);
        for (String branch: plainFilenamesIn(HEADS)) {
            if (currentBranch.equals(branch)) {
                branch = "*" + branch;
            }
            info.add(branch);
        }
        statusPrinter("Branches", info);
        info.clear();

        for (File blobFile : STAGED_FOR_ADD.listFiles()) {
            info.add(readObject(blobFile, Blob.class).getFilename());
        }
        statusPrinter("Staged Files", info);
        info.clear();

        for (String filename : plainFilenamesIn(STAGED_FOR_REMOVAL)) {
            info.add(filename);
        }
        statusPrinter("Removed Files", info);
        info.clear();

        Commit currentCommit = getCurrentCommit();
        Stage stagingArea = getStagingArea();
        for (String filename : currentCommit.getTrackedFiles().keySet()) {
            File cwdFile = join(CWD, filename);
            if (cwdFile.exists()) {
                Blob curVersion = new Blob(readContentsAsString(cwdFile), filename);
                String trackedVersionID = currentCommit.getTrackedFileBlobID(filename);
                if (!curVersion.getID().equals(trackedVersionID)) {
                    if (stagingArea.getStagedFileBlobID(filename) == null) {
                        info.add(filename + " (modified)");
                    }
                }
            } else {
                if (!join(STAGED_FOR_REMOVAL, filename).exists()) {
                    info.add(filename + " (deleted)");
                }
            }
        }

        for (String filename : stagingArea.getStagedFiles()) {
            File cwdFile = join(CWD, filename);
            if (cwdFile.exists()) {
                Blob curVersion = new Blob(readContentsAsString(cwdFile), filename);
                if (!stagingArea.getStagedFileBlobID(filename).equals(curVersion.getID())) {
                    info.add(filename + " (modified)");
                }
            } else {
                info.add(filename + " (deleted)");
            }
        }

        statusPrinter("Modifications Not Staged For Commit", info);
        info.clear();

        for (String filename : plainFilenamesIn(CWD)) {
            if (currentCommit.getTrackedFileBlobID(filename) == null) {
                if (stagingArea.getStagedFileBlobID(filename) == null) {
                    info.add(filename);
                }
            }  else {
                if (join(STAGED_FOR_REMOVAL, filename).exists()) {
                    info.add(filename);
                }
            }
        }
        statusPrinter("Untracked Files", info);
    }

    //Helper method for checkout
    /***
     * Takes as arguments a commit and a filename and change the corresponding file in the CWD
     * to the version in the commit if exists.
     */
    private static void renewCWDFile(Commit commit, String filename) {
        String blobID = commit.getTrackedFileBlobID(filename);
        if (blobID != null) {
            Blob targetBlob = readObject(join(BLOBS, blobID), Blob.class);
            writeContents(join(CWD, filename), targetBlob.getContents());
        } else {
            exit("File does not exists in that commit.");
        }
    }

    //Override renewCWDFile

    /**
     * Changes all files in CWD to version of files in the target commit
     */
    private static void renewCWDFile(Commit currentCommit, Commit targetCommit) {
        Set<String> trackedInTargetBranch = targetCommit.getTrackedFiles().keySet();
        for (String filename: trackedInTargetBranch) {
            String newBlobID = targetCommit.getTrackedFileBlobID(filename);
            Blob newBlob = readObject(join(BLOBS, newBlobID), Blob.class);
            if (!newBlobID.equals(currentCommit.getTrackedFileBlobID(filename))) {
                File newFile = join(CWD, filename);
                writeContents(newFile, newBlob.getContents());
            }
        }
    }

    //Helper method for checkout

    /**
     * remove all files from the CWD that are tracked in the current commit but not
     * tracked in the target commit
     */
    private static void removeUntrackedFiles(Commit currentCommit, Commit targetCommit) {
        Set<String> trackedInCurrentBranch = currentCommit.getTrackedFiles().keySet();
        Set<String> trackedInTargetBranch = targetCommit.getTrackedFiles().keySet();
        trackedInCurrentBranch.removeAll(trackedInTargetBranch);
        for (String filename : trackedInCurrentBranch) {
            restrictedDelete(join(CWD, filename));
        }
    }

    //Helper method for checkout

    /**
     * Check if there are any files in the CWD that are not tracked in the current commit
     * but tracked by target commit
     */
    private static void catchUntrackedFiles(Commit currentCommit, Commit targetCommit) {
        Set<String> trackedInCurrentBranch = currentCommit.getTrackedFiles().keySet();
        Set<String> trackedInTargetBranch = targetCommit.getTrackedFiles().keySet();
        for (String filename: plainFilenamesIn(CWD)) {
            if (!trackedInCurrentBranch.contains(filename)
                   && trackedInTargetBranch.contains(filename)) {
                exit("There is an untracked file in the way; delete it, "
                        + "or add and commit it first.");
            }
        }
    }

    public static void checkoutCommitFile(String commitID, String filename) {
        Commit targetCommit = findCommit(commitID);
        if (targetCommit != null) {
            renewCWDFile(targetCommit, filename);
        } else {
            exit("No commit with that id exists.");
        }
    }

    public static void checkoutFile(String filename) {
        Commit currentCommit = getCurrentCommit();
        renewCWDFile(currentCommit, filename);
    }

    public static void checkoutBranch(String branchName) {
        if (join(HEADS, branchName).exists()) {
            if (branchName.equals(readContentsAsString(HEAD))) {
                exit("No need to checkout the current branch");
            } else {
                Commit currentCommit = getCurrentCommit();
                String targetCommitID = readContentsAsString(join(HEADS, branchName));
                Commit targetCommit = readObject(join(COMMITS, targetCommitID), Commit.class);
                catchUntrackedFiles(currentCommit, targetCommit);
                renewCWDFile(currentCommit, targetCommit);
                removeUntrackedFiles(currentCommit, targetCommit);
                clearStagingArea();
                writeContents(HEAD, branchName);
            }
        } else {
            exit("No such branch exists.");
        }
    }

    public static void branch(String branchName) {
        File newBranch = join(HEADS, branchName);
        if (newBranch.exists()) {
            exit("A branch with that name already exists.");
        }
        String currentCommitID = getCurrentCommit().getID();
        writeContents(newBranch, currentCommitID);
    }

    public static void rmBranch(String branchName) {
        if (!join(HEADS, branchName).exists()) {
            exit("A branch with that name does not exists.");
        }
        if (branchName.equals(readContentsAsString(HEAD))) {
            exit("Cannot remove the current branch.");
        }
        join(HEADS, branchName).delete();
    }

    public static void reset(String commitID) {
        Commit targetCommit = findCommit(commitID);
        if (targetCommit != null) {
            Commit currentCommit = getCurrentCommit();
            catchUntrackedFiles(currentCommit, targetCommit);
            renewCWDFile(currentCommit, targetCommit);
            removeUntrackedFiles(currentCommit, targetCommit);
            String currentBranch = readContentsAsString(HEAD);
            writeContents(join(HEADS, currentBranch), targetCommit.getID());
            clearStagingArea();
        } else {
            exit("No commit with that id exists.");
        }
    }

    /**
     * Runtime analysis:
     * ---------------------------------------------------
     * Cost model --> number of nodes (commits) accessed
     * In the worst case scenario the commits are evenly distributed between two branches and the
     * latest common ancestor is the initial commit
     * ---------------------------------------------------
     * Method 1 --> Nested for loops (not recommended)
     * Explanation : Iterate over every single commit of any branch and at each time compare
     * it with every other commit of the other branch.
     * Runtime : O(N^2)
     * Analysis : Outer loop has N/2 iterations and inner loop has N/2 iterations but
     * the inner loop is executed for N/2 times so the number of nodes accessed is N/2 + (N/2)^2.
     * This shows that the time complexity is quadratic.
     * ---------------------------------------------------
     * Method 2 --> Breadth first search
     * Explanation : Start from the head commit of each branch and add them to queue.
     * Maintain two sets of visited commits for each branch using BFS algorithm.
     * Iterate over one set of visited commits for a branch and check if the other
     * set contains that commit.
     * Since LinkedHashSet maintains the insertion order, the first commit that appears
     * in both sets is the latest common ancestor
     * Runtime : O(N)
     * Analysis : Each commit is visited only once so if there are N commits in total
     * there are N visits causing the runtime to be O(N).

     * @param commit1 head commit of the current branch
     * @param commit2 Head commit of the given branch
     * @return commitID of the latest common ancestor
     */
    private static Commit getSplitPoint(Commit commit1, Commit commit2) {
        Set<String> branch1commits = bfs(commit1);
        Set<String> branch2commits = bfs(commit2);
        for (String commitID : branch1commits) {
            if (branch2commits.contains(commitID)) {
                return readObject(join(COMMITS, commitID), Commit.class);
            }
        }
        return null;
    }

    private static Set<String> bfs(Commit commit) {
        Set<String> visited = new LinkedHashSet<>();
        Queue<Commit> pending = new LinkedList<>();
        pending.add(commit);
        while (!pending.isEmpty()) {
            commit = pending.poll();
            if (visited.add(commit.getID())) { // Add to visited and check if it was already present
                String parentId1 = commit.getParentID(0);
                if (!parentId1.equals("")) {
                    pending.add(readObject(join(COMMITS, parentId1), Commit.class));
                }
                String parentId2 = commit.getParentID(1);
                if (!parentId2.equals("")) {
                    pending.add(readObject(join(COMMITS, parentId2), Commit.class));
                }
            }
        }
        return visited;
    }

    private static Commit[] checkMergeErr(String targetBranch) {
        String currentBranch = readContentsAsString(HEAD);
        if (!join(HEADS, targetBranch).exists()) {
            exit("A branch with that name does not exists.");
        }
        if (STAGED_FOR_ADD.list().length != 0 || STAGED_FOR_REMOVAL.list().length != 0) {
            exit("You have uncommitted changes.");
        }
        if (targetBranch.equals(readContentsAsString(HEAD))) {
            exit("Cannot merge a branch with itself.");
        }
        String targetCommitID = readContentsAsString(join(HEADS, targetBranch));
        Commit targetCommit = readObject(join(COMMITS, targetCommitID), Commit.class);
        Commit currentCommit = getCurrentCommit();
        Commit splitPoint = getSplitPoint(currentCommit, targetCommit);
        catchUntrackedFiles(currentCommit, targetCommit);
        if (splitPoint.getID().equals(readContentsAsString(join(HEADS, targetBranch)))) {
            exit("Given branch is an ancestor of the current branch.");
        }
        if (splitPoint.getID().equals(readContentsAsString(join(HEADS, currentBranch)))) {
            checkoutBranch(targetBranch);
            exit("Current branch fast-forwarded.");
        }
        return new Commit[]{currentCommit, targetCommit, splitPoint};
    }

    public static void merge(String targetBranch) {
        Commit[] heads = checkMergeErr(targetBranch);
        Commit currentCommit = heads[0];
        Commit targetCommit = heads[1];
        Commit splitPoint = heads[2];
        boolean gotConflict = false;
        HashMap<String, String> splitPointMap = splitPoint.getTrackedFiles();
        HashMap<String, String> currentMap = currentCommit.getTrackedFiles();
        HashMap<String, String> branchMap = targetCommit.getTrackedFiles();
        Stage stagingArea = getStagingArea();
        for (String filename : splitPointMap.keySet()) {
            if (currentMap.get(filename) != null) {
                if (currentMap.get(filename).equals(splitPointMap.get(filename))) {
                    if (!splitPointMap.get(filename).equals(branchMap.get(filename))) {
                        if (branchMap.get(filename) == null) {
                            rm(filename);
                        } else {
                            checkoutCommitFile(targetCommit.getID(), filename);
                            String blobID = targetCommit.getTrackedFileBlobID(filename);
                            stagingArea.addRec(filename, blobID);
                            join(BLOBS, blobID).renameTo(join(STAGED_FOR_ADD, blobID));
                        }
                    }
                } else {
                    if (!splitPointMap.get(filename).equals(branchMap.get(filename))) {
                        if (branchMap.get(filename) == null) {
                            String blobID = conflict(filename, currentCommit, targetCommit);
                            stagingArea.addRec(filename, blobID);
                            gotConflict = true;
                        } else {
                            if (!branchMap.get(filename).equals(currentMap.get(filename))) {
                                String blobID = conflict(filename, currentCommit, targetCommit);
                                stagingArea.addRec(filename, blobID);
                                gotConflict = true;
                            }
                        }
                    }
                }
            } else {
                if (!splitPointMap.get(filename).equals(branchMap.get(filename))) {
                    if (branchMap.get(filename) != null) {
                        String blobID = conflict(filename, currentCommit, targetCommit);
                        stagingArea.addRec(filename, blobID);
                        gotConflict = true;
                    }
                }
            }
        }
        Set<String> currentBranchFiles = currentCommit.getTrackedFiles().keySet();
        Set<String> splitPointFiles = splitPoint.getTrackedFiles().keySet();
        Set<String> givenBranchFiles = targetCommit.getTrackedFiles().keySet();
        givenBranchFiles.removeAll(splitPointFiles);
        for (String filename : givenBranchFiles) {
            if (currentBranchFiles.contains(filename)) {
                String currentVersion = currentCommit.getTrackedFileBlobID(filename);
                String otherVersion = targetCommit.getTrackedFileBlobID(filename);
                if (!currentVersion.equals(otherVersion)) {
                    String blobID = conflict(filename, currentCommit, targetCommit);
                    stagingArea.addRec(filename, blobID);
                    gotConflict = true;
                }
            }
        }
        givenBranchFiles.removeAll(currentBranchFiles);
        for (String filename : givenBranchFiles) {
            checkoutCommitFile(targetCommit.getID(), filename);
            String blobID = targetCommit.getTrackedFileBlobID(filename);
            stagingArea.addRec(filename, blobID);
            join(BLOBS, blobID).renameTo(join(STAGED_FOR_REMOVAL, blobID));
        }
        stagingArea.save();
        String commitMessage = "Merged " + targetBranch + " into "
                + readContentsAsString(HEAD) + ".";
        commit(commitMessage, targetCommit.getID());
        if (gotConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    private static String conflict(String filename, Commit currentCommit, Commit targetCommit) {
        String newContent = "<<<<<<< HEAD\n" + currentCommit.getTrackedFileContents(filename);
        newContent = newContent  + "=======\n";
        newContent = newContent + targetCommit.getTrackedFileContents(filename);
        newContent = newContent  + ">>>>>>>\n";
        writeContents(join(CWD, filename), newContent);
        Blob conflictBlob = new Blob(newContent, filename);
        writeObject(join(STAGED_FOR_ADD, conflictBlob.getID()), conflictBlob);
        return conflictBlob.getID();
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
