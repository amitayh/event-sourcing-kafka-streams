import Chance from 'chance';

const chance = new Chance();

const randomLineItem = () => ({
  description: chance.sentence({words: 4}),
  quantity: chance.integer({min: 1, max: 5}),
  price: chance.integer({min: 1, max: 100})
});

export const randomDraft = () => ({
  customer: {
    name: chance.name(),
    email: chance.email()
  },
  issueDate: '2018-04-01',
  dueDate: '2018-05-01',
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

export const pendingInvoice = id => ({
  id,
  customerName: '',
  customerEmail: '',
  total: ''
});

export const initialState = {
  page: 'list',
  loading: false,
  invoices: [],
  draft: randomDraft(),
  message: null,
};
