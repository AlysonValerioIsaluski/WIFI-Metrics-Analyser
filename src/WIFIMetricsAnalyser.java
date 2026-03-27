import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class WIFIMetricsAnalyser {
  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("O numero de argumentos deve ser 1 (caminho do arquivo csv de entrada)");
      System.exit(0);
    }

    String filePath = args[0];

    try (BufferedReader buffer = new BufferedReader(new FileReader(filePath))) {
      buffer.readLine();
      parseBuffer(filePath, buffer);
    } catch (IOException e) {
      System.err.println("Erro de arquivo: " + e.getMessage());
    }
  }

  private static void parseBuffer(String filePath, BufferedReader buffer) throws IOException {
    Path resultsPath = Path.of(filePath).resolveSibling("resultados_" + System.currentTimeMillis() + ".csv");

    // Dados a serem extraídos das colunas (exceto os dados não utilizados)
    String line, time = "0", protocol, frameLength = "", type;
    // String number, source, destination, seq, ttl;

    String[] csvResultsRow = new String[7];
    String prevFrameLength = "", packetStartTime = "0";

    boolean firstLoop = true;
    double timePrevRequest = 0, responseTime = 0, responseTimeSum = 0;
    int numPackets = 0;

    // Inicia leitura
    while ((line = buffer.readLine()) != null) {

      // Extrai colunas do buffer, e verifica se o número de colunas está correto
      String[] columns = line.split(",");
      if (columns.length != 9) continue;

      // No começo do leitor, salva o cabeçalho no arquivo com nome das colunas
      if (firstLoop) {
        csvResultsRow[0] = "fluxo_id";
        csvResultsRow[1] = "pacotes";
        csvResultsRow[2] = "tamanho_pacote";;
        csvResultsRow[3] = "ramanho_total";
        csvResultsRow[4] = "duracao_fluxo";
        csvResultsRow[5] = "rrt_medio";
        csvResultsRow[6] = "taxa";

        Files.writeString(resultsPath, String.join(",", csvResultsRow) + "\n",
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        prevFrameLength = columns[5].replace("\"", "").strip();
        firstLoop = false;
      }

      // Salva dados no arquivo antes de processar o próximo fluxo
      else if (!prevFrameLength.equals(frameLength)) {
        Double influxSize = Double.parseDouble(frameLength) * numPackets;
        Double influxDuration = Double.parseDouble(time) - Double.parseDouble(packetStartTime);

        csvResultsRow[0] = frameLength;
        csvResultsRow[1] = String.valueOf(numPackets);
        csvResultsRow[2] = frameLength;
        csvResultsRow[3] = String.valueOf(influxSize);
        csvResultsRow[4] = String.valueOf(influxDuration);
        csvResultsRow[5] = String.valueOf(responseTimeSum / numPackets);
        csvResultsRow[6] = String.valueOf(influxSize / influxDuration);

        Files.writeString(resultsPath, String.join(",", csvResultsRow) + "\n",
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        numPackets = 0;
        prevFrameLength = frameLength;
      }

      else {
        prevFrameLength = frameLength;
      }

      // Extrai dados das colunas (exceto os dados não utilizados)
      time = columns[1].replace("\"", "").strip();
      protocol = columns[4].replace("\"", "").strip();
      frameLength = columns[5].replace("\"", "").strip();
      type = columns[6].replace("\"", "").strip();
      // number = columns[0].replace("\"", "").strip();
      // source = columns[2].replace("\"", "").strip();
      // destination = columns[3].replace("\"", "").strip();
      // seq = columns[7].replace("\"", "").strip();
      // ttl = columns[8].replace("\"", "").strip();

      // Ignora linhas que não representam um procolo ICMP
      if (!protocol.equals("ICMP")) continue;

      // Determina se a linha é um request ou reply, e calcula rrt e adiciona na soma
      if (type.contains("request")) {
        timePrevRequest = Double.parseDouble(time);
      }
      else if (type.contains("reply")) {
        responseTime = Double.parseDouble(time) - timePrevRequest;
        responseTimeSum += responseTime;
      }
      else continue;

      if (numPackets == 0) {
        packetStartTime = time;
      }

      numPackets++;
    }

    // Salva últimos dados no arquivo
    Double influxSize = Double.parseDouble(frameLength) * numPackets;
    Double influxDuration = Double.parseDouble(time) - Double.parseDouble(packetStartTime);

    csvResultsRow[0] = frameLength;
    csvResultsRow[1] = String.valueOf(numPackets);
    csvResultsRow[2] = frameLength;
    csvResultsRow[3] = String.valueOf(influxSize);
    csvResultsRow[4] = String.valueOf(influxDuration);
    csvResultsRow[5] = String.valueOf(responseTimeSum / numPackets);
    csvResultsRow[6] = String.valueOf(influxSize / influxDuration);

    Files.writeString(resultsPath, String.join(",", csvResultsRow) + "\n",
            StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    System.out.println("Arquivo CSV com resultados exportado com sucesso com o caminho:" + resultsPath + "!");
  }
}