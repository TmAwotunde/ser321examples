/**
  File: Server.java
  Author: Student in Fall 2020B
  Description: Server class in package taskone.
*/

package taskone;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Class: Server
 * Description: Server tasks.
 */
class ThreadPoolServer{

    public static void main(String[] args) throws Exception {
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
            Socket sock = server.accept();
            Executor pool = Executors.newFixedThreadPool(3);
            for (int i=0; i < 3; i++) {
                System.out.println("Accepting a Request...");
                pool.execute(new Performer(sock, strings));
            }
        }
    }
}
