package org.adbs.dbxic.utils;

import org.adbs.dbxic.engine.core.DBMS;
import org.adbs.dbxic.storage.Block;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

/**
 * FileHandling: Basic file utilities.
 */
public class FileHandling {

    public static final String READ = "r";
    public static final String READ_WRITE = "rw";

    /**
     * getFileSize: returns the size (in bytes) of the file corresponding to the given filename.
     */
    public static long getFileSize(String filename)  throws IOException, FileNotFoundException {
        RandomAccessFile raf = new RandomAccessFile(filename, READ);
        long size = raf.length();
        raf.close();

        return size;
    } // getFileSize()

    
    /**
     * getNumberOfBlocks: returns the number of blocks required to save the file for the given filename.
     */
    public static int getNumberOfBlocks(String filename) throws IOException, FileNotFoundException {
        RandomAccessFile raf = new RandomAccessFile(filename, READ);
        int blocks = (int) (raf.length() / Block.BLOCK_SIZE + .5);
        raf.close();
        return blocks;
    } // getNumberOfBlocks()


    public static void setNumberOfBlocks(String filename, int nblocks) throws IOException, FileNotFoundException {
        RandomAccessFile raf = new RandomAccessFile(filename, READ_WRITE);
        int blocks = (int) (raf.length() / Block.BLOCK_SIZE + .5);
        if (blocks < nblocks) raf.setLength(nblocks*Block.BLOCK_SIZE);
        raf.close();
    }

    static Random rand = null;
    /**
     * getTempFileName: Creates and returns a new temporary file name
     */
    public static String getTempFileName() {
        if (rand == null) {
            rand = new Random();
            rand.setSeed(System.currentTimeMillis());
        }
        return DBMS.temp_dir + System.getProperty("file.separator") + "dbxic-" + rand.nextLong() + ".tmp";
    } // getTempFileName()

} // FileUtil
