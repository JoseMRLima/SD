import exceptions.NumeroDiasInvalidoException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Servidor de base de dados para séries temporais.
 * Deve ser usado num ambiente concorrente.
 */
public class SalesTraceServer implements ISalesTraceServer {

    // variáveis de instância

    private int D;
    /** gestor de clientes */
    private final ClientsManager users;
    /** gestor do dia atual */
    private final CurrentDayManager currentDay;
    /** cache de agregações */
    private final LRUCache cache;
    /** gestor dos dias passados */
    private final LastDaysManager lastDays;
    private final Lock readLock;
    private final Lock writeLock;


    // construtores

    /**
     * Construtor parametrizado de SalesTraceServerFacade
     * @param D número de dias (inicial)
     * @param S número máximo de séries em memória
     * @param C tamanho da cache
     */
    public SalesTraceServer(int D, int S, int C, String storage) {
        this.D = D;
        this.users = new ClientsManager();
        this.currentDay = new CurrentDayManager(D);
        this.lastDays = new LastDaysManager(S, D, storage);
        this.cache = new LRUCache(C);
        ReadWriteLock mudancaDiaLock = new ReentrantReadWriteLock();
        this.readLock = mudancaDiaLock.readLock();
        this.writeLock = mudancaDiaLock.writeLock();
    }


    // métodos de instância

    /**
     * Adiciona o registo de um cliente
     * @param username nome do cliente
     * @param password palavra passe do cliente
     * @param admin indica se o cliente é administrador
     * @return {@code true} se o registo for bem sucedido, {@code false} caso contrário
     */
    @Override
    public boolean registarCliente(String username, String password, boolean admin) {
        return this.users.addUser(username, password, admin);
    }

    /**
     * Autentica um cliente no servidor
     * @param username nome do cliente
     * @param password palavra passe do cliente
     * @return {@code true} se a autenticação for bem sucedida, {@code false} caso contrário
     */
    @Override
    public boolean autenticarCliente(String username, String password) {
        return this.users.autenticarUser(username, password);
    }

