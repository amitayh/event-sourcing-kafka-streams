import express from 'express';
import bodyParser from 'body-parser';
import {getInvoices} from './projection';
import {executeCommand} from './kafka';
import {commands} from './socket';

const app = express();

const createInvoice = (invoiceId, draft) => {
  return executeCommand(invoiceId, {
    CreateInvoice: {
      customerName: draft.customer.name,
      customerEmail: draft.customer.email,
      issueDate: draft.issueDate,
      dueDate: draft.dueDate,
      lineItems: draft.lineItems
    }
  });
};

const payInvoice = invoiceId => {
  return executeCommand(invoiceId, {PayInvoice: {}});
};

app.get('/invoices', async (req, res) => {
  res.json(await getInvoices());
});

app.post('/create', bodyParser.json(), async (req, res) => {
  const body = req.body;
  const result = await createInvoice(body.invoiceId, body.draft);
  commands[result.commandId] = body.socketId;
  res.json(result);
});

app.post('/pay', bodyParser.json(), async (req, res) => {
  const body = req.body;
  const result = await payInvoice(body.invoiceId);
  commands[result.commandId] = body.socketId;
  res.json(result);
});

export default app;
