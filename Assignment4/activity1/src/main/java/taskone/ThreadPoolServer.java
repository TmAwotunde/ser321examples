/**
  File: ThreadedServer.java
  Author: Student in Fall 2020B
  Description: Threaded Server class in package taskone.
*/
package taskone;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Class: ThreadedServer
 * Description: Threaded Server tasks.
 */
public class ThreadPoolServer {

    public static void main(String[] args) throws Exception {

        args = new String[]{"9099", "2"};
        int port, connections;
        StringList strings = new StringList();

        if (args.length != 2) {
            // gradle runServer -Pport=9099 -Pconnections=2 -q --console=plain
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
        connections = 1;
        try {
            connections = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Connections] must be an integer");
            System.exit(2);
        }
        ServerSocket server = new ServerSocket(port);
        ExecutorService threadPool = Executors.newFixedThreadPool(connections);
        System.out.println("Server Started...");
        while (true) {
            System.out.println("Accepting a Request...");
            Socket sock = server.accept();

           Runnable runnable = new Runnable() {
               @Override
               public void run() {
                   Performer performer = new Performer(sock, strings);
                    performer.doPerform();
               }
           };
           threadPool.execute(runnable);

        }

    }
}
