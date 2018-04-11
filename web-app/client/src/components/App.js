import React, {PureComponent} from 'react';
import {initialState} from '../model';
import InvoicesTable from './InvoicesTable';
import InvoiceForm from './InvoiceForm';
import Message from './Message';
import {
  createInvoice,
  draftEdited,
  lineItemAdded,
  lineItemEdited,
  lineItemRemoved,
  payInvoice
} from '../actions';

class App extends PureComponent {
  render() {
    const state = this.props.state || initialState;
    const dispatch = this.props.dispatch;
    return (
      <div className="container">
        <h2>List</h2>
        <InvoicesTable invoices={state.invoices} onPay={id => dispatch(payInvoice(id))}/>
        <h2>New</h2>
        <InvoiceForm
          invoice={state.draft}
          onChange={draft => dispatch(draftEdited(draft))}
          onChangeLineItem={(index, lineItem) => dispatch(lineItemEdited(index, lineItem))}
          onRemoveLineItem={index => dispatch(lineItemRemoved(index))}
          onAddLineItem={() => dispatch(lineItemAdded)}
        />
        <p>
          <button onClick={() => dispatch(createInvoice(state.draft))}>Create</button>
        </p>
        <Message message={state.message}/>
      </div>
    );
  }
}

export default App;
