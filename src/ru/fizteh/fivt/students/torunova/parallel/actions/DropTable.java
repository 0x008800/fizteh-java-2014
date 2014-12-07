package ru.fizteh.fivt.students.torunova.parallel.actions;

import ru.fizteh.fivt.students.torunova.parallel.CurrentTable;

import java.io.IOException;

/**
 * Created by nastya on 21.10.14.
 */
public class DropTable extends Action {
    @Override
    public boolean run(String[] args, CurrentTable currentTable) throws IOException {
        if (!checkNumberOfArguments(1, args.length)) {
            return false;
        }
        try {
            currentTable.getDb().removeTable(args[0]);
        } catch (IllegalStateException e) {
            System.err.println(e.getMessage());
            return false;
        }
        if (currentTable.get() != null) {
            if (currentTable.get().getName().equals(args[0])) {
                currentTable.reset();
            }

        }
        System.out.println("dropped");
        return true;
    }

    @Override
    public String getName() {
        return "drop";
    }
}
