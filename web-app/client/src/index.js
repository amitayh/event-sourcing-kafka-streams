import React from 'react';
import ReactDOM from 'react-dom';
import registerServiceWorker from './registerServiceWorker';
import {initialState} from './model';
import {subscribe} from './pusher';
import {originId} from './origin';
import {fetchInvoices} from './actions';
import App from './components/App';

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
subscribe(dispatch, originId);
dispatch(fetchInvoices);
