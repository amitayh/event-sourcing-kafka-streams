import express from 'express';
import bodyParser from 'body-parser';
import {getInvoices} from './projection';
import {executeCommand} from './kafka';
import {setSocketId} from './socket';

const app = express();

app.get('/invoices', async (req, res) => {
  res.json(await getInvoices());
});

app.post('/execute', bodyParser.json(), async (req, res) => {
  const body = req.body;
  const result = await executeCommand(body.invoiceId, body.command);
  setSocketId(result.commandId, body.socketId);
  res.json(result);
});

export default app;
