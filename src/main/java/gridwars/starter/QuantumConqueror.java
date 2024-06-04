package gridwars.starter;

import cern.ais.gridwars.api.Coordinates;
import cern.ais.gridwars.api.UniverseView;
import cern.ais.gridwars.api.bot.PlayerBot;
import cern.ais.gridwars.api.command.MovementCommand;

import java.util.*;

public class QuantumConqueror implements PlayerBot {

    Coordinates basePosition;
    Map<Coordinates, Integer> gravityMap;
    int turnThreshold = 50;
    double expansionFactor = 0.7;
    int minimumPopulationThreshold = 10;
    int aggressivePopulationThreshold = 50;
    Random random = new Random();

    public QuantumConqueror() {
        gravityMap = new HashMap<>();
    }

    @Override
    public void getNextCommands(UniverseView universeView, List<MovementCommand> commandList) {
        List<Coordinates> myCells = universeView.getMyCells();

        if (basePosition == null) {
            basePosition = myCells.get(0);
        }

        gravityMap.clear();

        for (Coordinates cell : myCells) {
            gravityMap.put(cell, calculateGravity(universeView, cell));
        }

        for (Coordinates cell : myCells) {
            int currentPopulation = universeView.getPopulation(cell);

            if (currentPopulation > minimumPopulationThreshold) {
                expandOrDefend(cell, universeView, commandList, currentPopulation);
            }
        }
    }

    private void expandOrDefend(Coordinates cell, UniverseView universeView, List<MovementCommand> commandList, int currentPopulation) {
        List<MovementCommand.Direction> directions = getAvailableDirections(cell, universeView);

        if (universeView.getCurrentTurn() < turnThreshold) {
            if (!directions.isEmpty()) {
                // Use DFS-like expansion
                MovementCommand.Direction selectedDirection = directions.get(random.nextInt(directions.size()));
                int expansionPopulation = (int) (currentPopulation * expansionFactor);
                move(commandList, cell, selectedDirection, expansionPopulation);
            }
        } else {
            // Switch to defense/attack mode after threshold
            if (currentPopulation > aggressivePopulationThreshold) {
                handleAggressiveMove(cell, universeView, commandList, currentPopulation, directions);
            } else {
                consolidate(cell, universeView, commandList);
            }
        }
    }

    private List<MovementCommand.Direction> getAvailableDirections(Coordinates cell, UniverseView universeView) {
        List<MovementCommand.Direction> directions = new ArrayList<>();
        for (MovementCommand.Direction dir : MovementCommand.Direction.values()) {
            Coordinates neighbor = cell.getNeighbour(dir);
            if (universeView.isEmpty(neighbor) || !universeView.belongsToMe(neighbor)) {
                directions.add(dir);
            }
        }
        return directions;
    }

    private void move(List<MovementCommand> commandList, Coordinates cell, MovementCommand.Direction direction, int population) {
        if (population > 0) {
            commandList.add(new MovementCommand(cell, direction, population));
        }
    }

    private void handleAggressiveMove(Coordinates cell, UniverseView universeView, List<MovementCommand> commandList, int currentPopulation, List<MovementCommand.Direction> directions) {
        if (!directions.isEmpty()) {
            // Prioritize attacking enemy cells
            for (MovementCommand.Direction direction : directions) {
                Coordinates neighbor = cell.getNeighbour(direction);
                if (!universeView.belongsToMe(neighbor) && !universeView.isEmpty(neighbor)) {
                    move(commandList, cell, direction, currentPopulation / 2);
                    return;
                }
            }
            // If no enemies nearby, expand to empty cells
            MovementCommand.Direction expansionDir = directions.get(random.nextInt(directions.size()));
            move(commandList, cell, expansionDir, currentPopulation / 3);
        }
    }

    private void consolidate(Coordinates cell, UniverseView universeView, List<MovementCommand> commandList) {
        // Redistribute resources to strengthen the base or reinforce weak cells
        List<MovementCommand.Direction> directions = getAvailableDirections(cell, universeView);
        if (!directions.isEmpty()) {
            MovementCommand.Direction direction = directions.get(random.nextInt(directions.size()));
            int populationToMove = universeView.getPopulation(cell) / 4;
            move(commandList, cell, direction, populationToMove);
        }
    }

    private int calculateGravity(UniverseView universeView, Coordinates cell) {
        int gravity = 0;
        for (MovementCommand.Direction dir : MovementCommand.Direction.values()) {
            Coordinates neighbor = cell.getNeighbour(dir);
            if (!universeView.isEmpty(neighbor) && !universeView.belongsToMe(neighbor)) {
                gravity++;
            }
        }
        return gravity;
    }
}
