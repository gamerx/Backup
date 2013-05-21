/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * (modified version)
 */
package com.bukkitbackup.full.utils;

import com.bukkitbackup.full.threading.BackupTask;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * General file manipulation utilities. <p> Facilities are provided in the
 * following areas: <ul> <li>writing to a file <li>reading from a file <li>make
 * a directory including parent directories <li>copying files and directories
 * <li>deleting files and directories <li>converting to and from a URL
 * <li>listing files and directories by filter and extension <li>comparing file
 * content <li>file last changed date <li>calculating a checksum </ul> <p>
 * Origin of code: Excalibur, Alexandria, Commons-Utils
 *
 * @author <a href="mailto:burton@relativity.yi.org">Kevin A. Burton</A>
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author <a href="mailto:Christoph.Reck@dlr.de">Christoph.Reck</a>
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 * @author <a href="mailto:jefft@apache.org">Jeff Turner</a>
 * @author Matthew Hawthorne
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 * @author Stephen Colebourne
 * @author Ian Springer
 * @author Chris Eldredge
 * @author Jim Harrington
 * @author Niall Pemberton
 * @author Sandy McArthur
 * @version $Id: FileUtils.java 1003647 2010-10-01 20:53:59Z niallp $
 */
public class FileUtils {

    public static int BUFFER_SIZE = 10240;
    /**
     * The number of bytes in a kilobyte.
     */
    public static final long ONE_KB = 1024;
    /**
     * The number of bytes in a megabyte.
     */
    public static final long ONE_MB = ONE_KB * ONE_KB;
    /**
     * The number of bytes in a 50 MB.
     */
    private static final long FIFTY_MB = ONE_MB * 50;
    /**
     * The number of bytes in a gigabyte.
     */
    /**
     * The system separator character.
     */
    private static final char SYSTEM_SEPARATOR = File.separatorChar;
    /**
     * The Windows separator character.
     */
    private static final char WINDOWS_SEPARATOR = '\\';

    /**
     * Copies a whole directory to a new location preserving the file dates.
     * This method copies the specified directory and all its child directories
     * and files to the specified destination. The destination is the new
     * location and name of the directory. <p> The destination directory is
     * created if it does not exist. If the destination directory did exist,
     * then this method merges the source with the destination, with the source
     * taking precedence. <p> <strong>Note:</strong> This method tries to
     * preserve the files' last modified date/times using {@link File#setLastModified(long)},
     * however it is not guaranteed that those operations will succeed. If the
     * modification operation fails, no indication is provided.
     *
     * @param srcDir an existing directory to copy, must not be
     * <code>null</code>
     * @param destDir the new directory, must not be
     * <code>null</code>
     *
     * @throws NullPointerException if source or destination is
     * <code>null</code>
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     * @since Commons IO 1.1
     */
    public static void copyDirectory(String srcDir, String destDir) throws IOException {
        copyDirectory(new File(srcDir), new File(destDir), true);
    }

    /**
     * Copies a whole directory to a new location. <p> This method copies the
     * contents of the specified source directory to within the specified
     * destination directory. <p> The destination directory is created if it
     * does not exist. If the destination directory did exist, then this method
     * merges the source with the destination, with the source taking
     * precedence. <p> <strong>Note:</strong> Setting
     * <code>preserveFileDate</code> to
     * <code>true</code> tries to preserve the files' last modified date/times
     * using {@link File#setLastModified(long)}, however it is not guaranteed
     * that those operations will succeed. If the modification operation fails,
     * no indication is provided.
     *
     * @param srcDir an existing directory to copy, must not be
     * <code>null</code>
     * @param destDir the new directory, must not be
     * <code>null</code>
     * @param preserveFileDate true if the file date of the copy should be the
     * same as the original
     *
     * @throws NullPointerException if source or destination is
     * <code>null</code>
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     * @since Commons IO 1.1
     */
    private static void copyDirectory(File srcDir, File destDir, boolean preserveFileDate) throws IOException {
        copyDirectory(srcDir, destDir, null, preserveFileDate);
    }

