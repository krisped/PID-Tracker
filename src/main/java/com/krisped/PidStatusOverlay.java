package com.krisped;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.*;

public class PidStatusOverlay extends Overlay {

    private final Client client;

    @Inject
    public PidStatusOverlay(Client client) {
        this.client = client;
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    // Simulerer en PID basert på spillerens hash-verdi.
    // Dette er kun en simulering og IKKE den faktiske PID'en fra spillet.
    private int getSimulatedPid(Player player) {
        return (int)(player.getHash() & 0xFFFF);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        Player local = client.getLocalPlayer();
        String text;
        if (local == null) {
            text = "No PID detected";
        } else {
            int localPid = getSimulatedPid(local);
            text = "Your simulated PID: " + localPid;
            // Henter motstander dersom du interagerer med en
            Player opponent = (local.getInteracting() instanceof Player)
                    ? (Player) local.getInteracting() : null;
            if (opponent != null) {
                int opponentPid = getSimulatedPid(opponent);
                text += " | Opponent's simulated PID: " + opponentPid;
            }
        }
        graphics.setFont(new Font("Arial", Font.BOLD, 16));
        graphics.setColor(Color.YELLOW);
        graphics.drawString(text, 10, 30);
        return null;
    }
}
