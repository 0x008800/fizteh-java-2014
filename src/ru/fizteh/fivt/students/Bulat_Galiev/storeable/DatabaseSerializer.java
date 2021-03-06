package ru.fizteh.fivt.students.Bulat_Galiev.storeable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ru.fizteh.fivt.storage.structured.Storeable;

public class DatabaseSerializer {
    private static final int BYTES_NUMBER = 8;
    static final int NUMBER_OF_DIRS = 16;
    static final int NUMBER_OF_FILES = 16;
    private Path filePathdb;
    private Map<String, Storeable> fileMap;
    private Map<String, Storeable> savedFileMap;
    private RandomAccessFile inputStream;
    private RandomAccessFile outputStream;
    private int recordsNumber;
    private int unsavedRecordsNumber;
    private Tabledb localtable;

    public DatabaseSerializer(final Path databasePath, final int dirName,
            final int fileName, final Tabledb singleTable) throws IOException {
        fileMap = new HashMap<>();
        savedFileMap = new HashMap<>();
        String dirString = Integer.toString(dirName) + ".dir";
        String fileString = Integer.toString(fileName) + ".dat";
        filePathdb = databasePath.resolve(dirString);
        filePathdb = filePathdb.resolve(fileString);
        localtable = singleTable;
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

    public final String readEncodedString(final int dataLength) throws IOException {
        byte[] byteData = new byte[dataLength];
        int read = inputStream.read(byteData);
        if (read < 0 || read != dataLength) {
            throw new IllegalArgumentException("Bad file format.");
        }
        String data = new String(byteData, "UTF-8");
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

            String key = readEncodedString(keyLength);
            String value = readEncodedString(valueLength);
            int nbytes = key.getBytes("UTF-8")[0];
            int ndirectory = Math.abs(nbytes % NUMBER_OF_DIRS);
            int nfile = Math.abs((nbytes / NUMBER_OF_DIRS) % NUMBER_OF_FILES);
            String dirString = Integer.toString(ndirectory) + ".dir";
            String fileString = Integer.toString(nfile) + ".dat";
            String dirfile = dirString + File.separator + fileString;
            if (!filePathdb.toString().endsWith(dirfile)) {
                throw new IOException(filePathdb + ": bad file format");
            }
            Storeable deserializedValue;
            try {
                deserializedValue = localtable.getLocalProvider().deserialize(
                        localtable, value);
            } catch (ParseException ex) {
                throw new IOException("Corrupted database");
            }
            savedFileMap.put(key, deserializedValue);

            bytesLeft -= keyLength + valueLength;
            recordsNumber++;
        }
        fileMap = new HashMap<String, Storeable>(savedFileMap);
        inputStream.close();
    }

    protected final void putData(final RandomAccessFile filedb)
            throws IOException {
        outputStream = new RandomAccessFile(filePathdb.toString(), "rw");
        filedb.setLength(0);
        Set<Map.Entry<String, Storeable>> rows = savedFileMap.entrySet();
        for (Map.Entry<String, Storeable> row : rows) {
            String serializedValue = localtable.getLocalProvider().serialize(
                    localtable, row.getValue());
            outputStream.writeInt(row.getKey().getBytes("UTF-8").length);
            outputStream.writeInt(serializedValue.getBytes("UTF-8").length);
            outputStream.write(row.getKey().getBytes("UTF-8"));
            outputStream.write(serializedValue.getBytes("UTF-8"));
        }
    }

    public final int commit() {
        int diffRecordsNumber = Math.abs(unsavedRecordsNumber);
        if ((recordsNumber == 0) && (fileMap.size() == 0)) {
            filePathdb.toFile().delete();
            filePathdb.getParent().toFile().delete();
        } else {
            try (RandomAccessFile filedb = new RandomAccessFile(
                    filePathdb.toString(), "rw")) {

                Set<Map.Entry<String, Storeable>> rows = fileMap.entrySet();
                for (Map.Entry<String, Storeable> row : rows) {
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
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return diffRecordsNumber;
    }

    public final int rollback() {
        int diffRecordsNumber = Math.abs(unsavedRecordsNumber);
        fileMap.clear();
        unsavedRecordsNumber = 0;
        return diffRecordsNumber;
    }

    public final Storeable put(final String key, final Storeable value) {

        String serializedValue1 = null;

        if (savedFileMap.get(key) != null) {
            serializedValue1 = localtable.getLocalProvider().serialize(
                    localtable, savedFileMap.get(key));
        }
        String serializedValue2 = localtable.getLocalProvider().serialize(
                localtable, value);

        if ((fileMap.containsKey(key)) && (fileMap.get(key) == null)
                && (serializedValue1 != null) && (serializedValue2 != null)
                && (!serializedValue1.equals(serializedValue2))
                && (savedFileMap.get(key) != null)) {
            unsavedRecordsNumber++;
        }
        Storeable putValue = fileMap.put(key, value);
        if (putValue == null) {
            unsavedRecordsNumber++;
            if (!fileMap.containsKey(key)) {
                putValue = savedFileMap.get(key);
            }
        }
        return putValue;
    }

    public final Storeable get(final String key) {
        Storeable getValue = fileMap.get(key);
        if (getValue == null) {
            getValue = savedFileMap.get(key);
        }
        return getValue;
    }

    public final Storeable remove(final String key) {
        Storeable getValue = fileMap.remove(key);
        if (getValue == null) {
            if (savedFileMap.get(key) != null) {
                fileMap.put(key, null);
                getValue = savedFileMap.get(key);
            }
        }
        unsavedRecordsNumber--;
        return getValue;
    }

    public final Set<String> list() {
        Set<String> mergedSet = new HashSet<>();
        mergedSet.addAll(savedFileMap.keySet());
        Set<Map.Entry<String, Storeable>> rows = fileMap.entrySet();
        for (Map.Entry<String, Storeable> row : rows) {
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
