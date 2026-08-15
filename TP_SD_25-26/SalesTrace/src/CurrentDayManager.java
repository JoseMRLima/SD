import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Gestor dos eventos do dia atual
 */
public class CurrentDayManager {

    // variáveis de classe

    private static final String OUTPUT_DIR = "data";

    // variáveis de instância

    private final List<Event> eventos;
    private final Lock eventosLock;
    private final Condition eventosCondition;
    private int D;


    // construtores

    /**
     * Construtor de CurrentDayManager
     * @param D número de dias passados
     */
    public CurrentDayManager(int D) {
        this.eventos = new ArrayList<>();
        this.eventosLock = new ReentrantLock();
        this.eventosCondition = this.eventosLock.newCondition();
        this.D = D;
    }


    // métodos de instância

    /**
     * Adiciona um evento ao dia atual
     * @param e evento a adicionar
     */
    public void adicionarEvento(Event e) {
        this.eventosLock.lock();
        try {

            this.eventos.addLast(e);

            // acordar threads que esperam por eventos
            this.eventosCondition.signalAll();

        } finally {
            this.eventosLock.unlock();
        }
    }

    /**
     * Notifica a ocorrência de n vendas consecutivas de qualquer produto
     * @param n número de vendas consecutivas
     * @return nome do produto (null se falhar)
     */
    public String notificarVendasConsecutivas(int n) {
        this.eventosLock.lock();
        try {

            int currentD = this.D;
            String ultimoProduto = null;
            int contadorConsecutivo = 0;
            int indiceAtual = 0;

            // enquanto não mudar de dia
            while (currentD == D) {

                // percorrer eventos desde o último indice
                while (indiceAtual < this.eventos.size()) {
                    Event e = this.eventos.get(indiceAtual);

                    if (e.getProduct().equals(ultimoProduto)) {
                        contadorConsecutivo++;
                    } else {
                        ultimoProduto = e.getProduct();
                        contadorConsecutivo = 1;
                    }

                    if (contadorConsecutivo == n) {
                        return ultimoProduto;
                    }

                    indiceAtual++;
                }

                this.eventosCondition.await();
            }

            return null;

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            this.eventosLock.unlock();
        }
    }

    /**
     * Notificar a ocorrência de vendas simultâneas de dois produtos
     * @param p1 produto 1
     * @param p2 produto 2
     * @return {@code true} se os produtos foram vendidos, {@code false} se o dia mudou
     */
    public boolean notificarVendasSimultaneas(String p1, String p2) {
        this.eventosLock.lock();
        try {

            int currentD = this.D;
            boolean p1Found = false;
            boolean p2Found = false;
            int index = 0;

            // enquanto o dia não mudar
            while (currentD == this.D) {
                int size = this.eventos.size();

                // procurar os produtos desde o último indice
                for (; index < size && (!p1Found || !p2Found); index++) {
                    Event e = this.eventos.get(index);
                    if (e.getProduct().equals(p1)) {
                        p1Found = true;
                    } else if (e.getProduct().equals(p2)) {
                        p2Found = true;
                    }
                }

                if (!p1Found || !p2Found) {
                    // esperar que chegue um evento
                    this.eventosCondition.await();
                } else
                    break;
            }

            return p1Found && p2Found;

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            this.eventosLock.unlock();
        }
    }

    /**
     * Efetua a mudança de dia, simulando a passagem de tempo
     */
    public void mudarDia() {
        this.eventosLock.lock();
        try {

            String filename = String.format("day_%04d.bin", this.D + 1);
            Path outputDir = Paths.get(OUTPUT_DIR);
            Path filePath = outputDir.resolve(filename);

            // guardar os eventos do dia atual em disco
            try (DataOutputStream dos = new DataOutputStream(Files.newOutputStream(filePath))) {

                // escrever o número de eventos no ficheiro
                dos.writeInt(this.eventos.size());

                for (Event e : this.eventos) {
                    dos.writeUTF(e.getProduct());
                    dos.writeInt(e.getStock());
                    dos.writeFloat(e.getPrice());
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            this.D += 1;
            this.eventos.clear();

            // sinalizar as threads que mudou de dia
            this.eventosCondition.signalAll();
        } finally {
            this.eventosLock.unlock();
        }
    }

}
