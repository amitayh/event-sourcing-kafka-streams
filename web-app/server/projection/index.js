import {conn} from './connection';
import {reader} from './reader';

conn.connect();
const read = reader(conn);

export const getInvoices = () => read();
