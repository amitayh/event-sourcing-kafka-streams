# SOS

1. Kafka

   ```bash
   $ cd ~/dev/personal/kafka_2.12-2.0.0
   ```

   1. Start Zookeeper
   
      ```bash
      $ bin/zookeeper-server-start.sh config/zookeeper.properties 
      ```
   
   2. Start Kafka
   
      ```bash
      $ bin/kafka-server-start.sh config/server.properties
      ```

2. Start stream-processing app

   ```bash
   $ cd ~/dev/personal/event-sourcing-kafka-streams/stream-processing-app
   ```
   
   1. Build
   
      ```bash
      $ sbt package
      ```
   
   2. Run command handler
      
      ```bash
      $ java -cp target/scala-2.12/kafka-streams-poc_2.12-0.1.jar:$(cat target/streams/compile/dependencyClasspath/\$global/streams/export) org.amitayh.invoices.CommandHandler
      ```
   
   3. Run projector
   
      ```bash
      $ DB=../projection.db java -cp target/scala-2.12/kafka-streams-poc_2.12-0.1.jar:$(cat target/streams/compile/dependencyClasspath/\$global/streams/export) org.amitayh.invoices.Projector
      ```

3. Run web app

   1. Build client
   
      ```bash
      $ cd ~/dev/personal/event-sourcing-kafka-streams/web-app/client
      $ npm run build
      ```

   2. Run node server
   
      ```bash
      $ cd ~/dev/personal/event-sourcing-kafka-streams/web-app
      $ DB=../projection.db npm run start
      ```

3. ???

4. PROFIT!
