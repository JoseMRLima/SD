
import exceptions.MensagemErroException;
import exceptions.TentativasExcedidasException;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SalesTraceClient implements ISalesTraceClient {

    // variáveis de instância

    private final Demultiplexer demultiplexer;
    private final Lock clientLock;
    private int count;

    // construtores

    public SalesTraceClient() {
        try {
            this.count = 0;
            this.clientLock = new ReentrantLock();
            Socket sock = new Socket("localhost", 12345);
            this.demultiplexer = new Demultiplexer(new TaggedConnection(sock));
            this.demultiplexer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // métodos de instância

    private int getNextTagNumber() {
        this.clientLock.lock();
        try {
            int tag = this.count;
            this.count++;
            return tag;
        } finally {
            this.clientLock.unlock();
        }
    }

    @Override
    public boolean signIn(String username, String password, boolean admin) throws TentativasExcedidasException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        int tag = this.getNextTagNumber();
        byte[] data;
        boolean out = false;

        try {
            dos.writeByte(RequestType.SIGN_IN.getValue());
            dos.writeUTF(username);
            dos.writeUTF(password);
            dos.writeBoolean(admin);
            dos.flush();

            data = baos.toByteArray();
            // enviar pedido de registo
            this.demultiplexer.send(tag, data);

            // receber resposta
            data = this.demultiplexer.receive(tag);

            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            int answer = dis.readByte();
            RequestType requestType = RequestType.fromValue(answer);

            if (requestType == RequestType.ERROR) {
                throw new TentativasExcedidasException("tentativas excedidas");
            } else if (requestType == RequestType.SIGN_IN) {
                out = dis.readBoolean();
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return out;
    }

    @Override
    public boolean logIn(String username, String password) throws TentativasExcedidasException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        int tag = this.getNextTagNumber();
        byte[] data;
        boolean out = false;

        try {
            dos.writeByte(RequestType.LOG_IN.getValue());
            dos.writeUTF(username);
            dos.writeUTF(password);
            dos.flush();

            data = baos.toByteArray();
            // enviar pedido de autenticacao
            this.demultiplexer.send(tag, data);

            // receber resposta
            data = this.demultiplexer.receive(tag);

            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            int answer = dis.readByte();
            RequestType requestType = RequestType.fromValue(answer);

            if (requestType == RequestType.ERROR) {
                throw new TentativasExcedidasException("tentativas excedidas");
            } else if (requestType == RequestType.LOG_IN) {
                out = dis.readBoolean();
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return out;
    }

    @Override
    public void addEvent(String product, int stock, float price) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        int tag = this.getNextTagNumber();
        byte[] data;

        try {
            dos.writeByte(RequestType.ADD_EVENT.getValue());
            dos.writeUTF(product);
            dos.writeInt(stock);
            dos.writeFloat(price);
            dos.flush();

            data = baos.toByteArray();
            // enviar mensagem ADD EVENT
            this.demultiplexer.send(tag, data);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getStockProduct(String product, int days) throws MensagemErroException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        int tag = this.getNextTagNumber();
        byte[] data;

        try {
            dos.writeByte(RequestType.STOCK.getValue());
            dos.writeUTF(product);
            dos.writeInt(days);
            dos.flush();

            data = baos.toByteArray();
            // enviar mensagem STOCK
            this.demultiplexer.send(tag, data);

            // receber resposta
            data = this.demultiplexer.receive(tag);

            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            int answer = dis.readByte();
            RequestType requestType = RequestType.fromValue(answer);

            if (requestType == RequestType.STOCK) {
                return dis.readInt();
            }

            throw new MensagemErroException("Erro no protocolo de comunicação!");

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public float getProfitProduct(String product, int days) throws MensagemErroException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        int tag = this.getNextTagNumber();
        byte[] data;

        try {
            dos.writeByte(RequestType.TOTAL.getValue());
            dos.writeUTF(product);
            dos.writeInt(days);
            dos.flush();

            data = baos.toByteArray();
            // enviar mensagem TOTAL
            this.demultiplexer.send(tag, data);

            // receber resposta
            data = this.demultiplexer.receive(tag);

            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            int answer = dis.readByte();
            RequestType requestType = RequestType.fromValue(answer);

            if (requestType == RequestType.TOTAL) {
                return dis.readFloat();
            }

            throw new MensagemErroException("Erro no protocolo de comunicação!");

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public float getAveragePriceProduct(String product, int days) throws MensagemErroException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        int tag = this.getNextTagNumber();
        byte[] data;

        try {
            dos.writeByte(RequestType.AVERAGE.getValue());
            dos.writeUTF(product);
            dos.writeInt(days);
            dos.flush();

            data = baos.toByteArray();
            // enviar mensagem AVERAGE
            this.demultiplexer.send(tag, data);

            // receber resposta
            data = this.demultiplexer.receive(tag);

            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            int answer = dis.readByte();
            RequestType requestType = RequestType.fromValue(answer);

            if (requestType == RequestType.AVERAGE) {
                return dis.readFloat();
            }

            throw new MensagemErroException("Erro no protocolo de comunicação!");

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public float getMaximumPriceProduct(String product, int days) throws MensagemErroException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        int tag = this.getNextTagNumber();
        byte[] data;

        try {
            dos.writeByte(RequestType.MAXIMUM.getValue());
            dos.writeUTF(product);
            dos.writeInt(days);
            dos.flush();

            data = baos.toByteArray();
            // enviar mensagem MAXIMUM
            this.demultiplexer.send(tag, data);

            // receber resposta
            data = this.demultiplexer.receive(tag);

            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            int answer = dis.readByte();
            RequestType requestType = RequestType.fromValue(answer);

            if (requestType == RequestType.MAXIMUM) {
                return dis.readFloat();
            }

            throw new MensagemErroException("Erro no protocolo de comunicação!");

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Event> filterEvents(Set<String> products, int day) throws MensagemErroException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        int tag = this.getNextTagNumber();
        byte[] data;

        try {
            String sto = day + " " + products;
            dos.writeByte(RequestType.FILTER.getValue());
            dos.writeInt(day);
            dos.writeInt(products.size());
            for (String s : products) {
                dos.writeUTF(s);
            }
            dos.flush();

            data = baos.toByteArray();
            // enviar mensagem FILTER
            this.demultiplexer.send(tag, data);

            // receber resposta
            data = this.demultiplexer.receive(tag);

            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            int answer = dis.readByte();
            RequestType requestType = RequestType.fromValue(answer);

            if (requestType == RequestType.FILTER) {
                List<Event> list = new ArrayList<>();
                // ler número de produtos
                int nrProdutos = dis.readInt();

                for (int i = 0; i < nrProdutos; i++) {
                    // ler nome do produto
                    String prod = dis.readUTF();
                    // ler número de vendas do produto
                    int nr = dis.readInt();
                    for (int j = 0; j < nr; j++) {
                        int q = dis.readInt();
                        float p = dis.readFloat();
                        list.add(new Event(prod, q, p));
                    }
                }

                return list;
            }

            throw new MensagemErroException("Erro no protocolo de comunicação!");

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean notifySimultaneousSales(String p1, String p2) throws MensagemErroException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        int tag = this.getNextTagNumber();
        byte[] data;

        try {
            dos.writeByte(RequestType.SIMULTANEOUS.getValue());
            dos.writeUTF(p1);
            dos.writeUTF(p2);
            dos.flush();

            data = baos.toByteArray();
            // enviar mensagem SIMULTANEOUS
            this.demultiplexer.send(tag, data);

            // receber resposta
            data = this.demultiplexer.receive(tag);

            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            int answer = dis.readByte();
            RequestType requestType = RequestType.fromValue(answer);

            if (requestType == RequestType.SIMULTANEOUS) {
                return dis.readBoolean();
            }

            throw new MensagemErroException("Erro no protocolo de comunicação!");

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String notifyConsecutiveSales(int n) throws MensagemErroException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        int tag = this.getNextTagNumber();
        byte[] data;

        try {
            dos.writeByte(RequestType.CONSECUTIVE.getValue());
            dos.writeInt(n);
            dos.flush();

            data = baos.toByteArray();
            // enviar mensagem CONSECUTIVE
            this.demultiplexer.send(tag, data);

            // receber resposta
            data = this.demultiplexer.receive(tag);

            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            int answer = dis.readByte();
            RequestType requestType = RequestType.fromValue(answer);

            if (requestType == RequestType.CONSECUTIVE) {
                boolean exists = dis.readBoolean();
                if (exists)
                    return dis.readUTF();
                return null;
            }

            throw new MensagemErroException("Erro no protocolo de comunicação!");

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean nextDay() throws MensagemErroException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        int tag = this.getNextTagNumber();
        byte[] data;

        try {
            dos.writeByte(RequestType.NEXT_DAY.getValue());
            dos.flush();

            data = baos.toByteArray();
            // enviar mensagem NEXT DAY
            this.demultiplexer.send(tag, data);

            // receber resposta
            data = this.demultiplexer.receive(tag);

            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            int answer = dis.readByte();
            RequestType requestType = RequestType.fromValue(answer);

            if (requestType == RequestType.NEXT_DAY) {
                return dis.readBoolean();
            }

            throw new MensagemErroException("Erro no protocolo de comunicação!");

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void exit() throws IOException {
        this.demultiplexer.close();
    }

}
