package battleship;

import java.util.*;

class Player {
    Grid gridPlayer = new Grid();
    Grid gridEnemy = new Grid();
    Ship aircraftCarrier = new Ship(5, "Aircraft carrier");
    Ship battleship = new Ship(4, "Battleship");
    Ship submarine = new Ship(3, "Submarine");
    Ship cruiser = new Ship(3, "Cruiser");
    Ship destroyer = new Ship(2, "Destroyer");
    Ship[] fleet = {aircraftCarrier, battleship, submarine, cruiser, destroyer};
    public String name;
    private static int counter = 1;

    Player() {
        this.name = "Player " + counter;
        counter++; // todo: check this stuff
    }

}


public class Game {
    Player player1 = new Player();
    Player player2 = new Player();
    //    Grid gridPlayer = new Grid();
//    Grid gridEnemy = new Grid();
//    Ship aircraftCarrier = new Ship(5, "Aircraft carrier");
//    Ship battleship = new Ship(4, "Battleship");
//    Ship submarine = new Ship(3, "Submarine");
//    Ship cruiser = new Ship(3, "Cruiser");
//    Ship destroyer = new Ship(2, "Destroyer");
//    Ship[] fleet = {aircraftCarrier, battleship, submarine, cruiser, destroyer};
    String currentMessage = "";


    public void placeShip(Ship ship, Grid gridPlayer) {
        boolean shipPlaced = false;
        while (!shipPlaced) {
            System.out.printf("Enter the coordinates of the %s (%d cells): \n", ship.name, ship.length);
            Scanner sc = new Scanner(System.in);
            String userInput = sc.nextLine();
            if (!Analyzer.validUserInputTwoCells(userInput)) {
                System.out.println("Error! Wrong user input! Try again: ");
                continue;
            }
            userInput = Input.sortUserInputFromLowToHigh(userInput);
//            if (!Analyzer.allConditionsForPlacementAreTrue(userInput, ship, this.gridPlayer)) {
            if (!Analyzer.allConditionsForPlacementAreTrue(userInput, ship, gridPlayer)) {
                continue;
            }
            // fill ship object
            ship.setPositionStart(userInput.split("\\s+")[0]);
            ship.setPositionFinish(userInput.split("\\s+")[1]);

            // fill grid object
            ArrayList<Integer> shipCells = Ship.listOfCellsForShipPlacement(userInput);
            for (Integer cell : shipCells) {
                gridPlayer.positionToShipReference.put(cell, ship);
            }
            gridPlayer.placeShipOnGrid(ship);
            shipPlaced = true;
        }
    }

    public void placeShips() {
        System.out.println("Player 1, place your ships on the game field");
        player1.gridPlayer.printCurrentState();
        for (int i = 0; i < player1.fleet.length; i++) {
            placeShip(player1.fleet[i], player1.gridPlayer);
        }
        System.out.println("Press enter and pass the move to another player");
        Scanner sc = new Scanner(System.in);
        sc.nextLine();
        System.out.println("Player 2, place your ships on the game field");
        player2.gridPlayer.printCurrentState();
        for (int i = 0; i < player2.fleet.length; i++) {
            placeShip(player2.fleet[i], player2.gridPlayer);
        }
        System.out.println("Press Enter and pass the move to another player");
        sc.nextLine();

//        placeShip(aircraftCarrier);
//        placeShip(battleship);
//        placeShip(submarine);
//        placeShip(cruiser);
//        placeShip(destroyer);
    }

