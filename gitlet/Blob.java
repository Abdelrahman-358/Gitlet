package gitlet;

import java.io.File;

public class Blob {

    // Directory for storing blobs
    public static final File BLOB_DIRECTORY = Repository.BLOBS_DIR;

    /**
     * Saves the contents of the given files as blobs in the blob directory.
     * If a blob with the same content already exists, it is not overwritten.
     *
     * @param files The files to be saved as blobs.
     */
    public static void saveBlobs(File[] files) {
        if (files == null) {
            return;
        }

        if (!BLOB_DIRECTORY.exists()) {
            BLOB_DIRECTORY.mkdirs();
        }

        for (File file : files) {
            String fileName = Repository.toString(file);
            fileName=Utils.sha1(fileName);
            saveBlob(file, fileName);
        }
    }

    /**
     * Saves a single file as a blob in the blob directory.
     *
     * @param file     The file to be saved as a blob.
     * @param fileName The name of the file.
     */
    private static void saveBlob(File file, String fileName) {
        File blobFile = new File(BLOB_DIRECTORY, fileName);
        if (!blobFile.exists()) {
            Utils.writeContents(blobFile, Utils.readContentsAsString(file));
        }
    }

    /**
     * Retrieves a blob file by its SHA-1 name.
     *
     * @param shaName The SHA-1 name of the blob.
     * @return The blob file, or null if it does not exist.
     */
    public static File getFile(String shaName) {
        File blobFile = new File(BLOB_DIRECTORY, shaName);
        return blobFile.exists() ? blobFile : null;
    }
}