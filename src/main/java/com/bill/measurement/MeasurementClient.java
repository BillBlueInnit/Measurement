package com.bill.measurement;

import com.bill.measurement.render.SelectionRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class MeasurementClient implements ClientModInitializer {
    public static final String MOD_ID = "measurement";

    @Override
    public void onInitializeClient() {
        MeasurementConfig.load();
        SelectionTool.register();
        MeasurementCommand.register();
        SelectionRenderer.register();
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> Selection.clear());
    }
}
