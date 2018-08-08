import uuidv4 from 'uuid/v4';
import {KafkaConsumer, Producer} from 'node-rdkafka';

const kafkaHost = process.env.KAFKA_HOST || 'localhost:9092';
//const connectionString = process.env.CONNECTION_STRING || 'localhost:2181';

export const startConsumer = (topic, f) => {
  const options = {
    'group.id': `kafka-streams-demo-${topic}`,
    'metadata.broker.list': kafkaHost
  };
  console.log(`creating consumer ${kafkaHost}/${topic}...`);
  const consumer = new KafkaConsumer(options, {});
  consumer.connect();
  consumer
    .on('ready', () => {
      console.log(`${topic} consumer ready`);
      consumer.subscribe([topic]);
      consumer.consume();
    })
    .on('data', data => {
      try {
        const key = data.key.toString();
        const value = data.value.toString();
        f(key, JSON.parse(value));
      } catch (e) {
        console.warn(e.message, data);
      }
    });
};

console.log(`creating producer ${kafkaHost}...`);
const commandsProducer = new Producer({'metadata.broker.list': kafkaHost}, {});
const commandsTopic = 'invoice-commands';

commandsProducer.connect();
commandsProducer.on('ready', () => {
  console.log('commands producer ready')
});
commandsProducer.on('event.error', err => {
  console.error('error occurred', err);
});

export const executeCommand = (invoiceId, commandPayload) => {
  const commandId = uuidv4();
  const command = {
    commandId: commandId,
    toEvents: commandPayload
  };
  return new Promise((resolve, reject) => {
    const result = commandsProducer.produce(
      commandsTopic,
      null,
      new Buffer(JSON.stringify(command)),
      invoiceId,
      Date.now()
    );
    if (result === true) {
      resolve({commandId, invoiceId: invoiceId})
    } else {
      reject(result);
    }
  });
};
