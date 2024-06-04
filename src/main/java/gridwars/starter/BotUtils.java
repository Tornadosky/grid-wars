package gridwars.utils;

import cern.ais.gridwars.api.Coordinates;
import cern.ais.gridwars.api.UniverseView;
import cern.ais.gridwars.api.command.MovementCommand;

import java.util.*;

public class BotUtils {
    // Count the number of neighboring cells that belong to the bot
    public static int countFriendlyNeighbors(Coordinates cell, UniverseView universeView) {
        int count = 0;
        for (MovementCommand.Direction direction : MovementCommand.Direction.values()) {
            Coordinates neighbor = cell.getNeighbour(direction);
            if (universeView.belongsToMe(neighbor)) {
                count++;
            }
        }
        return count;
    }

    // Count the number of enemy neighbors around the cell
    public static int countEnemyNeighbors(Coordinates cell, UniverseView universeView) {
        int count = 0;
        for (MovementCommand.Direction direction : MovementCommand.Direction.values()) {
            Coordinates neighbor = cell.getNeighbour(direction);
            if (!universeView.isEmpty(neighbor) && !universeView.belongsToMe(neighbor)) {
                count++;
            }
        }
        return count;
    }

    // Calculate total population of surrounding friendly neighbors
    public static int calculateNeighborPopulation(Coordinates cell, UniverseView universeView) {
        int totalPopulation = 0;
        for (MovementCommand.Direction direction : MovementCommand.Direction.values()) {
            Coordinates neighbor = cell.getNeighbour(direction);
            if (universeView.belongsToMe(neighbor)) {
                totalPopulation += universeView.getPopulation(neighbor);
            }
        }
        return totalPopulation;
    }

    // Get the direction(s) with the weakest enemy population
    public static List<MovementCommand.Direction> getWeakestEnemyDirections(Coordinates cell, UniverseView universeView) {
        List<MovementCommand.Direction> weakestDirections = new ArrayList<>();
        int minEnemyPopulation = Integer.MAX_VALUE;

        for (MovementCommand.Direction direction : MovementCommand.Direction.values()) {
            Coordinates neighbor = cell.getNeighbour(direction);
            if (!universeView.belongsToMe(neighbor) && !universeView.isEmpty(neighbor)) {
                int enemyPopulation = universeView.getPopulation(neighbor);
                if (enemyPopulation < minEnemyPopulation) {
                    minEnemyPopulation = enemyPopulation;
                    weakestDirections.clear();
                    weakestDirections.add(direction);
                } else if (enemyPopulation == minEnemyPopulation) {
                    weakestDirections.add(direction);
                }
            }
        }
        return weakestDirections;
    }

    // Calculate the distance between two coordinates using Manhattan distance
    public static int calculateManhattanDistance(Coordinates a, Coordinates b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    // Get directions to the nearest empty cell from the current cell
    public static List<MovementCommand.Direction> getEmptyNeighborDirections(Coordinates cell, UniverseView universeView) {
        List<MovementCommand.Direction> emptyDirections = new ArrayList<>();
        for (MovementCommand.Direction direction : MovementCommand.Direction.values()) {
            Coordinates neighbor = cell.getNeighbour(direction);
            if (universeView.isEmpty(neighbor)) {
                emptyDirections.add(direction);
            }
        }
        return emptyDirections;
    }

    // Find the direction(s) that lead to the largest cluster of enemy cells for an attack strategy
    public static List<MovementCommand.Direction> getEnemyClusterDirections(Coordinates cell, UniverseView universeView) {
        Map<MovementCommand.Direction, Integer> clusterMap = new HashMap<>();

        for (MovementCommand.Direction direction : MovementCommand.Direction.values()) {
            Coordinates neighbor = cell.getNeighbour(direction);
            int clusterSize = 0;

            // Count consecutive enemy cells in this direction
            while (!universeView.isEmpty(neighbor) && !universeView.belongsToMe(neighbor)) {
                clusterSize++;
                neighbor = neighbor.getNeighbour(direction);
            }

            if (clusterSize > 0) {
                clusterMap.put(direction, clusterSize);
            }
        }

        int maxClusterSize = clusterMap.values().stream().max(Integer::compare).orElse(0);

        List<MovementCommand.Direction> resultDirections = new ArrayList<>();
        for (Map.Entry<MovementCommand.Direction, Integer> entry : clusterMap.entrySet()) {
            if (entry.getValue() == maxClusterSize) {
                resultDirections.add(entry.getKey());
            }
        }

        return resultDirections;
    }

    // Get all enemy cells within a specific radius for targeting
    public static List<Coordinates> getEnemiesInRadius(Coordinates cell, UniverseView universeView, int radius) {
        List<Coordinates> enemyCells = new ArrayList<>();
        Queue<Coordinates> queue = new LinkedList<>();
        Set<Coordinates> visited = new HashSet<>();
        queue.add(cell);
        visited.add(cell);

        while (!queue.isEmpty()) {
            Coordinates current = queue.poll();
            int distance = calculateManhattanDistance(cell, current);
            if (distance > radius) {
                continue;
            }

            for (MovementCommand.Direction direction : MovementCommand.Direction.values()) {
                Coordinates neighbor = current.getNeighbour(direction);
                if (!visited.contains(neighbor) && !universeView.belongsToMe(neighbor) && !universeView.isEmpty(neighbor)) {
                    enemyCells.add(neighbor);
                    queue.add(neighbor);
                    visited.add(neighbor);
                }
            }
        }

        return enemyCells;
    }

    // Check if a cell is surrounded by friendly cells (for defensive purposes)
    public static boolean isSurroundedByFriendlyCells(Coordinates cell, UniverseView universeView) {
        for (MovementCommand.Direction direction : MovementCommand.Direction.values()) {
            Coordinates neighbor = cell.getNeighbour(direction);
            if (!universeView.belongsToMe(neighbor)) {
                return false;
            }
        }
        return true;
    }
}
