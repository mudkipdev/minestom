package net.minestom.demo.commands;

import net.minestom.command.builder.Command;
import net.minestom.command.builder.arguments.ArgumentLiteral;
import net.minestom.command.builder.arguments.ArgumentLoop;

public class RedirectTestCommand extends Command {
    public RedirectTestCommand() {
        super("redirect");

        final ArgumentLiteral a = new ArgumentLiteral("a");
        final ArgumentLiteral b = new ArgumentLiteral("b");
        final ArgumentLiteral c = new ArgumentLiteral("c");
        final ArgumentLiteral d = new ArgumentLiteral("d");

        addSyntax((_, _) -> {}, new ArgumentLoop<>("test", a,b,c,d));
    }
}