    /**
     * Adiciona um evento à série temporal do dia atual
     * @param e evento a adicionar
     */
    @Override
    public void adicionarEvento(Event e) {
        this.readLock.lock();
        try {
            this.currentDay.adicionarEvento(e);
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Devolve a quantidade de unidades de um produto p vendidas nos últimos d dias anteriores
     * @param p nome do produto
     * @param d número de dias
     * @return quantidade de vendas
     */
    @Override
    public int getQuantidadeVendas(String p, int d) throws NumeroDiasInvalidoException {
        this.readLock.lock();
        try {
            if (d > this.D) {
                throw new NumeroDiasInvalidoException("Número de dias inválido");
            }

            // procurar na cache
            CacheKey k = new CacheKey(QueryType.STOCK, p, d);
            Number value = this.cache.get(k);

            // valor está na cache
            if (value != null)
                return value.intValue();

            int total = 0;
            // calcular a agregação
            for (int i = 1; i <= d; i++) {
                List<Event> serie = this.lastDays.requestEvents(i);

                for (Event e : serie) {
                    if (e.getProduct().equals(p))
                        total += e.getStock();
                }

                this.lastDays.releaseEvents(i);
            }

            // adicionar agregação à cache
            this.cache.put(k, total);

            return total;
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Devolve o valor total das vendas de um produto p nos últimos d dias anteriores.
     * @param p nome do produto
     * @param d número de dias
     * @return volume de vendas
     */
    @Override
    public float getVolumeVendas(String p, int d) throws NumeroDiasInvalidoException {
        this.readLock.lock();
        try {
            if (d > this.D) {
                throw new NumeroDiasInvalidoException("Número de dias inválido");
            }

            // procurar na cache
            CacheKey k = new CacheKey(QueryType.TOTAL, p, d);
            Number value = this.cache.get(k);

            // valor está na cache
            if (value != null)
                return value.floatValue();

            float total = 0;
            // calcular a agregação
            for (int i = 1; i <= d; i++) {
                List<Event> serie = this.lastDays.requestEvents(i);

                for (Event e : serie) {
                    if (e.getProduct().equals(p))
                        total += e.getPrice();
                }

                this.lastDays.releaseEvents(i);
            }

            // adicionar agregação à cache
            this.cache.put(k, total);

            return total;
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Devolve o preço médio de venda de um produto p nos últimos d dias anteriores
     * @param p nome do produto
     * @param d número de dias
     * @return preço médio de vendas
     */
    @Override
    public float getPrecoMedio(String p, int d) throws NumeroDiasInvalidoException {
        this.readLock.lock();
        try {
            if (d > this.D) {
                throw new NumeroDiasInvalidoException("Número de dias inválido");
            }

            // procurar na cache
            CacheKey k = new CacheKey(QueryType.AVERAGE, p, d);
            Number value = this.cache.get(k);

            // valor está na cache
            if (value != null)
                return value.floatValue();

            float total = 0;
            int count = 0;
            // calcular a agregação
            for (int i = 1; i <= d; i++) {
                List<Event> serie = this.lastDays.requestEvents(i);

                for (Event e : serie) {
                    if (e.getProduct().equals(p)) {
                        total += e.getPrice();
                        count++;
                    }
                }

                this.lastDays.releaseEvents(i);
            }

            // adicionar agregação à cache
            this.cache.put(k, total / count);

            return total / count;
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Devolve o preço máximo de venda de um produto p nos últimos d dias anteriores
     * @param p nome do produto
     * @param d número de dias
     * @return preço máximo de venda
     */
    @Override
    public float getPrecoMaximo(String p, int d) throws NumeroDiasInvalidoException {
        this.readLock.lock();
        try {
            if (d > this.D) {
                throw new NumeroDiasInvalidoException("Número de dias inválido");
            }

            // procurar na cache
            CacheKey k = new CacheKey(QueryType.MAXIMUM, p, d);
            Number value = this.cache.get(k);

            // valor está na cache
            if (value != null)
                return value.floatValue();

            float max = 0;
            // calcular a agregação
            for (int i = 1; i <= d; i++) {
                List<Event> serie = this.lastDays.requestEvents(i);

                for (Event e : serie) {
                    if (e.getProduct().equals(p)) {
                        float temp = e.getPrice();
                        if (temp > max)
                            max = temp;
                    }
                }

                this.lastDays.releaseEvents(i);
            }

            // adicionar agregação à cache
            this.cache.put(k, max);

            return max;
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Devolve a lista de eventos relativa aos produtos pertencentes a um conjunto c, do dia anterior d
     * @param produtos conjunto de produtos
     * @param d dia desejado
     * @return lista de eventos
     */
    @Override
    public List<Event> filtrarEventos(Set<String> produtos, int d) throws NumeroDiasInvalidoException {
        this.readLock.lock();
        try {
            if (d > this.D) {
                throw new NumeroDiasInvalidoException("Número de dias inválido");
            }

            List<Event> serie = this.lastDays.requestEvents(d);

            List<Event> out = new ArrayList<>();
            for (Event e : serie) {
                String p = e.getProduct();
                if (produtos.contains(p))
                    out.add(e);
            }

            this.lastDays.releaseEvents(d);

            return out;
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Reporta a ocorrência de vendas simultâneas de dois produtos no dia atual
     * @param p1 produto 1
     * @param p2 produto 2
     * @return {@code true} se os produtos foram vendidos, {@code false} caso contrário (ou dia terminou)
     */
    @Override
    public boolean notificarVendasSimultaneas(String p1, String p2) {
        return this.currentDay.notificarVendasSimultaneas(p1, p2);
    }

    /**
     * Reporta a ocorrência de n vendas consecutivas de um produto no dia atual
     * @param n número de vendas
     * @return nome do produto, {@code null} caso o dia termine
     */
    @Override
    public String notificarVendasConsecutivas(int n) {
        return this.currentDay.notificarVendasConsecutivas(n);
    }

    /**
     * Começa um novo dia
     */
    @Override
    public void mudarDia() {
        this.writeLock.lock();
        try {
            // esta operação tem de ser atómica
            this.currentDay.mudarDia();
            this.cache.clear();
            this.lastDays.mudarDia();
            this.D += 1;
        } finally {
            this.writeLock.unlock();
        }
    }

    /**
     * Indica se o cliente é administrador
     * @param username nome do cliente
     * @return {@code true} se o cliente for administrador, {@code false} caso contrário
     */
    @Override
    public boolean utilizadorAdmin(String username) {
        return this.users.userIsAdmin(username);
    }

}
