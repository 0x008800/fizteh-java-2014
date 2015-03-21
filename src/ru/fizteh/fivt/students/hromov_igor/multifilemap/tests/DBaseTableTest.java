package ru.fizteh.fivt.students.hromov_igor.multifilemap.tests;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.fizteh.fivt.storage.strings.Table;
import ru.fizteh.fivt.storage.strings.TableProvider;
import ru.fizteh.fivt.storage.strings.TableProviderFactory;
import ru.fizteh.fivt.students.hromov_igor.multifilemap.base.DBProviderFactory;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DBaseTableTest {

    @Rule
    public TemporaryFolder Folder = new TemporaryFolder();
    public String path = Folder.newFolder("test").getAbsolutePath();


    public Table table;

    @Before
    public void initTable() throws Exception {
        TableProviderFactory Factory = new DBProviderFactory();
        TableProvider provider = Factory.create(path);
        table = provider.createTable("table");
        if (table == null) table = provider.getTable("table");
    }

    @Test(expected = IllegalArgumentException.class)
    public void putNull() throws Exception{
        table.put(null, null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void getNull() {
        table.get(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void removeNull() {
        table.remove(null);
    }

    @Test
    public void testPutAndGet() throws Exception{
        table.put("1", "2");
        assertEquals("2", table.get("1"));
        assertEquals("2", table.put("1", "3"));
        assertEquals("3", table.get("1"));
    }

    @Test
    public void testPutAndRemove() throws Exception{
        table.put("1", "2");
        assertEquals("2", table.remove("1"));
        assertNull(table.get("1"));
    }

    @Test
    public void testSize() throws Exception{
        table.remove("1");
        table.remove("3");
        int count = table.size();
        table.put("1", "2");
        table.put("3", "4");
        table.put("3", "5");
        assertEquals(count + 2 , table.size());
        table.remove("1");
        table.remove("1");
        table.remove("3");
        assertEquals(count, table.size());
    }



    @Test
    public void testRollBack() throws Exception{
        assertEquals(0, table.rollback());
        table.put("1", "2");
        table.put("2", "3");
        table.put("3", "4");
        table.remove("1");
        table.put("1", "5");
        assertEquals(5, table.rollback());
    }

    @Test
    public void testCommit() throws Exception{
        assertEquals(0, table.commit());
        table.put("1", "2");
        table.put("2", "3");
        table.put("3", "4");
        table.remove("3");
        assertEquals(4, table.commit());
    }

    @Test
    public void testCommitAndRollback() throws Exception{
        table.put("1", "a");
        table.put("2", "b");
        assertEquals(2, table.commit());
        table.remove("1");
        table.remove("2");
        assertNull(table.get("1"));
        assertNull(table.get("2"));
        assertEquals(2, table.rollback());
        assertEquals("2", table.get("1"));
        assertEquals("3", table.get("2"));
        table.remove("1");
        assertEquals(1, table.commit());
        assertEquals(2, table.size());
        table.put("1", "2");
        assertEquals(3, table.size());
        assertEquals(1, table.rollback());
        assertEquals(2, table.size());
        assertEquals(null, table.get("1"));
    }
}
