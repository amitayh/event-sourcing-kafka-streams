import {DynamoDB} from 'aws-sdk';

const db = new DynamoDB({region: 'eu-west-1'});

const params = {
  ExpressionAttributeValues: {':v1': {S: '1'}},
  KeyConditionExpression: 'tenant_id = :v1',
  TableName: 'invoices'
};

export const getInvoices = () => {
  return new Promise((resolve, reject) => {
    db.query(params, (err, data) => {
      if (err) {
        reject(err);
      } else {
        resolve(data);
      }
    });
  });
};
