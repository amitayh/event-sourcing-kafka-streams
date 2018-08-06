import {DynamoDB} from 'aws-sdk';

const db = new DynamoDB({region: 'eu-west-1'});

const params = {
  ExpressionAttributeValues: {':v1': {S: '1'}},
  KeyConditionExpression: 'tenant_id = :v1',
  TableName: 'invoices'
};

const transform = response => {
  return response.Items.map(item => {
    return {
      id: item.invoice_id.S,
      customerName: item.customer_name.S,
      customerEmail: item.customer_email.S,
      issueDate: item.invoice_issue_date.S,
      dueDate: item.invoice_due_date.S,
      total: Number(item.invoice_total.N),
      status: item.invoice_status.S
    };
  });
};

export const getInvoices = () => {
  return new Promise((resolve, reject) => {
    db.query(params, (err, data) => {
      if (err) {
        reject(err);
      } else {
        resolve(transform(data));
      }
    });
  });
};
