import React, {PureComponent} from 'react';

const formatter = new Intl.NumberFormat(['en-US'], {style: 'currency', currency: 'USD'});

const zeroPad = number => number.toString().padStart(6, '0');

export default class InvoicesTable extends PureComponent {
  render() {
    const {invoices, onPay} = this.props;
    const total = invoices.length;
    return (
      <table className="table table-bordered table-hover">
        <thead>
          <tr>
            <th>#</th>
            <th>Customer</th>
            <th>Due date</th>
            <th>Total</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {invoices.map((invoice, index) => {
            return <InvoicesRow
              key={invoice.id}
              number={total - index}
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
    const {invoice, number, onPay} = this.props;
    return invoice.pending ?
      this.renderPending(invoice, number) :
      this.renderInvoice(invoice, number, onPay);
  }

  renderPending(invoice, number) {
    return (
      <tr className="warning">
        <td>{zeroPad(number)}</td>
        <td/>
        <td/>
        <td/>
        <td/>
        <td>
          <button className="btn btn-default" disabled>Pay</button>
        </td>
      </tr>
    );
  }

  renderInvoice(invoice, number, onPay) {
    const status = invoice.status;
    const payDisabled = (status === 'Paid');
    const statusStyle = (status === 'Paid') ? 'success' : '';
    return (
      <tr>
        <td>{zeroPad(number)}</td>
        <td>
          {invoice.customerName}
          {' '}
          &lt;<a href={`mailto:${invoice.customerEmail}`}>{invoice.customerEmail}</a>&gt;
        </td>
        <td>{invoice.dueDate}</td>
        <td align="right">{formatter.format(invoice.total)}</td>
        <td className={statusStyle}>{status}</td>
        <td>
          <button
            className="btn btn-default"
            disabled={payDisabled}
            onClick={() => onPay()}
          >Pay</button>
        </td>
      </tr>
    );
  }
}
