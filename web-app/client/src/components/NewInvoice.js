import React, {PureComponent} from 'react';
import InvoiceForm from './InvoiceForm';
import {
  createInvoice,
  draftEdited,
  lineItemAdded,
  lineItemEdited,
  lineItemRemoved, listPage
} from '../actions';

class NewInvoices extends PureComponent {
  render() {
    const {draft, dispatch} = this.props;
    return (
      <div>
        <h2>New</h2>
        <InvoiceForm
          invoice={draft}
          onChange={updated => dispatch(draftEdited(updated))}
          onChangeLineItem={(index, lineItem) => dispatch(lineItemEdited(index, lineItem))}
          onRemoveLineItem={index => dispatch(lineItemRemoved(index))}
          onAddLineItem={() => dispatch(lineItemAdded)}
        />
        <p>
          <button
            className="btn btn-primary"
            onClick={() => dispatch(createInvoice(draft))}
          >Create</button>
          {' '}
          <button
            className="btn btn-default"
            onClick={() => dispatch(listPage)}
          >Back to list</button>
        </p>
      </div>
    );
  }
}

export default NewInvoices;
