package ru.fizteh.fivt.students.ZatsepinMikhail.Telnet.ServerPackage.CommandsTableProvider;

import ru.fizteh.fivt.students.ZatsepinMikhail.Proxy.MultiFileHashMap.MFileHashMap;

import java.io.PrintStream;

public abstract class CommandTableProvider {
    protected String name;
    protected int numberOfArguments;

    public abstract boolean run(MFileHashMap dataBase, String[] args, PrintStream output);
    @Override
    public final String toString() {
            return name;
        }
}
