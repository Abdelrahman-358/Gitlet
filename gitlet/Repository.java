package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.File;
import java.io.IOException;

import static gitlet.Utils.*;


/**
 * Represents a gitlet repository.
 *  does at a high level.
 *
 * @author Abdelrahman Mostafa
 */
public class Repository implements Serializable {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /**
     * commits directory
     */
    public static final File COMMIT_DIR = join(GITLET_DIR, "commits");
    public static final File commitTree = join(COMMIT_DIR, "commitTree");
    /**
     * blobs directory
     */
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    /**
     * Head File
     */
    public static File HEAD = join(GITLET_DIR, "heads");
    /**
     * Branches File
     */
    public static File BRANCH = join(GITLET_DIR, "branches");
    /**
     * staging directory that contain staging for adding and staging for removing .
     */
    public static File StagingAreaDir = join(GITLET_DIR, "stagingArea");
    /**
     * staging for adding directory .
     */
    public static File StagingForAdding = join(StagingAreaDir, "stagingForAdding");
    /**
     * staging for removing directory.
     */
    public static File StagingForRemoving = join(StagingAreaDir, "stagingForRemoving");

    public static String head = "no";
    // last commit
    public static String firstParent;
    /**
     * track the loaded commits applying lazy load and cashing
     */
    private static Map<String, Integer> isLoaded = new TreeMap<>();
    private static Map<String, String> trackedByName = new TreeMap<>();
    /** ------------------------------------------------------------init command----------------------------------------- */

    /**
     * Initializes a new Gitlet version-control system in the current directory.
     * This system starts with one commit: the initial commit, which contains no files and has the commit message
     * "initial commit". It creates a single branch called `master`, which initially points to this initial commit.
     * The `master` branch is set as the current branch. The timestamp for the initial commit is set to the Unix Epoch
     * (00:00:00 UTC, Thursday, 1 January 1970). Since the initial commit is the same across all repositories created by
     * Gitlet, it will have the same UID, and all commits in all repositories will trace back to it.
     * <p>
     */
    public static void init() {
        if (isInitialized()) {
            errorMessage("A Gitlet version-control system already exists in the current directory.");
        }
        setupPersistence();

        Commit initialCommit = new Commit();
        String commitName = initialCommit.saveCommit();
        // at the initial commit the head and its parent is the same thing
        setHead(commitName);
        Branches.updateBranch("master", commitName);
        Branches.updateCurrentBranch("master");

    }
    /**--------------------------------------------------------------------------add command-----------------------------*/
    /**
     * Adds a copy of the file as it currently exists to the staging area (also called staging the file for addition).
     * Staging an already-staged file overwrites the previous entry in the staging area with the new contents.
     * The staging area should be located somewhere in `.gitlet`. If the current working version of the file is
     * identical to the version in the current commit, the file is not staged for addition and is removed from
     * the staging area if it is already there. Additionally, if the file was staged for removal, it is no longer
     * staged for removal after this command.
     *
     * @param fileName The name of the file to be staged for addition.
     */


