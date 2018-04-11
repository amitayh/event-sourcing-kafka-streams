import * as api from './api';
import {emptyDraft, emptyLineItem, pendingInvoice} from './model';

export const fetchInvoicesSuccess = invoices => ({
  type: 'FETCH_INVOICES_SUCCESS',
  nextState: state => ({...state, loading: false, invoices})
});

export const fetchInvoicesFailure = {
  type: 'FETCH_INVOICES_FAILURE',
  nextState: state => ({...state, loading: false, invoices: []})
};

export const fetchInvoices = {
  type: 'FETCH_INVOICES',
  nextState: state => ({...state, loading: true, invoices: []}),
  runEffect: dispatch => {
    api.fetchInvoices()
      .then(invoices => dispatch(fetchInvoicesSuccess(invoices)))
      .catch(() => dispatch(fetchInvoicesFailure))
  }
};

export const invoiceUpdated = updated => ({
  type: 'INVOICE_UPDATED',
  nextState: state => {
    const invoices = state.invoices.map(invoice => {
      return (invoice.id === updated.id) ? updated : invoice;
    });
    return {...state, invoices};
  }
});

export const draftEdited = draft => ({
  type: 'DRAFT_EDITED',
  nextState: state => ({...state, draft})
});

export const lineItemEdited = (index, lineItem) => ({
  type: 'LINE_ITEM_EDITED',
  nextState: state => {
    const draft = state.draft;
    const lineItems = draft.lineItems.map((current, currentIndex) => {
      return (currentIndex === index) ? lineItem : current;
    });
    return {...state, draft: {...draft, lineItems}};
  }
});

export const lineItemRemoved = index => ({
  type: 'LINE_ITEM_REMOVED',
  nextState: state => {
    const draft = state.draft;
    const lineItems = draft.lineItems.filter((_, currentIndex) => {
      return (currentIndex !== index);
    });
    return {...state, draft: {...draft, lineItems}};
  }
});

export const lineItemAdded = {
  type: 'LINE_ITEM_ADDED',
  nextState: state => {
    const draft = state.draft;
    const lineItems = [...draft.lineItems, emptyLineItem];
    return {...state, draft: {...draft, lineItems}};
  }
};

export const clearMessage = {
  type: 'CLEAR_MESSAGE',
  nextState: state => ({...state, message: null})
};

export const commandExecutionStarted = command => ({
  type: 'COMMAND_EXECUTION_STARTED',
  nextState: state => {
    const message = 'Loading...';
    const invoices = [...state.invoices, pendingInvoice(command.invoiceId)];
    return {...state, invoices, message}
  }
});

export const commandExecutionFinished = commandId => ({
  type: 'COMMAND_EXECUTION_FINISHED',
  nextState: state => ({...state, message: `Success! ${commandId}`}),
  runEffect: dispatch => setTimeout(() => dispatch(clearMessage), 3000)
});

export const createInvoice = draft => ({
  type: 'CREATE_INVOICE',
  nextState: state => ({...state, draft: emptyDraft}),
  runEffect: dispatch => {
    api.createInvoice(draft)
      .then(command => dispatch(commandExecutionStarted(command)));
  }
});

export const payInvoice = id => ({
  type: 'PAY_INVOICE',
  nextState: state => state,
  runEffect: dispatch => {
    api.payInvoice(id)
      // .then(command => dispatch(commandExecutionStarted(command)));
  }
});
