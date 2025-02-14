package com.krisped;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;

@PluginDescriptor(
        name = "PID Checker",
        description = "Låser PID-resultatet i 10 sekunder etter siste interaksjon",
        tags = {"pid", "pvp", "status"}
)
public class PidCheckerPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private PidOverlay pidOverlay;

    @Inject
    private PidStatusOverlay pidStatusOverlay;

    // Låste verdier
    private Player lockedOpponent = null;
    private Player lockedPidHolder = null;
    // Tidspunkt for siste interaksjon (brukes til å "låse" resultatet i 10 sekunder)
    private long lastInteractionTime = 0;
    // Blink-teller for outline
    private int blinkCounter = 0;

    @Override
    protected void startUp() throws Exception {
        overlayManager.add(pidOverlay);
        overlayManager.add(pidStatusOverlay);
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(pidOverlay);
        overlayManager.remove(pidStatusOverlay);
        lockedOpponent = null;
        lockedPidHolder = null;
        blinkCounter = 0;
        lastInteractionTime = 0;
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        updateLockedInteraction();
        // Øk blink-telleren (brukes for blinkende outline)
        blinkCounter = (blinkCounter + 1) % 20;
    }

    // Denne metoden "låser" den første interaksjonen og beholder resultatet i 10 sekunder
    // etter siste registrerte interaksjon. Dersom ingen interaksjon skjer i 10 sekunder,
    // tømmes de låste verdiene.
    private void updateLockedInteraction() {
        Player local = client.getLocalPlayer();
        if (local == null) {
            return;
        }

        Player currentInteraction = (local.getInteracting() instanceof Player)
                ? (Player) local.getInteracting()
                : null;

        if (currentInteraction != null) {
            // Ved første interaksjon – lås opp motstanderen og beregn PID
            if (lockedOpponent == null) {
                lockedOpponent = currentInteraction;
                lastInteractionTime = System.currentTimeMillis();
                determinePidHolder(local, lockedOpponent);
            }
            else {
                // Dersom samme interaksjon fortsetter, forny tidsstempelet
                if (lockedOpponent.equals(currentInteraction)) {
                    lastInteractionTime = System.currentTimeMillis();
                }
                // Hvis en ny interaksjon skjer, ignorer den så lenge låsen er aktiv
            }
        }
        else {
            // Ingen interaksjon: dersom mer enn 10 sekunder har gått siden siste interaksjon,
            // tømmes de låste verdiene.
            if (lockedOpponent != null && System.currentTimeMillis() - lastInteractionTime > 10000) {
                lockedOpponent = null;
                lockedPidHolder = null;
                lastInteractionTime = 0;
            }
        }
    }

    // Bestemmer PID basert på spillernavn (invertert slik at dersom ditt navn er alfabetisk
    // før motstanderens, gis PID til motstanderen, ellers til deg).
    private void determinePidHolder(Player local, Player opponent) {
        if (local.getName() != null && opponent.getName() != null) {
            if (local.getName().compareTo(opponent.getName()) <= 0) {
                lockedPidHolder = opponent;
            } else {
                lockedPidHolder = local;
            }
        }
    }

    public Player getPidHolder() {
        return lockedPidHolder;
    }

    public int getBlinkCounter() {
        return blinkCounter;
    }
}
