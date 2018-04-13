import uuidv4 from 'uuid/v4';
import {Client, ConsumerGroup, HighLevelProducer} from 'kafka-node';

export const startConsumer = (topic, f) => {
  const consumer = new ConsumerGroup({}, topic);
  consumer.on('message', message => {
    f(message.key, JSON.parse(message.value));
  });
};

const client = new Client();
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
