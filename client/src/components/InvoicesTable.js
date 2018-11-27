import React, {PureComponent} from 'react';

const formatter = new Intl.NumberFormat(['en-US'], {style: 'currency', currency: 'USD'});

const zeroPad = number => number.toString().padStart(6, '0');

export default class InvoicesTable extends PureComponent {
  render() {
    const {invoices, onPay, onDelete} = this.props;
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
              onPay={() => onPay(invoice.id)}
              onDelete={() => onDelete(invoice.id)}/>;
          })}
        </tbody>
      </table>
    );
  }
}

class InvoicesRow extends PureComponent {
  render() {
    const {invoice, number, onPay, onDelete} = this.props;
    if (invoice.status === 'Deleted') {
      return null;
    } else if (invoice.pending) {
      return this.renderPending(invoice, number);
    } else {
      return this.renderInvoice(invoice, number, onPay, onDelete);
    }
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
          {' '}
          <button className="btn btn-default" disabled>Delete</button>
        </td>
      </tr>
    );
  }

  renderInvoice(invoice, number, onPay, onDelete) {
    const status = invoice.status;
    const isPaid = (status === 'Paid');
    const isDeleting = !!invoice.deleting;
    const statusStyle = isPaid ? 'success' : '';
    const rowStyle = isDeleting ? 'danger' : '';
    return (
      <tr className={rowStyle}>
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
            disabled={isPaid || isDeleting}
            onClick={() => onPay()}>Pay</button>
          {' '}
          <button
            className="btn btn-danger"
            disabled={isDeleting}
            onClick={() => onDelete()}
            >Delete</button>
        </td>
      </tr>
    );
  }
}
