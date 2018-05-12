const commands = {};

export const setSocketId = (commandId, socketId) => {
  commands[commandId] = socketId;
};

const socketForCommand = commandId => {
  const socketId = commands[commandId];
  delete commands[commandId];
  return socketId;
};

export const initCommandHandler = socket => ({
  CommandSucceeded: command => {
    const commandId = command.commandId;
    const socketId = socketForCommand(commandId);
    socket.to(socketId).emit('command-succeeded', commandId);
  },
  CommandFailed: command => {
    const socketId = socketForCommand(command.commandId);
    socket.to(socketId).emit('command-failed', command);
  }
});