    public static void add(String fileName) {
        checkInitialized();
        File file = new File(CWD, fileName);
        if (!file.exists()) {
            errorMessage("File does not exist.");
        }
        // fileSha is the name of the blob
        // at staging area the file stored with its original name
        // we compare the sha1 of the file and existing file at current commit

        if (isTheSameAsTheCurrentCommit(fileName)) {
            // remove it from staging area
            // and if staged for removal unstage in
            StagingArea.unstageFromAdd(fileName);
            StagingArea.unstageFromRemove(fileName);

        } else {
            StagingArea.stageForAdd(file, fileName);
        }

    }
    /**-----------------------------------------------------------------commit command-----------------------------------*/
    /**
     * Creates a new commit with the provided message. This commit:
     * - Loads the content of the parent commit.
     * - Removes files that are staged for removal (via the `rm` command).
     * - Adds files that are staged for addition.
     * - Clears the staging area after the commit.
     * - Adds the commit as a new node in the commit tree.
     * - Sets the head pointer to point to this new commit.
     * - Updates the parent of the new commit to point to the current head.
     * <p>
     * Each commit is identified by its SHA-1 ID, which includes references to its files (blobs),
     * parent reference, log message, and commit time.
     *
     * @param message The commit message describing the changes made in this commit.
     */
    public static void commit(String message) {
        checkInitialized();

        if (message == null) {
            errorMessage("Please enter a commit message.");
        }
        // get the files from staging file
        // remove from them the files tha staged to be removed

        copyTheLastCommitTrackedFiles();
        boolean x = copyFilesFromStagingArea();
        boolean y = removeFilesThatStagedTobeRemoved();

        if (!x & !y)
            errorMessage("No changes added to the commit.");

        firstParent = getHead();

        Commit commit = new Commit(message, firstParent, firstParent, trackedByName);
        String newHead = commit.saveCommit();

        Blob.saveBlobs(StagingArea.getFilesStagedForAddingFiles());

        setHead(newHead);

        StagingArea.clear();

        Branches.updateBranch(Branches.getCurrentBranch(), newHead);

    }
    /**----------------------------------------------------------------------------rm command----------------------------*/
    /**
     * Unstages the file if it is currently staged for addition. If the file is tracked in the current commit,
     * stages it for removal and removes the file from the working directory (unless the user has already done so).
     * The file is only removed if it is tracked in the current commit.
     *
     * @param fileName The name of the file to be removed or unstaged.
     */
    public static void rm(String fileName) {
        checkInitialized();

        if (StagingArea.isStagedForAdding(fileName)) {
            StagingArea.unstageFromAdd(fileName);
        } else if (trackedByCurrentCommit(fileName)) {
            StagingArea.stageForRemove(fileName, trackedByName.get(fileName));
            removeFileFromCWD(fileName);
        } else {
            errorMessage("No reason to remove the file.");
        }

    }
    /**----------------------------------------------------------------------------rm-Branch----------------------------*/
    /**
     * Deletes the branch with the given name. This operation only removes the pointer associated
     * with the branch; it does not delete any commits or other data created under the branch.
     *
     * @param branchName The name of the branch to delete. Must not be null or empty.
     */
    public static void rmBranch(String branchName) {
        checkInitialized();

        if (!Branches.branchExists(branchName)) {
            errorMessage("A branch with that name does not exist.");
        } else if (branchName.equals(Branches.getCurrentBranch())) {
            errorMessage("Cannot remove the current branch.");
        } else {
            Branches.removeBranch(branchName);
        }
    }
    /**----------------------------------------------------------------------------log command---------------------------*/
    /**
     * Starting at the current head commit, displays information about each commit backwards along the commit tree
     * until the initial commit. This follows the first parent commit links, ignoring any second parents found in merge
     * commits (similar to `git log --first-parent` in regular Git). This set of commit nodes is called the commit’s history.
     * For every node in this history, the information displayed includes the commit ID, the time the commit was made,
     * and the commit message.:
     */
    public static void log() {
        checkInitialized();

        String current = getHead();
        while (true) {
            Commit commit = Commit.getCommitByName(current);
            printCommit(current, commit.getMessage(), commit.getDate());
            String par = commit.getFirstParent();
            if (Objects.equals(par, null)) {
                break;
            }
            current = par;
        }
    }
    /**--------------------------------------------------------------------------- global-log------------------------*/
    /**
     * Displays the log of all commits in the repository. For each commit, it prints the commit ID,
     * commit message, and commit date in a formatted manner.
     */
    public static void global_log() {
        checkInitialized();

        List<String> ls = Utils.plainFilenamesIn(COMMIT_DIR);
        for (String s : ls) {
            Commit commit = Commit.getCommitByName(s);
            printCommit(s, commit.getMessage(), commit.getDate());
        }
    }
    /**---------------------------------------------------------------------------find-----------------------------------*/
    /**
     * Searches for and prints all commits in the `COMMIT_DIR` directory that match the given commit message.
     * If no commits with the specified message are found, an error message is displayed.
     *
     * @param message The commit message to search for. This is a case-sensitive string.
     */
    public static void find(String message) {
        checkInitialized();

        List<String> ls = Utils.plainFilenamesIn(COMMIT_DIR);

        boolean found = false;

        for (String s : ls) {
            Commit commit = Commit.getCommitByName(s);

            if (commit.getMessage().equals(message)) {
                found = true;
                System.out.println(s);
            }
        }
        if (!found) {
            errorMessage("Found no commit with that message.");
        }
    }
    /**---------------------------------------------------------------------------status---------------------------------*/
    /**
     * Displays the current status of the repository, including:
     * - Existing branches, with the current branch marked by an asterisk (*).
     * - Files staged for addition.
     * - Files staged for removal.
     * - Modifications not staged for commit.
     * - Untracked files.
     * <p>
     * The output follows a specific format to clearly present the repository's state
     */
    public static void status() {
        checkInitialized();

        printBranches();

        printStagedFiles();

        printRemovedFiles();

        printModificationsNotStagedForCommit();

        printUntrackedFiles();
    }
    /**--------------------------------------------------------------------------- checkout------------------------------*/
    /**
     * Checkout is a kind of general command that can do a few different things depending on what its arguments are.
     * There are 3 possible use cases. In each section below, you’ll see 3 numbered points. Each corresponds to the
     * respective usage of checkout.
     * <p>
     * java gitlet.Main checkout -- [file name]
     * <p>
     * java gitlet.Main checkout [commit id] -- [file name]
     * <p>
     * java gitlet.Main checkout [branch name]
     * Descriptions:
     * <p>
     * Takes the version of the file as it exists in the head commit and puts it in the working directory, overwriting
     * the version of the file that’s already there if there is one. The new version of the file is not staged.
     * <p>
     * Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory,
     * overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.
     * <p>
     * Takes all files in the commit at the head of the given branch, and puts them in the working directory, overwriting
     * the versions of the files that are already there if they exist. Also, at the end of this command, the given branch
     * will now be considered the current branch (HEAD). Any files that are tracked in the current branch but are not present
     * in the checked-out branch are deleted. The staging area is cleared, unless the checked-out branch is the current branch
     * (see Failure cases below).
     * Failure cases
     * 1: If the file does not exist in the previous commit, abort, printing the error message File does not exist in that commit. Do not change the CWD.
     */
    public static void checkout(String fileName) {
        checkInitialized();

        if (!trackedByCurrentCommit(fileName)) {
            errorMessage("File does not exist in that commit.");
        } else {
            loadFile(fileName, getHead());
        }
    }

