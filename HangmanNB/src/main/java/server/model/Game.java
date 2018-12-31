package server.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

class Game {

    private String word, message;

    private static String startMsg = "Enter a word or character as a guess. Enter 'next' to restart, or 'Disconnect' to quit.";
    private static String loseMsg = "You lose";
    private static String winMsg = "You win!";
    private static String iGCorrect = "Correct Guess";
    private static String iGWrong = "Wrong Guess";
    private int life, score;
    private Boolean gameOver;
    // char array to hold the word
    private char[] magicWord;
    // Char array to hold the game state
    private char[] hiddenWord;
    private final ArrayList<String> dictionary = new ArrayList<String>();
    private Boolean fileRead = false;

    String startGame(Boolean newGame) {
        if (!fileRead) {
            try {
                readFile();
            } catch (IOException ioex) {
                System.out.println("Couldnt read the file");
            }
        }
        gameOver = false;
        word = dictionary.get((int) (Math.random() * dictionary.size()));
        life = word.length();
        magicWord = word.toCharArray();
        hiddenWord = setup(word);
        message = startMsg;
        if (newGame == true) {
            score = 0;
        }
        System.out.println("Word is " + word);
        return createResponse();
    }

    String gameEntry(String received) {
        boolean guessed = false;
        if (life <= 0) {
            message = "The game is finished please type Next to get a new word";
        } else {
            if (received.length() == 1) {
                received.toLowerCase();
                for (int i = 0; i < magicWord.length; i++) {
                    if (magicWord[i] == received.charAt(0)) {
                        hiddenWord[i] = received.charAt(0);
                        guessed = true;
                        message = iGCorrect;
                    }
                }
                if (guessed == false) {
                    life = life - 1;
                    message = iGWrong;
                }
            } else {
                if (word.equalsIgnoreCase(received)) {
                    hiddenWord = received.toCharArray();
                } else {
                    life = life - 1;
                    message = iGWrong;
                }
            }
            if (life == 0 && gameOver == false) {
                score--;
                message = loseMsg;
                gameOver = true;
            }
            if (!(new String(hiddenWord).contains("_")) && gameOver == false) {
                score++;
                message = winMsg;
                gameOver = true;
            }
        }

        String result = new MessageToSend(hiddenWord, life, score, message).toString();
        System.out.println(result);
        return result;
    }

    String restart() {
        if (!gameOver) {
            score--;
        }
        return startGame(false);
    }

    private String createResponse() {
        MessageToSend msg = new MessageToSend(hiddenWord, life, score, message);
        String response = msg.toString();
        return response;
    }

    public void readFile() throws IOException {
// The path is specific to my directories and it has to be changed accordinly
        File possibleWords = new File("C:\\Users\\MVPIMP\\Documents\\NetBeansProjects\\hangman\\words.txt");
        Scanner wordInput = new Scanner(possibleWords);
        while (wordInput.hasNext()) {
            String add = wordInput.next();
            if (add.length() != 1) {
                dictionary.add(add.toLowerCase());
            }
        }
        fileRead = true;
        return;
    }

    public char[] setup(String word) {
        int lengthOfWord = word.length();
        char[] trial = new char[lengthOfWord];
        for (int i = 0; i < lengthOfWord; i++) {
            trial[i] = '_';
        }
        return trial;
    }

}
