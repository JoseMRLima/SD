import java.net.Socket;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Tests {

    private static final int NUM_THREADS = 5;
    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    private static void usage() {
        System.out.println("Usage: java Tests <D>");
        System.out.println("  D: número de dias passados");
    }

    public static void main(String[] args) {

        if (args.length != 1) {
            usage();
            System.exit(1);
        }

        int D;
        try {
            D = Integer.parseInt(args[0]);
            if (D <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            System.err.println("Error: number_of_days must be a positive integer");
            System.exit(1);
            return;
        }

        System.out.printf("%-10s | %-10s | %-10s | %-15s | %-15s%n",
                "Operação", "Clientes", "Ops/Cli", "Ops/s", "Latência(ms)");

        try {
            // Testes de Registo de Eventos
            runTest("AddEvent", 1, 1000, D);
            runTest("AddEvent", 10, 1000, D);
            runTest("AddEvent", 50, 1000, D);
            runTest("AddEvent", 100, 1000, D);

            System.out.println();

            // Testes de Operações de Agregação
            runTest("GetStock", 1, 100, D);
            runTest("GetStock", 10, 100, D);
            runTest("GetStock", 50, 100, D);
            runTest("GetStock", 100, 100, D);

            System.out.println();

            runTest("GetProfit", 1, 500, D);
            runTest("GetProfit", 10, 500, D);
            runTest("GetProfit", 50, 500, D);
            runTest("GetProfit", 100, 500, D);

            System.out.println();

            runTest("GetAverage", 1, 1000, D);
            runTest("GetAverage", 10, 1000, D);
            runTest("GetAverage", 50, 1000, D);
            runTest("GetAverage", 100, 1000, D);

            System.out.println();

            runTest("GetMax", 1, 50, D);
            runTest("GetMax", 10, 100, D);
            runTest("GetMax", 50, 500, D);
            runTest("GetMax", 100, 1000, D);

            System.out.println();

            // Teste de Robustez
            runRobustness();

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    private static class TestSync {
        private final Lock lock = new ReentrantLock();
        private final Condition startCond = lock.newCondition();
        private final Condition endCond = lock.newCondition();

        private final int totalClients;
        private int readyCount = 0;
        private int doneCount = 0;
        private long totalTime = 0;

        public TestSync(int n) {
            this.totalClients = n;
        }

        public void awaitStart() throws InterruptedException {
            lock.lock();
            try {
                readyCount++;
                if (readyCount < totalClients + 1) {
                    while (readyCount < totalClients + 1) {
                        startCond.await();
                    }
                } else {
                    startCond.signalAll();
                }
            } finally {
                lock.unlock();
            }
        }

        public void registerDone(long time) {
            lock.lock();
            try {
                if (time > 0) this.totalTime += time;
                doneCount++;
                if (doneCount == totalClients) {
                    endCond.signal();
                }
            } finally {
                lock.unlock();
            }
        }

        public void awaitEnd() throws InterruptedException {
            lock.lock();
            try {
                while (doneCount < totalClients) {
                    endCond.await();
                }
            } finally {
                lock.unlock();
            }
        }

        public long getTotalTime() {
            lock.lock();
            try { return totalTime; } finally { lock.unlock(); }
        }
    }

    private static class TicketCounter {
        private int next = 0;
        private Lock l = new ReentrantLock();

        public int getTicket() {
            l.lock();
            try {
                return next++;
            } finally {
                l.unlock();
            }
        }
    }


    private static void runTest(String op, int nClientes, int opsPorCliente, int D) throws Exception {
        TestSync sync = new TestSync(nClientes);
        Thread[] threads = new Thread[nClientes];

        for (int i = 0; i < nClientes; i++) {
            final int id = i;
            threads[i] = new Thread(() -> {
                try {
                    ISalesTraceClient client = new SalesTraceClient();
                    String user = "u_" + op + "_" + nClientes + "_" + id + "_" + System.nanoTime();

                    boolean logged = false;
                    try {
                        if (client.signIn(user, "pass", false))
                            logged = true;
                    } catch (Exception ignored) {}

                    sync.awaitStart();

                    if (!logged) {
                        sync.registerDone(0);
                        return;
                    }

                    String[] products = {
                            "hammer",
                            "apple",
                            "notebook",
                            "screwdriver",
                            "chair",
                            "bottle",
                            "keyboard",
                            "mouse",
                            "lamp",
                            "backpack",
                            "pencil",
                            "charger",
                            "car",
                            "bike",
                            "door",
                            "phone",
                            "pants",
                            "sneakers",
                            "wood",
                            "pillow",
                            "oil",
                            "bread",
                    };

                    Random rand = new Random();
                    long start = System.nanoTime();
                    TicketCounter counter = new TicketCounter();

                    Thread[] t = new Thread[NUM_THREADS];
                    for (int j = 0; j < NUM_THREADS; j++) {
                        t[j] = new Thread(() -> {
                            while (true) {
                                int ticket = counter.getTicket();
                                if (ticket >= opsPorCliente)
                                    break;

                                String prod = products[rand.nextInt(products.length)];
                                int d = rand.nextInt(1, D);

                                switch (op) {
                                    case "AddEvent":
                                        client.addEvent(prod, rand.nextInt(100, 1000), rand.nextFloat(100, 1000));
                                        break;
                                    case "GetStock":
                                        client.getStockProduct(prod, d);
                                        break;
                                    case "GetProfit":
                                        client.getProfitProduct(prod, d);
                                        break;
                                    case "GetAverage":
                                        client.getAveragePriceProduct(prod, d);
                                        break;
                                    case "GetMax":
                                        client.getMaximumPriceProduct(prod, d);
                                        break;
                                }
                            }
                        });
                        t[j].start();
                    }

                    for (int j = 0; j < NUM_THREADS; j++)
                        t[j].join();

                    long end = System.nanoTime();

                    sync.registerDone(end - start);
                    Thread.sleep(1000);
                    client.exit();

                } catch (Exception e) {
                    sync.registerDone(0);
                }
            });
            threads[i].start();
        }

        sync.awaitStart();
        long gStart = System.nanoTime();
        sync.awaitEnd();
        long gEnd = System.nanoTime();

        double sec = (gEnd - gStart) / 1e9;
        long totalOps = (long) nClientes * opsPorCliente;
        double throughput = totalOps / sec;
        double latency = (sync.getTotalTime() / (double) nClientes / opsPorCliente) / 1e6;

        System.out.printf("%-10s | %-10d | %-10d | %-15.2f | %-15.4f%n",
                op, nClientes, opsPorCliente, throughput, latency);
    }

    private static class ResCounter {
        int count = 0;
        final Lock lock = new ReentrantLock();
        void inc() { lock.lock(); try { count++; } finally { lock.unlock(); } }
        int get() { lock.lock(); try { return count; } finally { lock.unlock(); } }
    }

    private static void runRobustness() {
        System.out.print("Teste Robustez ... ");
        ResCounter res = new ResCounter();

        Thread slow = new Thread(() -> {
            try {
                Socket s = new Socket(HOST, PORT);
                Thread.sleep(25000);
                s.close();
            } catch (Exception e) {}
        });
        slow.start();

        Thread normal = new Thread(() -> {
            try {
                Thread.sleep(500);
                ISalesTraceClient client = new SalesTraceClient();
                boolean ok = false;
                try {
                    if (client.signIn("u_rob_" + System.nanoTime(), "pass", false)) ok = true;
                } catch (Exception e) {}

                if (ok) {
                    client.addEvent("Check", 1, 10.0f);
                    res.inc();
                    Thread.sleep(200);
                    client.exit();
                } else {
                    System.out.println(" (Login falhou) ");
                }
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }
        });
        normal.start();

        try {
            normal.join();
            slow.join();
        } catch (InterruptedException e) {}

        System.out.println(res.get() > 0 ? "OK (Passou)" : "FALHOU");
    }

}