    /**
     * Copies a filtered directory to a new location. <p> This method copies the
     * contents of the specified source directory to within the specified
     * destination directory. <p> The destination directory is created if it
     * does not exist. If the destination directory did exist, then this method
     * merges the source with the destination, with the source taking
     * precedence. <p> <strong>Note:</strong> Setting
     * <code>preserveFileDate</code> to
     * <code>true</code> tries to preserve the files' last modified date/times
     * using {@link File#setLastModified(long)}, however it is not guaranteed
     * that those operations will succeed. If the modification operation fails,
     * no indication is provided.
     *
     * <h4>Example: Copy directories only</h4>
     * <pre>
     *  // only copy the directory structure
     *  FileUtils.copyDirectory(srcDir, destDir, DirectoryFileFilter.DIRECTORY, false);
     * </pre>
     *
     * <h4>Example: Copy directories and txt files</h4>
     * <pre>
     *  // Create a filter for ".txt" files
     *  IOFileFilter txtSuffixFilter = FileFilterUtils.suffixFileFilter(".txt");
     *  IOFileFilter txtFiles = FileFilterUtils.andFileFilter(FileFileFilter.FILE, txtSuffixFilter);
     *
     *  // Create a filter for either directories or ".txt" files
     *  FileFilter filter = FileFilterUtils.orFileFilter(DirectoryFileFilter.DIRECTORY, txtFiles);
     *
     *  // Copy using the filter
     *  FileUtils.copyDirectory(srcDir, destDir, filter, false);
     * </pre>
     *
     * @param srcDir an existing directory to copy, must not be
     * <code>null</code>
     * @param destDir the new directory, must not be
     * <code>null</code>
     * @param filter the filter to apply, null means copy all directories and
     * files
     * @param preserveFileDate true if the file date of the copy should be the
     * same as the original
     *
     * @throws NullPointerException if source or destination is
     * <code>null</code>
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     * @since Commons IO 1.4
     */
    public static void copyDirectory(File srcDir, File destDir,
            FileFilter filter, boolean preserveFileDate) throws IOException {
        if (srcDir == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (destDir == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (!srcDir.exists()) {
            throw new FileNotFoundException("Source '" + srcDir + "' does not exist");
        }
        if (!srcDir.isDirectory()) {
            throw new IOException("Source '" + srcDir + "' exists but is not a directory");
        }
        if (srcDir.getCanonicalPath().equals(destDir.getCanonicalPath())) {
            throw new IOException("Source '" + srcDir + "' and destination '" + destDir + "' are the same");
        }

        // Cater for destination being directory within the source directory (see IO-141)
        List<String> exclusionList = null;
        if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath())) {
            File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
            if (srcFiles != null && srcFiles.length > 0) {
                exclusionList = new ArrayList<String>(srcFiles.length);
                for (File srcFile : srcFiles) {
                    File copiedFile = new File(destDir, srcFile.getName());
                    exclusionList.add(copiedFile.getCanonicalPath());
                }
            }
        }
        doCopyDirectory(srcDir, destDir, filter, preserveFileDate, exclusionList);
    }

    /**
     * Internal copy directory method.
     *
     * @param srcDir the validated source directory, must not be
     * <code>null</code>
     * @param destDir the validated destination directory, must not be
     * <code>null</code>
     * @param filter the filter to apply, null means copy all directories and
     * files
     * @param preserveFileDate whether to preserve the file date
     * @param exclusionList List of files and directories to exclude from the
     * copy, may be null
     * @throws IOException if an error occurs
     * @since Commons IO 1.1
     */
    private static void doCopyDirectory(File srcDir, File destDir, FileFilter filter, boolean preserveFileDate, List<String> exclusionList) throws IOException {
        // recurse
        File[] files = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
        if (files == null) // null if security restricted
        {
            throw new IOException("Failed to list contents of " + srcDir);
        }
        if (destDir.exists()) {
            if (!destDir.isDirectory()) {
                throw new IOException("Destination '" + destDir + "' exists but is not a directory");
            }
        } else if (!destDir.mkdirs()) {
            throw new IOException("Destination '" + destDir + "' directory cannot be created");
        }
        if (!destDir.canWrite()) {
            throw new IOException("Destination '" + destDir + "' cannot be written to");
        }
        for (File file : files) {
            File copiedFile = new File(destDir, file.getName());
            if (exclusionList == null || !exclusionList.contains(file.getCanonicalPath())) {
                if (file.isDirectory()) {
                    doCopyDirectory(file, copiedFile, filter, preserveFileDate, exclusionList);
                } else {
                    doCopyFile(file, copiedFile, preserveFileDate);
                }
            }
        }

        // Do this last, as the above has probably affected directory metadata
        if (preserveFileDate) {
            destDir.setLastModified(srcDir.lastModified());
        }
    }