    public static void checkout(String commitName, String fileName) {
        if (!Commit.commitExists(commitName)) {
            errorMessage("No commit with that id exists.");
        } else if (!trackedByCurrentCommit(fileName)) {
            errorMessage("File does not exist in that commit.");
        } else {
            loadFile(fileName, commitName);
        }
    }
    /**
     *
     * Takes all files in the commit at the head of the given branch, and puts them in the working directory, overwriting
     *      * the versions of the files that are already there if they exist. Also, at the end of this command, the given branch
     *      * will now be considered the current branch (HEAD). Any files that are tracked in the current branch but are not present
     *      * in the checked-out branch are deleted. The staging area is cleared, unless the checked-out branch is the current branch
     *      * (see Failure cases below).
     * */
    /*** If no branch with that name exists, print No such branch exists. If that branch is the current
     branch, print No need to checkout the current branch. If a working file is untracked in the current
     branch and would be overwritten by the checkout, print There is an untracked file in the way; delete it,
     or add and commit it first. and exit; perform this check before doing anything else. Do not change the CWD.
     */
    public static void checkoutBranch(String branchName) {
        checkInitialized();

        if (!Branches.branchExists(branchName)) {
            errorMessage("No such branch exists.");
        } else if (branchName.equals(Branches.getCurrentBranch())) {
            errorMessage("No need to checkout the current branch.");
        } else if (thereExistUnTrackedFile()) {
            errorMessage("There is an untracked file in the way; delete it, or add and commit it first.");
        } else {
            Branches.loadBranch(branchName);
        }

    }
    /**--------------------------------------------------------------------------- Branch------------------------------*/
    /**
     * Description: Creates a new branch with the given name, and points it at the current head commit. A branch is
     * nothing more than a name for a reference (a SHA-1 identifier) to a commit node. This command does NOT immediately
     * switch to the newly created branch (just as in real Git). Before you ever call branch, your code should be running
     * with a default branch called “master”.
     * <p>
     * Failure cases: If a branch with the given name already exists, print the error message A branch with that name already exists.
     */

    public static void branch(String branchName) {
        checkInitialized();

        if (Branches.branchExists(branchName)) {
            errorMessage("A branch with that name already exists.");
        } else {
            Branches.makeNewBranch(branchName);
        }
    }
    /**--------------------------------------------------------------------------- reset------------------------------*/
    /**
     * Description: Checks out all the files tracked by the given commit. Removes tracked files that are not present in that
     * commit. Also moves the current branch’s head to that commit node. See the intro for an example of what happens to the
     * head pointer after using reset. The [commit id] may be abbreviated as for checkout. The staging area is cleared. The
     * command is essentially checkout of an arbitrary commit that also changes the current branch head
     */
    public static void reset(String commitName) {
        checkInitialized();

        if (!Commit.commitExists(commitName)) {
            errorMessage("No commit with that id exists.");
        } else if (thereExistUnTrackedFile()) {
            errorMessage("There is an untracked file in the way; delete it, or add and commit it first.");
        } else {
            Commit.loadCommitFiles(commitName);
            Branches.updateBranch(Branches.getCurrentBranch(), commitName);
        }
    }
    /** --------------------------------------------------------------------------- merge----------------------------*/


