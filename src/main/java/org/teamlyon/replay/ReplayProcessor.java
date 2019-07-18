package org.teamlyon.replay;

import org.teamlyon.replay.model.EpicPlayer;
import org.teamlyon.replay.model.Player;
import org.teamlyon.replay.model.ProcessedReplay;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
            if (!elimination.isKnocked()) {
                Player victim = getPlayer(elimination.getVictimId());
                victim.timeLiving = elimination.getTime();
                if (!elimination.getVictimId().equalsIgnoreCase(elimination.getKillerId())) {
                    Player killer = getPlayer(elimination.getKillerId());
                    killer.eliminations.add(victim);
                }
            }
        }
    }

    public ProcessedReplay<FortniteReplayReader> process() {
        processElims();
        this.playerBuffer.sort(new Player.TimeComparator());
        for (int i = 0; i < this.playerBuffer.size(); i++) {
            this.playerBuffer.get(i).placement = i + 1;
        }
        return new ProcessedReplay<>() {
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


    public static ProcessedReplay fromFile(File file,
                                           Fortnite fortnite) throws Exception {
        ReplayProcessor processor = new ReplayProcessor(new FortniteReplayReader(file),
                fortnite);
        return processor.process();
    }

    private static PointSystem.PointResults getPointResults(EpicPlayer epicPlayer,
                                                            List<PointSystem.PointResults> results) {
        for (PointSystem.PointResults result : results) {
            if (result.player.equals(epicPlayer)) {
                return result;
            }
        }
        PointSystem.PointResults pointResults = new PointSystem.PointResults();
        pointResults.player = epicPlayer;
        results.add(pointResults);
        return pointResults;
    }

    public static void updateResults(List<PointSystem.PointResults> results,
                                     PointSystem system,
                                     ProcessedReplay replay) {
        for (Object player : replay.getPlayers()) {
            EpicPlayer p = ((EpicPlayer) player);
            getPointResults(p, results).combine(PointSystem.wfcFormat().calculate(p));
        }
    }

    public static List<PointSystem.PointResults> tournament(PointSystem system,
                                                            Fortnite fortnite,
                                                            File... replays) throws Exception {
        List<ProcessedReplay> replayList = new ArrayList<>();
        for (File replay : replays) {
            replayList.add(fromFile(replay, fortnite));
        }
        List<PointSystem.PointResults> results = new ArrayList<>();
        for (ProcessedReplay processedReplay : replayList) {
            updateResults(results, system, processedReplay);
        }
        results.sort(Comparator.comparingInt(o -> o.totalPoints));
        Collections.reverse(results);
        return results;
    }

    public static void main(String... args) throws Exception {
        Fortnite fortnite = DefaultFortnite.Builder.newInstance(System.getenv("email"),
                System.getenv("pass")).build();
        ProcessedReplay g1 = fromFile(new File("g1.replay"), fortnite);
        ProcessedReplay g2 = fromFile(new File("g2.replay"), fortnite);
        ProcessedReplay g3 = fromFile(new File("g3.replay"), fortnite);
        List<PointSystem.PointResults> results = new ArrayList<>();
        updateResults(results, PointSystem.wfcFormat(), g1);
        updateResults(results, PointSystem.wfcFormat(), g2);
        updateResults(results, PointSystem.wfcFormat(), g3);
        results.sort(Comparator.comparingInt(o -> o.totalPoints));
        Collections.reverse(results);
        for (int i = 0; i < results.size(); i++) {
            PointSystem.PointResults current = results.get(i);
            System.out.println((i + 1) + ". " + current.player.displayName);
            System.out.println("  Total points: " + current.totalPoints);
            System.out.println("  Victory points: " + current.victoryRoyalePoints);
            System.out.println("  Elimination points: " + current.eliminationPoints);
            System.out.println("  Placement points: " + current.placementPoints);
        }
    }
}
