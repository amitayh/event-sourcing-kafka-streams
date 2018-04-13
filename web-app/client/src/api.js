import {socket} from './socket';

const toJson = res => res.json();

export const fetchInvoices = () => {
  return fetch('/api/invoices').then(toJson);
};

export const createInvoice = (invoiceId, draft) => {
  const options = {
    method: 'POST',
    headers: {'content-type': 'application/json'},
    body: JSON.stringify({socketId: socket.id, invoiceId, draft})
  };
  return fetch('/api/create', options).then(toJson);
};

export const payInvoice = invoiceId => {
  const options = {
    method: 'POST',
    headers: {'content-type': 'application/json'},
    body: JSON.stringify({socketId: socket.id, invoiceId})
  };
  return fetch('/api/pay', options).then(toJson);
};