    public void startGame() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            this.makeAShot(player1, player2);
            if (gameIsFinished(player2)) {
                break;
            } else {
                System.out.println("Press Enter and pass the move to another player");
                sc.nextLine();

            }
//            System.out.println("Player 2, it's your turn:");
            this.makeAShot(player2, player1);
            if (gameIsFinished(player1)) {
                break;
            } else {
                System.out.println("Press Enter and pass the move to another player");
                sc.nextLine();

            }
        }
        System.out.print("You sank the last ship. You won. Congratulations!");
    }

    public void makeAShot(Player playerActive, Player playerPassive) {
        boolean success = false;
        Grid gridPlayerActive = playerActive.gridPlayer;
        Grid gridActiveFogOfWar = playerActive.gridEnemy;
        System.out.println();
        gridActiveFogOfWar.printCurrentState();
        System.out.println("---------------------");
        gridPlayerActive.printCurrentState();
        System.out.printf("\n%s, it's your turn:\n", playerActive.name);
        while (!success) {
//            System.out.println("Take a shoot!\n");
            this.currentMessage = "";
            Scanner sc = new Scanner(System.in);
            String userInput = sc.nextLine();
            System.out.println();
            boolean validInput = Analyzer.validUserInputSingleCell(userInput);
            if (!validInput) {
                System.out.println("Error! You entered the wrong coordinates ! Try again: ");
                continue;
            }
            int[] gridPos = Input.convertToGridPos(userInput);
//            grid.setGridCellValue(gridPos[0], gridPos[1], grid.noHitShipSign);
            String valueInGridPosDefendingPlayer = playerPassive.gridPlayer.getSingleCellValue(gridPos[0], gridPos[1]);
            if (Objects.equals(valueInGridPosDefendingPlayer, "O")) {
                gridActiveFogOfWar.setGridCellValue(gridPos[0], gridPos[1], gridPlayerActive.hasBeenHitSign);
                playerPassive.gridPlayer.setGridCellValue(gridPos[0], gridPos[1], gridPlayerActive.hasBeenHitSign);
                this.currentMessage = Grid.updateShipHitCounter(gridPos[0], gridPos[1], playerPassive);
                gridActiveFogOfWar.setGridCellValue(gridPos[0], gridPos[1], gridPlayerActive.hasBeenHitSign);
                playerPassive.gridPlayer.setGridCellValue(gridPos[0], gridPos[1], gridPlayerActive.hasBeenHitSign);
                if ("".equals(this.currentMessage)) {
                    this.currentMessage = "You hit a ship!";
                }
            } else if (Objects.equals(valueInGridPosDefendingPlayer, gridPlayerActive.hasBeenHitSign)) {
                this.currentMessage = "You missed!";
            } else {
                this.currentMessage = "You missed!";
                playerPassive.gridPlayer.setGridCellValue(gridPos[0], gridPos[1], gridPlayerActive.missSign);
                gridActiveFogOfWar.setGridCellValue(gridPos[0], gridPos[1], gridPlayerActive.missSign);
            }
            if (!"You sank last ship.".equals(this.currentMessage)) {
                System.out.println(this.currentMessage);
            }
//            gridEnemy.printCurrentState();
            success = true;

        }
    }

    private boolean gameIsFinished(Player player) {
        return player.gridPlayer.positionToShipReference.isEmpty();
    }
}

class Grid {
    public String[][] gridWithValues = new String[10][10];
    public Map<Integer, String> cellsWithShips = new HashMap<>();
    public Map<Integer, String> cellsNotAvailableForPlacement = new HashMap<>();
    public Map<Integer, Ship> positionToShipReference = new HashMap<>();

    public static String updateShipHitCounter(int line, int row, Player playerPassive) {
        int position = Input.convertGridPosToIntegerKey(new int[]{line, row});
        Ship ship = playerPassive.gridPlayer.positionToShipReference.get(position);
        playerPassive.gridPlayer.positionToShipReference.remove(position);
        ship.hitCounter++;
        if (ship.hitCounter >= ship.length && playerPassive.gridPlayer.positionToShipReference.isEmpty()) {
            return "You sank last ship.";
        } else if (ship.hitCounter >= ship.length) {
            return "You sank a ship!";
        }
        return "";
    }

    String fogOfWarSign = "~";
    String noHitShipSign = "O";
    String hasBeenHitSign = "X";
    String missSign = "M";

    Grid() {
        Arrays.stream(getGridWithValues()).forEach(a -> Arrays.fill(a, fogOfWarSign));
//        printCurrentState();
    }

