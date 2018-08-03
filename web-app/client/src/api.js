import {socket} from './socket';

const toJson = res => res.json();

const execute = (invoiceId, command) => {
  const options = {
    method: 'POST',
    headers: {'content-type': 'application/json'},
    body: JSON.stringify({socketId: socket.id, invoiceId, command})
  };
  return fetch('/api/execute', options).then(toJson);
};

export const fetchInvoices = () => {
  return fetch('/api/invoices').then(toJson);
};

export const createInvoice = (invoiceId, draft) => {
  return execute(invoiceId, {
    CreateInvoice: {
      customerName: draft.customer.name,
      customerEmail: draft.customer.email,
      issueDate: draft.issueDate,
      dueDate: draft.dueDate,
      lineItems: draft.lineItems
    }
  });
};

export const payInvoice = invoiceId => {
  return execute(invoiceId, {PayInvoice: {}});
};

export const deleteInvoice = invoiceId => {
  return execute(invoiceId, {DeleteInvoice: {}});
};

export const removeLineItem = (invoiceId, lineItemId) => {
  return execute(invoiceId, {RemoveLineItem: {lineItemId}});
};
