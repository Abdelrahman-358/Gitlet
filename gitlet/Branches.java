package gitlet;


import java.io.File;
import java.util.*;

import static gitlet.Utils.readContentsAsString;
/**
 * Represents a gitlet Branch object.
 *  does at a high level.
 *
 * @author Abdelrahman Mostafa
 */
public class Branches {
    public static final File BRANCH = Repository.BRANCH;
    public static String currentBranch = "no";


    public static void makeNewBranch(String branchName) {
        File file = new File(BRANCH, branchName);
        Utils.writeContents(file, Repository.getHead());
    }

    public static void loadBranch(String branchName) {

        File file = new File(BRANCH, branchName);
        String commitName = readContentsAsString(file);
        Commit.loadCommitFiles(commitName);
        updateCurrentBranch(branchName);
    }

    /**
     * 1-if the file modified in the given branch since the split point and not modified in the current modify it and stage it
     * 2-if the file modified in the current branch since the split point  but not in the given branch  stay as it is.
     * 3-if the file modified in both current and given as the same way is left unchanged by the merge. and if it removed but there exist a file with the same name
     * that file is left alone not tracked nor staged in the merge
     * 4-if the file that was not present at the split point and are present only in the current branch it remains the same
     * 5-if the file that was not present at the split point and are present only in the given branch it checked out and staged
     * 6-if the file present at the split point unmodified it the current branch and absent in the given branch should be removed
     * 7-if the file present at the split point unmodified in the given branch and absent in the current branch should remain absent
     */
    public static void mergeBranch(String branchName) {
        File file = new File(BRANCH, branchName);
        String branchCommitName = readContentsAsString(file);
        Commit currentCommit = Commit.getCommitByName(Repository.getHead());
        Commit givenCommit = Commit.getCommitByName(branchCommitName);
        Commit splitCommit = Commit.getLowestCommonAncestor(Repository.getHead(), branchCommitName);
        if (splitCommit != null && splitCommit.equals(givenCommit)) {
            Repository.errorMessage("Given branch is an ancestor of the current branch.");
        }
        if (splitCommit != null && splitCommit.equals(currentCommit)) {
            Repository.checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        Set<String> files = new HashSet<String>();
        boolean conflict = false;
        files.addAll(currentCommit.getTrackByName().keySet());
        files.addAll(givenCommit.getTrackByName().keySet());
        files.addAll(splitCommit.getTrackByName().keySet());
        for (String fileName : files) {
            String current = currentCommit.getTrackByName().get(fileName);
            String given = givenCommit.getTrackByName().get(fileName);
            String split = splitCommit.getTrackByName().get(fileName);
            // 1-if the file modified in the given branch since the split point and not modified in the current modify it and stage it
            if (given != null && split != null && split.equals(current) && !split.equals(given)) {
                // load the file from given commit and stage it for addition
                Repository.loadFile(fileName, branchCommitName);
                File updated = Blob.getFile(splitCommit.getTrackByName().get(fileName));
                StagingArea.stageForAdd(updated, fileName);
            }
            // 2-if the file modified in the current branch since the split point  but not in the given branch  stay as it is.
            else if (split != null && current != null && !split.equals(current) && split.equals(given)) {
                // load file from current
                Repository.loadFile(fileName, Repository.getHead());
            } else if (split == null && given == null && current != null) {
                //load file from current
                Repository.loadFile(fileName, Repository.getHead());
            } else if (split == null && current == null && given != null) {
                // load it from given and stage it for addition
                Repository.loadFile(fileName, branchCommitName);
                File updated = Blob.getFile(splitCommit.getTrackByName().get(fileName));
                StagingArea.stageForAdd(updated, fileName);
            } else if (split != null && given == null && split.equals(current)) {
                // remove it from current and stage it for removal
                Repository.removeFileFromCWD(fileName);
                StagingArea.stageForRemove(fileName, currentCommit.getTrackByName().get(fileName));
            } else if (split != null && current == null && split.equals(given)) {
                // do nothing
            } else if (!Objects.equals(split, current) && !Objects.equals(current, given) && !Objects.equals(split, given)) {
                // conflict
                conflict = true;
                String currentContent = readContentsAsString(Blob.getFile(currentCommit.getTrackByName().get(fileName)));
                String givenContent = readContentsAsString(Blob.getFile(givenCommit.getTrackByName().get(fileName)));
                String finalContent = "<<<<<<< HEAD\n" + currentContent + "=======\n" + givenContent + ">>>>>>>\n";
                File newFile = new File(Repository.CWD, fileName);
                Utils.writeContents(newFile, finalContent);
                StagingArea.stageForAdd(newFile, fileName);
            }
        }
        String commitMessage = "Merged " + branchName + " into " + getCurrentBranch() + ".";
        Repository.commit(commitMessage);
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /**
     * Updates the reference commit for a specific branch in the repository.
     * This method writes the provided commit reference (`refCommit`) to a file
     * named after the branch (`branchName`) in the current working directory (CWD).
     *
     * @param branchName The name of the branch to update. This will be used as the filename
     *                   where the commit reference is stored. Must not be null or empty.
     * @param refCommit  The commit reference (e.g., a commit hash) to associate with the branch.
     */
    public static void updateBranch(String branchName, String refCommit) {
        File file = new File(BRANCH, branchName);
        Utils.writeContents(file, refCommit);
    }

    /**
     * Updates the current branch in the repository to the specified branch name.
     * This method writes the provided branch name (`branchName`) to a file named
     * "currentBranch" in the branch directory (`BRANCH`).
     *
     * @param branchName The name of the branch to set as the current branch. This will be
     *                   written to the "currentBranch" file. Must not be null or empty.
     */
    public static void updateCurrentBranch(String branchName) {
        File file = new File(Repository.GITLET_DIR, "currentBranch");
        Utils.writeContents(file, branchName);
    }

    /**
     * Retrieves the name of the current branch in the repository.
     * If the current branch is not already cached (i.e., it is set to "no"),
     * this method reads the branch name from the "currentBranch" file in the
     * current working directory (CWD) and caches it for future use.
     *
     * @return The name of the current branch. This value is read from the
     * "currentBranch" file if not already cached.
     */
    public static String getCurrentBranch() {
        if (currentBranch.equals("no")) {
            File file = new File(Repository.GITLET_DIR, "currentBranch");
            currentBranch = readContentsAsString(file);
        }
        return currentBranch;
    }

    public static void removeBranch(String branchName) {
        File file = new File(BRANCH, branchName);
        file.delete();
    }

    public static boolean branchExists(String branchName) {
        File file = new File(BRANCH, branchName);
        return file.exists();
    }

    public static List<String> getBranchFiles() {
        if (BRANCH.exists()) {
            return Utils.plainFilenamesIn(BRANCH);
        }
        return null;
    }

}
