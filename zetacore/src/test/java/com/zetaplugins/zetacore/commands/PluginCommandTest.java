package com.zetaplugins.zetacore.commands;

import com.zetaplugins.zetacore.commands.exceptions.CommandException;
import com.zetaplugins.zetacore.commands.exceptions.CommandUsageException;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class PluginCommandTest {

    @Mock
    private JavaPlugin plugin;

    @Mock
    private Server server;

    @Mock
    private CommandSender sender;

    @Mock
    private Command command;

    @BeforeEach
    void setup() {
        lenient().when(plugin.getServer()).thenReturn(server);
    }

    static class SimpleCommand extends PluginCommand<JavaPlugin> {
        private final boolean returnValue;

        SimpleCommand(JavaPlugin plugin, boolean returnValue) {
            super(plugin);
            this.returnValue = returnValue;
        }

        @Override
        public boolean execute(CommandSender sender, Command command, String label, ArgumentList args) throws CommandException {
            return returnValue;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, Command command, ArgumentList args) {
            return List.of("completion");
        }
    }

    @Test
    void onCommand_delegatesToExecute_and_returnsValue() {
        var trueCmd = new SimpleCommand(plugin, true);
        assertTrue(trueCmd.onCommand(sender, command, "lbl", new String[]{}));

        var falseCmd = new SimpleCommand(plugin, false);
        assertFalse(falseCmd.onCommand(sender, command, "lbl", new String[]{}));
    }

    @Test
    void onTabComplete_delegatesToTabComplete() {
        var cmd = new SimpleCommand(plugin, true);
        List<String> completions = cmd.onTabComplete(sender, command, "lbl", new String[]{});
        assertEquals(List.of("completion"), completions);
    }

    @Test
    void whenExecuteThrowsRegisteredCommandException_handlerIsInvoked_and_returnUsed() {
        class ThrowingCommand extends PluginCommand<JavaPlugin> {
            ThrowingCommand(JavaPlugin plugin) { super(plugin); }

            @Override
            public boolean execute(CommandSender sender, Command command, String label, ArgumentList args) throws CommandException {
                throw new CommandUsageException("Usage error");
            }

            @Override
            public List<String> tabComplete(CommandSender sender, Command command, ArgumentList args) {
                return List.of();
            }
        }

        var cmd = new ThrowingCommand(plugin);
        final boolean[] handled = {false};

        cmd.registerExceptionHandler(CommandUsageException.class, (ctx, e) -> {
            handled[0] = true;
            return true;
        });

        boolean result = cmd.onCommand(sender, command, "lbl", new String[]{});
        assertTrue(handled[0], "registered handler should be invoked");
        assertTrue(result, "handler return value should be returned by onCommand");
    }

    @Test
    void whenExecuteThrowsAndNoHandler_returnsFalse() {
        class ThrowingCommand extends PluginCommand<JavaPlugin> {
            ThrowingCommand(JavaPlugin plugin) { super(plugin); }

            @Override
            public boolean execute(CommandSender sender, Command command, String label, ArgumentList args) throws CommandException {
                throw new CommandUsageException("Usage error");
            }

            @Override
            public List<String> tabComplete(CommandSender sender, Command command, ArgumentList args) {
                return List.of();
            }
        }

        var cmd = new ThrowingCommand(plugin);
        // No handler registered -> onCommand should return false
        boolean result = cmd.onCommand(sender, command, "lbl", new String[]{});
        assertFalse(result);
    }
}
