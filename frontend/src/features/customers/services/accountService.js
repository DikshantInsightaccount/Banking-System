import { http } from "../../../api/http.js";
import { ENDPOINTS } from "../../../api/endpoints.js";

export const accountService = {
  //  Get all accounts
  getAll: async () =>
    (await http.get(ENDPOINTS.ACCOUNTS.ALL)).data,

  //  Get account by ID
  getById: async (accountId) =>
    (await http.get(ENDPOINTS.ACCOUNTS.BY_ID(accountId))).data,

  //  Get accounts by customer
  getByCustomer: async (customerId) =>
    (await http.get(ENDPOINTS.ACCOUNTS.BY_CUSTOMER(customerId))).data,

  //  Open new account

  open: async (payload) =>
    (await http.post("/accounts", payload)).data,


  //  Activate account (matches @PutMapping("/{accountId}/activate"))
  activate: async (accountId) =>
    (await http.put(ENDPOINTS.ACCOUNTS.ACTIVATE(accountId))).data,

  //  Deactivate account (matches @PutMapping("/{accountId}/deactivate"))
  deactivate: async (accountId) =>
    (await http.put(ENDPOINTS.ACCOUNTS.DEACTIVATE(accountId))).data,
};