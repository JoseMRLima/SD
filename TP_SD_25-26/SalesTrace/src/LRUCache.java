import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Cache de Agreações que implementa um algoritmo LRU (Least Recently Used), Thread Safe.
 */
public class LRUCache {

    // variáveis de instância

    /** capacidade da cache */
    private final int capacity;
    private final Map<CacheKey, Node> map;
    /** least recently used */
    private Node head;
    /** most recently used */
    private Node tail;
    private final Lock lock;

    /**
     * Classe interna que representa um nodo de uma lista duplamente ligada.
     */
    private static class Node {
        private CacheKey key;
        private Number value;
        private Node prev, next;

        public Node(CacheKey k, Number v) {
            key = k;
            value = v;
        }
    }


    // construtores

    /**
     * Cria uma nova Cache LRU com a dada capacidade
     * @param capacity capacidade máxima
     */
    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.map = new HashMap<>();

        this.head = new Node(null, null);
        this.tail = new Node(null, null);
        this.head.next = tail;
        this.tail.prev = head;
        this.lock = new ReentrantLock();
    }


    // métodos de instância

    /**
     * Devolve o valor associado à chave dada.
     * Se a chave existir, o valor é colocado na posição "most recently used".
     *
     * @param key chave associada a um valor
     * @return valor associado à chave, ou {@code null} se não for encontrado
     */
    public Number get(CacheKey key) {
        this.lock.lock();
        try {
            Node node = this.map.get(key);
            if (node == null) return null;

            // colocar no fim da lista (último acesso)
            remove(node);
            addLast(node);

            System.out.println("[CACHE] hit {key=" + key + ", value=" + node.value + "]");

            return node.value;
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Adiciona uma nova entrada à cache.
     * Se a chave existir, o valor é atualizado.
     * Se a chave é nova e a cacbe está cheia,
     * o elemento "least recently used" é removido.
     * @param key chave a ser inserida
     * @param value valor associado à chave
     */
    public void put(CacheKey key, Number value) {
        this.lock.lock();
        try {
            Node existing = map.get(key);

            if (existing != null) {
                // atualizar valor e colocar no fim
                existing.value = value;
                remove(existing);
                addLast(existing);
                return;
            }

            // se cache está cheia, remover least recently used
            if (map.size() == capacity) {
                Node lru = head.next;
                remove(lru);
                map.remove(lru.key);
            }

            Node newNode = new Node(key, value);
            addLast(newNode);
            map.put(key, newNode);
        } finally {
            this.lock.unlock();
        }
    }

    // remove node da lista
    private void remove(Node n) {
        n.prev.next = n.next;
        n.next.prev = n.prev;
    }

    // adicionar node ao fim da lista
    private void addLast(Node n) {
        n.prev = tail.prev;
        n.next = tail;
        tail.prev.next = n;
        tail.prev = n;
    }

    public void clear() {
        this.lock.lock();
        try {
            this.map.clear();
            this.head = new Node(null, null);
            this.tail = new Node(null, null);
            this.head.next = tail;
            this.tail.prev = head;
        } finally {
            this.lock.unlock();
        }
    }

}
