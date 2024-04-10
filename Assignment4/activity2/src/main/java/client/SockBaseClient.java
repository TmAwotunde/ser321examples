package client;

import java.net.*;
import java.io.*;

import org.json.*;

import buffers.RequestProtos.Request;
import buffers.ResponseProtos.Response;
import buffers.ResponseProtos.Entry;


import java.util.*;
import java.util.stream.Collectors;

import static buffers.ResponseProtos.Response.ResponseType.*;


class SockBaseClient {
    private enum States {
        // no game started
        GameRunning,
        // game is at menu
        GameMenu,
        // game fully initialized

    }

    static boolean second = false;
    public static States gameState = States.GameMenu;
    public static Player player;


    public static void main(String[] args) throws Exception {
        Socket serverSock = null;
        OutputStream out = null;
        InputStream in = null;
        int port = 9099; // default port


        // Make sure two arguments are given
        if (args.length != 2) {
            System.out.println("Expected arguments: <host(String)> <port(int)>");
            System.exit(1);
        }
        String host = args[0];

        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port] must be integer");
            System.exit(2);
        }

        // Ask user for username
        System.out.println("Please provide your name for the server.");

        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        String strToSend = stdin.readLine();
        // Build the first request object just including the name

        Request op = Request.newBuilder()
                .setOperationType(Request.OperationType.NAME)
                .setName(strToSend).build();
        Response response = null;
        boolean flag = true;
        int flag1 = 0;

//            do {

        // connect to the server
        serverSock = new Socket(host, port);

        // write to the server
        out = serverSock.getOutputStream();

        in = serverSock.getInputStream();

        op.writeDelimitedTo(out); //write requests
        int tileRepeater = 0;
        String name = strToSend;
        String board = "";
        do {
            try {
                // read from the server
//                if (serverSock.isClosed()) {
//                    serverSock = new Socket(host, port);
//                }

                if (flag1 != 0) {
//                    BufferedReader stdin1 = new BufferedReader(new InputStreamReader(System.in));
//                    String strToSend1 = stdin1.readLine();
                    // connect to the server
//                    try {
                    if (gameState == States.GameMenu) {
                        System.out.println("* \nWhat would you like to do? \n 1 - to see the leader board \n 2 - to enter a game \n 3 - quit the game");
                        Scanner scanner = new Scanner(System.in);
                        int choice = scanner.nextInt(); //should wait here why is it not waiting
                        // write to the server
//                    out = serverSock.getOutputStream();
                        // user entered an integer
                        switch (choice) {
                            case (1):
//                            op = Request.newBuilder()
//                                    .setOperationType(Request.OperationType.LEADER).build();
//                            op.writeDelimitedTo(out);
                                Request op1 = Request.newBuilder()
                                        .setOperationType(Request.OperationType.LEADER)
                                        .setName(strToSend).build();
                                op1.writeDelimitedTo(out);
                                break;
                            case (2):
                                op = Request.newBuilder().setOperationType(Request.OperationType.NEW).build();
                                op.writeDelimitedTo(out);
                                System.out.println("Game start");
                                break;
                            case (3):
                                op = Request.newBuilder().setOperationType(Request.OperationType.QUIT).build();
                                op.writeDelimitedTo(out);
                                System.out.println("leaving game");
                                break;
                            default:
                                System.out.println("Not a valid choice try again");
                                break;
                        }
                    }

                }
                    if(gameState == States.GameRunning){
                        Scanner scanner = new Scanner(System.in);
                        String gameInput;
                        System.out.println("Enter game input <row1><column1>,<letter><number> - Example 'a1'\n");

                        do {
                            System.out.println(board);
                            gameInput = scanner.nextLine();
                            if(Objects.equals(gameInput, "")){
                                System.out.println("No input given try again \n");
                            }
                        }while (Objects.equals(gameInput, ""));

                        if(second == false){
                            op = Request.newBuilder()
                                    .setOperationType(Request.OperationType.TILE1)
                                    .setTile(gameInput)
                                    .setName(name)
                                    .build();
                            op.writeDelimitedTo(out);
                        }else if (second == true){
                            op = Request.newBuilder()
                                    .setOperationType(Request.OperationType.TILE2)
                                    .setTile(gameInput)
                                    .setName(name)
                                    .build();
                            op.writeDelimitedTo(out);
                        }
                        response = Response.parseDelimitedFrom(in);//wait response here?
                    }

                    if(gameState != States.GameRunning){
                        response = Response.parseDelimitedFrom(in);//wait response here?
                    }


                if (response == null || !response.hasResponseType()) {
                    System.out.println("response is empty or null");
                }

                switch (response.getResponseType()) {
                    case GREETING:
                        if (flag1 == 0) {
                            System.out.println(response.getMessage());
//                            System.out.println("* \nWhat would you like to do? \n 1 - to see the leader board \n 2 - to enter a game \n 3 - quit the game");
                            flag1++;
                        }
                        for(Entry lead: response.getLeaderList()){
                            if(lead.getName() == name){
                                player = new Player(lead.getName(), lead.getWins(), lead.getLogins());
                            }
                        }
//                        player = new Player();
                        op = null;
                        break;
                    case LEADER:
                        for (Entry lead : response.getLeaderList()) {
                            System.out.println("Name: \t" + lead.getName());
                            System.out.println("\t\tWins: " + lead.getWins());
                            System.out.println("\t\tLogin: " + lead.getLogins());
                        }
                        op = null;
                        break;
                    case PLAY:
//                        System.out.println(response.getBoard());
                        if(response.hasSecond()){
                            second = response.getSecond();
                        }
                        if(response.hasBoard()){
                            System.out.println("Current board: \n");
                            System.out.println(response.getBoard());
                            board = response.getBoard();
                        }
                        if(response.hasFlippedBoard()){
                            System.out.println("What you picked: \n");
                            System.out.println(response.getFlippedBoard());
                        }
                        if(response.hasMessage()){
                            System.out.println(response.getMessage());
                        }
                        gameState = States.GameRunning;
                        break;
                    case BYE:
                        System.out.println("Game exit\n");
                        System.out.println(response.getMessage());
                        if (in != null) in.close();
                        if (out != null) out.close();
                        serverSock.close();
                        flag = false;
                        System.exit(0);
                        break;
                    case WON:
                        System.out.println("HOOORAY YOU WON!!!");
                        if(response.hasBoard()){
                            System.out.println(response.getBoard());
                        }
                        gameState = States.GameMenu;
                        break;
                    case ERROR:
                        if(response.hasSecond()){
                            second = response.getSecond();
                        }
                        System.out.println(response.getMessage());
                    default:
                        System.out.println();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } while (flag);

    }
}


