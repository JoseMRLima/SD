import exceptions.NumeroDiasInvalidoException;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ClientHandler implements Runnable {

    // variáveis de classe

    /** número máximo de tentativas para um cliente se autenticar/registar */
    private static final int MAX_TRIES = 5;
    /** threads por cliente */
    private static final int WORKERS_PER_CONNECTION = 5;


    // variáveis de instância

    /** nome do utilizador */
    private String username;
    /** password do utilizador */
    private String password;
    /** indica se o cliente é administrador */
    private boolean isAdmin;

    private final Socket socket;
    private final ISalesTraceServer server;


    // construtores

    public ClientHandler(Socket s, ISalesTraceServer server) {
        this.socket = s;
        this.server = server;

        this.username = "";
        this.password = "";
        this.isAdmin = false;
    }


    // métodos de instância

    @Override
    public void run() {

        try {
            TaggedConnection connection = new TaggedConnection(this.socket);

            boolean loggedIn = false;

            // fase de autenticação/registo
            /*
            IMPORTANTE: nesta fase não é preciso locks,
                        porque apenas uma thread está a trabalhar aqui
             */

            int nrTries = 0;
            while (nrTries < MAX_TRIES && !loggedIn) {

                TaggedConnection.Frame frame = connection.receive();

                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(frame.data));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);

                int requestType = dis.readByte();
                RequestType request = RequestType.fromValue(requestType);

                if (request == RequestType.SIGN_IN) {
                    // SIGN IN

                    // ler nome do utilizador
                    String user = dis.readUTF();
                    // ler password
                    String pass = dis.readUTF();
                    // indica se é admin ou não
                    boolean admin = dis.readBoolean();

                    // registar o utilizador
                    boolean signedIn = server.registarCliente(user, pass, admin);

                    if (signedIn) {
                        loggedIn = true;
                        this.username = user;
                        this.password = pass;
                        this.isAdmin = admin;
                    }

                    // enviar resposta ao cliente
                    dos.writeByte(RequestType.SIGN_IN.getValue());
                    dos.writeBoolean(signedIn);
                    dos.flush();
                    connection.send(frame.tag, baos.toByteArray());

                } else if (request == RequestType.LOG_IN) {
                    // LOG IN

                    // ler nome de utilizador
                    String username = dis.readUTF();
                    // ler password
                    String password = dis.readUTF();

                    boolean logged = server.autenticarCliente(username, password);

                    if (logged) {
                        loggedIn = true;
                        this.username = username;
                        this.password = password;
                        this.isAdmin = server.utilizadorAdmin(this.username);
                    }

                    // enviar resposta ao cliente
                    dos.writeByte(RequestType.LOG_IN.getValue());
                    dos.writeBoolean(logged);
                    dos.flush();
                    connection.send(frame.tag, baos.toByteArray());

                } else {
                    // outro tipo de pedido (inválido nesta altura)

                    // enviar resposta de erro ao cliente
                    dos.writeByte(RequestType.ERROR.getValue());
                    dos.flush();
                    connection.send(frame.tag, baos.toByteArray());

                }

                nrTries++;
            }

            if (!loggedIn) {
                connection.close();
                return;
            }

            System.out.println("[HANDLER] " + this.username + " is logged in");


            // fase de realização de operações

            Runnable worker = () -> {
                while (true) {

                    try {
                        TaggedConnection.Frame frame = connection.receive();

                        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(frame.data));
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        DataOutputStream dos = new DataOutputStream(baos);

                        int requestType = dis.readByte();
                        RequestType request = RequestType.fromValue(requestType);

                        System.out.println("[HANDLER] " + username + " requesting " + request.toString());

                        switch (request) {
                            case RequestType.ADD_EVENT:
                                // ADD EVENT

                                // ler produto
                                String p = dis.readUTF();
                                // ler quantidade
                                int stock = dis.readInt();
                                // ler preço da venda
                                float price = dis.readFloat();

                                server.adicionarEvento(new Event(p, stock, price));

                                // não precisa de enviar resposta

                                break;
                            case RequestType.STOCK:
                                // STOCK

                                // ler produto
                                String x = dis.readUTF();
                                // ler número de dias
                                int m = dis.readInt();

                                try {
                                     int value = server.getQuantidadeVendas(x, m);
                                    // enviar resposta
                                    dos.writeByte(RequestType.STOCK.getValue());
                                    dos.writeInt(value);
                                    dos.flush();
                                    connection.send(frame.tag, baos.toByteArray());
                                } catch (NumeroDiasInvalidoException e) {
                                    dos.writeByte(RequestType.ERROR.getValue());
                                    dos.flush();
                                    connection.send(frame.tag, baos.toByteArray());
                                }

                                break;
                            case RequestType.TOTAL:
                                // TOTAL

                                // ler produto
                                String j = dis.readUTF();
                                // ler número de dias
                                int h = dis.readInt();

                                try {
                                    float total = server.getVolumeVendas(j, h);
                                    // enviar resposta
                                    dos.writeByte(RequestType.TOTAL.getValue());
                                    dos.writeFloat(total);
                                    dos.flush();
                                    connection.send(frame.tag, baos.toByteArray());

                                } catch (NumeroDiasInvalidoException e) {
                                    dos.writeByte(RequestType.ERROR.getValue());
                                    dos.flush();
                                    connection.send(frame.tag, baos.toByteArray());
                                }

                                break;
                            case RequestType.AVERAGE:
                                // AVERAGE

                                // ler produto
                                String q = dis.readUTF();
                                // ler número de dias
                                int b = dis.readInt();

                                try {
                                    float average = server.getPrecoMedio(q, b);

                                    // enviar resposta
                                    dos.writeByte(RequestType.AVERAGE.getValue());
                                    dos.writeFloat(average);
                                    dos.flush();
                                    connection.send(frame.tag, baos.toByteArray());

                                } catch (NumeroDiasInvalidoException e) {
                                    dos.writeByte(RequestType.ERROR.getValue());
                                    dos.flush();
                                    connection.send(frame.tag, baos.toByteArray());
                                }

                                break;
                            case RequestType.MAXIMUM:
                                // MAXIMUM

                                // ler produto
                                String l = dis.readUTF();
                                // ler número de dias
                                int s = dis.readInt();

                                try {
                                    float maximum = server.getPrecoMaximo(l, s);

                                    // enviar resposta
                                    dos.writeByte(RequestType.MAXIMUM.getValue());
                                    dos.writeFloat(maximum);
                                    dos.flush();
                                    connection.send(frame.tag, baos.toByteArray());

                                } catch (NumeroDiasInvalidoException e) {
                                    dos.writeByte(RequestType.ERROR.getValue());
                                    dos.flush();
                                    connection.send(frame.tag, baos.toByteArray());
                                }

                                break;
                            case RequestType.FILTER:
                                // FILTER

                                // ler dia
                                int day = dis.readInt();
                                // ler número de elementos no set
                                int setLen = dis.readInt();
                                Set<String> prods = new HashSet<>();

                                // ler elementos do set
                                for (int i = 0; i < setLen; i++) {
                                    prods.add(dis.readUTF());
                                }

                                try {
                                    List<Event> events = server.filtrarEventos(prods, day);

                                    // enviar resposta
                                    dos.writeByte(RequestType.FILTER.getValue());
                                    this.efficientSerialize(events, dos);
                                    dos.flush();
                                    connection.send(frame.tag, baos.toByteArray());

                                } catch (NumeroDiasInvalidoException e) {
                                    dos.writeByte(RequestType.ERROR.getValue());
                                    dos.flush();
                                    connection.send(frame.tag, baos.toByteArray());
                                }

                                break;
                            case RequestType.SIMULTANEOUS:
                                // SIMULTANEOUS

                                // ler produto 1
                                String p1 = dis.readUTF();
                                // ler produto 2
                                String p2 = dis.readUTF();

                                Runnable notifierSim = () -> {
                                    try {
                                        boolean sim = server.notificarVendasSimultaneas(p1, p2);

                                        dos.writeByte(RequestType.SIMULTANEOUS.getValue());
                                        dos.writeBoolean(sim);
                                        dos.flush();
                                        connection.send(frame.tag, baos.toByteArray());
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                };

                                // criar thread para notificar cliente
                                new Thread(notifierSim).start();

                                break;
                            case RequestType.CONSECUTIVE:
                                // CONSECUTIVE

                                // ler número de dias
                                int n = dis.readInt();

                                Runnable notifierCon = () -> {
                                    try {
                                        String pr = server.notificarVendasConsecutivas(n);

                                        dos.writeByte(RequestType.CONSECUTIVE.getValue());
                                        // indica se existe produto ou não
                                        dos.writeBoolean(pr != null);
                                        if (pr != null) {
                                            dos.writeUTF(pr);
                                            dos.flush();
                                        }
                                        connection.send(frame.tag, baos.toByteArray());
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                };

                                // criar thread para notificar cliente
                                new Thread(notifierCon).start();

                                break;
                            case RequestType.NEXT_DAY:
                                // NEXT DAY

                                // não é preciso usar locks para ver se é admin
                                // porque nesta fase essa informação é constante

                                if (this.isAdmin) {
                                    server.mudarDia();
                                }

                                // enviar resposta ao cliente
                                dos.writeByte(RequestType.NEXT_DAY.getValue());
                                dos.writeBoolean(this.isAdmin);
                                dos.flush();
                                connection.send(frame.tag, baos.toByteArray());

                                break;
                            default:
                                break;
                        }

                    } catch (IOException ignored) {
                        System.out.println("[HANDLER] " + this.username + " connection closed");
                        try {
                            connection.close();
                            return;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                }
            };

            for (int i = 0; i < WORKERS_PER_CONNECTION; i++) {
                new Thread(worker).start();
            }

        } catch (IOException ignored) {
            System.out.println("[HANDLER] atempt of connection closed");
        }

    }

    private void efficientSerialize(List<Event> eventos, DataOutputStream dos) throws IOException {
        Map<String, List<Event>> produtosEvent = new HashMap<>();

        for (Event e : eventos) {
            String p = e.getProduct();
            produtosEvent.compute(p, (k,v) -> {
                if (v == null) v = new ArrayList<>();
                v.add(e);
                return v;
            });
        }

        // escrever número de produtos
        dos.writeInt(produtosEvent.size());

        for (Map.Entry<String, List<Event>> ent : produtosEvent.entrySet()) {
            String p = ent.getKey();
            // escrever nome do produto
            dos.writeUTF(p);
            List<Event> t = ent.getValue();
            // escrever número de vendas do produto
            dos.writeInt(t.size());
            for (Event e : t) {
                // escrever par (quantidade, preço)
                dos.writeInt(e.getStock());
                dos.writeFloat(e.getPrice());
            }
        }

    }

}
