import React, {PureComponent} from 'react';

const formatter = new Intl.NumberFormat(['en-US'], {style: 'currency', currency: 'USD'});

export default class InvoicesTable extends PureComponent {
  render() {
    const {invoices, onPay} = this.props;
    return (
      <table className="table table-bordered table-hover">
        <thead>
          <tr>
            {/*<th>ID</th>*/}
            <th>Customer</th>
            <th>Due date</th>
            <th>Total</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {invoices.map(invoice => {
            return <InvoicesRow
              key={invoice.id}
              invoice={invoice}
              onPay={() => onPay(invoice.id)}/>;
          })}
        </tbody>
      </table>
    );
  }
}

class InvoicesRow extends PureComponent {
  render() {
    const {invoice, onPay} = this.props;
    const payDisabled = (invoice.status === 'Paid');
    return (
      <tr>
        {/*<td>{invoice.id}</td>*/}
        <td>{invoice.customerName} &lt;{invoice.customerEmail}&gt;</td>
        <td>{invoice.dueDate}</td>
        <td align="right">{formatter.format(invoice.total)}</td>
        <td>{invoice.status}</td>
        <td>
          <button
            className="btn btn-success"
            disabled={payDisabled}
            onClick={() => onPay()}>Pay</button>
        </td>
      </tr>
    );
  }
}
