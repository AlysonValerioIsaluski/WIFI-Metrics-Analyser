import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

public class Main {
  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("Erro de numero de argumentos, deve ser 1 (caminho de arquivo do csv)");
      System.exit(0);
    }
    String filePath = args[0];

    try (BufferedReader buffer = new BufferedReader(new FileReader(filePath))) {
      String line;

      buffer.readLine();

      boolean firstLoop = true;

      while ((line = buffer.readLine()) != null) {
        String[] columns = line.split(",");
        if (columns.length < 6)
          continue;

        String time = columns[0];
        String source = columns[1];
        String destination = columns[2];
        String protocol = columns[3];
        String frameLength = columns[4];
        String info = columns[5];

        //String[] splitInfo = info.split(",");
        //System.out.println("AAA: " + splitInfo.length);

        // Shows information
        if (firstLoop) {
          System.out.println("time: " + time + ", source: " + source +
                  ", destination: " + destination + ", protocol: " + protocol +
                  ", frameLength: " + frameLength + ", info: " + info);
          System.out.println();
          firstLoop = false;
          continue;
        }
        for (String column : columns) {
          System.out.printf(column + ", ");
        }
        System.out.println();
      }
    } catch (IOException e) {
      System.err.println("Erro de arquivo: " + e.getMessage());
    }
  }
}
