import React, {PureComponent} from 'react';

export default class InvoicesTable extends PureComponent {
  render() {
    const {invoices} = this.props;
    return (
      <table border="1">
        <thead>
          <tr>
            <th>ID</th>
            <th>Customer</th>
            <th>Email</th>
            <th>Total</th>
          </tr>
        </thead>
        <tbody>
          {invoices.map(invoice => <InvoicesRow key={invoice.id} invoice={invoice}/>)}
        </tbody>
      </table>
    );
  }
}

class InvoicesRow extends PureComponent {
  render() {
    const {invoice} = this.props;
    return (
      <tr>
        <td>{invoice.id}</td>
        <td>{invoice.customerName}</td>
        <td>{invoice.customerEmail}</td>
        <td>${invoice.total}</td>
      </tr>
    );
  }
}
