/**
 * Representa uma chave para a Cache de Agreações
 */
public class CacheKey {

    // variáveis de classe

    /** tipo de agregação */
    private final QueryType tipo;
    /** nome do produto */
    private final String produto;
    /** número de dias */
    private final int dias;


    // construtores

    /**
     * Construtor de uma chave para a Cache de Agregações
     * @param tipo tipo de agregação
     * @param prod nome do produto
     * @param dias número de dias
     */
    public CacheKey(QueryType tipo, String prod, int dias) {
        this.tipo = tipo;
        this.produto = prod;
        this.dias = dias;
    }


    // métodos de instância

    /**
     * Compara um objeto a uma chave de agregações
     * @param obj objeto a comparar
     * @return {@code true} se forem iguais, {@code false} caso contrário
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;

        CacheKey other = (CacheKey) obj;
        return this.tipo == other.tipo && this.produto.equals(other.produto) && this.dias == other.dias;
    }

    /**
     * Determina um código hash correspondente à chave de agregações
     * @return código hash
     */
    @Override
    public int hashCode() {
        int hash = 31;

        hash += this.tipo.hashCode();
        hash += this.produto.hashCode();
        hash += this.dias;

        return hash;
    }

    /**
     * Devolve uma representação textual de chave de agregações
     * @return representação textual
     */
    public String toString() {
        return "{tipo=" + tipo + ", produto=" + produto + ", dias=" + dias + "}";
    }

}