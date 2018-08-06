import {DynamoDB} from 'aws-sdk';

const db = new DynamoDB();

const params = {
  Key: {
    'status': {
      S: 'New'
    },
  },
  TableName: 'invoices'
};

export const getInvoices = () => {
  return new Promise((resolve, reject) => {
    db.getItem(params, (err, data) => {
      if (err) {
        reject(err)
      } else {
        resolve(data);
      }
    });
  });
};
