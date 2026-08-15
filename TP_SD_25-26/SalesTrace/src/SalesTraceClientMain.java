import exceptions.MensagemErroException;
import exceptions.TentativasExcedidasException;

import java.util.*;

public class SalesTraceClientMain {

    private static final ISalesTraceClient client = new SalesTraceClient();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        menuAutenticacao();

        menuOperacoes();

    }

    private static void menuAutenticacao() {
        boolean autenticado = false;

        while (!autenticado) {
            System.out.println("=== Autenticação ===");
            System.out.println("1 - Login");
            System.out.println("2 - Registo");
            System.out.println("0 - Sair");
            System.out.print("Opção: ");

            String opcao = scanner.nextLine();

            try {
                switch (opcao) {
                    case "0":
                        System.out.println("A encerrar...");
                        System.exit(0);
                    case "1":
                        autenticado = login();
                        break;

                    case "2":
                        autenticado = registo();
                        break;

                    default:
                        System.out.println("Opção inválida.");
                }
            } catch (TentativasExcedidasException e) {
                System.out.println("Número máximo de tentativas excedido.");
                System.out.println("A encerrar...");
                System.exit(0);
            }
        }

        System.out.println("Autenticação bem sucedida!");
    }

    private static boolean login() throws TentativasExcedidasException {
        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        boolean ok = client.logIn(username, password);

        if (!ok) {
            System.out.println("Credenciais inválidas.");
        }

        return ok;
    }

    private static boolean registo() throws TentativasExcedidasException {
        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        System.out.print("É administrador? (s/n): ");
        String adminInput = scanner.nextLine();
        boolean isAdmin = adminInput.equalsIgnoreCase("s");

        boolean ok = client.signIn(username, password, isAdmin);

        if (!ok) {
            System.out.println("Registo inválido.");
        }

        return ok;
    }


    private static void menuOperacoes() {

        while (true) {
            System.out.println("=== Operações ===");
            System.out.println("1 - Adicionar Evento");
            System.out.println("2 - Agregação de Informação");
            System.out.println("3 - Filtrar Eventos");
            System.out.println("4 - Notificação de Ocorrências");
            System.out.println("5 - Mudar de Dia");
            System.out.println("0 - Sair");
            System.out.print("Opção: ");

            String opcao = scanner.nextLine();

            switch (opcao) {
                case "0":
                    System.out.println("A encerrar...");
                    System.exit(0);
                case "1":
                    adicionarEvento();
                    break;
                case "2":
                    agregacaoInformacao();
                    break;
                case "3":
                    filtrarEventos();
                    break;
                case "4":
                    notificaoOcorrencias();
                    break;
                case "5":
                    mudarDia();
                    break;

                default:
                    System.out.println("Opção inválida.");
            }
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Input inválido, tente novamente.");
            }
        }
    }

    private static float readFloat(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Float.parseFloat(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Input inválido, tente novamente.");
            }
        }
    }

    private static void adicionarEvento() {

        System.out.print("Produto: ");
        String produto = scanner.nextLine();

        int quantidade = readInt("Quantidade: ");
        float custo = readFloat("Preço: ");

        client.addEvent(produto, quantidade, custo);

    }

    private static void agregacaoInformacao() {
        RequestType tipo = RequestType.NONE;
        while (tipo == RequestType.NONE) {
            System.out.println("=== Agregação de Informação ===");
            System.out.println("1 - Quantidade de Vendas");
            System.out.println("2 - Volume de Vendas");
            System.out.println("3 - Preço Médio de Vendas");
            System.out.println("4 - Preço Máximo de Vendas");
            System.out.print("Opção: ");

            String opcao = scanner.nextLine();

            switch (opcao) {
                case "1":
                    tipo = RequestType.STOCK;
                    break;
                case "2":
                    tipo = RequestType.TOTAL;
                    break;
                case "3":
                    tipo = RequestType.AVERAGE;
                    break;
                case "4":
                    tipo = RequestType.MAXIMUM;
                    break;

                default:
                    System.out.println("Opção inválida, tente novamente.");
            }

        }

        System.out.print("Produto: ");
        String produto = scanner.nextLine();

        int dias = readInt("Nr Dias: ");

        try {
            switch (tipo) {
                case RequestType.STOCK:
                    int stock = client.getStockProduct(produto, dias);
                    System.out.printf("STOCK: [produto: %s, dias: %d, resultado: %d]\n", produto, dias, stock);
                    break;
                case RequestType.TOTAL:
                    float total = client.getProfitProduct(produto, dias);
                    System.out.printf("TOTAL: [produto: %s, dias: %d, resultado: %.2f]\n", produto, dias, total);
                    break;
                case RequestType.AVERAGE:
                    float average = client.getAveragePriceProduct(produto, dias);
                    System.out.printf("AVERAGE: [produto: %s, dias: %d, resultado: %.2f]\n", produto, dias, average);
                    break;
                case RequestType.MAXIMUM:
                    float maximum = client.getMaximumPriceProduct(produto, dias);
                    System.out.printf("MAXIMUM: [produto: %s, dias: %d, resultado: %.2f]\n", produto, dias, maximum);
                    break;
            }
        } catch (MensagemErroException ignored) {
            System.out.println("Input de agregação inválido ou erro do servidor.");
        }


    }

    private static void filtrarEventos() {

        int dia = readInt("Dia: ");

        Set<String> produtos = new HashSet<>();
        System.out.println("Introduza o nome dos produtos (escreva 'exit' para sair):");

        while (true) {
            System.out.print("Produto: ");
            String linha = scanner.nextLine();

            if (linha.equalsIgnoreCase("exit")) {
                break;
            }

            produtos.add(linha);
        }

        try {
            List<Event> list = client.filterEvents(produtos, dia);
            for (Event e : list) {
                System.out.println(e);
            }
        } catch (MensagemErroException ignored) {
            System.out.println("Input inválido ou erro do servidor.");
        }

    }

    private static void notificaoOcorrencias() {
        RequestType tipo = RequestType.NONE;
        while (tipo == RequestType.NONE) {
            System.out.println("=== Notificação de ocorrências ===");
            System.out.println("1 - Vendas Simultâneas");
            System.out.println("2 - Vendas Consecutivas");
            System.out.print("Opção: ");

            String opcao = scanner.nextLine();

            switch (opcao) {
                case "1":
                    tipo = RequestType.SIMULTANEOUS;
                    break;
                case "2":
                    tipo = RequestType.CONSECUTIVE;
                    break;

                default:
                    System.out.println("Opção inválida, tente novamente.");
            }
        }

        if (tipo == RequestType.SIMULTANEOUS) {
            System.out.print("Produto 1: ");
            String p1 = scanner.nextLine();

            System.out.print("Produto 2: ");
            String p2 = scanner.nextLine();

            try {
                boolean out = client.notifySimultaneousSales(p1, p2);
                System.out.println("SIMULTANEOUS [resultado: " + out + "]");
            } catch (MensagemErroException ignored) {
                System.out.println("Input inválido ou erro do servidor.");
            }

        } else {
            int nrVendas = readInt("Nr vendas: ");

            try {
                String p = client.notifyConsecutiveSales(nrVendas);
                if (p == null)
                    p = "null";
                System.out.println("CONSECUTIVE [resultado: " + p + "]");
            } catch (MensagemErroException ignored) {
                System.out.println("Input inválido ou erro do servidor.");
            }

        }

    }

    private static void mudarDia() {
        try {
            boolean out = client.nextDay();
            System.out.println("NEXT DAY [resultado: " + out + "]");
        } catch (MensagemErroException ignored) {
            System.out.println("Erro do servidor.");
        }
    }

}
