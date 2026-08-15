/**
 * Representa a informação de um Cliente
 */
public class ClientInfo {

    // variáveis de instância

    /** nome do cliente */
    private final String username;
    /** palavra passe do cliente */
    private final String password;
    /** indica se o cliente é administrador */
    private final boolean admin;


    // construtores

    /**
     * Construtor de ClientInfo
     * @param username nome do client
     * @param password palavra passe do client
     * @param admin indica se o cliente é administrador
     */
    public ClientInfo(String username, String password, boolean admin) {
        this.username = username;
        this.password = password;
        this.admin = admin;
    }


    // métodos de instância

    /**
     * Devolve o nome do cliente
     * @return nome do cliente
     */
    public String getUsername() {
        return username;
    }

    /**
     * Devolve a palavra passe do cliente
     * @return palavra passe do cliente
     */
    public String getPassword() {
        return password;
    }

    /**
     * Indica se o cliente é administrador
     * @return {@code true} se o cliente é administrador, {@code false} caso contrário
     */
    public boolean isAdmin() {
        return admin;
    }

    /**
     * Devolve uma representação textual da informação do cliente
     * @return representação textual
     */
    public String toString() {
        return "User {username=" + username + ", password=" + password + ", isAdmin=" + admin + "}";
    }

}
