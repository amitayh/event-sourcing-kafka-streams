import {originId} from './origin';
import uuidv4 from 'uuid/v4';

const baseUrl = 'http://localhost:8080/api';
const toJson = res => res.json();

const execute = (invoiceId, payload) => {
  const options = {
    method: 'POST',
    headers: {'content-type': 'application/json'},
    body: JSON.stringify({
      originId,
      commandId: uuidv4(),
      expectedVersion: null,
      payload
    })
  };
  return fetch(`${baseUrl}/execute/${invoiceId}`, options).then(toJson);
};

export const fetchInvoices = () => {
  return fetch(`${baseUrl}/invoices`).then(toJson);
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

export const removeLineItem = (invoiceId, index) => {
  return execute(invoiceId, {RemoveLineItem: {index}});
};
