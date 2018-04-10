import Chance from 'chance';

const chance = new Chance();

const randomLineItem = () => ({
  description: chance.sentence({words: 4}),
  quantity: chance.integer({min: 1, max: 20}),
  price: chance.integer({min: 1, max: 100})
});

const randomDraft = () => ({
  customer: {
    name: chance.name(),
    email: chance.email()
  },
  issueDate: '2018-04-01',
  dueDate: '2018-05-01',
  lineItems: chance.n(randomLineItem, chance.d6())
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
  customerName: '⏳',
  customerEmail: '⏳',
  total: '⏳'
});

export const initialState = {
  loading: false,
  invoices: [],
  draft: randomDraft(),
  message: null,
};
