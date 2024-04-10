package server;

import java.net.*;
import java.io.*;
import java.util.*;

import client.Player;
import org.json.*;

import java.lang.*;

import buffers.RequestProtos.Request;
import buffers.RequestProtos.Logs;
import buffers.RequestProtos.Message;
import buffers.ResponseProtos.Response;
import buffers.ResponseProtos.Entry;

import static buffers.ResponseProtos.Response.ResponseType.*;

class SockBaseServer implements Runnable{
    static String logFilename = "logs.txt";

    ServerSocket serv = null;
    InputStream in = null;
    OutputStream out = null;
    Socket clientSocket = null;
    int port = 9099; // default port
    Game game;

    JSONObject leaderBoard = new JSONObject(); //keeps track of all new users and existing ones


    public SockBaseServer(Socket sock, Game game) {
        this.clientSocket = sock;
        this.game = game;
        try {
            in = clientSocket.getInputStream();
            out = clientSocket.getOutputStream();
        } catch (Exception e) {
            System.out.println("Error in constructor: " + e);
        }
    }


    public JSONObject readJsonLeaderFile() {
        JSONObject JSON = new JSONObject();
        try {

            FileReader reader = new FileReader("leaderboard.json");
            JSONTokener tokener = new JSONTokener(reader);
            JSON = new JSONObject(tokener);
            return JSON;

        } catch (FileNotFoundException E) {
            System.out.println("File not found");
        }
        return JSON;
    }

    public boolean isJsonFileEmpty() {

        File file = new File("leaderboard.json");

        return file.exists() && file.length() == 0;

    }

    public void writeToJson(String name, int wins, int logins) {
        JSONObject playerObj = new JSONObject();

        if (!isJsonFileEmpty()) {
            try {
                playerObj.put("Name", name);
                playerObj.put("Won", wins);
                playerObj.put("Login", logins);
                leaderBoard.put(name, playerObj);
                FileWriter file = new FileWriter("leaderboard.json");
                file.write(leaderBoard.toString());
                file.flush();
            } catch (IOException E) {
                E.printStackTrace();
            }
            System.out.println("Successfully wrote JSON object to file.");
        } else {//is empty
            playerObj.put("Name", name);
            playerObj.put("Won", wins);
            playerObj.put("Login", logins);
            leaderBoard.put(name, playerObj);
            try {
                FileWriter file = new FileWriter("leaderboard.json");
                file.write(leaderBoard.toString());
                file.flush();
            } catch (IOException E) {
                E.printStackTrace();
            }

        }

    }

