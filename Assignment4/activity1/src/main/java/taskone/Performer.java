/**
 * File: Performer.java
 * Author: Student in Fall 2020B
 * Description: Performer class in package taskone.
 */

package taskone;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

/**
 * Class: Performer 
 * Description: Threaded Performer for server tasks.
 */
class Performer implements Runnable{

    private StringList state;
    private Socket conn;

    public Performer(Socket sock, StringList strings) {
        this.conn = sock;
        this.state = strings;
    }

    public JSONObject add(String str) {
        JSONObject json = new JSONObject();
        json.put("ok", true);
        json.put("type", "add");
        state.add(str);
        json.put("data", state.toString());
        return json;
    }

    public JSONObject clear() {
        JSONObject json = new JSONObject();
        json.put("ok", true);
        json.put("type", "clear");
        state.clear();
        json.put("data", state.toString());
        return json;
    }

    public JSONObject display() {
        JSONObject json = new JSONObject();
        json.put("ok", true);
        json.put("type", "display");
        json.put("data", state.toString());
        return json;
    }

    public JSONObject delete(int idx) {
        JSONObject json = new JSONObject();
        json.put("ok", true);
        json.put("type", "delete");
        String delete = state.delete(idx);
        if (state.size() == 0) {
            json.put("data", "no data");
            json.put("index", idx);
            json.put("flag", 2);
        } else if (idx > state.size() || idx < 0) {
            json.put("data", "out of bounds");
            json.put("index", idx);
            json.put("flag", 0);
        } else {
            json.put("data", delete);
            json.put("index", idx);
            json.put("flag", 1);
        }
        return json;
    }

    public JSONObject find(String str) {
        JSONObject json = new JSONObject();
        json.put("ok", true);
        json.put("type", "find");
        json.put("data", str);
        json.put("index", state.indexOf(str) + 1);
        if (state.indexOf(str) == -1) {
            json.put("data", str);
            json.put("index", -1);
        }
        return json;
    }

    public JSONObject quit() {
        JSONObject json = new JSONObject();
        json.put("ok", true);
        json.put("data", "leaving now");
        json.put("type", "quit");
        return json;
    }

    public JSONObject prepend(int idx, String str) {
        JSONObject json = new JSONObject();
        json.put("ok", true);
        boolean valid = state.prepend(str, idx);
        if (!valid) {
            json.put("data", "Index not with in bounds");
            json.put("flag", false);
            json.put("index", idx);
            json.put("type", "prepend");
        } else {
            json.put("data", state.toString());
            json.put("flag", true);
            json.put("type", "prepend");
        }
        return json;
    }

    public static JSONObject error(String err) {
        JSONObject json = new JSONObject();
        json.put("ok", false);
        json.put("message", err);
        return json;
    }

    public void doPerform() {
        boolean quit = false;
        OutputStream out = null;
        InputStream in = null;
        try {
            out = conn.getOutputStream();
            in = conn.getInputStream();
            System.out.println("Server connected to client:");
            while (!quit) {
                byte[] messageBytes = NetworkUtils.receive(in);
                JSONObject message = JsonUtils.fromByteArray(messageBytes);
                JSONObject returnMessage = new JSONObject();

                int choice = message.getInt("selected");
                switch (choice) {
                    case (1):
                        //adding
                        String inStr = (String) message.get("data");
                        returnMessage = add(inStr);
                        break;
                    case (2):
                        //clear
                        returnMessage = clear();
                        break;
                    case (3):
                        //find
                        inStr = (String) message.get("data");
                        returnMessage = find(inStr);
                        break;
                    case (4):
                        //display
                        returnMessage = display();
                        break;
                    case (5):
                        //delete
                        returnMessage = delete(message.getInt("IndexDeletion"));
                        break;
                    case (6):
                        //prepend
                        returnMessage = prepend(message.getInt("index"), message.getString("data"));
                        break;
                    case (0):
                        //quit
                        returnMessage = quit();
                        break;
                    default:
                        returnMessage = error("Invalid selection: " + choice
                                + " is not an option");
                        break;
                }
                // we are converting the JSON object we have to a byte[]
                byte[] output = JsonUtils.toByteArray(returnMessage);
                NetworkUtils.send(out, output);
            }
            // close the resource
            System.out.println("close the resources of client ");
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        boolean quit = false;
        OutputStream out = null;
        InputStream in = null;
        try {
            out = conn.getOutputStream();
            in = conn.getInputStream();
            System.out.println("Server connected to client:");
            while (!quit) {
                byte[] messageBytes = NetworkUtils.receive(in);
                JSONObject message = JsonUtils.fromByteArray(messageBytes);
                JSONObject returnMessage = new JSONObject();

                int choice = message.getInt("selected");
                switch (choice) {
                    case (1):
                        //adding
                        String inStr = (String) message.get("data");
                        returnMessage = add(inStr);
                        break;
                    case (2):
                        //clear
                        returnMessage = clear();
                        break;
                    case (3):
                        //find
                        inStr = (String) message.get("data");
                        returnMessage = find(inStr);
                        break;
                    case (4):
                        //display
                        returnMessage = display();
                        break;
                    case (5):
                        //delete
                        returnMessage = delete(message.getInt("IndexDeletion"));
                        break;
                    case (6):
                        //prepend
                        returnMessage = prepend(message.getInt("index"), message.getString("data"));
                        break;
                    case (0):
                        //quit
                        returnMessage = quit();
                        break;
                    default:
                        returnMessage = error("Invalid selection: " + choice
                                + " is not an option");
                        break;
                }
                // we are converting the JSON object we have to a byte[]
                byte[] output = JsonUtils.toByteArray(returnMessage);
                NetworkUtils.send(out, output);
            }
            // close the resource
            System.out.println("close the resources of client ");
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
