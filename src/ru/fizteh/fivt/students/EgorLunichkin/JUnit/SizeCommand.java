package ru.fizteh.fivt.students.EgorLunichkin.JUnit;

import ru.fizteh.fivt.students.EgorLunichkin.MultiFileHashMap.Table;

public class SizeCommand implements Command {
    public SizeCommand(JUnitDataBase jdb) {
        this.jUnitDataBase = jdb;
    }

    private JUnitDataBase jUnitDataBase;

    public void run() {

    }

    public void runOnTable(Table table) {
        
    }
}
