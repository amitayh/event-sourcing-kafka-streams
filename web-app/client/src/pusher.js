import Pusher from 'pusher-js';
import {commandExecutionFailed, commandExecutionSucceeded, invoiceUpdated} from './actions';

export const subscribe = (dispatch, originId) => {
  const pusherApiKey = '2bc1d0ff70b5ecc853d9';
  const pusherOptions = {cluster: 'eu', encrypted: true};
  const pusher = new Pusher(pusherApiKey, pusherOptions);

  const results = pusher.subscribe(`command-results-${originId}`);
  results.bind('command-succeeded', result => {
    dispatch(commandExecutionSucceeded(result.commandId));
  });
  results.bind('command-failed', result => {
    dispatch(commandExecutionFailed(result.commandId, result.cause));
  });

  const records = pusher.subscribe('invoice-records');
  records.bind('invoice-updated', invoice => {
    dispatch(invoiceUpdated(invoice));
  });
};
