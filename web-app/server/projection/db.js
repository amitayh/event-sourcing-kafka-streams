import sqlite3 from 'sqlite3';

const verbose = sqlite3.verbose();
const file = process.env.DB;

export const db = new verbose.Database(file);
