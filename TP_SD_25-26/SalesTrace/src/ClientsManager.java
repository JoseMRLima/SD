import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Gestor da informação de clientes
 */
public class ClientsManager {

    // variáveis de instância

    private final Map<String, ClientInfo> users;
    private final Lock userLock;


    // construtores

    /**
     * Construtor do gestor de clientes
     */
    public ClientsManager() {
        this.users = new HashMap<>();
        this.userLock = new ReentrantLock();
    }


    // métodos de instância

    /**
     * Indica se um cliente é administrador
     * @param username nome do cliente
     * @return {@code true} se o cliente for administrador, {@code false} caso contrário
     */
    public boolean userIsAdmin(String username) {
        this.userLock.lock();
        try {
            ClientInfo user = this.users.get(username);
            return user != null && user.isAdmin();
        } finally {
            this.userLock.unlock();
        }
    }

    /**
     * Adiciona um cliente ao gestor
     * @param username nome do cliente
     * @param password palavra passe do cliente
     * @param admin indica se o cliente é administrador
     * @return {@code true} se o registo for bem sucedido, {@code false} caso contrário
     */
    public boolean addUser(String username, String password, boolean admin) {
        this.userLock.lock();
        try {
            if (this.users.containsKey(username)) {
                return false;
            }

            ClientInfo user = new ClientInfo(username, password, admin);
            this.users.put(username, user);
            return true;
        } finally {
            this.userLock.unlock();
        }
    }

    /**
     * Verifica se as credenciais do cliente estão corretas
     * @param username nome do cliente
     * @param password palavra passe do cliente
     * @return {@code true} se a autenticação for bem sucedida, {@code false} caso contrário
     */
    public boolean autenticarUser(String username, String password) {
        this.userLock.lock();
        try {
            ClientInfo user = this.users.get(username);
            return user != null && user.getPassword().equals(password);
        } finally {
            this.userLock.unlock();
        }
    }

    /**
     * Mostra os clientes registados
     * para Debugging!!!
     */
    private void showUsers() {
        this.userLock.lock();
        try {
            for (ClientInfo u : this.users.values()) {
                System.out.println(u.toString());
            }
        } finally {
            this.userLock.unlock();
        }
    }

}
