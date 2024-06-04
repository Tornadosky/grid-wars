package gridwars.starter;

import cern.ais.gridwars.api.Coordinates;
import cern.ais.gridwars.api.UniverseView;
import cern.ais.gridwars.api.bot.PlayerBot;
import cern.ais.gridwars.api.command.MovementCommand;
import gridwars.utils.BotUtils;

import java.util.*;

/**
 * Frieren: Advanced strategy bot for GridWars, focusing on long-term territory dominance, efficient population management,
 * and adaptive behavior for expansion, defense, and attack.
 */
public class Frieren implements PlayerBot {

    Coordinates basePosition;
    int currentTurn;
    int turnThreshold = 40; // Switch strategy after 40 turns
    int defensePopulationThreshold = 20; // Minimum population for cells to switch to defense
    int attackPopulationThreshold = 50; // Minimum population to initiate an attack
    int maxClusterRadius = 3; // Radius to detect enemy clusters
    double expansionFactor = 0.6; // Population fraction to use for expansion
    double defenseFactor = 0.3; // Population fraction to use for defense

    Random random = new Random();

    @Override
    public void getNextCommands(UniverseView universeView, List<MovementCommand> commandList) {
        currentTurn = universeView.getCurrentTurn();
        List<Coordinates> myCells = universeView.getMyCells();

        if (basePosition == null) {
            basePosition = myCells.get(0);  // Establish base on first turn
        }

        for (Coordinates cell : myCells) {
            int currentPopulation = universeView.getPopulation(cell);

            if (currentPopulation > defensePopulationThreshold) {
                if (shouldExpand(cell, universeView)) {
                    handleExpansion(cell, universeView, commandList, currentPopulation);
                } else if (currentPopulation > attackPopulationThreshold) {
                    handleAttack(cell, universeView, commandList, currentPopulation);
                } else {
                    handleDefense(cell, universeView, commandList, currentPopulation);
                }
            }
        }
    }

    // 1. Expansion Strategy: Prioritize empty neighboring cells, expanding territory in the early game.
    private void handleExpansion(Coordinates cell, UniverseView universeView, List<MovementCommand> commandList, int currentPopulation) {
        List<MovementCommand.Direction> emptyDirections = BotUtils.getEmptyNeighborDirections(cell, universeView);

        if (!emptyDirections.isEmpty()) {
            MovementCommand.Direction selectedDirection = selectBestDirectionForExpansion(cell, universeView, emptyDirections);
            int populationToMove = (int) (currentPopulation * expansionFactor);
            BotUtils.move(commandList, cell, selectedDirection, populationToMove);
        }
    }

    // 2. Attack Strategy: Find weak enemy cells and focus on reducing enemy population or capturing territory.
    private void handleAttack(Coordinates cell, UniverseView universeView, List<MovementCommand> commandList, int currentPopulation) {
        List<MovementCommand.Direction> weakEnemyDirections = BotUtils.getWeakestEnemyDirections(cell, universeView);

        if (!weakEnemyDirections.isEmpty()) {
            MovementCommand.Direction attackDirection = weakEnemyDirections.get(random.nextInt(weakEnemyDirections.size()));
            int attackPopulation = (int) (currentPopulation * 0.5); // Use 50% of population for attacks
            BotUtils.move(commandList, cell, attackDirection, attackPopulation);
        }
    }

    // 3. Defense Strategy: Redistribute population to bolster cells at risk or to create strongholds.
    private void handleDefense(Coordinates cell, UniverseView universeView, List<MovementCommand> commandList, int currentPopulation) {
        List<MovementCommand.Direction> directions = BotUtils.getAvailableDirections(cell, universeView);

        if (!directions.isEmpty()) {
            MovementCommand.Direction direction = directions.get(random.nextInt(directions.size()));
            int defensePopulation = (int) (currentPopulation * defenseFactor);
            BotUtils.move(commandList, cell, direction, defensePopulation);
        }
    }

