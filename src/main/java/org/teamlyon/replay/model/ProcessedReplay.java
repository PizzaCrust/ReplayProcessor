package org.teamlyon.replay.model;

import java.util.List;
import java.util.UUID;

public interface ProcessedReplay<HANDLE> {

    List<Player> getPlayers(); // sorted descending order

    UUID getMatchId();

    String getName();

    default Player getWinner() {
        return getPlayers().get(0);
    }

    HANDLE getHandle();

}
