
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.locks.*;

/**
 * Conexão com mensagens com etiquetas.
 */
public class TaggedConnection implements AutoCloseable {

    // variáveis de instância

    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final Lock receiveLock = new ReentrantLock();
    private final Lock sendLock = new ReentrantLock();


    // classes auxiliares

    public static class Frame {
        public final int tag;
        public final byte[] data;

        public Frame(int tag, byte[] data) {
            this.tag = tag;
            this.data = data;
        }
    }


    // construtores

    /**
     * Construtor de TaggedConnection
     * @param socket socket da conexão
     * @throws IOException
     */
    public TaggedConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
    }


    // métodos de instância

    /**
     * Enviar uma mensagem com etiqueta
     * @param frame mensagem com etiqueta
     * @throws IOException
     */
    public void send(Frame frame) throws IOException {
        send(frame.tag, frame.data);
    }

    /**
     * Enviar uma mensagem com etiqueta
     * @param tag etiqueta
     * @param data mensagem em bytes
     * @throws IOException
     */
    public void send(int tag, byte[] data) throws IOException {
        sendLock.lock();
        try{
            out.writeInt(tag);
            out.writeInt(data.length);
            out.write(data);
            out.flush();
        } finally{
            sendLock.unlock();
        }
    }

    /**
     * Recebe uma mensagem com etiqueta
     * @return mensagem com etiqueta
     * @throws IOException
     */
    public Frame receive() throws IOException {
        receiveLock.lock();
        try {
            int tag = in.readInt();
            int len = in.readInt();
            byte[] data = new byte[len];
            in.readFully(data);
            return new Frame(tag,data);
        } finally {
            receiveLock.unlock();
        }
    }

    /**
     * Fecha um TaggedConnection
     * @throws IOException
     */
    public void close() throws IOException {
        socket.close();
        // System.out.println("closing socket");
    }

}