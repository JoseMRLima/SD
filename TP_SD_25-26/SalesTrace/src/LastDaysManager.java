import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Gestor das séries temporais dos dias passados. Controla as threads que consumem
 * as séries temporais, não permitindo que estejam mais de S séries em memória.
 * Uma série temporal pode estar a ser processado por várias threads, e quando
 * houver uma thread que não esteja a ser processada, é removida da memória, dando
 * lugar a outras.
 */
public class LastDaysManager {

    // variáveis de instância

    /** número máximo de séries em memória */
    private final int S;
    /** número de dias passados */
    private int D;
    private final Lock lastDaysLock;
    private final Map<Integer, Entry<Integer, List<Event>>> lastDays;
    /** número atual de séries em memória */
    private int countS;
    private final Condition seriesCondition;
    private final String storagePath;
    private boolean changingDay;
    private final Condition changingDayCondition;


    // construtores

    /**
     * Construtor do gestor dos dias passados
     * @param S número máximo de séries em memória
     * @param D número de dias anteriores
     * @param storage caminho para a pasta onde se situam as séries (em disco)
     */
    public LastDaysManager(int S, int D, String storage) {
        this.S = S;
        this.D = D;
        this.lastDays = new HashMap<>();
        this.lastDaysLock = new ReentrantLock();
        this.countS = 0;
        this.seriesCondition = this.lastDaysLock.newCondition();
        this.storagePath = storage;
        this.changingDay = false;
        this.changingDayCondition = this.lastDaysLock.newCondition();
    }


    // métodos de instância

    /**
     * Devolve a série do dia d (1 .. D)
     * @param d dia da série
     * @return Lista de Eventos
     */
    public List<Event> requestEvents(int d) {
        this.lastDaysLock.lock();
        try {

            // esperar até terminar de mudar de dia
            while (this.changingDay) {
                this.changingDayCondition.await();
            }

            int nrDay = this.D - d + 1;
            // verifica se série está em memória
            Entry<Integer, List<Event>> day = this.lastDays.get(nrDay);

            if (day == null) {
                // série não está em memória

                // esperar que haja um lugar em memória para a série
                while (this.countS == S) {
                    this.seriesCondition.await();
                }

                List<Event> out = this.getDayEvents(nrDay);

                // adicionar série ao Map das séries em memória
                this.lastDays.put(nrDay, new SimpleEntry<>(1, out));
                this.countS++;

                return out;

            } else {
                // série está em memória
                List<Event> events = day.getValue();
                int users = day.getKey();

                // atualizar o número de threads a consumir esta série
                this.lastDays.put(nrDay, new SimpleEntry<>(users + 1, events));

                return events;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            this.lastDaysLock.unlock();
        }
    }

    /**
     * Indica que a thread já não está a consumir a série temporal do dia d
     * @param d dia da série temporal
     */
    public void releaseEvents(int d) {
        this.lastDaysLock.lock();
        try {

            int nrDay = this.D - d + 1;
            Entry<Integer, List<Event>> day = this.lastDays.get(nrDay);

            if (day != null) {
                int nrConsumers = day.getKey();
                if (nrConsumers == 1) {
                    // existe apenas um consumidor, logo a série pode ser removida da memória
                    this.lastDays.remove(nrDay);
                    this.countS--;

                    // sinalizar as threads que esperam por lugares em memória
                    this.seriesCondition.signalAll();
                } else {
                    // atualizar o contador de consumidores
                    this.lastDays.put(nrDay, new SimpleEntry<>(nrConsumers - 1, day.getValue()));
                }

            }

        } finally {
            this.lastDaysLock.unlock();
        }
    }

    /**
     * Efetua a mudança de dia
     */
    public void mudarDia() {
        this.lastDaysLock.lock();
        try {
            this.changingDay = true;

            // esperar até que não haja séries em memória
            while (this.countS != 0) {
                this.seriesCondition.await();
            }

            // remover todas as séries de memória
            this.lastDays.clear();

            this.D += 1;
            this.changingDay = false;
            this.changingDayCondition.signalAll();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            this.lastDaysLock.unlock();
        }

    }

    // Traz a série do dia desejado para memória
    private List<Event> getDayEvents(int day) {
        List<Event> out = new ArrayList<>();
        Path file = Path.of(this.storagePath, String.format("day_%04d.bin", day));
        try (DataInputStream dis = new DataInputStream(Files.newInputStream(file))) {

            // ler número de entradas
            int nrEntries = dis.readInt();

            for (int i = 0; i < nrEntries; i++) {
                String p = dis.readUTF();
                int q = dis.readInt();
                float c = dis.readFloat();

                out.add(new Event(p, q, c));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return out;
    }

}