    public static void merge(String branchName) {
        checkInitialized();

        if (!StagingArea.isAddingStageEmpty() || !StagingArea.isRemovalStageEmpty()) {
            errorMessage("You have uncommitted changes.");
        } else if (!Branches.branchExists(branchName)) {
            errorMessage("A branch with that name does not exist.");
        } else if (Branches.getCurrentBranch().equals(branchName)) {
            errorMessage("Cannot merge a branch with itself.");
        } else {
            // handle merge commit has no changes in it
            // handle untracked files error
            Branches.mergeBranch(branchName);
        }

    }
    /** --------------------------------------------------------------------------- add-Remote-------------------------*/
    public static void addRemote(String remotName,String remoteDirectory){
        //TODO:complete this function
    }
    /** --------------------------------------------------------------------------- remove-Remote----------------------*/
    public static void removeRemote(String remotName){
        //TODO:complete this function
    }
    /** --------------------------------------------------------------------------- push-------------------------------*/
    public static void push(String remoteName,String remoteBranchName){
        //TODO:complete this function

    }
    /** --------------------------------------------------------------------------- fetch------------------------------*/
    public static void fetch(String remoteName,String remoteBranchName){
        //TODO:complete this function

    }
    /** --------------------------------------------------------------------------- pull-------------------------------*/
    public static void pull(String remoteName,String remoteBranchName){
        //TODO:complete this function

    }

    /**
     * --------------------------------------------------------------------------- helper methods------------------------
     */

    public static void get(String branchName) {

        if (!Branches.branchExists(branchName)) {
            errorMessage("No such branch.");
        }
        File f = new File(BRANCH, branchName);
        Commit.getLowestCommonAncestor(getHead(), Utils.readContentsAsString(f));
    }

    /**
     * Loads the contents of a file from a specific commit into the current working directory.
     * <p>
     * This method retrieves the specified file from the given commit and writes its contents
     * to a file with the same name in the current working directory. If the file already exists,
     * it will be overwritten with the contents from the commit.
     *
     * @param fileName   The name of the file to be loaded from the commit.
     * @param commitName The name (or hash) of the commit from which the file is to be loaded.
     */

    public static void loadFile(String fileName, String commitName) {
        Commit commit = Commit.getCommitByName(commitName);
        String blobName = commit.getTrackedFileByName(fileName);
        File file = new File(Repository.CWD, fileName);
        File blob = Blob.getFile(blobName);
        Utils.writeContents(file, Utils.readContentsAsString(blob));
    }

    public static void printBranches() {
        System.out.println("=== Branches ===");
        List<String> files = Branches.getBranchFiles();
        for (String f : files) {
            if (f.equals(Branches.getCurrentBranch())) {
                System.out.print("*");
            }
            System.out.println(f);
        }
        System.out.println();
    }


    public static void printStagedFiles() {
        System.out.println("=== Staged Files ===");
        List<String> files = StagingArea.getStagedForAdding();
        for (String f : files) {
            System.out.println(f);
        }
        System.out.println();
    }

    public static void printRemovedFiles() {
        System.out.println("=== Removed Files ===");
        List<String> files = StagingArea.getStagedToBeRemoved();
        for (String f : files) {
            System.out.println(f);
        }
        System.out.println();
    }

    // TODO:fill out this function
    public static void printModificationsNotStagedForCommit() {
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
    }

    public static void printUntrackedFiles() {
        System.out.println("=== Untracked Files ===");
        List<String> list = getUntrackedFiles();
        for (String f : list) {
            System.out.println(f);
        }
        System.out.println();
    }

    /**
     * Knowing if the file with this name is tracked by current commit or not
     *
     * @return true if yes false otherwise
     */
    public static boolean trackedByCurrentCommit(String fileName) {
        copyTheLastCommitTrackedFiles();
        return trackedByName.containsKey(fileName);

    }

    public static boolean trackedByCommit(String commitName, String fileName) {
        Commit commit = Commit.getCommitByName(commitName);
        return commit.isFileTracked(fileName);
    }

