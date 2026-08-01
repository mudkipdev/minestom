package net.minestom.server.command;

import net.minestom.command.CommandSender;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CommandSenderTest {

    @Test
    public void testMessageSending() {
        SenderTest sender = new SenderTest();

        assertNull(sender.getMostRecentMessage());

        sender.sendMessage("Hey!!");
        assertEquals(sender.getMostRecentMessage(), Component.text("Hey!!"));

        sender.sendMessage(new String[]{"Message", "Sending", "Test"});
        assertEquals(sender.getMostRecentMessage(), Component.text("Test"));

        sender.sendMessage(Component.text("Message test!", NamedTextColor.GREEN));
        assertEquals(sender.getMostRecentMessage(), Component.text("Message test!", NamedTextColor.GREEN));
    }

    private static final class SenderTest implements CommandSender {

        private @Nullable Component mostRecentMessage = null;

        @Override
        public void sendMessage(Component message) {
            mostRecentMessage = message;
        }

        @Nullable Component getMostRecentMessage() {
            return mostRecentMessage;
        }

        @Override
        public Identity identity() {
            return Identity.nil();
        }
    }
}
