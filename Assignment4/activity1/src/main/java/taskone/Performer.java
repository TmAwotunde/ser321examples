/**
  File: Performer.java
  Author: Student in Fall 2020B
  Description: Performer class in package taskone.
*/

package taskone;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.json.JSONObject;

/**
 * Class: Performer 
 * Description: Performer for server tasks.
 */
class Performer {

    private StringList state;
    private Socket conn;

    public Performer(Socket sock, StringList strings) {
        this.conn = sock;
        this.state = strings;
    }

    public JSONObject add(JSONObject req) {
        System.out.println("In add");
        JSONObject resp = new JSONObject();
        resp.put("type", 1);
        boolean error = false;
        String completeDetails = "";
        String errorDetail = "";
        if (!req.has("data")){
            error = true;
            errorDetail = "required data missing";
            completeDetails = "the 'data' object is missing from request.";
        }
        if(!req.getJSONObject("data").has("string")) {
            error = true;
            errorDetail = "required data missing";
            completeDetails = "the 'string' object in 'data' is missing from request.";
        }
        if(error) {
            resp.put("ok", false);
            resp.put("type", 4);
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", errorDetail);
            errorResponse.put("details", completeDetails);
            resp.put("data", errorResponse);
            return resp;
        }

        String str = req.getJSONObject("data").getString("string");
        state.add(str);
        resp.put("ok", true);
        resp.put("type", 1);
        resp.put("data", state.toString());
        return resp;
    }

    public JSONObject switchStrings(JSONObject req) {
        System.out.println("In String Switching");
        JSONObject resp = new JSONObject();
        resp.put("type", 4);
        boolean error = false;
        String completeDetails = "";
        String errorDetail = "";
        if (!req.has("data")){
            error = true;
            errorDetail = "required data missing";
            completeDetails = "the 'data' object is missing from request.";
        } else if(!req.getJSONObject("data").has("index1")) {
            error = true;
            errorDetail = "required data missing";
            completeDetails = "the 'index1' is missing in 'data' object from request.";
        } else if (!req.getJSONObject("data").has("index2")) {
            error = true;
            errorDetail = "required data missing";
            completeDetails = "the 'index2' is missing in 'data' object from request.";
        }
        int index1 = req.getJSONObject("data").getInt("index1");
        int index2 = req.getJSONObject("data").getInt("index2");
        if (index1 < 0 || index1 >= state.size()) {
            error = true;
            errorDetail = "index out of bounds";
            completeDetails = "the 'index1' has the index " + index1 + ", but size is " + state.size();
        } else if (index2 < 0 || index2 >= state.size()) {
            error = true;
            errorDetail = "index out of bounds";
            completeDetails = "the 'index2' has the index " + index2 + ", but size is " + state.size();
        }
        if(error) {
            resp.put("ok", false);
            resp.put("type", 4);
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", errorDetail);
            errorResponse.put("details", completeDetails);
            resp.put("data", errorResponse);
            return resp;
        }
        String data1 = state.get(index1);
        state.set(index1, state.get(index2));
        state.set(index2, data1);
        resp.put("ok", true);
        resp.put("type", 4);
        resp.put("data", state.toString());
        return resp;
    }

    public static JSONObject unknown(int type) {
        JSONObject json = new JSONObject();
        json.put("type", type); // echo initial type
        json.put("ok", false);
        JSONObject data = new JSONObject();
        data.put("error", "unknown request");
        json.put("data", data);
        return json;
    }

    public static JSONObject quit() {
        JSONObject json = new JSONObject();
        json.put("type", 0); // echo initial type
        json.put("ok", true);
        json.put("data", "Bye");
        return json;
    }

    public JSONObject display() {
        JSONObject json = new JSONObject();
        json.put("type", 2); // echo initial type
        json.put("ok", true);
        json.put("data", state.toString());
        return json;
    }

    public JSONObject sort() {
        JSONObject json = new JSONObject();
        state.sort();
        json.put("type", 3); // echo initial type
        json.put("ok", true);
        json.put("data", state.toString());
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
                        case (4):
                            returnMessage = switchStrings(message);
                            break;
                        case (3):
                            returnMessage = sort();
                            break;
                        case (2):
                            returnMessage = display();
                            break;
                        case (1):
                            returnMessage = add(message);
                            break;
                        case (0):
                            returnMessage = quit();
                            quit = true;
                            break;
                        default:
                            returnMessage = unknown(choice);
                            break;
                    }
                    System.out.println(returnMessage);
                // we are converting the JSON object we have to a byte[]
                byte[] output = JsonUtils.toByteArray(returnMessage);
                NetworkUtils.send(out, output);
            }
            // close the resource
            System.out.println("close the resources of client ");
            out.close();
            in.close();
            conn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
