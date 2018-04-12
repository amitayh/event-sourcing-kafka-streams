import React, {PureComponent} from 'react';
import {initialState} from '../model';
import InvoicesList from './InvoicesList';
import NewInvoice from './NewInvoice';
import styles from './App.css';

class App extends PureComponent {
  render() {
    const state = this.props.state || initialState;
    const dispatch = this.props.dispatch;
    return (
      <div className={`container ${styles.container}`}>
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

export default App;