    public void printCurrentState() {
//        System.out.println();
        System.out.print(" ");
        String firstLine = " 1 2 3 4 5 6 7 8 9 10";
        System.out.println(firstLine);
        char start = 'A';
        for (int i = 0; i < getGridWithValues().length; i++) {
            System.out.print((char) (start + i));
            for (int j = 0; j < getGridWithValues().length; j++) {
                System.out.print(" ");
                System.out.print(getGridWithValues()[i][j]);
            }
            System.out.println();
        }
//        System.out.println();
    }


    public String[][] getGridWithValues() {
        return gridWithValues;
    }

    public void setGridCellValue(int line, int column, String value) {
        this.gridWithValues[line][column] = value;
    }

    private boolean cellIsFogOfWar(int line, int column) {
        return this.fogOfWarSign.equals(this.gridWithValues[line][column]);
    }

    private void placeShipIntoGridArray(int[] start, int[] finish) {
        for (int i = 0; i < gridWithValues.length; i++) {
            if (i >= start[0] && i <= finish[0]) {
                for (int j = 0; j < gridWithValues.length; j++) {
                    if (j >= start[1] && j <= finish[1]) {
//                        System.out.printf("line:%d row:%d", i, j);
                        setGridCellValue(i, j, this.noHitShipSign);
                    }
                }
            }
        }
    }

    public void placeShipOnGrid(Ship ship) {
        // convert positions into grid data
        int[] start = ship.getPositionStartGrid();
        int[] finish = ship.getPositionFinishGrid();

        // set positions on grid array
        placeShipIntoGridArray(start, finish);

        // fill hashmap available
        fillCellsNotAvailableForPlacement(start, finish);

        // fill hashmap ships
        fillCellsWithShips(start, finish);

        // print current grid sate
        this.printCurrentState();

    }

    private void fillCellsWithShips(int[] start, int[] finish) {
        ArrayList<Integer> cells = Ship.listOfCellsForShipPlacement(start, finish);
        for (Integer i : cells) {
            this.cellsWithShips.put(i, this.noHitShipSign);
        }
    }

    private void fillCellsNotAvailableForPlacement(int[] start, int[] finish) {
        ArrayList<Integer> cells = Ship.listOfCellsForShipPlacement(start, finish);
        Integer max = Collections.max(cells);
        Integer min = Collections.min(cells);
        if (max - min < 10) {
            this.cellsNotAvailableForPlacement.put(max + 1 - 10, this.fogOfWarSign);
            this.cellsNotAvailableForPlacement.put(max + 1 + 10, this.fogOfWarSign);
            this.cellsNotAvailableForPlacement.put(min - 1 - 10, this.fogOfWarSign);
            this.cellsNotAvailableForPlacement.put(min - 1 + 10, this.fogOfWarSign);
            this.cellsNotAvailableForPlacement.put(min - 1, this.fogOfWarSign);
            this.cellsNotAvailableForPlacement.put(max + 1, this.fogOfWarSign);
            for (Integer i : cells) {
                this.cellsNotAvailableForPlacement.put(i, this.noHitShipSign);
                this.cellsNotAvailableForPlacement.put(i + 10, this.fogOfWarSign);
                this.cellsNotAvailableForPlacement.put(i - 10, this.fogOfWarSign);
            }
        } else {
            this.cellsNotAvailableForPlacement.put(max + 10 - 1, this.fogOfWarSign);
            this.cellsNotAvailableForPlacement.put(max + 10 + 1, this.fogOfWarSign);
            this.cellsNotAvailableForPlacement.put(min - 10 - 1, this.fogOfWarSign);
            this.cellsNotAvailableForPlacement.put(min - 10 + 1, this.fogOfWarSign);
            this.cellsNotAvailableForPlacement.put(min - 10, this.fogOfWarSign);
            this.cellsNotAvailableForPlacement.put(max + 10, this.fogOfWarSign);
            for (Integer i : cells) {
                this.cellsNotAvailableForPlacement.put(i, this.noHitShipSign);
                this.cellsNotAvailableForPlacement.put(i + 1, this.fogOfWarSign);
                this.cellsNotAvailableForPlacement.put(i - 1, this.fogOfWarSign);
            }
        }

    }

