import express from 'express';
import bodyParser from 'body-parser';
import {getInvoices} from './projection';
import {executeCommand} from './kafka';
import {setSocketId} from './socket';

const app = express();

app.get('/invoices', (req, res, next) => {
  getInvoices()
    .then(invoices => res.json(invoices))
    .catch(err => next(err));
});

app.post('/execute', bodyParser.json(), (req, res, next) => {
  const body = req.body;
  executeCommand(body.invoiceId, body.command)
    .then(result => {
      setSocketId(result.commandId, body.socketId);
      res.json(result);
    })
    .catch(err => next(err));
});

export default app;
