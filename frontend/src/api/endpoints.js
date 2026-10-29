export const ENDPOINTS = {
  TRANSACTIONS: {
    ALL: "/api/transactions/all",
    BY_ID: (id) => `/api/transactions/${id}`,
    DEPOSIT: "/api/transactions/deposit",
    WITHDRAW: "/api/transactions/withdraw",
    TRANSFER: "/api/transactions/transfer",
  },

  ACCOUNTS: {
    ALL: "/accounts",
    BY_ID: (id) => `/accounts/${id}`,
    BY_CUSTOMER: (id) => `/accounts/customer/${id}`,
    OPEN: "/accounts",
    ACTIVATE: (id) => `/accounts/${id}/activate`,
    DEACTIVATE: (id) => `/accounts/${id}/deactivate`,
  },

  CUSTOMERS: {
    ALL: "/api/customers/all",
    BY_ID: (id) => `/api/customers/${id}`,
    UPDATE: (id) => `/api/customers/update/${id}`,
    KYC: (id) => `/api/customers/${id}/kyc`,
  },

  DEPOSITS: {
    FIXED: (accountId) =>
      `/deposits/fixed/${accountId}`,

    RECURRING: (accountId) =>
      `/deposits/recurring/${accountId}`,

    FIXED_BY_ACCOUNT: (accountId) =>
      `/deposits/fixed/account/${accountId}`,

    RECURRING_BY_ACCOUNT: (accountId) =>
      `/deposits/recurring/account/${accountId}`,
  },
};