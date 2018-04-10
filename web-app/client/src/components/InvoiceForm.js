import React, {PureComponent} from 'react';

export default class InvoiceForm extends PureComponent {
  render() {
    const {invoice, onChange, onAddLineItem} = this.props;
    const {customer, lineItems} = invoice;
    return (
      <table border="1">
        <tbody>
        <tr>
          <th>Customer</th>
          <td>
            <input
              placeholder="Name"
              value={customer.name}
              onChange={e => {
                const updatedCustomer = {name: e.target.value, email: customer.email};
                onChange({...invoice, customer: updatedCustomer})
              }}
            />
            <input
              type="email"
              placeholder="Email"
              value={customer.email}
              onChange={e => {
                const updatedCustomer = {name: customer.name, email: e.target.value};
                onChange({...invoice, customer: updatedCustomer})
              }}
            />
          </td>
        </tr>
        <tr>
          <th>Issue date</th>
          <td>
            <input
              type="date"
              value={invoice.issueDate}
              onChange={e => onChange({...invoice, issueDate: e.target.value})}
            />
          </td>
        </tr>
        <tr>
          <th>Due date</th>
          <td>
            <input
              type="date"
              value={invoice.dueDate}
              onChange={e => onChange({...invoice, dueDate: e.target.value})}
            />
          </td>
        </tr>
        {lineItems.map((lineItem, index) => this.renderLineItem(lineItem, index))}
        <tr>
          <td/>
          <td>
            <button onClick={() => onAddLineItem()}>Add</button>
          </td>
        </tr>
        </tbody>
      </table>
    );
  }

  renderLineItem(lineItem, index) {
    const {onChangeLineItem, onRemoveLineItem} = this.props;
    return (
      <tr key={`line-item-${index}`}>
        <th>Line item #{index + 1}</th>
        <td>
          <LineItem
            lineItem={lineItem}
            onChange={updated => onChangeLineItem(index, updated)}
            onRemove={() => onRemoveLineItem(index)}
          />
        </td>
      </tr>
    );
  }
}

class LineItem extends PureComponent {
  render() {
    const {lineItem, onChange, onRemove} = this.props;
    return (
      <div>
        <input
          placeholder="Description"
          value={lineItem.description}
          onChange={e => onChange({...lineItem, description: e.target.value})}
        />
        <input
          type="number"
          placeholder="Price"
          value={lineItem.price}
          onChange={e => onChange({...lineItem, price: Number(e.target.value)})}
        />
        <input
          type="number"
          placeholder="Qty."
          value={lineItem.quantity}
          onChange={e => onChange({...lineItem, quantity: Number(e.target.value)})}
        />
        <button onClick={() => onRemove()}>Remove</button>
      </div>
    );
  }
}
