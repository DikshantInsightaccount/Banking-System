import { http } from "../../../api/http.js";
import { ENDPOINTS } from "../../../api/endpoints.js";

export const transactionService = {
  getAll: async () =>
    (await http.get(ENDPOINTS.TRANSACTIONS.ALL)).data,

  getById: async (id) =>
    (await http.get(ENDPOINTS.TRANSACTIONS.BY_ID(id))).data,

  deposit: async (payload) =>
    (await http.post(ENDPOINTS.TRANSACTIONS.DEPOSIT, payload)).data,

  withdraw: async (payload) =>
    (await http.post(ENDPOINTS.TRANSACTIONS.WITHDRAW, payload)).data,

  transfer: async (payload) =>
    (await http.post(ENDPOINTS.TRANSACTIONS.TRANSFER, payload)).data,
};