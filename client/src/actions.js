import debounce from 'debounce';
import uuidv4 from 'uuid/v4';
import * as api from './api';
import {emptyLineItem, pendingInvoice, randomDraft, tempId} from './model';

export const newInvoicePage = {
  type: 'NEW_INVOICE_PAGE',
  nextState: state => ({...state, page: 'new', draft: randomDraft()})
};

export const listPage = {
  type: 'LIST_PAGE',
  nextState: state => ({...state, page: 'list', draft: null})
};

export const clearMessage = {
  type: 'CLEAR_MESSAGE',
  nextState: state => ({...state, message: null})
};

const clearMessageDelayed =
  debounce(dispatch => dispatch(clearMessage), 3000);

export const showMessage = message => ({
  type: 'SHOW_MESSAGE',
  nextState: state => ({...state, message}),
  runEffect: dispatch => clearMessageDelayed(dispatch)
});

export const fetchInvoicesSuccess = invoices => ({
  type: 'FETCH_INVOICES_SUCCESS',
  nextState: state => ({...state, invoices})
});

export const fetchInvoices = {
  type: 'FETCH_INVOICES',
  nextState: state => ({...state, invoices: []}),
  runEffect: dispatch => {
    api.fetchInvoices()
      .then(invoices => dispatch(fetchInvoicesSuccess(invoices)))
  }
};

const invoiceInList = (invoices, needle) => {
  const result = invoices.find(invoice => invoice.id === needle.id);
  return (result !== undefined);
};

const updateList = (invoices, updated) => {
  return invoices.map(invoice => {
    return (invoice.id === updated.id) ? updated : invoice;
  });
};

const addToList = (invoices, invoice) => [invoice, ...invoices];

export const invoiceUpdated = invoice => ({
  type: 'INVOICE_UPDATED',
  nextState: state => {
    const invoices = state.invoices;
    const updated = invoiceInList(invoices, invoice) ?
      updateList(invoices, invoice) :
      addToList(invoices, invoice);
    return {...state, invoices: updated};
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

export const emptyInvoiceAdded = invoiceId => ({
  type: 'EMPTY_INVOICE_ADDED',
  nextState: state => {
    const invoices = state.invoices.map(invoice => {
      return (invoice.id === tempId) ?
        {...invoice, id: invoiceId} :
        invoice
    });
    return {...state, invoices}
  }
});

export const commandExecutionStarted =
  showMessage('Loading...');

export const commandExecutionSucceeded = commandId =>
  showMessage(`Command ${commandId} succeeded!`);

export const commandExecutionFailed = (commandId, cause) =>
  showMessage(`Command ${commandId} failed!\n${cause}`);

export const createInvoice = draft => ({
  type: 'CREATE_INVOICE',
  nextState: state => {
    const invoices = [pendingInvoice, ...state.invoices];
    return {...state, page: 'list', draft: null, invoices};
  },
  runEffect: dispatch => {
    const invoiceId = uuidv4();
    dispatch(commandExecutionStarted);
    dispatch(emptyInvoiceAdded(invoiceId));
    api.createInvoice(invoiceId, draft);
  }
});

export const payInvoice = id => ({
  type: 'PAY_INVOICE',
  nextState: state => state,
  runEffect: dispatch => {
    dispatch(commandExecutionStarted);
    api.payInvoice(id);
  }
});

export const deleteInvoice = id => ({
  type: 'DELETE_INVOICE',
  nextState: state => {
    const invoices = state.invoices.map(invoice => {
      return (invoice.id === id) ?
        {...invoice, deleting: true} :
        invoice
    });
    return {...state, invoices}
  },
  runEffect: dispatch => {
    dispatch(commandExecutionStarted);
    api.deleteInvoice(id);
  }
});

export const removeLineItem = (invoiceId, index) => ({
  type: 'REMOVE_LINE_ITEM',
  nextState: state => state,
  runEffect: dispatch => {
    dispatch(commandExecutionStarted);
    api.removeLineItem(invoiceId, index);
  }
});
