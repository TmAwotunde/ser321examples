/**
  File: ThreadedServer.java
  Author: Student in Fall 2020B
  Description: Threaded Server class in package taskone.
*/
package taskone;

import java.net.ServerSocket;
import java.net.Socket;


/**
 * Class: ThreadedServer
 * Description: Threaded Server tasks.
 */
public class ThreadedServer {

    public static void main(String[] args) throws Exception {

        args = new String[]{"9099"};
        int port;
        StringList strings = new StringList();

        if (args.length != 1) {
            // gradle runServer -Pport=9099 -q --console=plain
            System.out.println("Usage: gradle runServer -Pport=9099 -q --console=plain");
            System.exit(1);
        }
        port = -1;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port] must be an integer");
            System.exit(2);
        }
        ServerSocket server = new ServerSocket(port);
        System.out.println("Server Started...");
        while (true) {
            System.out.println("Accepting a Request...");
            Socket sock = server.accept();

            Thread thread = new Thread(() -> {
                Performer performer = new Performer(sock, strings);
                performer.doPerform();
            });
            thread.start();

        }

    }
}
