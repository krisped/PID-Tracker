package com.krisped;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Point; // RuneLite sitt Point
import net.runelite.client.util.Text;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.*;

public class PidOverlay extends Overlay {

    private final Client client;
    private final PidCheckerPlugin plugin;

    @Inject
    public PidOverlay(Client client, PidCheckerPlugin plugin) {
        this.client = client;
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        Player pidHolder = plugin.getPidHolder();
        if (pidHolder == null) {
            return null;
        }
        drawPidText(graphics, pidHolder);
        // Tegn blinkende outline dersom blinkCounter er partall
        if (plugin.getBlinkCounter() % 2 == 0) {
            drawOutline(graphics, pidHolder, Color.RED);
        }
        return null;
    }

    private void drawPidText(Graphics2D graphics, Player player) {
        String pidText = "PID Holder: " + Text.removeTags(player.getName());
        if (player.getConvexHull() == null) {
            return;
        }
        java.awt.Point awtPoint = player.getConvexHull().getBounds().getLocation();
        Point canvasPoint = new Point(awtPoint.x, awtPoint.y);
        OverlayUtil.renderTextLocation(graphics, canvasPoint, pidText, Color.RED);
    }

    private void drawOutline(Graphics2D graphics, Player player, Color color) {
        if (player.getConvexHull() != null) {
            OverlayUtil.renderPolygon(graphics, player.getConvexHull(), color);
        }
    }
}
