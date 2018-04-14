# Event Sourcing with Kafka Streams

This is a POC for using [Kafka Streams](https://kafka.apache.org/documentation/streams/) as a backbone for an event sourced system.

## Running the stream processing app

1. Start Zookeeper / Kafka ([help](https://kafka.apache.org/11/documentation/streams/quickstart#quickstart_streams_startserver))

2. Create an empty SQLite DB file for projection

   ```
   $ cd stream-processing-app
   $ touch ~/projection.db
   ```

4. Run the [cleanup script](stream-processing-app/src/main/scala/org/amitayh/invoices/Cleanup.scala) (only for first run):

   ```
   $ DB=~/projection.db sbt "runMain org.amitayh.invoices.Cleanup"
   ```

5. Start the [CommandHandler](stream-processing-app/src/main/scala/org/amitayh/invoices/CommandHandler.scala):

   ```
   $ sbt "runMain org.amitayh.invoices.CommandHandler"
   ```

5. Start the [Projector](stream-processing-app/src/main/scala/org/amitayh/invoices/Projector.scala):

   ```
   $ DB=~/projection.db sbt "runMain org.amitayh.invoices.CommandHandler"
   ```
## Running the web app

1. Install dependencies
   
   ```
   $ cd web-app
   $ npm install
   $ cd client
   $ npm install
   $ cd ..
   ```

2. Build client

    ```
    $ npm run build-client
    ```

3. Run node server

    ```
    $ npm run start
    ```

4. Open http://localhost:3001/

## License

Copyright © 2018 Amitay Horwitz

Distributed under the MIT License
