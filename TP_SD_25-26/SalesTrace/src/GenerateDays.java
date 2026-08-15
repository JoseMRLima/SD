
import java.io.*;
import java.nio.file.*;
import java.util.Random;

public class GenerateDays {

    private static final String OUTPUT_DIR = "data";
    private static final int MIN_ENTRIES = 500;
    private static final int MAX_ENTRIES = 10000;
    private static final int MIN_QTY = 100;
    private static final int MAX_QTY = 10000;
    private static final float MIN_PRICE = 100.0F;
    private static final float MAX_PRICE = 100000.0F;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java GenerateDays <number_of_days>");
            System.exit(1);
        }

        int numDays;
        try {
            numDays = Integer.parseInt(args[0]);
            if (numDays <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: number_of_days must be a positive integer");
            System.exit(1);
            return;
        }

        Path outputDir = Paths.get(OUTPUT_DIR);

        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            System.err.println("Failed to create output directory");
            e.printStackTrace();
            System.exit(1);
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

        for (int day = 1; day <= numDays; day++) {
            String filename = String.format("day_%04d.bin", day);
            Path filePath = outputDir.resolve(filename);

            try (DataOutputStream dos = new DataOutputStream(Files.newOutputStream(filePath))) {
                int numEntries = rand.nextInt(MIN_ENTRIES, MAX_ENTRIES);

                // escrever o número de vendas no ficheiro
                dos.writeInt(numEntries);

                for (int i = 0; i < numEntries; i++) {
                    String prod = products[rand.nextInt(products.length)];
                    int quantity = rand.nextInt(MIN_QTY, MAX_QTY);
                    float price = rand.nextFloat(MIN_PRICE, MAX_PRICE);

                    dos.writeUTF(prod);
                    dos.writeInt(quantity);
                    dos.writeFloat(price);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Done. Generated " + numDays + " BIN files in '" + OUTPUT_DIR + "/'.");
    }
}
