import mysql from 'mysql';

export const conn = mysql.createConnection({
  host: 'localhost',
  user: 'root',
  password: '',
  database: 'invoices'
});
