package ru.fizteh.fivt.students.EgorLunichkin.JUnit;

import ru.fizteh.fivt.students.EgorLunichkin.MultiFileHashMap.Command;
import ru.fizteh.fivt.students.EgorLunichkin.MultiFileHashMap.CreateCommand;

public class JUnitCreateCommand implements JUnitCommand {
    public JUnitCreateCommand(JUnitDataBase jdb, String name) {
        this.tableName = name;
        this.jUnitDataBase = jdb;
    }

    private JUnitDataBase jUnitDataBase;
    private String tableName;

    public void run() throws Exception {
        Command create = new CreateCommand(jUnitDataBase.multiDataBase, tableName);
        create.run();
        jUnitDataBase.tables.put(tableName, new HybridTable(jUnitDataBase.multiDataBase.tables.get(tableName)));
    }
}
