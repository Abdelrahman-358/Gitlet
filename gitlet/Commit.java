package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a gitlet commit object.
 *  does at a high level.
 *
 * @author Abdelrahman Mostafa
 */
public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */
    /**
     * The message of this Commit.
     */
    private final String message;
    private String name = null;
    /**
     * the date of this commit.
     */
    private final Date date;
    /**
     * parent of the commit.
     */
    private final String firstParent;
    private final String secondParent;
    /**
     * that contains the referencing files.
     */
    // Map Store Name -> sha
    private final Map<String, String> trackByName;
    // Map Store sha -> Name
    private final Map<String, String> trackBySha;

    /**
     * initial commit
     */
    public Commit() {
        this.message = "initial commit";
        this.date = new Date(0);
        this.firstParent = null;
        this.secondParent = null;
        this.trackByName = new TreeMap<>();
        this.trackBySha = new TreeMap<>();

    }

    public Commit(String message, String firstParent, String secondParent, Map<String, String> tracked) {
        if (message == null) {
            Repository.errorMessage("Please enter a commit message.");
        }
        this.message = message;
        this.date = new Date();
        this.firstParent = firstParent;
        this.secondParent = secondParent;
        // tacked sha -> file
        this.trackBySha = new TreeMap<>();
        ;
        this.trackByName = new TreeMap<>();
        ;
        for (Map.Entry<String, String> entry : tracked.entrySet()) {
            trackByName.put(entry.getKey(), entry.getValue());
            trackBySha.put(entry.getValue(), entry.getKey());
        }
    }

    public String saveCommit() {

        String name = Utils.sha1(this.toString());
        File f = new File(Repository.COMMIT_DIR, name);
        this.name = name;
        Utils.writeObject(f, this);
        return name;
    }

    /**
     * Loads the files associated with a specific commit into the current working directory (CWD).
     * This function performs the following operations:
     * <p>
     * Retrieves the tracked files for the given commit using {@link Repository#getTrackedFilesByCommit(String)}.
     * Retrieves the list of files currently present in the CWD using {@link Repository#getCWDFiles()}.
     * Deletes any files in the CWD that are not part of the tracked files for the specified commit.
     * Writes the contents of the tracked files (blobs) from the commit into the corresponding files in the CWD.
     *
     * @param commitName The name of the commit whose files are to be loaded into the CWD.
     *                   This should correspond to a valid commit in the repository.
     * @see Repository#getTrackedFilesByCommit(String)
     * @see Repository#getCWDFiles()
     */
    public static void loadCommitFiles(String commitName) {
        Map<String, String> wanted = Repository.getTrackedFilesByCommit(commitName);
        List<String> CWDFiles = Repository.getCWDFiles();
        for (String fileName : CWDFiles) {
            if (!wanted.containsKey(fileName)) {
                File del = new File(Repository.CWD, fileName);
                if (del.exists()) {
                    del.delete();
                }
            }
        }
        for (Map.Entry<String, String> entry : wanted.entrySet()) {
            String fileName = entry.getKey();
            String blobName = entry.getValue();

            File blobFile = new File(Repository.BLOBS_DIR, blobName);


            File newFile = new File(Repository.CWD, fileName);
            Utils.writeContents(newFile, Utils.readContentsAsString(blobFile));
        }
    }


    public static Commit getLowestCommonAncestor(String firstCommit, String secondCommit) {
        Commit first = Commit.getCommitByName(firstCommit);
        Commit second = Commit.getCommitByName(secondCommit);
        Set<String> set = getCommitList(first).stream().map(Commit::getName).collect(Collectors.toSet());
        Commit split = getCommitList(second).stream()
                .filter(commit -> set.contains(commit.getName()))
                .max(Comparator.comparing(Commit::getDate))
                .orElse(null);

        return split;
    }

    public static List<Commit> getCommitList(Commit commit) {
        List<Commit> ans = new ArrayList<>();
        dfs(commit, new HashSet<>(), ans);
        return ans;
    }

    public static void dfs(Commit current, Set<String> visited, List<Commit> list) {
        list.add(current);
        visited.add(current.getName());
        String first = current.getFirstParent();
        if (first != null && !visited.contains(first)) {
            dfs(getCommitByName(first), visited, list);
        }
        String second = current.getSecondParent();
        if (second != null && !visited.contains(second)) {
            dfs(getCommitByName(second), visited, list);
        }
    }


    /**
     * return commit object using commit name
     */
    public static Commit getCommitByName(String name) {
        File f = new File(Repository.COMMIT_DIR, name);
        Commit commit = Utils.readObject(f, Commit.class);
        return commit;
    }

    public static boolean commitExists(String commitName) {
        File f = new File(Repository.COMMIT_DIR, commitName);
        return f.exists();
    }

    /**
     * getters
     */
    public String getTrackedFileByName(String name) {
        return this.trackByName.get(name);
    }

    public String getMessage() {
        return this.message;
    }

    public String getName() {
        return this.name;
    }

    public Date getDate() {
        return this.date;
    }

    public String getFirstParent() {
        return this.firstParent;
    }

    public String getSecondParent() {
        return this.secondParent;
    }

    public Map<String, String> getTrackByName() {
        return this.trackByName;
    }

    public Map<String, String> getTrackBySha() {
        return this.trackBySha;
    }

    public boolean isFileTracked(String fileName) {
        return this.trackByName.containsKey(fileName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Message: ").append(message).append("\n");
        sb.append("Date: ").append(date).append("\n");
        sb.append("firstParent: ").append(firstParent).append("\n");
        sb.append("secondParent: ").append(secondParent).append("\n");
        sb.append("Tracked Files:\n");
        for (Map.Entry<String, String> entry : trackBySha.entrySet()) {
            sb.append("  ").append(entry.getKey()).append("\n");
        }
        for (Map.Entry<String, String> entry : trackByName.entrySet()) {
            sb.append("  ").append(entry.getKey()).append("\n");
        }
        return sb.toString();
    }


}
