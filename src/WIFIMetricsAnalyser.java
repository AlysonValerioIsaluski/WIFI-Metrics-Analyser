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

    String[] csvResultsRow = new String[8];
    String line, prevFrameLength = "", packetStartTime = "0", prevTime = "0";

    boolean firstLoop = true;
    double timePrevRequest = 0, responseTime, responseTimeSum = 0;
    int numPackets = 0;

    // Inicia leitura
    while ((line = buffer.readLine()) != null) {

      // Extrai colunas do buffer, e verifica se o número de colunas está correto
      // Regex que separa por vírgula, a não ser que a vírgula enteja dentro de aspas
      String[] columns = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
      if (columns.length < 7) continue;

      // Extrai dados das colunas (exceto os dados não utilizados)
      String currentProtocol = columns[4].replace("\"", "").strip();

      // Ignora linhas que não representam um procolo ICMP
      if (!currentProtocol.equals("ICMP")) continue;

      String currentTime = columns[1].replace("\"", "").strip();
      String currentFrameLength = columns[5].replace("\"", "").strip();
      String currentInfo = columns[6].replace("\"", "").strip();

      // No começo do leitor, salva o cabeçalho no arquivo com nome das colunas
      if (firstLoop) {
        csvResultsRow[0] = "fluxo_id";
        csvResultsRow[1] = "pacotes";
        csvResultsRow[2] = "tamanho_pacote";
        csvResultsRow[3] = "tamanho_total";
        csvResultsRow[4] = "duracao_fluxo";
        csvResultsRow[5] = "rtt_medio";
        csvResultsRow[6] = "rtt_ping";
        csvResultsRow[7] = "taxa";

        Files.writeString(resultsPath, String.join(",", csvResultsRow) + "\n",
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        prevFrameLength = currentFrameLength;
        packetStartTime = currentTime;

        firstLoop = false;
      }

      // Salva dados no arquivo antes de processar o próximo fluxo
      else if (!prevFrameLength.equals(currentFrameLength)) {
        Double influxSize = Double.parseDouble(prevFrameLength) * numPackets;
        Double influxDuration = Double.parseDouble(prevTime) - Double.parseDouble(packetStartTime);

        csvResultsRow[0] = prevFrameLength; // fluxo_id
        csvResultsRow[1] = String.valueOf(numPackets); // pacotes
        csvResultsRow[2] = prevFrameLength; // tamanho_pacote
        csvResultsRow[3] = String.valueOf(influxSize); // tamanho_total
        csvResultsRow[4] = String.valueOf(influxDuration); // duracao_fluxo
        csvResultsRow[5] = String.valueOf(responseTimeSum / (int) (numPackets / 2)); // rtt_medio
        csvResultsRow[6] = ""; // rtt_ping deve ser preenchido manualmente
        csvResultsRow[7] = String.valueOf((influxSize * 8) / influxDuration); // taxa

        Files.writeString(resultsPath, String.join(",", csvResultsRow) + "\n",
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        // Reseta variáveis para o novo fluxo
        numPackets = 0;
        responseTimeSum = 0;
        packetStartTime = currentTime;
        prevFrameLength = currentFrameLength;
      }

      // Determina se a linha é um request ou reply, e calcula rtt e adiciona na soma
      if (currentInfo.contains("ping) request")) {
        timePrevRequest = Double.parseDouble(currentTime);
      }
      else if (currentInfo.contains("ping) reply")) {
        responseTime = Double.parseDouble(currentTime) - timePrevRequest;
        responseTimeSum += responseTime;
      }

      numPackets++;
      prevTime = currentTime;
    }

    // Salva últimos dados no arquivo
    Double influxSize = Double.parseDouble(prevFrameLength) * numPackets;
    Double influxDuration = Double.parseDouble(prevTime) - Double.parseDouble(packetStartTime);

    csvResultsRow[0] = prevFrameLength; // fluxo_id
    csvResultsRow[1] = String.valueOf(numPackets); // pacotes
    csvResultsRow[2] = prevFrameLength; // tamanho_pacote
    csvResultsRow[3] = String.valueOf(influxSize); // tamanho_total
    csvResultsRow[4] = String.valueOf(influxDuration); // duracao_fluxo
    csvResultsRow[5] = String.valueOf(responseTimeSum / (int) (numPackets / 2 )); // rtt_medio
    csvResultsRow[6] = ""; // rtt_ping deve ser preenchido manualmente
    csvResultsRow[7] = String.valueOf((influxSize * 8) / influxDuration); // taxa

    Files.writeString(resultsPath, String.join(",", csvResultsRow) + "\n",
            StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    System.out.println("Arquivo CSV com resultados exportado com sucesso. Caminho:" + resultsPath + "!");
  }
}