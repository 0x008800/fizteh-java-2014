package ru.fizteh.fivt.students.pavel_voropaev.project;

import ru.fizteh.fivt.students.pavel_voropaev.project.database.DatabaseFactory;
import ru.fizteh.fivt.students.pavel_voropaev.project.interpreter.Command;
import ru.fizteh.fivt.students.pavel_voropaev.project.interpreter.Interpreter;
import ru.fizteh.fivt.students.pavel_voropaev.project.interpreter.commands.database.*;
import ru.fizteh.fivt.students.pavel_voropaev.project.interpreter.commands.table.*;
import ru.fizteh.fivt.students.pavel_voropaev.project.master.TableProvider;
import ru.fizteh.fivt.students.pavel_voropaev.project.master.TableProviderFactory;

public class Main {

    public static void main(String[] args) {
        String dbPath = System.getProperty("fizteh.db.dir");
        if (dbPath == null) {
            System.err.println("You must specify fizteh.db.dir via -Dfizteh.db.dir JVM parameter");
            System.exit(1);
        }
        
        TableProviderFactory factory = new DatabaseFactory();
        TableProvider db = factory.create(dbPath);
        
        Command[] commands = new Command[]{
                new Create(db), new Drop(db), new ShowTables(db), new Size(db), new Use(db),
                new Commit(db), new Get(db), new ListKeys(db), new Put(db), new Remove(db), new Rollback(db),
                new Exit(db)};

        Interpreter interpreter = new Interpreter(commands);
        interpreter.run(args);


    }

}