    /**
     * Internal copy file method.
     *
     * @param srcFile the validated source file, must not be
     * <code>null</code>
     * @param destFile the validated destination file, must not be
     * <code>null</code>
     * @param preserveFileDate whether to preserve the file date
     * @throws IOException if an error occurs
     */
    private static void doCopyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException {
        if (destFile.exists() && destFile.isDirectory()) {
            throw new IOException("Destination '" + destFile + "' exists but is a directory");
        }
        if (!srcFile.exists()) {
            throw new IOException("Source file '" + srcFile + "' does not exist");
        }
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel input = null;
        FileChannel output = null;
        try {
            fis = new FileInputStream(srcFile);
            fos = new FileOutputStream(destFile);
            input = fis.getChannel();
            output = fos.getChannel();
            long size = input.size();
            long pos = 0;
            long count = 0;
            while (pos < size) {
                count = (size - pos) > FIFTY_MB ? FIFTY_MB : (size - pos);
                pos += output.transferFrom(input, pos, count);
            }
        } finally {
            closeQuietly(output);
            closeQuietly(fos);
            closeQuietly(input);
            closeQuietly(fis);
        }

        if (srcFile.length() != destFile.length()) {
            throw new IOException("Failed to copy full contents from '"
                    + srcFile + "' to '" + destFile + "'");
        }
        if (preserveFileDate) {
            destFile.setLastModified(srcFile.lastModified());
        }
    }

    /**
     * Unconditionally close a
     * <code>Closeable</code>. <p> Equivalent to {@link Closeable#close()},
     * except any exceptions will be ignored. This is typically used in finally
     * blocks. <p> Example code:
     * <pre>
     *   Closeable closeable = null;
     *   try {
     *       closeable = new FileReader("foo.txt");
     *       // process closeable
     *       closeable.close();
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(closeable);
     *   }
     * </pre>
     *
     * @param closeable the object to close, may be null or already closed
     * @since Commons IO 2.0
     */
    private static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    /**
     * Deletes a directory recursively.
     *
     * @param directory directory to delete
     * @throws IOException in case deletion is unsuccessful
     */
    public static void deleteDirectory(File directory) throws IOException {

        // Check provided file exists.
        if (!directory.exists()) {
            return;
        }
        
        cleanDirectory(directory);

        // Attempt deletion.
        if (!directory.delete()) {
            String message = "Unable to delete directory " + directory + ".";
            throw new IOException(message);
        }
    }