    public String getSingleCellValue(int line, int row) {
        return this.gridWithValues[line][row];
    }
}

class Ship {
    public final String name;
    public final int length;
    String[] state;
    public String positionStart;
    public String positionFinish;
    public int hitCounter;

    Ship(int length, String name) {
        this.length = length;
        this.state = new String[length];
        this.name = name;
        this.hitCounter = 0;
    }

    public void setPositionStart(String positionStart) {
        this.positionStart = positionStart;
    }

    public void setPositionFinish(String positionFinish) {
        this.positionFinish = positionFinish;
    }

    public int[] getPositionStartGrid() {
        int line = this.positionStart.charAt(0) - 'A';
//        int row = this.positionStart.charAt(1) - '0' - 1;
        String digitsCell = this.positionStart.replaceAll("[^0-9]", "");
        int dc = Integer.parseInt(digitsCell) - 1;
        return new int[]{line, dc};
    }

    public int[] getPositionFinishGrid() {
        int line = this.positionFinish.charAt(0) - 'A';
//        int row = this.positionFinish.charAt(1) - '0' - 1;
        String digitsCell = this.positionFinish.replaceAll("[^0-9]", "");
        int dc = Integer.parseInt(digitsCell) - 1;
        return new int[]{line, dc};
    }


    public static ArrayList<Integer> listOfCellsForShipPlacement(String userInput) {
        ArrayList<Integer> cells = new ArrayList<>();

        int[] positionStart = Input.convertToGridPos(userInput.split("\\s+")[0]);
        int[] positionFinish = Input.convertToGridPos(userInput.split("\\s")[1]);
        int start = Input.convertGridPosToIntegerKey(positionStart);
        int finish = Input.convertGridPosToIntegerKey(positionFinish);

        if ((finish - start) % 10 > 0) {
            for (int i = start; i <= finish; i++) {
                cells.add(i);
            }
        } else if ((finish - start) % 10 == 0) {
            for (int i = start; i <= finish; i += 10) {
                cells.add(i);
            }
        }
        return cells;
    }

    public static ArrayList<Integer> listOfCellsForShipPlacement(int[] positionStart, int[] positionFinish) {
        ArrayList<Integer> cells = new ArrayList<>();
        int start = Input.convertGridPosToIntegerKey(positionStart);
        int finish = Input.convertGridPosToIntegerKey(positionFinish);

        if ((finish - start) % 10 > 0) {
            for (int i = start; i <= finish; i++) {
                cells.add(i);
            }
        } else if ((finish - start) % 10 == 0) {
            for (int i = start; i <= finish; i += 10) {
                cells.add(i);
            }
        }
        return cells;
    }


}

class Input {
    public static int[] convertToGridPos(String userInputSingleCell) {
        int line = userInputSingleCell.charAt(0) - 'A';
//        int row = userInputSingleCell.charAt(1) - '0' - 1;
        String digitsCell = userInputSingleCell.replaceAll("[^0-9]", "");
        int dc = Integer.parseInt(digitsCell) - 1;
        return new int[]{line, dc};
    }

    public static String convertToHumanPos(int[] position) {
        char line = (char) (position[0] + 'A');
        String row = Integer.toString(position[1] + 1);
        return String.format("%c%s", line, row);
    }

    public static String sortUserInputFromLowToHigh(String userInput) {
        String userInputCellOne = userInput.split("\\s+")[0];
        String userInputCellTwo = userInput.split("\\s")[1];
        String digitsCellOne = userInputCellOne.replaceAll("[^0-9]", "");
        String digitsCellTwo = userInputCellTwo.replaceAll("[^0-9]", "");
        int dc1 = Integer.parseInt(digitsCellOne);
        int dc2 = Integer.parseInt(digitsCellTwo);
        if ((userInputCellOne.charAt(0) <= userInputCellTwo.charAt(0)) &&
                (dc1 <= dc2)) {
            return userInput;
        } else {
            return String.format("%s %s", userInputCellTwo, userInputCellOne);
        }
    }

