import React, {PureComponent} from 'react';
import InvoicesTable from './InvoicesTable';
import {newInvoicePage, payInvoice, removeLineItem} from '../actions';

const id1 = '4d6b05ad-98ec-46e4-b3c7-d12ac0dddc77';
const id2 = 'ccc77095-b1f7-4a65-82c6-f1014c205b53';

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
          {' '}
          <button
            className="btn btn-default"
            onClick={() => dispatch(removeLineItem(id1, id2))}>Invalid command</button>
        </p>
      </div>
    );
  }
}

export default InvoicesList;
