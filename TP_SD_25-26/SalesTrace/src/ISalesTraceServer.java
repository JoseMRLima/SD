import exceptions.NumeroDiasInvalidoException;

import java.util.List;
import java.util.Set;

/**
 * Representa um servidor de base de dados para séries temporais
 */
public interface ISalesTraceServer {

    /**
     * Adiciona o registo de um cliente
     * @param username nome do cliente
     * @param password palavra passe do cliente
     * @param admin indica se o cliente é administrador
     * @return {@code true} se o registo for bem sucedido, {@code false} caso contrário
     */
    boolean registarCliente(String username, String password, boolean admin);

    /**
     * Autentica um cliente no servidor
     * @param username nome do cliente
     * @param password palavra passe do cliente
     * @return {@code true} se a autenticação for bem sucedida, {@code false} caso contrário
     */
    boolean autenticarCliente(String username, String password);

    /**
     * Adiciona um evento à série temporal do dia atual
     * @param e evento a adicionar
     */
    void adicionarEvento(Event e);

    /**
     * Devolve a quantidade de unidades de um produto p vendidas nos últimos d dias anteriores
     * @param p nome do produto
     * @param d número de dias
     * @return quantidade de vendas
     */
    int getQuantidadeVendas(String p, int d) throws NumeroDiasInvalidoException;

    /**
     * Devolve o valor total das vendas de um produto p nos últimos d dias anteriores.
     * @param p nome do produto
     * @param d número de dias
     * @return volume de vendas
     */
    float getVolumeVendas(String p, int d) throws NumeroDiasInvalidoException;

    /**
     * Devolve o preço médio de venda de um produto p nos últimos d dias anteriores
     * @param p nome do produto
     * @param d número de dias
     * @return preço médio de vendas
     */
    float getPrecoMedio(String p, int d) throws NumeroDiasInvalidoException;

    /**
     * Devolve o preço máximo de venda de um produto p nos últimos d dias anteriores
     * @param p nome do produto
     * @param d número de dias
     * @return preço máximo de venda
     */
    float getPrecoMaximo(String p, int d) throws NumeroDiasInvalidoException;

    /**
     * Devolve a lista de eventos relativa aos produtos pertencentes a um conjunto c, do dia anterior d
     * @param produtos conjunto de produtos
     * @param d dia desejado
     * @return lista de eventos
     */
    List<Event> filtrarEventos(Set<String> produtos, int d) throws NumeroDiasInvalidoException;

    /**
     * Reporta a ocorrência de vendas simultâneas de dois produtos no dia atual
     * @param p1 produto 1
     * @param p2 produto 2
     * @return {@code true} se os produtos foram vendidos, {@code false} caso contrário (ou dia terminou)
     */
    boolean notificarVendasSimultaneas(String p1, String p2);

    /**
     * Reporta a ocorrência de n vendas consecutivas de um produto no dia atual
     * @param n número de vendas
     * @return nome do produto, {@code null} caso o dia termine
     */
    String notificarVendasConsecutivas(int n);

    /**
     * Começa um novo dia
     */
    void mudarDia();

    /**
     * Indica se o cliente é administrador
     * @param username nome do cliente
     * @return {@code true} se o cliente for administrador, {@code false} caso contrário
     */
    boolean utilizadorAdmin(String username);

}