    public static int convertGridPosToIntegerKey(int[] position) {
        int row = position[0];
        int line = position[1];
        return row * 10 + line;
    }

    public static int convertHumanPosToIntegerKey(String userInputSingleCell) {
        int[] position = convertToGridPos(userInputSingleCell);
        return convertGridPosToIntegerKey(position);
    }
}

class Analyzer {
    public static boolean validUserInputTwoCells(String userInput) {
        return userInput.matches("^[A-J]([1-9]|10) [A-J]([1-9]|10)$");
    }

    public static boolean validUserInputSingleCell(String userInput) {
        return userInput.matches("^[A-J]([1-9]|10)$");
    }

    public static boolean validShipPlacement(String userInput) {
        String userInputCellOne = userInput.split("\\s+")[0];
        String userInputCellTwo = userInput.split("\\s")[1];
        String digitsCellOne = userInputCellOne.replaceAll("[^0-9]", "");
        String digitsCellTwo = userInputCellTwo.replaceAll("[^0-9]", "");
        int dc1 = Integer.parseInt(digitsCellOne);
        int dc2 = Integer.parseInt(digitsCellTwo);

        int line = userInputCellOne.charAt(0) - userInputCellTwo.charAt(0);
        int row = dc2 - dc1;
        return line == 0 || row == 0;
    }

    public static boolean validShipLength(String userInput, Ship ship) {
        String userInputCellOne = userInput.split("\\s+")[0];
        String userInputCellTwo = userInput.split("\\s")[1];
        String digitsCellOne = userInputCellOne.replaceAll("[^0-9]", "");
        String digitsCellTwo = userInputCellTwo.replaceAll("[^0-9]", "");
        int dc1 = Integer.parseInt(digitsCellOne);
        int dc2 = Integer.parseInt(digitsCellTwo);

        int line = userInputCellTwo.charAt(0) - userInputCellOne.charAt(0);
        int row = dc2 - dc1;
        int lengthByInput = Math.max(Math.abs(line), Math.abs(row)) + 1;
        return lengthByInput == ship.length;
    }

    public static boolean cellsAreAvailableForPlacement(String userInput, Grid grid) {
        String userInputCellOne = userInput.split("\\s+")[0];
        String userInputCellTwo = userInput.split("\\s+")[1];
        int[] firstPosition = Input.convertToGridPos(userInputCellOne);
        int[] lastPosition = Input.convertToGridPos(userInputCellTwo);

        // get positions for whole ship
        ArrayList<Integer> cellsForShip = Ship.listOfCellsForShipPlacement(firstPosition, lastPosition);

        // check if all are available
        for (Integer i : cellsForShip) {
            if (grid.cellsNotAvailableForPlacement.containsKey(i)) {
                return false;
            }
        }
        return true;
    }


    public static boolean allConditionsForPlacementAreTrue(String userInput, Ship ship, Grid grid) {
        boolean validInp = validUserInputTwoCells(userInput);
        if (!validInp) {
            System.out.println("Error! Wrong user input! Try again: ");
            return false;
        }
        boolean validShip = validShipPlacement(userInput);
        if (!validShip) {
            System.out.println("Error! Wrong input, ship has to be in line or in row! Try again: ");
            return false;
        }
        boolean validShLen = validShipLength(userInput, ship);
        if (!validShLen) {
            System.out.println("Error! Wrong length of the ship! Try again: ");
            return false;
        }

        boolean cellsAvailable = cellsAreAvailableForPlacement(userInput, grid);
        if (!cellsAvailable) {
            System.out.println("Error! Wrong ship location! Try again: ");
            return false;
        }
        return true;
    }
}
