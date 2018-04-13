const toRecord = row => ({
  id: row.id,
  customerName: row.customer_name,
  customerEmail: row.customer_email,
  issueDate: row.issue_date,
  dueDate: row.due_date,
  total: row.total,
  status: row.status
});

export const reader = db => () => new Promise((resolve, reject) => {
  db.all('SELECT * FROM invoices ORDER BY ROWID DESC', (error, results) => {
    if (error) {
      reject(error);
    } else {
      resolve(results.map(toRecord));
    }
  })
});
