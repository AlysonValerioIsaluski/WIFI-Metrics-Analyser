# WIFI-Metrics-Analyser
Java application that receives a csv file exported from wireshark with icmp filter, and extracts and processes data from request and reply pings, then exports a csv file containing: Number of packages per influx, Size of Influx Package, Total size of Packages in Influx, Influx Duration, Average of Influx Ping Response Time (rrt), Influx Throughput


# Dependencies

JDK 21

No external libraries or frameworks needed


# Compiling
in src directory:

javac WIFIMetricsAnalyser.java


# Execution
in src directory:

java WIFIMetricsAnalyser.class wireshark-exported-csv-path
