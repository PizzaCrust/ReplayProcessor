package org.teamlyon.replay.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Represents an indexed player inside of the replay.
 */
public class Player {

    public int placement;

    public final List<Player> eliminations = new ArrayList<>();

    public final String accountId;

    public int timeLiving = Math.toIntExact(TimeUnit.MINUTES.toMillis(99));

    public Player(String accountId) {
        this.accountId = accountId;
    }

    public static class TimeComparator implements Comparator<Player> {
        @Override
        public int compare(Player o1, Player o2) {
            return o2.timeLiving - o1.timeLiving;
        }
    }

    public static class PlacementComparator implements Comparator<Player> {
        @Override
        public int compare(Player o1, Player o2) {
            return o2.placement -  o1.placement;
        }
    }

}
