package org.teamlyon.replay;

import org.teamlyon.replay.model.EpicPlayer;
import org.teamlyon.replay.model.Player;
import org.teamlyon.replay.model.ProcessedReplay;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import fortnitereplayreader.FortniteReplayReader;
import fortnitereplayreader.model.Elimination;
import io.github.robertograham.fortnite2.client.Fortnite;
import io.github.robertograham.fortnite2.implementation.DefaultFortnite;

public class ReplayProcessor {

    private final FortniteReplayReader reader;
    private final Fortnite fortnite;

    private final List<Player> playerBuffer = new ArrayList<>();

    public ReplayProcessor(FortniteReplayReader reader,
                           Fortnite fortnite) {
        this.reader = reader;
        this.fortnite = fortnite;
    }

    private Player getPlayer(String id) {
        for (Player player : playerBuffer) {
            if (player.accountId.equalsIgnoreCase(id)) {
                return player;
            }
        }
        Player player;
        if (fortnite != null) {
            player = new EpicPlayer(id, fortnite);
        } else {
            player = new Player(id);
        }
        playerBuffer.add(player);
        return player;
    }

    private void processElims() {
        for (Elimination elimination : reader.getEliminations()) {
            if (!elimination.getVictimId().equalsIgnoreCase(elimination.getKillerId())) {
                //not suicide/storm
                Player victim = getPlayer(elimination.getVictimId());
                victim.timeLiving = elimination.getTime();
                getPlayer(elimination.getKillerId()).eliminations.add(victim);
            }
        }
    }

    public ProcessedReplay process() {
        processElims();
        this.playerBuffer.sort(new Player.TimeComparator());
        for (int i = 0; i < this.playerBuffer.size(); i++) {
            this.playerBuffer.get(i).placement = i + 1;
        }
        return new ProcessedReplay<FortniteReplayReader>() {
            @Override
            public List<Player> getPlayers() {
                return playerBuffer;
            }

            @Override
            public UUID getMatchId() {
                return reader.getMeta().getMatchId();
            }

            @Override
            public String getName() {
                return reader.getMeta().getFriendlyName();
            }

            @Override
            public FortniteReplayReader getHandle() {
                return reader;
            }
        };
    }

    public static void main(String... args) throws Exception {
        ReplayProcessor processor =
                new ReplayProcessor(new FortniteReplayReader(new File(System.getenv(
                "replayFile"))), DefaultFortnite.Builder.newInstance(System.getenv("email"),
                System.getenv("pass")).build());
        ProcessedReplay replay = processor.process();
        List<Player> players = replay.getPlayers();
        System.out.println("Match ID: " + replay.getMatchId());
        for (int i = 0; i < players.size(); i++) {
            EpicPlayer player = (EpicPlayer) players.get(i);
            System.out.println((i+1) + ". " + player.displayName + " - "
                            + player.eliminations.size()
                            + " " +
                            "elims" + " - " + TimeUnit.MINUTES.convert(player.timeLiving,
                    TimeUnit.MILLISECONDS) + " minutes");
        }
    }

}
