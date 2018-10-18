# Invoices - Kafka Streams

```
brew services start mysql

~/dev/personal/kafka_2.12-2.0.0
bin/zookeeper-server-start.sh config/zookeeper.properties
bin/kafka-server-start.sh config/server.properties

~/dev/personal/invoices-kafka-streams
BOOTSTRAP_SERVERS=localhost:9092 java -jar commandhandler/target/scala-2.12/commandhandler.jar
BOOTSTRAP_SERVERS=localhost:9092 DB_DRIVER=com.mysql.cj.jdbc.Driver DB_URL="jdbc:mysql://localhost/invoices?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC" DB_USER=root DB_PASS="" java -jar listprojector/target/scala-2.12/listprojector.jar
BOOTSTRAP_SERVERS=localhost:9092 DB_DRIVER=com.mysql.cj.jdbc.Driver DB_URL="jdbc:mysql://localhost/invoices?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC" DB_USER=root DB_PASS="" java -jar web/target/scala-2.12/web.jar
```
