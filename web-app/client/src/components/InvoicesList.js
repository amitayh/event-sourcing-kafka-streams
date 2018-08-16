import React, {PureComponent} from 'react';
import InvoicesTable from './InvoicesTable';
import {newInvoicePage, payInvoice, deleteInvoice, removeLineItem} from '../actions';

const id1 = '4d6b05ad-98ec-46e4-b3c7-d12ac0dddc77';

class InvoicesList extends PureComponent {
  render() {
    const {invoices, dispatch} = this.props;
    return (
      <div>
        <h2>List</h2>
        <InvoicesTable
          invoices={invoices}
          onPay={id => dispatch(payInvoice(id))}
          onDelete={id => dispatch(deleteInvoice(id))}/>
        <p>
          <button
            className="btn btn-primary"
            onClick={() => dispatch(newInvoicePage)}>Create new invoice</button>
          {' '}
          <button
            className="btn btn-default"
            onClick={() => dispatch(removeLineItem(id1, 0))}>Invalid command</button>
        </p>
      </div>
    );
  }
}

export default InvoicesList;