    // 4. Strategic Expansion: Select the best direction to expand based on enemy proximity and available population.
    private MovementCommand.Direction selectBestDirectionForExpansion(Coordinates cell, UniverseView universeView, List<MovementCommand.Direction> emptyDirections) {
        Map<MovementCommand.Direction, Integer> directionScores = new HashMap<>();

        for (MovementCommand.Direction direction : emptyDirections) {
            Coordinates neighbor = cell.getNeighbour(direction);
            int score = BotUtils.countFriendlyNeighbors(neighbor, universeView) * 2 - BotUtils.countEnemyNeighbors(neighbor, universeView); // Friendly neighbors are good, enemy neighbors bad
            directionScores.put(direction, score);
        }

        return Collections.max(directionScores.entrySet(), Map.Entry.comparingByValue()).getKey(); // Choose the highest scored direction
    }

    // 5. Dynamic Expansion: Decide whether to expand or consolidate based on current turn and population density.
    private boolean shouldExpand(Coordinates cell, UniverseView universeView) {
        int friendlyNeighbors = BotUtils.countFriendlyNeighbors(cell, universeView);
        return currentTurn < turnThreshold && friendlyNeighbors < 3; // Expand early if not surrounded by friendly cells
    }

    // 6. Enemy Cluster Detection: Detect nearby enemy clusters within a specific radius and decide whether to engage.
    private boolean detectEnemyClusters(Coordinates cell, UniverseView universeView) {
        List<Coordinates> enemiesInRadius = BotUtils.getEnemiesInRadius(cell, universeView, maxClusterRadius);
        return !enemiesInRadius.isEmpty(); // If any enemies are in range, return true
    }

    // 7. Population Redistribution: Redistribute population among nearby friendly cells to ensure a balanced defense.
    private void redistributePopulation(Coordinates cell, UniverseView universeView, List<MovementCommand> commandList) {
        List<MovementCommand.Direction> friendlyDirections = BotUtils.getAvailableDirections(cell, universeView);
        int currentPopulation = universeView.getPopulation(cell);

        if (!friendlyDirections.isEmpty() && currentPopulation > defensePopulationThreshold) {
            MovementCommand.Direction direction = friendlyDirections.get(random.nextInt(friendlyDirections.size()));
            int populationToRedistribute = (int) (currentPopulation * 0.4); // Redistribute 40% of population
            BotUtils.move(commandList, cell, direction, populationToRedistribute);
        }
    }

    // 8. Prioritize Resource Pooling: Focus on consolidating resources in critical cells (e.g., near base or defense lines).
    private boolean prioritizeResourcePooling(Coordinates cell, UniverseView universeView) {
        return BotUtils.calculateManhattanDistance(cell, basePosition) < 5; // Prioritize cells close to the base for pooling
    }

    // 9. Weighted Decision System: Create a flexible decision-making process to handle diverse scenarios.
    private void applyWeightedDecisionSystem(Coordinates cell, UniverseView universeView, List<MovementCommand> commandList) {
        int currentPopulation = universeView.getPopulation(cell);
        double expansionWeight = 0.4;
        double defenseWeight = 0.3;
        double attackWeight = 0.3;

        if (detectEnemyClusters(cell, universeView)) {
            attackWeight += 0.2; // Increase attack priority if enemies are nearby
        }

        if (prioritizeResourcePooling(cell, universeView)) {
            defenseWeight += 0.3; // Increase defense priority if near base
        }

        double totalWeight = expansionWeight + defenseWeight + attackWeight;
        double rand = random.nextDouble() * totalWeight;

        if (rand < expansionWeight) {
            handleExpansion(cell, universeView, commandList, currentPopulation);
        } else if (rand < expansionWeight + defenseWeight) {
            handleDefense(cell, universeView, commandList, currentPopulation);
        } else {
            handleAttack(cell, universeView, commandList, currentPopulation);
        }
    }
}
