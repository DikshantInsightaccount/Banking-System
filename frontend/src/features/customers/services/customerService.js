import { http } from "../../../api/http.js";
import { ENDPOINTS } from "../../../api/endpoints.js";
export const customerService = {
  getAll: async () => {
    const res = await http.get(ENDPOINTS.CUSTOMERS.ALL);
    return res.data;
  },

  getById: async (customerId) => {
    const res = await http.get(
      ENDPOINTS.CUSTOMERS.BY_ID(customerId)
    );
    return res.data;
  },

  update: async (customerId, payload) => {
    const res = await http.put(
      ENDPOINTS.CUSTOMERS.UPDATE(customerId),
      payload
    );
    return res.data;
  },

  updateKyc: async (customerId, status) => {
    const res = await http.put(
      ENDPOINTS.CUSTOMERS.KYC(customerId),
      null,
      { params: { status } }
    );
    return res.data;
  }
};