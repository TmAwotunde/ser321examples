/**
  File: Client.java
  Author: Student in Fall 2020B
  Description: Client class in package taskone.
*/

package taskone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;
import org.json.JSONObject;

/**
 * Class: Client
 * Description: Client tasks.
 * Basic methods are given but you can change them and make adjustements as you see fit.
 */
public class Client {
    private static BufferedReader stdin;

    // The functions to build the requests do not have to include more error handling, you can assume
    // we will input the correct data
    /**
     * Function JSONObject for add() request.
     */
    public static JSONObject add() {
        String strToSend = null;
        JSONObject request = new JSONObject();
        request.put("selected", 1);
        try {
            System.out.print("Please input the string: ");
            strToSend = stdin.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject data = new JSONObject();
        data.put("string", strToSend);
        request.put("data", data);
        return request;
    }


    /**
     * Function JSONObject for display() request.
     */
    public static JSONObject display() {
        JSONObject request = new JSONObject();
        request.put("selected", 2);
        return request;
    }

    /**
     * Function JSONObject for sort request.
     */
    public static JSONObject sort() {
        JSONObject request = new JSONObject();
        request.put("selected", 3);
        return request;
    }

    /**
     * Function JSONObject for switch request
     */
    public static JSONObject switchStrings() {
        JSONObject request = new JSONObject();
        request.put("selected", 4);
        int indexInput1 = 0;
        int indexInput2 = 0;
        try {
            System.out.print("Please input the first index: ");
            indexInput1 = Integer.parseInt(stdin.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject data = new JSONObject();
        data.put("index1", indexInput1);

        try {
            System.out.print("Please input the second index: ");
            indexInput2 = Integer.parseInt(stdin.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        data.put("index2", indexInput2);
        request.put("data", data);
        return request;
    }

    /**
     * Function JSONObject quit().
     */
    public static JSONObject quit() {
        JSONObject request = new JSONObject();
        request.put("selected", 0);
        return request;
    }

    /**
     * Function main().
     */
    public static void main(String[] args) throws IOException {

        args = new String[]{"localhost", "9099"};

        String host;
        int port;
        Socket sock;
        stdin = new BufferedReader(new InputStreamReader(System.in));
        try {
            if (args.length != 2) {
                // gradle runClient -Phost=localhost -Pport=9099 -q --console=plain
                System.out.println("Usage: gradle runClient -Phost=localhost -Pport=9099");
                System.exit(0);
            }

            host = args[0];
            port = -1;
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException nfe) {
                System.out.println("[Port] must be an integer");
                System.exit(2);
            }

            sock = new Socket(host, port);
            OutputStream out = sock.getOutputStream();
            InputStream in = sock.getInputStream();
            Scanner input = new Scanner(System.in);
            int choice;
            do {
                System.out.println();
                // TODO: you will need to change the menu based on the tasks for this assignment, see Readme!
                System.out.println("Client Menu");
                System.out.println("Please select a valid option (1-4). 0 to disconnect the client");
                System.out.println("1. add: Adds the given String to the end of the list");
                System.out.println("2. display: Displays the current list");
                System.out.println("3. sort: Sorts the elements in the list");
                System.out.println("4. switch: Switches the two Strings at the given index");
                System.out.println("0. quit");
                System.out.println();
                choice = input.nextInt(); // do not have to error handle in case no int is given, we will input the correct thing
                JSONObject request = null;
                switch (choice) {
                    case (1):
                        request = add();
                        break;
                    case (2):
                        request = display();
                        break;
                    case (3):
                        request = sort();
                        break;
                    case (4):
                        request = switchStrings();
                        break;
                    case (0):
                        request = quit();
                        break;
                    default:
                        System.out.println("Please select a valid option (0-4).");
                        break;
                }

                if (request != null) {
                    System.out.println(request);
                    NetworkUtils.send(out, JsonUtils.toByteArray(request));
                    byte[] responseBytes = NetworkUtils.receive(in);
                    JSONObject response = JsonUtils.fromByteArray(responseBytes);

                    if (!response.getBoolean("ok")) {
                        System.out.println(response.getJSONObject("data"));
                    } else {
                        System.out.println();
                        System.out.println("The response from the server: ");
                        System.out.println("type: " + response.getInt("type"));
                        System.out.println("data: " + response.getString("data"));
                        int type = response.getInt("type");
                        if (type == 0) {
                            sock.close();
                            out.close();
                            in.close();
                            System.exit(0);
                        }
                    }
                }
            } while (true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}