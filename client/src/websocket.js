import {commandExecutionFailed, commandExecutionSucceeded, invoiceUpdated} from './actions';
import {baseUrl} from './config';

const handlers = {
  CommandSucceeded: (dispatch, message) => {
    dispatch(commandExecutionSucceeded(message.commandId));
  },
  CommandFailed: (dispatch, message) => {
    dispatch(commandExecutionFailed(message.commandId, message.cause));
  },
  InvoiceUpdated: (dispatch, message) => {
    dispatch(invoiceUpdated(message.record));
  }
};

export const subscribe = (dispatch, originId) => {
  const events = new EventSource(`${baseUrl}/events/${originId}`);
  events.addEventListener('message', event => {
    const message = JSON.parse(event.data);
    Object.keys(message).forEach(key => {
      const handler = handlers[key];
      if (handler) {
        handler(dispatch, message[key]);
      }
    });
  });
};
