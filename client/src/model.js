import Chance from 'chance';

const chance = new Chance();

const randomLineItem = () => ({
  description: chance.sentence({words: 4}),
  quantity: chance.integer({min: 1, max: 5}),
  price: chance.integer({min: 1, max: 100})
});

const plusMonths = (date, monthsToAdd) => new Date(
  date.getFullYear(),
  date.getMonth() + monthsToAdd,
  date.getDate()
);

const formatDate = date => date.toISOString().substr(0, 10);

export const randomDraft = () => ({
  customer: {
    name: chance.name(),
    email: chance.email()
  },
  issueDate: formatDate(new Date()),
  dueDate: formatDate(plusMonths(new Date(), 1)),
  lineItems: chance.n(randomLineItem, chance.integer({min: 2, max: 5}))
});

export const emptyLineItem = {
  description: '',
  quantity: 0,
  price: 0
};

export const emptyDraft = {
  customer: {
    name: '',
    email: ''
  },
  issueDate: '2018-04-01',
  dueDate: '2018-05-01',
  lineItems: []
};

export const tempId = '__TEMP__';

export const pendingInvoice = {id: tempId, pending: true};

export const initialState = {
  page: 'list',
  invoices: [],
  draft: null,
  message: null,
};
