package ru.fizteh.fivt.students.Bulat_Galiev.junit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DatabaseSerializer {
    private static final int BYTES_NUMBER = 8;
    static final int NUMBER_OF_DIRS = 16;
    static final int NUMBER_OF_FILES = 16;
    static final String ENCODING = "UTF-8";
    private Path filePathdb;
    private Map<String, String> fileMap;
    private Map<String, String> savedFileMap;
    private RandomAccessFile inputStream;
    private RandomAccessFile outputStream;
    private int recordsNumber;
    private int unsavedRecordsNumber;

    public DatabaseSerializer(final Path databasePath, final int dirName,
            final int fileName) throws IOException {
        fileMap = new HashMap<>();
        savedFileMap = new HashMap<>();
        String dirString = Integer.toString(dirName) + ".dir";
        String fileString = Integer.toString(fileName) + ".dat";
        filePathdb = databasePath.resolve(dirString);
        filePathdb = filePathdb.resolve(fileString);
        if (!Files.exists(filePathdb)) {
            filePathdb.getParent().toFile().mkdir();
            filePathdb.toFile().createNewFile();
        }
        try (RandomAccessFile filedb = new RandomAccessFile(
                filePathdb.toString(), "r")) {
            if (filedb.length() > 0) {
                this.getData(filedb);
            }
        } catch (FileNotFoundException e) {
            filePathdb.toFile().createNewFile();
        }
    }

    public final String readUTF8String(final int dataLength) throws IOException {
        byte[] byteData = new byte[dataLength];
        int read = inputStream.read(byteData);
        if (read < 0 || read != dataLength) {
            throw new IllegalArgumentException("Bad file format.");
        }
        String data = new String(byteData, ENCODING);
        return data;
    }

    protected final void getData(final RandomAccessFile filedb)
            throws IOException {
        inputStream = new RandomAccessFile(filePathdb.toString(), "r");
        long bytesLeft = inputStream.length();
        while (bytesLeft > 0) {
            int keyLength = inputStream.readInt();
            int valueLength = inputStream.readInt();

            bytesLeft -= BYTES_NUMBER;

            String key = readUTF8String(keyLength);
            String value = readUTF8String(valueLength);
            int nbytes = key.getBytes(ENCODING)[0];
            int ndirectory = Math.abs(nbytes % NUMBER_OF_DIRS);
            int nfile = Math.abs((nbytes / NUMBER_OF_DIRS) % NUMBER_OF_FILES);
            String dirString = Integer.toString(ndirectory) + ".dir";
            String fileString = Integer.toString(nfile) + ".dat";
            String dirfile = dirString + File.separator + fileString;
            if (!filePathdb.toString().endsWith(dirfile)) {
                throw new IOException(filePathdb + ": bad file format");
            }
            savedFileMap.put(key, value);

            bytesLeft -= keyLength + valueLength;
            recordsNumber++;
        }
        fileMap = new HashMap<String, String>(savedFileMap);
        inputStream.close();
    }

    protected final void putData(final RandomAccessFile filedb)
            throws IOException {
        outputStream = new RandomAccessFile(filePathdb.toString(), "rw");
        filedb.setLength(0);
        Set<Map.Entry<String, String>> rows = savedFileMap.entrySet();
        for (Map.Entry<String, String> row : rows) {
            outputStream.writeInt(row.getKey().getBytes(ENCODING).length);
            outputStream.writeInt(row.getValue().getBytes(ENCODING).length);
            outputStream.write(row.getKey().getBytes(ENCODING));
            outputStream.write(row.getValue().getBytes(ENCODING));
        }
    }

    public final int commit() throws IOException {
        int diffrecordsNumber = Math.abs(unsavedRecordsNumber);
        if ((recordsNumber == 0) && (fileMap.size() == 0)) {
            filePathdb.toFile().delete();
            filePathdb.getParent().toFile().delete();
        } else {
            RandomAccessFile filedb = new RandomAccessFile(
                    filePathdb.toString(), "rw");

            Set<Map.Entry<String, String>> rows = fileMap.entrySet();
            for (Map.Entry<String, String> row : rows) {
                if (row.getValue() == null) {
                    savedFileMap.remove(row.getKey());
                    recordsNumber -= 2;
                } else {
                    savedFileMap.put(row.getKey(), row.getValue());
                }
            }
            this.putData(filedb);
            recordsNumber += unsavedRecordsNumber;
            unsavedRecordsNumber = 0;
            fileMap.clear();
            outputStream.close();
        }
        return diffrecordsNumber;
    }

    public final int rollback() {
        int diffrecordsNumber = Math.abs(unsavedRecordsNumber);
        fileMap.clear();
        unsavedRecordsNumber = 0;
        return diffrecordsNumber;
    }

    public final String put(final String key, final String value) {
        if ((fileMap.containsKey(key)) && (fileMap.get(key) == null)
                && (savedFileMap.get(key) != null)
                && (!savedFileMap.get(key).equals(value))) {
            unsavedRecordsNumber++;
        }
        String putValue = fileMap.put(key, value);
        if (putValue == null) {
            unsavedRecordsNumber++;
            if (!fileMap.containsKey(key)) {
                putValue = savedFileMap.get(key);
            }
        }
        return putValue;
    }

    public final String get(final String key) {
        String getValue = fileMap.get(key);
        if (getValue == null) {
            getValue = savedFileMap.get(key);
        }
        return getValue;
    }

    public final String remove(final String key) {
        String getValue = fileMap.remove(key);
        if (getValue != null) {
            unsavedRecordsNumber--;
        } else {
            if (savedFileMap.get(key) != null) {
                fileMap.put(key, null);
                getValue = savedFileMap.get(key);
                unsavedRecordsNumber--;
            }
        }
        return getValue;
    }

    public final Set<String> list() {
        Set<String> mergedSet = new HashSet<>();
        mergedSet.addAll(savedFileMap.keySet());
        Set<Map.Entry<String, String>> rows = fileMap.entrySet();
        for (Map.Entry<String, String> row : rows) {
            if (row.getValue() == null) {
                mergedSet.remove(row.getKey());
            } else {
                mergedSet.add(row.getKey());
            }
        }
        return mergedSet;
    }

    public final int getRecordsNumber() {
        return recordsNumber + unsavedRecordsNumber;
    }

    public final int getChangedRecordsNumber() {
        return Math.abs(unsavedRecordsNumber);
    }
}
