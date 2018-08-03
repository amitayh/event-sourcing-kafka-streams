const toRecord = row => ({
  id: row.id,
  customerName: row.customer_name,
  customerEmail: row.customer_email,
  issueDate: row.issue_date,
  dueDate: row.due_date,
  total: row.total,
  status: row.status
});

const sql = `
  SELECT *
  FROM invoices
  WHERE status != 'Deleted'
  ORDER BY ROWID DESC
`;

export const reader = db => () => new Promise((resolve, reject) => {
  db.all(sql, (error, results) => {
    if (error) {
      reject(error);
    } else {
      resolve(results.map(toRecord));
    }
  })
});
