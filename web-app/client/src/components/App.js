import React, {PureComponent} from 'react';
import {initialState} from '../model';
import InvoicesList from './InvoicesList';
import NewInvoice from './NewInvoice';
import './Message.css';

class App extends PureComponent {
  render() {
    const state = this.props.state || initialState;
    const dispatch = this.props.dispatch;
    return (
      <div className="container">
        <Message message={state.message}/>
        <h1>My Invoices</h1>
        {this.renderPage(state, dispatch)}
      </div>
    );
  }

  renderPage(state, dispatch) {
    switch (state.page) {
      case 'list': return <InvoicesList invoices={state.invoices} dispatch={dispatch}/>;
      case 'new': return <NewInvoice draft={state.draft} dispatch={dispatch} />;
      default: return null;
    }
  }
}

class Message extends PureComponent {
  render() {
    const {message} = this.props;
    return (message !== null) ?
      <div className="alert alert-info text-center message">{message}</div> :
      null;
  }
}

export default App;
