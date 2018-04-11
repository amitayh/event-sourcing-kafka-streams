import {db} from './db';
import {reader} from './reader';

const read = reader(db);

export const getInvoices = () => read();
