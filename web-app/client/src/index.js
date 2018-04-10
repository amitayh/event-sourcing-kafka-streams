import React from 'react';
import ReactDOM from 'react-dom';
import registerServiceWorker from './registerServiceWorker';
import {socket} from './socket';
import {initialState} from './model';
import {createCommandFinished, fetchInvoices, invoiceUpdated} from './actions';
import App from './components/App';

socket.on('command-succeeded', commandId => {
  dispatch(createCommandFinished(commandId));
});
socket.on('invoice-updated', invoice => {
  dispatch(invoiceUpdated(invoice));
});

ReactDOM.render(<App />, document.getElementById('root'));
registerServiceWorker();

let state = initialState;

const debug = (action, state) => {
  console.groupCollapsed(action.type);
  console.info('Old state', state);
  console.info('New state', action.nextState(state));
  console.groupEnd();
};

const render = () => ReactDOM.render(
    <App state={state} dispatch={dispatch}/>,
    document.getElementById('root')
);

function dispatch(action) {
  debug(action, state);
  state = action.nextState(state);
  if (typeof action.runEffect === 'function') {
    action.runEffect(dispatch);
  }
  render();
}

render(); // Initial render
dispatch(fetchInvoices);