    /**
     * Recursively empties a directory without deleting it.
     *
     * @param directory directory to recursively empty
     * @throws IOException in case cleaning is unsuccessful
     */
    private static void cleanDirectory(File directory) throws IOException {

        // Check it exists.
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        // Check it is a directory.
        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        // Retrieve contents.
        File[] files = directory.listFiles();
        if (files == null) {
            throw new SecurityException("Failed to list contents of " + directory);
        }

        // Attempt file deletion.
        IOException exception = null;
        for (File file : files) {
            try {
                forceDelete(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }

        // Do we need to throw an exception?
        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Deletes a file. If file is a directory, delete it and all
     * sub-directories. <p> The difference between File.delete() and this method
     * are: <ul> <li>A directory to be deleted does not have to be empty.</li>
     * <li>You get exceptions when a file or directory cannot be deleted.
     * (java.io.File methods returns a boolean)</li> </ul>
     *
     * @param file file or directory to delete, must not be
     * <code>null</code>
     * @throws NullPointerException if the directory is
     * <code>null</code>
     * @throws FileNotFoundException if the file was not found
     * @throws IOException in case deletion is unsuccessful
     */
    private static void forceDelete(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            boolean filePresent = file.exists();
            if (!file.delete()) {
                if (!filePresent) {
                    throw new FileNotFoundException("File does not exist: " + file);
                }
                String message =
                        "Unable to delete file: " + file;
                throw new IOException(message);
            }
        }
    }

    /**
     * Determines whether the specified file is a Symbolic Link rather than an
     * actual file. <p> Will not return true if there is a Symbolic Link
     * anywhere in the path, only if the specific file is.
     *
     * @param file the file to check
     * @return true if the file is a Symbolic Link
     * @throws IOException if an IO error occurs while checking the file
     * @since Commons IO 2.0
     */
    private static boolean isSymlink(File file) throws IOException {
        if (file == null) {
            throw new NullPointerException("File must not be null");
        }
        if (isSystemWindows()) {
            return false;
        }
        File fileInCanonicalDir = null;
        if (file.getParent() == null) {
            fileInCanonicalDir = file;
        } else {
            File canonicalDir = file.getParentFile().getCanonicalFile();
            fileInCanonicalDir = new File(canonicalDir, file.getName());
        }

        return !fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile());
    }

    /**
     * Determines if Windows file system is in use.
     *
     * @return true if the system is Windows
     */
    private static boolean isSystemWindows() {
        return SYSTEM_SEPARATOR == WINDOWS_SEPARATOR;
    }
    public final static String LINE_SEPARATOR = System.getProperty("line.separator");
    public final static String FILE_SEPARATOR = System.getProperty("file.separator");

    /**
     * Zip up a directory
     *
     * @param directory
     * @param zipName
     * @throws IOException
     */
    public static void zipDir(String directory, String zipName) throws IOException {
        // Make sure name is correct.
        if (!zipName.endsWith(".zip")) {
            zipName += ".zip";
        }
        // create a ZipOutputStream to zip the data to
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipName));
        zipDir(directory, zos, "");
        // close the stream
        closeQuietly(zos);
    }

    /**
     * Zip up a directory path
     *
     * @param directory
     * @param zos
     * @param path
     * @throws IOException
     */
    private static void zipDir(String directory, ZipOutputStream zos, String path) throws IOException {
        File zipDir = new File(directory);
        // get a listing of the directory content
        String[] dirList = zipDir.list();
        byte[] readBuffer = new byte[2156];
        int bytesIn = 0;
        // loop through dirList, and zip the files
        for (int i = 0; i < dirList.length; ++i) {
            File f = new File(zipDir, dirList[i]);
            if (f.isDirectory()) {
                zipDir(f.getPath(), zos, path.concat(f.getName()).concat(FILE_SEPARATOR));
                continue;
            }
            FileInputStream fis = new FileInputStream(f);
            try {
                zos.putNextEntry(new ZipEntry(path.concat(f.getName())));
                bytesIn = fis.read(readBuffer);
                while (bytesIn != -1) {
                    zos.write(readBuffer, 0, bytesIn);
                    bytesIn = fis.read(readBuffer);
                }
            } finally {
                closeQuietly(fis);
            }
        }

    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public static boolean checkFolderAndCreate(File toCheck) {
        if (!toCheck.exists()) {
            try {
                if (toCheck.mkdirs()) {
                    return true;
                }
            } catch (Exception e) {
                LogUtils.exceptionLog(e);
            }
        }
        return false;
    }

    /**
     * Add the folder specified to a ZIP file.
     *
     * @param folderToZIP
     *
     * ZIPENABLED
     *
     * backups/temp/blah -> backups/blah.zip
     *
     * ~~~ OR ~~~
     *
     * backups/temp/blah -> ( backups/blah
     *
     * sourceDIR finalDIR
     *
     */
    /**
     * Copies items from the temp DIR to the main DIR after ZIP if needed. After
     * it has done the required action, it deletes the source folder.
     *
     * @param sourceDIR The source directory. (ex: "backups/temp/xxxxxxxx")
     * @param finalDIR The final destination. (ex: "backups/xxxxxxxx")
     */
    public static void doCopyAndZIP(String sourceDIR, String finalDIR, boolean shouldZIP, boolean useTempFolder) {

        if (useTempFolder) {
            if (shouldZIP) {
                try {
                    FileUtils.zipDir(sourceDIR, finalDIR);
                } catch (IOException ioe) {
                    LogUtils.exceptionLog(ioe, "Failed to ZIP backup: IO Exception.");
                }
            } else {
                try {
                    FileUtils.copyDirectory(sourceDIR, finalDIR);
                } catch (IOException ex) {
                    Logger.getLogger(BackupTask.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
            try {
                // Delete the original doBackup directory.
                FileUtils.deleteDirectory(new File(sourceDIR));
                new File(sourceDIR).delete();
            } catch (IOException ioe) {
                LogUtils.exceptionLog(ioe, "Failed to delete temp folder: IO Exception.");
            }
        } else {
            if (shouldZIP) {
                try {
                    FileUtils.zipDir(sourceDIR, finalDIR);
                } catch (IOException ioe) {
                    LogUtils.exceptionLog(ioe, "Failed to ZIP backup: IO Exception.");
                }
                try {
                    // Delete the original doBackup directory.
                    FileUtils.deleteDirectory(new File(sourceDIR));
                    new File(sourceDIR).delete();
                } catch (IOException ioe) {
                    LogUtils.exceptionLog(ioe, "Failed to delete temp folder: IO Exception.");
                }
            }

        }



    }

    public static File[] listFilesInDir(File directory) {
        // List all the files inside this folder.
        File[] filesList = directory.listFiles(new FileFilter() {

            public boolean accept(File file) {
                return file.isFile();
            }
        });
        return filesList;
    }
    
    
    public static File[] listItemsInDir(File directory) {
        return directory.listFiles();
    }

    public static long getTotalFolderSize(File folder) {
        long bytes = 0L;
        File[] filelist = folder.listFiles();
        for (int i = 0; i < filelist.length; i++) {
            if (filelist[i].isDirectory()) {
                bytes += getTotalFolderSize(filelist[i]);
            } else {
                bytes += filelist[i].length();
            }
        }
        return bytes;
    }
}