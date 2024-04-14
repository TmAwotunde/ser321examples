package server;

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;

import client.Player;
import proto.RequestProtos.*;
import proto.ResponseProtos.*;

class SockBaseServer {
    static String logFilename = "logs.txt";

    ServerSocket socket = null;
    InputStream in = null;
    OutputStream out = null;
    Socket clientSocket = null;
    int port = 9099; // default port
    Game game;
    Map<String, Player> leaderboard;


    public SockBaseServer(Socket sock, Game game, Map<String, Player> leader){
        this.clientSocket = sock;
        this.game = game;
        leaderboard = leader;
        try {
            in = clientSocket.getInputStream();
            out = clientSocket.getOutputStream();
        } catch (Exception e){
            System.out.println("Error in constructor: " + e);
        }
    }

    // Handles the communication right now it just accepts one input and then is done you should make sure the server stays open
    // can handle multiple requests and does not crash when the client crashes
    // you can use this server as based or start a new one if you prefer. 
    public void handleRequests() {
        String name = "";
        System.out.println("Ready...");
        boolean run = true;
        while (run) {
            try {

                // read the proto object and put into new objct
                Request op = Request.parseDelimitedFrom(in);
                String result = null;
                Response response = Response.newBuilder().build();

                if(op != null)
                    switch(op.getOperationType()){
                        // if the operation is NAME (so the beginning then say there is a connection and greet the client)
                        case NAME:
                            response = nameRequest(op);
                            break;
                        case LEADERBOARD:
                            Response.Builder res = Response.newBuilder()
                                    .setResponseType(Response.ResponseType.LEADERBOARD);
                            for (Player each: leaderboard.values()) {
                                Leader leader = Leader.newBuilder()
                                        .setName(each.getName())
                                        .setWins(each.getWins())
                                        .setLogins(each.getLogins())
                                        .build();
                                res.addLeaderboard(leader);
                            }
                            response = res.build();
                            break;
                        case START:
                            game.newGame();
                            response = Response.newBuilder().setResponseType(Response.ResponseType.TASK).setPhrase(game.getPhrase())
                                    .setTask(game.getTask()).build();
                            break;
                        case GUESS:
                            String guess = op.getGuess();
                            if (game.markGuess(guess.toUpperCase().charAt(0))){
                                response = Response.newBuilder().setResponseType(Response.ResponseType.WON).setPhrase(game.getPhrase())
                                        .setMessage("You won!").build();
                                leaderboard.get(name).setWins(leaderboard.get(name).getWins() + 1);
                            } else {
                                response = Response.newBuilder().setResponseType(Response.ResponseType.TASK).setPhrase(game.getPhrase())
                                        .setTask(game.getTask()).build();
                            }
                            break;
                        case QUIT:
                            response = Response.newBuilder().setResponseType(Response.ResponseType.BYE) .build();
                            run = false;
                            break;
                        default:    //Error
                    }
                // write response to client
                response.writeDelimitedTo(out);

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        try{
            if (out != null)  out.close();
            if (in != null)   in.close();
            if (clientSocket != null) clientSocket.close();
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println("Error closing connection to client");
        }
    }

    private Response nameRequest(Request op) throws IOException {
        String name = op.getName(); // get name from proto
        Response response; // create new response

        if (leaderboard.containsKey(name.toLowerCase())){
            leaderboard.get(name.toLowerCase()).setLogins(leaderboard.get(name.toLowerCase()).getLogins() + 1);
        } else {
            leaderboard.put(name.toLowerCase(), new Player(name, 0, 1));
        }
        // writing a connect message to the log with name and CONNECT
        writeToLog(name, Message.CONNECT);
        System.out.println("Got a connection and a name: " + name);

        // sett fields for response
        return response = Response.newBuilder()
            .setResponseType(Response.ResponseType.WELCOME)
            .setHello("Hello " + name + " and welcome. \n")
            .build();
    }

    /**
     * Writing a new entry to our log
     * @param name - Name of the person logging in
     * @param message - type Message from Protobuf which is the message to be written in the log (e.g. Connect) 
     * @return String of the new hidden image
     */
    public static void writeToLog(String name, Message message){
        try {
            // read old log file 
            Logs.Builder logs = readLogFile();

            // get current time and data
            Date date = java.util.Calendar.getInstance().getTime();

            // we are writing a new log entry to our log
            // add a new log entry to the log list of the Protobuf object
            logs.addLog(date.toString() + ": " +  name + " - " + message);

            // open log file
            FileOutputStream output = new FileOutputStream(logFilename);
            Logs logsObj = logs.build();

            // This is only to show how you can iterate through a Logs object which is a protobuf object
            // which has a repeated field "log"
            for (String log: logsObj.getLogList()){
                System.out.println(log);
            }

            // write to log file
            logsObj.writeTo(output);
        }catch(Exception e){
            System.out.println("Issue while trying to save");
        }
    }

    /**
     * Reading the current log file
     * @return Logs.Builder a builder of a logs entry from protobuf
     */
    public static Logs.Builder readLogFile() throws Exception{
        Logs.Builder logs = Logs.newBuilder();

        try {
            // just read the file and put what is in it into the logs object
            return logs.mergeFrom(new FileInputStream(logFilename));
        } catch (FileNotFoundException e) {
            System.out.println(logFilename + ": File not found.  Creating a new file.");
            return logs;
        }
    }

    public static void main (String args[]) throws Exception {
        Game game = new Game();
        Map<String, Player> leader = Collections.synchronizedMap(new HashMap<>());

        if (args.length != 2) {
            System.out.println("Expected arguments: <port(int)> <delay(int)>");
            System.exit(1);
        }
        int port = 9099; // default port
        Socket clientSocket = null;
        ServerSocket socket = null;

        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException nfe) {
            System.exit(2);
        }
        try {
            socket = new ServerSocket(port);
            System.out.println("Server Started...");
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(2);
        }

        while (true) {
            try{
                System.out.println("Accepting a Client...");
                clientSocket = socket.accept();
                runThread(clientSocket, game, leader);
            }
            catch(Exception e){
                System.out.println("Server encountered an error while connecting to a client.");
            }
            
        }

    }

    private static void runThread(Socket clientSocket, Game game, Map<String, Player> leader) {
        new Thread(() -> {
            SockBaseServer server = new SockBaseServer(clientSocket, game, leader);
            server.handleRequests();
        }).start();
    }

}

