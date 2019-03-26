# Event Sourcing with Kafka Streams

This is a POC for using [Kafka Streams](https://kafka.apache.org/documentation/streams/)
as a backbone for an event sourced system.

## Running the project locally

You can either run the project with [Docker Compose](https://docs.docker.com/compose/),
or run everything on your host.

### Prerequisites

1. Install sbt ([help](https://www.scala-sbt.org/))

2. Build the project:

   ```
   $ sbt assembly
   ```

### Run with Docker Compose

1. Update your host IP address in [.env](.env)

2. Build images

   ```
   $ docker-compose build
   ```

3. Start the containers

   ```
   $ docker-compose up
   ```

### Run on host

#### Setup

1. Run Zookeeper / Kafka ([help](https://kafka.apache.org/quickstart))

2. Run MySQL ([help](https://dev.mysql.com/doc/mysql-getting-started/en/))

3. Install the [schema](listdao/src/main/resources/schema.sql)

4. Create the topics (edit `config/local.properties` as needed):

   ```
   $ bin/setup.sh config/local.properties
   ```

#### Running

1. Run the [command handler](commandhandler/src/main/scala/org/amitayh/invoices/commandhandler/CommandHandler.scala):

   ```
   $ bin/commandhandler.sh config/local.properties
   ```

2. Run the [invoices list projector](listprojector/src/main/scala/org/amitayh/invoices/projector/ListProjector.scala):

   ```
   $ bin/projector.sh config/local.properties
   ```

3. Run the [web server](web/src/main/scala/org/amitayh/invoices/web/InvoicesServer.scala):

   ```
   $ bin/web.sh config/local.properties
   ```

If everything worked, you should be able to see the app running at `http://localhost:8080/index.html`

## License

Copyright © 2018 Amitay Horwitz

Distributed under the MIT License
