package gitlet;

import java.io.File;
import java.util.List;
/**
 * Represents a gitlet StagingArea object.
 *  does at a high level.
 *
 * @author Abdelrahman Mostafa
 */
public class StagingArea {

    // Directory for staging files
    public static final File STAGING_FOR_ADDING = Repository.StagingForAdding;
    public static final File STAGING_FOR_REMOVING = Repository.StagingForRemoving;

    /**
     * Stages a file for addition. If the file already exists in the staging area, it is overwritten.
     *
     * @param file     The file to be staged.
     * @param fileName The name of the file.
     */
    public static void stageForAdd(File file, String fileName) {
        File stagedFile = new File(STAGING_FOR_ADDING, fileName);
        Utils.writeContents(stagedFile, Utils.readContentsAsString(file));
    }

    /**
     * Stages a file for removal.
     *
     * @param fileName    The name of the file to be staged for removal.
     * @param fileContent The content of the file.
     */
    public static void stageForRemove(String fileName, String fileContent) {
        File stagedFile = new File(STAGING_FOR_REMOVING, fileName);
        Utils.writeContents(stagedFile, fileContent);
    }

    /**
     * Removes a file from the staging area for addition.
     *
     * @param fileName The name of the file to be removed.
     */
    public static void unstageFromAdd(String fileName) {
        removeFile(new File(STAGING_FOR_ADDING, fileName));
    }

    /**
     * Removes a file from the staging area for removal.
     *
     * @param fileName The name of the file to be removed.
     */
    public static void unstageFromRemove(String fileName) {
        removeFile(new File(STAGING_FOR_REMOVING, fileName));
    }

    /**
     * Helper method to remove a file if it exists.
     *
     * @param file The file to be removed.
     */
    private static void removeFile(File file) {
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Returns an array of files staged for addition.
     *
     * @return An array of files staged for addition, or an empty array if none exist.
     */
    public static List<String> getStagedForAdding() {
        List<String> list= Utils.plainFilenamesIn(STAGING_FOR_ADDING);
            return list;
    }
    public static boolean isAddingStageEmpty() {
        return STAGING_FOR_ADDING.listFiles().length == 0;
    }
    public static File[] getFilesStagedForAddingFiles() {
       return STAGING_FOR_ADDING.listFiles();
    }

    /**
     * Returns an array of files staged for removal.
     *
     * @return An array of files staged for removal, or an empty array if none exist.
     */
    public static List<String> getStagedToBeRemoved() {
        List<String> list= Utils.plainFilenamesIn(STAGING_FOR_REMOVING);
        return list;
    }
    public static boolean isRemovalStageEmpty() {
        return STAGING_FOR_REMOVING.listFiles().length == 0;
    }

    /**
     * Checks if a file is staged for addition.
     *
     * @param fileName The name of the file to check.
     * @return True if the file is staged for addition, false otherwise.
     */
    public static boolean isStagedForAdding(String fileName) {
        return new File(STAGING_FOR_ADDING, fileName).exists();
    }

    /**
     * Clears the staging area by deleting all files staged for addition and removal.
     */
    public static void clear() {
        clearDirectory(STAGING_FOR_ADDING);
        clearDirectory(STAGING_FOR_REMOVING);
    }

    /**
     * Helper method to clear all files in a directory.
     *
     * @param directory The directory to clear.
     */
    private static void clearDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        }
    }
}