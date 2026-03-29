# WIFI-Metrics-Analyser
Aplicação de Java que recebe de entrada um arquivo csv exportado do wireshark com filtro icmp, e extrai e processa os dados dos pings "request" e "reply", e então exporta um arquivo csv contendo: Número de pacotes por fluxo, tamanho dos pacotes do fluxo, tamanho total dos pacotes do fluxo, duração do fluxo, tempo médio de resposta dos pings (RRT) e taxa de transmissão (throughput).


# Dependências

JDK 21

Nenhuma biblioteca externa ou frameworks necessárias.


# Compilação
no diretório src:

`javac WIFIMetricsAnalyser.java`


# Execution
no diretório src:

`java WIFIMetricsAnalyser.class caminho-do-arquivo-csv-exportado-pelo-wireshark`
