export const commands = {};

export const initCommandHandler = socket => ({
  CommandSucceeded: command => {
    const commandId = command.commandId;
    const socketId = commands[commandId];
    delete commands[commandId];
    socket.to(socketId).emit('command-succeeded', commandId);
  },
  CommandFailed: command => {
    const commandId = command.commandId;
    const socketId = commands[commandId];
    delete commands[commandId];
    socket.to(socketId).emit('command-failed', command);
  }
});
