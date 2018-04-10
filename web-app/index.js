import 'babel-core/register';
import 'babel-polyfill';
import express from 'express';
import {Server} from 'http';
import io from 'socket.io';
import {startConsumer} from './server/kafka';
import {commands} from './server/socket';
import api from './server/api';

const app = express();
const server = Server(app);
const socket = io(server);

app.use(express.static('client/build'));
app.use('/api', api);

// WS
startConsumer('invoice-command-results', {}, (id, result) => {
  if (result.CommandSucceeded) {
    const commandId = result.CommandSucceeded.commandId;
    const socketId = commands[commandId];
    delete commands[commandId];
    socket.to(socketId).emit('command-succeeded', commandId);
  }
});
startConsumer('invoice-states', {}, (id, record) => {
  socket.sockets.emit('invoice-updated', {...record, id});
});

// socket.on('connection', client => {
//   console.log('a user connected');
//   client.on('disconnect', () => {
//     console.log('a user disconnected');
//   });
// });

const port = 3001;
server.listen(port, () => {
  console.log(`listening on *:${port}`);
});
