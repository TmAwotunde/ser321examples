package client;

import java.net.*;
import java.io.*;

import proto.RequestProtos.*;
import proto.ResponseProtos.*;

class SockBaseClient {

    public static void main (String args[]) throws Exception {
        Socket serverSock = null;
        OutputStream out = null;
        InputStream in = null;
        int i1=0, i2=0;
        int port = 9099; // default port
        String clientName = "";

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
        System.out.println("Please provide your name for the server. :-)");
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        String strToSend = stdin.readLine();
        clientName = strToSend;

        // Build the first request object just including the name
        Request op = Request.newBuilder()
                .setOperationType(Request.OperationType.NAME)
                .setName(clientName)
                .build();
        Response response;
        try {
            // connect to the server
            serverSock = new Socket(host, port);
            out = serverSock.getOutputStream();
            in = serverSock.getInputStream();

            // write to server
            op.writeDelimitedTo(out);

            boolean run = true;
            while (run) {
                // read from the server
                response = Response.parseDelimitedFrom(in);
                System.out.println("TASK: " + response.getResponseType());
                boolean menu = true;
                // handling the response from server
                switch(response.getResponseType()){
                    case WELCOME:     // client in state menu
                        System.out.println(response.getHello());
                        break;
                    case WON:
                        System.out.println("Prompt: \n" + response.getPhrase());
                        System.out.println("Message: \n" + response.getMessage());
                        break;
                    case BYE:
                        menu = false;
                        run = false;
                        break;
                    case TASK:
                        System.out.println("Prompt: \n" + response.getPhrase());
                        System.out.println("Task: \n" + response.getTask());
                        System.out.println("Answer or type exit to exit");
                        strToSend = stdin.readLine();
                        if (strToSend.equals("exit")){
                            Request request = Request.newBuilder().setOperationType(Request.OperationType.QUIT).build();
                            menu = false;
                            request.writeDelimitedTo(out);
                            run = false;
                        } else{
                            Request request = Request.newBuilder().setOperationType(Request.OperationType.GUESS).setGuess(strToSend).build();
                            menu = false;
                            request.writeDelimitedTo(out);
                        }
                        break;
                    case ERROR:
                        System.out.println("There is some error: " + response.getMessage());
                        break;
                    case LEADERBOARD:
                        for (Leader leader: response.getLeaderboardList()) {
                            System.out.println(leader.getName() + "\t" + leader.getWins());
                        }
                        break;
                    default:
                        System.out.println("Default");
                }

                if (menu){
                    // show client options
                    System.out.println("What would you like to do? \n 1 - to see the leader board \n 2 - to enter a game \n 3 - to exit");
                    strToSend = stdin.readLine(); // read in what user wants to do
                    int option = Integer.parseInt(strToSend);
                    Request request = null;
                    switch (option){
                        case 1:
                            request = Request.newBuilder().setOperationType(Request.OperationType.LEADERBOARD).build();
                            break;
                        case 2:
                            request = Request.newBuilder().setOperationType(Request.OperationType.START).build();
                            break;
                        case 3:
                            request = Request.newBuilder().setOperationType(Request.OperationType.QUIT).build();
                            break;
                    }
                    request.writeDelimitedTo(out);
                }

            }

        } catch (SocketException se) {
            se.printStackTrace();
            System.out.println("Exiting...");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null)   in.close();
            if (out != null)  out.close();
            if (serverSock != null) serverSock.close();
        }
    }
}


