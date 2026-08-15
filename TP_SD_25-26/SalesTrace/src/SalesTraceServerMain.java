import java.net.ServerSocket;
import java.net.Socket;

public class SalesTraceServerMain {

    public static void main(String[] args) throws Exception {

        if (args.length != 4) {
            usage();
            System.exit(1);
        }

        int D = Integer.parseInt(args[0]);
        int S = Integer.parseInt(args[1]);
        int C = Integer.parseInt(args[2]);
        String storage = args[3];

        ISalesTraceServer server = new SalesTraceServer(D, S, C, storage);

        int port = 12345;
        ServerSocket ss = new ServerSocket(port);

        System.out.printf("[SERVER] server is online {D=%d, S=%d, C=%d}\n", D, S, C);

        while (true) {
            Socket s = ss.accept();

            System.out.println("[SERVER] new connection!");
            ClientHandler handler = new ClientHandler(s, server);
            new Thread(handler).start();
        }

    }

    private static void usage() {
        System.err.println("Usage:");
        System.err.println("  java SalesTrace <D> <S> <C> <P> <I>");
        System.err.println();
        System.err.println("Arguments:");
        System.err.println("  D : number days passed");
        System.err.println("  S : maximum number of days stored in memory");
        System.err.println("  C : cache size");
        System.err.println("  P : path to the folder with the events");
    }

}
