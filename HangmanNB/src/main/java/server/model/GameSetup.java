package server.model;

public class GameSetup {

    private String gameData = "Game Data Default";
    private Boolean isGameStarted = false;
    private Game game;
    private Boolean newGame = true;

    public String getGameData() {
        System.out.println("Server Sending word: " + gameData);
        return gameData;
    }

    public void setGameData(String received) {
        System.out.println("Server Received word: " + received);
        switch (received.toLowerCase()) {
            case "start":
                startGame();
                break;
            case "next":
                restart();
                break;
            default:
                gameEntry(received);
                break;
        }

    }

    private void startGame() {
        game = new Game();
        this.gameData = game.startGame(newGame);
    }

    private void restart() {
        this.gameData = game.restart();
    }

    private void gameEntry(String input) {
        this.gameData = game.gameEntry(input);
    }

}
