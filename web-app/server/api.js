import express from 'express';
import bodyParser from 'body-parser';
//import {getInvoices} from './projection';
import {executeCommand} from './kafka';
import {setSocketId} from './socket';

const app = express();

app.get('/invoices', (req, res) => {
  //res.json(await getInvoices());
  res.json([{
    id: '917f7417-2581-4bff-b222-5d4e2cce6588',
    customerName: 'Amitay Horwitz',
    customerEmail: 'amitayh@gmail.com',
    issueDate: '2018-08-05',
    dueDate: '2018-09-04',
    total: 12.34,
    status: 'New'
  }]);
});

app.post('/execute', bodyParser.json(), async (req, res) => {
  const body = req.body;
  const result = await executeCommand(body.invoiceId, body.command);
  setSocketId(result.commandId, body.socketId);
  res.json(result);
});

export default app;
