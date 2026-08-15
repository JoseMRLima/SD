/**
 * Representa um Evento guardado na base de dados.
 * Classe Imutável.
 */
public class Event {

    /** nome do produto */
    private final String product;
    /** quantidade vendida */
    private final int stock;
    /** preço da venda */
    private final float price;

    /**
     * Construtor de um Evento
     * @param p nome do produto
     * @param s quantidade vendida
     * @param c preço da venda
     */
    public Event(String p, int s, float c) {
        this.product = p;
        this.stock = s;
        this.price = c;
    }

    /**
     * Devolve o nome do produto
     * @return nome do produto
     */
    public String getProduct() {
        return product;
    }

    /**
     * Devolve a quantidade vendida
     * @return quantidade vendida
     */
    public int getStock() {
        return stock;
    }

    /**
     * Devolve o preço da venda
     * @return preço da venda
     */
    public float getPrice() {
        return price;
    }

    /**
     * Devolve uma representação textual de um Evento
     * @return representação textual
     */
    public String toString() {
        return "Event {product=" + product + ", stock=" + stock + ", price=" + price + "}";
    }

}
