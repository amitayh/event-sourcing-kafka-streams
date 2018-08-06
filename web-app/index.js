import 'babel-polyfill';
import express from 'express';
import {Server} from 'http';
import io from 'socket.io';
import {startConsumer} from './server/kafka';
import {initCommandHandler} from './server/socket';
import api from './server/api';

const app = express();
const server = Server(app);
const socket = io(server);

app.use(express.static('client/build'));
app.use('/api', api);

// WS
const commandHandler = initCommandHandler(socket);
startConsumer('invoice-command-results', (id, result) => {
  Object.keys(result).forEach(key => {
    const handler = commandHandler[key];
    if (handler) {
      handler(result[key]);
    }
  });
});
startConsumer('invoice-records', (id, record) => {
  socket.sockets.emit('invoice-updated', {...record, id});
});

// socket.on('connection', client => {
//   console.log('a user connected');
//   client.on('disconnect', () => {
//     console.log('a user disconnected');
//   });
// });

const port = process.env.PORT || 3001;
server.listen(port, () => {
  console.log(`listening on *:${port}`);
});
