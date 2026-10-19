package com.bill.measurement;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/** {@code /measurement}, {@code /measurement on|off} and {@code /measurement clear}, all handled on the client. */
final class MeasurementCommand {
    private MeasurementCommand() {
    }

    static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> dispatcher.register(
                literal("measurement")
                        .executes(context -> report(context.getSource()))
                        .then(literal("on").executes(context -> setEnabled(context.getSource(), true)))
                        .then(literal("off").executes(context -> setEnabled(context.getSource(), false)))
                        .then(literal("clear").executes(context -> clear(context.getSource())))));
    }

    private static int setEnabled(FabricClientCommandSource source, boolean enabled) {
        MeasurementConfig.setEnabled(enabled);
        source.sendFeedback(Texts.prefixed(Component.translatable(enabled ? "measurement.command.on" : "measurement.command.off")));
        return 1;
    }

    private static int clear(FabricClientCommandSource source) {
        Selection.clear();
        source.sendFeedback(Texts.prefixed(Component.translatable("measurement.command.clear")));
        return 1;
    }

    private static int report(FabricClientCommandSource source) {
        Selection selection = Selection.current();
        if (selection == null || selection.first() == null && selection.second() == null) {
            source.sendError(Texts.prefixed(Component.translatable("measurement.command.no_selection")));
            return 0;
        }
        if (!selection.complete()) {
            source.sendError(Texts.prefixed(Component.translatable("measurement.command.missing_corner",
                    selection.first() == null ? 1 : 2)));
            return 0;
        }
        if (source.getWorld().dimension() != selection.dimension()) {
            source.sendError(Texts.prefixed(Component.translatable("measurement.command.other_dimension")));
            return 0;
        }

        BlockCounter.Result blocks = BlockCounter.count(source.getWorld(), selection.min(), selection.max());
        MutableComponent message = Texts.prefixed(Component.translatable("measurement.report.area",
                Texts.corner(1, selection.first()), Texts.corner(2, selection.second())));
        line(message, "measurement.report.length", Texts.number(selection.length()));
        line(message, "measurement.report.width", Texts.number(selection.width()));
        line(message, "measurement.report.height", Texts.number(selection.height()));
        line(message, "measurement.report.volume", Texts.number(selection.volume()));
        line(message, "measurement.report.solid", Texts.number(blocks.solid()));
        if (blocks.unloaded() > 0) {
            message.append("\n").append(Component.translatable("measurement.report.unloaded", Texts.number(blocks.unloaded()))
                    .withStyle(ChatFormatting.GRAY));
        }
        source.sendFeedback(message);
        return (int) Math.min(Integer.MAX_VALUE, selection.volume());
    }

    private static void line(MutableComponent message, String key, Component value) {
        message.append("\n").append(Component.translatable(key, value));
    }
}