    // Handles the communication right now it just accepts one input and then is done you should make sure the server stays open
    // can handle multiple requests and does not crash when the server crashes
    // you can use this server as based or start a new one if you prefer.
    @Override
    public void run() {
        boolean flag = true;
        String name = "";
        System.out.println("Ready...");
        int row1 = 0;
        int col1 = 0;
        int row2 = 0;
        int col2 = 0;
        Response.Builder res = null;
        Response response = null;
        do {
            try {
                // read the proto object and put into new objct
                Request op = Request.parseDelimitedFrom(in);

                int flag1 = 0;
                if (!isJsonFileEmpty()) {//if not empty
                    JSONObject read = readJsonLeaderFile();
                    leaderBoard = read;
                }

                switch (op.getOperationType()) {
                    case NAME:
                        // get name from proto object
                        name = op.getName();
                        int won = 0;
                        int login = 0;
                        // writing a connect message to the log with name and CONNENCT
                        writeToLog(name, Message.CONNECT);
                        System.out.println("Got a connection and a name: " + name);
                        res = Response.newBuilder()
                                .setResponseType(GREETING);
                        Entry entry = null;
                        if (isJsonFileEmpty()) {
                            Player player = new Player(name, 0, 1);
                            writeToJson(player.getName(), player.getWins(), player.getLogin());
                            entry = Entry.newBuilder()
                                    .setName(player.getName())
                                    .setWins(player.getWins())
                                    .setLogins(player.getLogin())
                                    .build();
                            res.addLeader(entry);
                            res.setMessage("Hello " + name + " and welcome. Welcome to a simple game of battleship. ");
                        } else if (!leaderBoard.has(name)) { //if does not contain in leader is a new player
                            Player player = new Player(name, 0, 1);
                            writeToJson(player.getName(), player.getWins(), player.getLogin());
                            entry = Entry.newBuilder()
                                    .setName(player.getName())
                                    .setWins(player.getWins())
                                    .setLogins(player.getLogin())
                                    .build();
                            res.addLeader(entry);
                            res.setMessage("Hello " + name + " and welcome. Welcome to a simple game of battleship. ");
                        } else {//updates leader board if they exist
                            String name1 = leaderBoard.getJSONObject(name).getString("Name");
                            won = leaderBoard.getJSONObject(name).getInt("Won");
                            login = leaderBoard.getJSONObject(name).getInt("Login");
                            login++;
                            writeToJson(name1, won, login);
                            entry = Entry.newBuilder()
                                    .setName(name1)
                                    .setWins(won)
                                    .setLogins(login)
                                    .build();

                            res.addLeader(entry);
                            res.setMessage("Hello " + name + " and welcome. Welcome to a simple game of battleship. ");
                        }
                        response = res.build();
                        response.writeDelimitedTo(out);
                        break;
                    case NEW:
                        game.newGame(); // starting a new game

                        System.out.println("GAME HAS STARTED");
                        System.out.println("\n\nExample response:");
                        System.out.println("Type: " + response.getResponseType());
                        System.out.println("Board: \n" + response.getBoard());
                        System.out.println("Original Board: \n" + game.showOriginalBoard());
                        System.out.println("Eval: \n" + response.getEval());
                        System.out.println("Second: \n" + response.getSecond());
                        // Example on how you could build a simple response for PLAY as answer to NEW
                        response = Response.newBuilder()
                                .setResponseType(Response.ResponseType.PLAY)
                                .setBoard(game.getHiddenBoard()) // gets the hidden board
                                .setSecond(false)
                                .build();

                        response.writeDelimitedTo(out);
                        break;
                    case TILE1:
                        game.checkWin();
                        game.showOriginalBoard();
                        String tile = op.getTile();
                        row1 = tile.charAt(0) - 'a' + 1;

                        switch (tile.charAt(1)) {
                            case ('1'):
                                col1 = 2;
                                break;
                            case ('2'):
                                col1 = 4;
                                break;
                            case ('3'):
                                col1 = 6;
                                break;
                            case ('4'):
                                col1 = 8;
                                break;
                            default:
                                System.out.println("Invalid column number\n");
                                response = Response.newBuilder()
                                        .setResponseType(ERROR)
                                        .setMessage("Invalid column number\n")
                                        .build();

                                break;
                        }
                        System.out.println("row1: \t" + row1);
                        System.out.println("Col: \t" + col1);
                        char tileCheck = game.getTile(row1,col1);
                        if (!tile.matches("[a-d][1-4]")) {
                            System.out.println("Incorrect format\n");
                        } else if (tile.length() != 2) {
                            System.out.println("Invalid input. Please enter a letter (a-d) followed by a number (1-4).\n");
                            response = Response.newBuilder()
                                    .setResponseType(ERROR)
                                    .setMessage("Invalid input. Please enter a letter (a-d) followed by a number (1-4).\n")
                                    .build();

                        } else if (row1 < 1 || row1 >= game.getRow() || col1 < 2 || col1 > game.getCol()) {
                            System.out.println("Not with in bounds\n");
                            System.out.println("Column size " + game.getCol());
                            System.out.println("Row size " + game.getRow());
                            response = Response.newBuilder()
                                    .setResponseType(ERROR)
                                    .setMessage("Not with in bounds\n")
                                    .build();

                        } else if ((row1 > 1) || (row1 < game.getRow()) || (col1 > 2) || (col1 < game.getCol()) || !game.getWon()) {//with in bounds and condition met
                            System.out.println("tile location " + game.getTile(row1, col1));
                            if(game.getHiddenTile(row1,col1) == '?'){
                                game.replaceOneCharacter(row1,col1);
                                response = Response.newBuilder()
                                        .setResponseType(Response.ResponseType.PLAY)
                                        .setBoard(game.getHiddenBoard())
                                        .setFlippedBoard(game.tempFlipWrongTiles(row1, col1)) // gets the hidden board
                                        .setSecond(true)
                                        .setMessage(String.valueOf(game.getTile(row1, col1)))
                                        .build();
                            }else{
                                response = Response.newBuilder()
                                        .setResponseType(Response.ResponseType.PLAY)
                                        .setBoard(game.getHiddenBoard())
                                        .setSecond(false)
                                        .setMessage(String.valueOf(game.getTile(row1, col1)) + " has already been picked try again")
                                        .build();
                            }
                        } else {
                            System.out.println("Options not recognize\n");
                            response = Response.newBuilder()
                                    .setResponseType(ERROR)
                                    .setMessage("Options not recognize\n")
                                    .build();

                        }
                        game.checkWin();
                        if (game.getWon()) {
                            JSONObject player = leaderBoard.getJSONObject(op.getName());
                            writeToJson(player.getString("Name"), player.getInt("Won"), player.getInt("Login"));
                            System.out.println("User has won");
                            response = Response.newBuilder()
                                    .setResponseType(WON)
                                    .setBoard(game.getHiddenBoard())
                                    .setMessage("User has won!!\n")
                                    .build();
//                            response.writeDelimitedTo(out);
                        }
                        response.writeDelimitedTo(out);
                        break;
                    case TILE2:
                        game.checkWin();
                        System.out.println("Hidden board: \n" + game.getHiddenBoard());
                        System.out.println("Original board: \n" + game.showOriginalBoard());
                        String tile1 = op.getTile();
                        row2 = tile1.charAt(0) - 'a' + 1;
                        switch (tile1.charAt(1)) {
                            case ('1'):
                                col2 = 2;
                                break;
                            case ('2'):
                                col2 = 4;
                                break;
                            case ('3'):
                                col2 = 6;
                                break;
                            case ('4'):
                                col2 = 8;
                                break;
                            default:
                                System.out.println("Invalid column number\n");
                                response = Response.newBuilder()
                                        .setResponseType(ERROR)
                                        .setMessage("Invalid column number\n")
                                        .build();
//                                response.writeDelimitedTo(out);
                                break;
                        }
                        System.out.println("Tile input " + tile1);
                        System.out.println("row2: " + row2);
                        System.out.println("Co2: " + col2);
                        if (!tile1.matches("[a-d][1-4]")) {
                            System.out.println("Incorrect format");
                        } else if (tile1.length() != 2) {
                            System.out.println("Invalid input. Please enter a letter (a-d) followed by a number (1-4).\n");
                            response = Response.newBuilder()
                                    .setResponseType(ERROR)
                                    .setFlippedBoard(game.getHiddenBoard())
                                    .setMessage("Invalid input. Please enter a letter (a-d) followed by a number (1-4).\n")
                                    .build();
//                            response.writeDelimitedTo(out);
                        } else if (row2 < 1 || row2 >= game.getRow() || col2 < 2 || col2 > game.getCol()) {
                            System.out.println("Not with in bounds\n");
                            response = Response.newBuilder()
                                    .setResponseType(ERROR)
                                    .setFlippedBoard(game.getHiddenBoard())
                                    .setMessage("Not with in bounds\n")
                                    .build();
//                            response.writeDelimitedTo(out);
                        } else if(row1 == row2 && col1 == col2){
                            game.tempFlipWrongTiles(row1,col1);
                            System.out.println("Tile has already been picked");
                            response = Response.newBuilder()
                                    .setResponseType(ERROR)
                                    .setFlippedBoard(game.getHiddenBoard())
                                    .setMessage("Tile has already been picked\n")
                                    .build();
                        }else if (row2 > 1 || row2 < game.getRow() || col2 > 2 || col2 < game.getCol() || !game.getWon()) {
                            System.out.println("tile location \n" + game.getTile(row2, col2));
                            System.out.println("Original board: \n" + game.showOriginalBoard());
                            System.out.println("Hidden Board: \n" + game.getHiddenBoard());
                            if(((row1 == row2) && (col1 == col2)) && (game.getHiddenTile(row1,col1) != '?') && (game.getHiddenTile(row2,col2) != '?')){
                                System.out.println("Can not pick the same spot on board");
                                response = Response.newBuilder()
                                        .setResponseType(PLAY)
                                        .setBoard(game.getHiddenBoard())
                                        .setEval(false)
                                        .setSecond(true)
                                        .setMessage("Spot has already been flipped try again\n")
                                        .build();
                            }else{
                                char test = game.getHiddenTile(row1,col1);
                                char test1 = game.getHiddenTile(row2,col2);
                                System.out.println("character acquisition: t1 " + test);
                                System.out.println("character acquisition: t2 " + test1);
                                if(game.getHiddenTile(row1,col1) == '?' && game.getHiddenTile(row2,col2) == '?'){
                                    if (game.matchFound(row1, col1, row2, col2)) {
                                        System.out.println("Match found\n");
                                        game.replaceTwoCharacter(row1, col1, row2, col2);
                                        response = Response.newBuilder()
                                                .setResponseType(PLAY)
                                                .setBoard(game.getHiddenBoard())
                                                .setEval(true)
                                                .setSecond(false)
                                                .setMessage("Match has been found\n")
                                                .build();
//                                response.writeDelimitedTo(out);
                                        row1 = 0;
                                        row2 = 0;
                                        col2 = 0;
                                        col1 = 0;
                                    } else {
                                        System.out.println("Match has not been found\n");
                                        response = Response.newBuilder()
                                                .setResponseType(Response.ResponseType.PLAY)
                                                .setBoard(game.tempFlipWrongTiles(row1, col1, row2, col2)) // gets the hidden board
                                                .setMessage("Not a match") // gets the hidden board
                                                .setEval(false)
                                                .setSecond(false)
                                                .build();
//                                response.writeDelimitedTo(out);
                                        row1 = 0;
                                        row2 = 0;
                                        col2 = 0;
                                        col1 = 0;
                                    }
                                }else{
                                    System.out.println("Tile has already been uncover");
                                    response = Response.newBuilder()
                                            .setResponseType(Response.ResponseType.PLAY)
                                            .setBoard(game.getHiddenBoard()) // gets the hidden board
                                            .setMessage("Tile has already been uncovered") // gets the hidden board
                                            .setEval(false)
                                            .setSecond(true)
                                            .build();
                                }
                            }
                        } else {
                            System.out.println("Options not recognize\n");
                            response = Response.newBuilder()
                                    .setResponseType(ERROR)
                                    .setBoard(game.getHiddenBoard()) // gets the hidden board
                                    .setEval(false)
                                    .setSecond(false)
                                    .setMessage("Invalid option")
                                    .build();
//                            response.writeDelimitedTo(out);
                        }
                        game.checkWin();
                        if (game.getWon()) {
                            JSONObject player = leaderBoard.getJSONObject(op.getName());
                            writeToJson(player.getString("Name"), player.getInt("Won") + 1, player.getInt("Login"));
                            System.out.println("User has won");
                            response = Response.newBuilder()
                                    .setResponseType(WON)
                                    .setBoard(game.getHiddenBoard())
                                    .setMessage("User has won!!\n")
                                    .build();
//                            response.writeDelimitedTo(out);

                        }
                        assert response != null;
                        response.writeDelimitedTo(out);
                        break;
                    case QUIT:
                        response = Response.newBuilder()
                                .setResponseType(Response.ResponseType.BYE)
                                .setMessage("Leaving menu good bye").build();
                        response.writeDelimitedTo(out);
                        flag = false;
                        break;
                    case LEADER:
                        res = Response.newBuilder()
                                .setResponseType(Response.ResponseType.LEADER);
                        // building an Entry for the leaderboard
                        Entry leader = null;

                        for (String player : leaderBoard.keySet()) {
                            JSONObject playerObj = leaderBoard.getJSONObject(player);
                            String namePlayer = playerObj.getString("Name");
                            int win = playerObj.getInt("Won");
                            int login1 = playerObj.getInt("Login");
                            System.out.println(name + " - Won: " + win + " - Login: " + login1);
                            leader = Entry.newBuilder()
                                    .setName(namePlayer)
                                    .setWins(win)
                                    .setLogins(login1)
                                    .build();
                            res.addLeader(leader);

                        }
                        response = res.build();
                        response.writeDelimitedTo(out);
                        break;
                    default:
                        System.out.println();
                        break;
                }

            } catch (Exception ex) {

                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("Output stream close");
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("Input stream close");
                }
                if (clientSocket != null) {
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("Client socket close");
                }
                break;
            }
        } while (flag);
    }

    /**
     * Writing a new entry to our log
     *
     * @param name    - Name of the person logging in
     * @param message - type Message from Protobuf which is the message to be written in the log (e.g. Connect)
     * @return String of the new hidden image
     */
    public static void writeToLog(String name, Message message) {
        try {
            // read old log file 
            Logs.Builder logs = readLogFile();

            // get current time and data
            Date date = java.util.Calendar.getInstance().getTime();
            System.out.println(date);

            // we are writing a new log entry to our log
            // add a new log entry to the log list of the Protobuf object
            logs.addLog(date.toString() + ": " + name + " - " + message);

            // open log file
            FileOutputStream output = new FileOutputStream(logFilename);
            Logs logsObj = logs.build();

            // This is only to show how you can iterate through a Logs object which is a protobuf object
            // which has a repeated field "log"

            for (String log : logsObj.getLogList()) {

                System.out.println(log);
            }

            // write to log file
            logsObj.writeTo(output);
        } catch (Exception e) {
            System.out.println("Issue while trying to save");
        }
    }

    /**
     * Reading the current log file
     *
     * @return Logs.Builder a builder of a logs entry from protobuf
     */
    public static Logs.Builder readLogFile() throws Exception {
        Logs.Builder logs = Logs.newBuilder();

        try {
            // just read the file and put what is in it into the logs object
            return logs.mergeFrom(new FileInputStream(logFilename));
        } catch (FileNotFoundException e) {
            System.out.println(logFilename + ": File not found.  Creating a new file.");
            return logs;
        }
    }


    public static void main(String args[]) throws Exception {
        Game game = new Game();
        if (args.length != 2) {
            System.out.println("Expected arguments: <port(int)> <delay(int)>");
            System.exit(1);
        }
        int port = 8080; // default port
        int sleepDelay = 10000; // default delay
        Socket clientSocket = null;
        ServerSocket serv = null;

        try {
            port = Integer.parseInt(args[0]);
            sleepDelay = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port|sleepDelay] must be an integer");
            System.exit(2);
        }
        try {
            serv = new ServerSocket(port);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }


//        Socket sock = server.accept();
//        Performer performer = new Performer(sock,strings);
//        Thread thread = new Thread(performer);
//        thread.start();
        while (true) {
            System.out.println("Wait for connection");
            clientSocket = serv.accept();
            System.out.println("Connection made");
//            SockBaseServer server = new SockBaseServer(clientSocket, game, list, tracker);
            SockBaseServer server = new SockBaseServer(clientSocket, game);
            Thread thread = new Thread(server);
            thread.start();
//            server.start();
        }


    }

}

