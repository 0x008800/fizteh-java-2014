package ru.fizteh.fivt.students.ZatsepinMikhail.Telnet.ClientPackage;

import ru.fizteh.fivt.students.ZatsepinMikhail.Proxy.FileMap.Shell;
import ru.fizteh.fivt.students.ZatsepinMikhail.Proxy.MultiFileHashMap.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientMain {
    public static void main(String[] args) {

        if (System.getProperty("fizteh.db.dir") == null) {
            System.out.println("we need working directory");
            System.exit(6);
        }
        Path dataBaseDirectory
                = Paths.get(System.getProperty("user.dir")).resolve(System.getProperty("fizteh.db.dir"));

        boolean allRight = true;
        if (Files.exists(dataBaseDirectory)) {
            if (!Files.isDirectory(dataBaseDirectory)) {
                System.err.println("working directory is a file");
                System.exit(4);
            }
        } else {
            try {
                Files.createDirectory(dataBaseDirectory);
            } catch (IOException e) {
                System.err.println("error while creating directory");
                allRight = false;
            } finally {
                if (!allRight) {
                    System.exit(5);
                }
            }
        }

        RealRemoteTableProviderFactory factory = new RealRemoteTableProviderFactory();
        try {
            Shell<RealRemoteTableProvider> myShell = new Shell<>(new );
            setUpShell(myShell);
            if (args.length > 0) {
                allRight = myShell.packetMode(args);
            } else {
                allRight = myShell.interactiveMode();
            }
            if (allRight) {
                System.exit(0);
            } else {
                System.exit(1);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(3);
        }
    }


    public static void setUpShell(Shell<RealRemoteTableProvider> myShell) {
        myShell.addCommand(new CommandCreate());
        myShell.addCommand(new CommandDrop());
        myShell.addCommand(new CommandUse());
        myShell.addCommand(new CommandGetDistribute());
        myShell.addCommand(new CommandPutDistribute());
        myShell.addCommand(new CommandListDistribute());
        myShell.addCommand(new CommandRemoveDistribute());
        myShell.addCommand(new CommandShowTables());
        myShell.addCommand(new CommandRollback());
        myShell.addCommand(new CommandCommit());
        myShell.addCommand(new CommandSize());
    }
}
