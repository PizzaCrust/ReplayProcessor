package org.teamlyon.replay;

import org.teamlyon.replay.model.EpicPlayer;
import org.teamlyon.replay.model.Player;

import java.util.Arrays;
import java.util.List;

public class PointSystem {

    public int victoryRoyalePts;
    public int elimPts;

    public static class PlacementThreshold {

        public int place;
        public int pts;

        public PlacementThreshold(int place, int pts) {
            this.place = place;
            this.pts = pts;
        }
    }

    public List<PlacementThreshold> placementPts;

    public PointSystem(int victoryRoyalePts, int elimPts, List<PlacementThreshold> placementPts) {
        this.victoryRoyalePts = victoryRoyalePts;
        this.elimPts = elimPts;
        this.placementPts = placementPts;
    }

    public PointSystem(){}

    public static class PointResults {

        public EpicPlayer player;
        public int placementPoints = 0;
        public int eliminationPoints = 0;
        public int victoryRoyalePoints = 0;
        public int totalPoints = 0;

        public void combine(PointResults pointResults) {
            this.placementPoints += pointResults.placementPoints;
            this.eliminationPoints += pointResults.eliminationPoints;
            this.victoryRoyalePoints += pointResults.victoryRoyalePoints;
            this.totalPoints += pointResults.totalPoints;
        }

    }

    public PointResults calculate(EpicPlayer player) {
        PointResults results = new PointResults();
        results.eliminationPoints = player.eliminations.size() * this.elimPts;
        if (player.placement == 1) {
            results.victoryRoyalePoints = this.victoryRoyalePts;
        }
        for (PlacementThreshold placementPt : this.placementPts) {
            if (placementPt.place >= player.placement) {
                results.placementPoints += placementPt.pts;
            }
        }
        results.totalPoints =
                results.eliminationPoints + results.victoryRoyalePoints + results.placementPoints;
        results.player = player;
        return results;
    }

    public static PointSystem wfcFormat() {
        return new PointSystem(2, 1, Arrays.asList(new PlacementThreshold(25, 2),
                new PlacementThreshold(15, 2), new PlacementThreshold(5, 2)));
    }

}
