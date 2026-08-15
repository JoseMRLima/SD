
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.*;

/**
 * Implementação de um Demultiplexer para comunicação cliente-servidor.
 * Cada tag possui uma fila independente de mensagens.
 */
public class Demultiplexer implements AutoCloseable {

    // variáveis de instância

    /** conexão com etiqueta */
    private final TaggedConnection conn;
    private final Map<Integer, Entry> map = new HashMap<>();

    private final Lock lock = new ReentrantLock();
    /** indica se o Demultiplexer está fechado */
    private boolean closed = false;

    private static class Entry {
        private final Condition cond;
        private final Deque<byte[]> queue = new ArrayDeque<>();

        public Entry(Condition c) {
            this.cond = c;
        }
    }


    // construtores

    /**
     * Construtor de um Demultiplexer
     * @param conn conexão com etiquetas
     */
    public Demultiplexer(TaggedConnection conn) {
        this.conn = conn;
    }


    // métodos de instância

    /**
     * Inicia o Demultiplexer
     */
    public void start() {
        Thread reader = new Thread(() -> {
            try {
                while (true) {
                    TaggedConnection.Frame frame = conn.receive();

                    lock.lock();
                    try {
                        Entry e = map.computeIfAbsent(
                                frame.tag,
                                t -> new Entry(lock.newCondition())
                        );
                        e.queue.add(frame.data);
                        // acordar quem espera esta tag
                        e.cond.signal();
                    } finally {
                        lock.unlock();
                    }
                }
            } catch (IOException e) {
                // se falhar a leitura, fecha tudo
                try { close(); } catch (IOException ignored) {}
            }
        });

        reader.setDaemon(true);
        reader.start();
    }

    /**
     * Envia uma mensagem com etiqueta
     * @param frame mensagem com etiqueta
     * @throws IOException
     */
    public void send(TaggedConnection.Frame frame) throws IOException {
        conn.send(frame);
    }

    /**
     * Envia uma mensagem, especificando uma etiqueta
     * @param tag etiqueta
     * @param data mensagem em bytes
     * @throws IOException
     */
    public void send(int tag, byte[] data) throws IOException {
        conn.send(tag, data);
    }

    /**
     * Recebe uma mensagem, dada uma etiqueta
     * @param tag etiqueta da mensagem
     * @return mensagem em bytes
     * @throws IOException
     * @throws InterruptedException
     */
    public byte[] receive(int tag) throws IOException, InterruptedException {
        lock.lock();
        try {
            Entry e = map.computeIfAbsent(tag, t -> new Entry(lock.newCondition()));

            while (e.queue.isEmpty() && !closed) {
                // esperar pela tag
                e.cond.await();
            }

            if (closed)
                throw new IOException("Demultiplexer closed");

            return e.queue.poll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Fecha um Demultiplexer
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        lock.lock();
        try {
            closed = true;
            // acordar todas as threads que esperavam
            for (Entry e : map.values()) {
                e.cond.signalAll();
            }
        } finally {
            lock.unlock();
        }

        conn.close();
    }

}
