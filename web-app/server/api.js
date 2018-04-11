import express from 'express';
import bodyParser from 'body-parser';
import {getInvoices} from './projection';
import {createInvoice, payInvoice} from './kafka';
import {commands} from './socket';

const app = express();

app.get('/invoices', async (req, res) => {
  res.json(await getInvoices());
});

app.post('/create', bodyParser.json(), async (req, res) => {
  const body = req.body;
  const result = await createInvoice(body.draft);
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