    public static void copyTheLastCommitTrackedFiles() {
        if (isLoaded.containsKey(getHead())) {
            return;
        }
        isLoaded.put(getHead(), 1);
        String commitName = getHead();
        File f = Utils.join(COMMIT_DIR, commitName);
        Commit lastCommit = Utils.readObject(f, Commit.class);

        trackedByName = lastCommit.getTrackByName();

    }

    public static boolean copyFilesFromStagingArea() {

        File[] files = StagingArea.getFilesStagedForAddingFiles();
        if (files == null || files.length == 0) {
            return false;
        }
        for (File f : files) {
            String contentSha = sha1(toString(f));

            trackedByName.put(f.getName(), contentSha);

        }
        return true;
    }

    public static void printCommit(String name, String message, Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");
        String formattedDate = dateFormat.format(date);
        System.out.println("===");
        System.out.println("commit " + name);
        System.out.println("Date: " + formattedDate);
        System.out.println(message);
        System.out.println();
    }


    public static boolean removeFilesThatStagedTobeRemoved() {
        List<String> files = StagingArea.getStagedToBeRemoved();
        if (files.size() > 0) {
            for (String f : files) {
                trackedByName.remove(f);
            }
            return true;
        }
        return false;
    }


    /**
     * printing error message.
     */
    public static void errorMessage(String message) {
        System.err.println(message);
        System.exit(0);
    }

    /**
     * Sets up the directory structure and files required.
     */
    public static void setupPersistence() {
        GITLET_DIR.mkdirs();
        COMMIT_DIR.mkdirs();
        BLOBS_DIR.mkdirs();
        StagingAreaDir.mkdirs();
        StagingForAdding.mkdirs();
        StagingForRemoving.mkdirs();
        BRANCH.mkdirs();
        try {
            HEAD.createNewFile();

        } catch (IOException e) {
            errorMessage("Error creating head");
        }
    }

    /**
     * function that return the head of the current branch.
     */
    public static String getHead() {
        // Check if the head is already cached
        if (head.equals("no")) {
            head = Utils.readContentsAsString(HEAD);
        }
        return head;
    }

    public static boolean thereExistUnTrackedFile() {

        return (getUntrackedFiles().size() != 0);
    }

    public static List<String> getCWDFiles() {
        return Utils.plainFilenamesIn(CWD);
    }

    public static List<String> getUntrackedFiles() {
        List<String> CWDFiles = getCWDFiles();
        List<String> stagedFiles = StagingArea.getStagedForAdding();
        Map<String, String> trackedByCommit = getTrackedFilesByCommit(getHead());
        Map<String, String> tracked = new TreeMap<>();
        ;
        for (String s : trackedByCommit.keySet()) {
            tracked.put(s, s);
        }
        for (String s : stagedFiles) {
            tracked.put(s, s);
        }
        List<String> untrackedFiles = new ArrayList<>();
        for (String s : CWDFiles) {
            if (!tracked.containsKey(s)) {
                untrackedFiles.add(s);
            }
        }

        return untrackedFiles;
    }

    public static Map<String, String> getTrackedFilesByCommit(String commitName) {
        Commit commit = Commit.getCommitByName(commitName);
        Map<String, String> list = commit.getTrackByName();
        return list;
    }


    /**
     * updating the head.
     */
    public static void setHead(String name) {
        File file = new File(GITLET_DIR, "heads");
        Utils.writeContents(file, name);
        head = name;
    }

    /**
     * Knowing if the current working version of the file
     * is identical to the version in the current commit
     */
    public static boolean isTheSameAsTheCurrentCommit(String fileName) {
        copyTheLastCommitTrackedFiles();
        File file = new File(CWD, fileName);
        String contentSha = sha1(toString(file));
        return (contentSha.equals(trackedByName.get(fileName)));
    }

    /**
     * function to check if we initialize a gitlet directory.
     */
    public static boolean isInitialized() {
        return GITLET_DIR.exists();
    }

    // removing file from current working directory
    public static void removeFileFromCWD(String fileName) {
        File file = new File(CWD, fileName);
        if (file.exists()) {
            file.delete();
        }
    }
    public static void checkInitialized() {
        if (!isInitialized()) {
            errorMessage("Not in an initialized Gitlet directory.");
        }
    }

    /**
     * To Know if the file exist in the current working directory or not
     */
    public static boolean fileExistInCWD(File file) {
        return file.exists();
    }

    public static String toString(File file) {
        return (Utils.readContentsAsString(file) + file.getName());
    }

}
