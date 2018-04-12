import React, {PureComponent} from 'react';
import InvoicesTable from './InvoicesTable';
import {newInvoicePage, payInvoice} from '../actions';

class InvoicesList extends PureComponent {
  render() {
    const {invoices, dispatch} = this.props;
    return (
      <div>
        <h2>List</h2>
        <InvoicesTable invoices={invoices} onPay={id => dispatch(payInvoice(id))}/>
        <p>
          <button
            className="btn btn-primary"
            onClick={() => dispatch(newInvoicePage)}>Create new invoice</button>
        </p>
      </div>
    );
  }
}

export default InvoicesList;
