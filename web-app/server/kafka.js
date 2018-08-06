import uuidv4 from 'uuid/v4';
import {Client, ConsumerGroup, HighLevelProducer} from 'kafka-node';

const kafkaHost = process.env.KAFKA_HOST || 'localhost:9092';
const connectionString = process.env.CONNECTION_STRING || 'localhost:2181';

export const startConsumer = (topic, f) => {
  const consumer = new ConsumerGroup({kafkaHost}, topic);
  consumer.on('message', message => {
    try {
      const value = message.value;
      if (value !== '') {
        f(message.key, JSON.parse(value));
      }
    } catch (e) {
      console.warn(e.message, message);
    }
  });
};

const client = new Client(connectionString);
const commandsProducer = new HighLevelProducer(client);
const commandsTopic = 'invoice-commands';

commandsProducer.on('ready', () => {
  console.log('commands producer ready')
});

export const executeCommand = (invoiceId, commandPayload) => {
  const commandId = uuidv4();
  const command = {
    commandId: commandId,
    toEvents: commandPayload
  };
  const payload = {
    topic: commandsTopic,
    messages: [JSON.stringify(command)],
    key: invoiceId
  };
  return new Promise((resolve, reject) => {
    commandsProducer.send([payload], err => {
      if (err) {
        reject(err);
      } else {
        resolve({commandId, invoiceId: invoiceId});
      }
    });
  });
};
