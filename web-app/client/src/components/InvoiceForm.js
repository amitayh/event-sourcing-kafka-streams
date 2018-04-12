import React, {PureComponent} from 'react';

export default class InvoiceForm extends PureComponent {
  render() {
    const {invoice, onChange, onAddLineItem, onChangeLineItem, onRemoveLineItem} = this.props;
    const {customer, lineItems} = invoice;
    return (
      <div>
        <div className="row">
          <div className="col-md-3">
            <label htmlFor="customer-name">Customer name</label>
            <input
              type="text"
              id="customer-name"
              className="form-control"
              value={customer.name}
              onChange={e => {
                const updatedCustomer = {name: e.target.value, email: customer.email};
                onChange({...invoice, customer: updatedCustomer})
              }}
              required
            />
          </div>
          <div className="col-md-3 mb-3">
            <label htmlFor="customer-email">Customer email</label>
            <input
              type="email"
              id="customer-email"
              className="form-control"
              value={customer.email}
              onChange={e => {
                const updatedCustomer = {name: customer.name, email: e.target.value};
                onChange({...invoice, customer: updatedCustomer})
              }}
              required
            />
          </div>
          <div className="col-md-3">
            <label htmlFor="issue-date">Issue date</label>
            <input
              type="date"
              id="issue-date"
              className="form-control"
              value={invoice.issueDate}
              onChange={e => onChange({...invoice, issueDate: e.target.value})}
              required
            />
          </div>
          <div className="col-md-3">
            <label htmlFor="due-date">Due date</label>
            <input
              type="date"
              id="due-date"
              className="form-control"
              value={invoice.dueDate}
              onChange={e => onChange({...invoice, dueDate: e.target.value})}
              required
            />
          </div>
        </div>
        <table className="table table-bordered">
          <caption>Line items</caption>
          <thead>
          <tr>
            <th>#</th>
            <th width="60%">Description</th>
            <th width="15%">Price</th>
            <th width="15%">Qty.</th>
            <th>Actions</th>
          </tr>
          </thead>
          <tbody>
          {lineItems.map((lineItem, index) => {
            return <LineItem
              key={`line-item-${index}`}
              number={index + 1}
              lineItem={lineItem}
              onChange={updated => onChangeLineItem(index, updated)}
              onRemove={() => onRemoveLineItem(index)}
            />
          })}
          <tr>
            <td colSpan="4"/>
            <td>
              <button
                className="btn btn-success"
                onClick={() => onAddLineItem()}>Add
              </button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    );
  }
}

class LineItem extends PureComponent {
  render() {
    const {number, lineItem, onChange, onRemove} = this.props;
    return (
      <tr>
        <td>{number}</td>
        <td>
          <input
            className="form-control"
            value={lineItem.description}
            onChange={e => onChange({...lineItem, description: e.target.value})}
          />
        </td>
        <td>
          <input
            type="number"
            className="form-control"
            value={lineItem.price}
            onChange={e => onChange({...lineItem, price: Number(e.target.value)})}
          />
        </td>
        <td>
          <input
            type="number"
            className="form-control"
            placeholder="Qty."
            value={lineItem.quantity}
            onChange={e => onChange({...lineItem, quantity: Number(e.target.value)})}
          />
        </td>
        <td>
          <button
            className="btn btn-danger"
            onClick={() => onRemove()}>Remove</button>
        </td>
      </tr>
    );
  }
}
