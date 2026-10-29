import API from "../api/api";
import { ENDPOINTS } from "../api/endpoints";
 
// Uses localStorage accountnumber like your Statements.jsx
const accountNumber = () => localStorage.getItem("accountnumber");
 
// Same filtering logic you use in Statements.jsx
export function filterTransactionsByAccount(txs, accNumber) {
  const acc = String(accNumber || "").toLowerCase();
 
  return Array.isArray(txs)
    ? txs.filter((tx) => {
        const candidates = [
          String(tx?.account?.accountnumber || "").toLowerCase(),
          String(tx?.accountnumber || "").toLowerCase(),
          String(tx?.account_number || "").toLowerCase(),
          String(tx?.fromnumber || "").toLowerCase(),
          String(tx?.tonumber || "").toLowerCase(),
          String(tx?.from_number || "").toLowerCase(),
          String(tx?.to_number || "").toLowerCase(),
          String(tx?.fromAccount?.accountnumber || "").toLowerCase(),
          String(tx?.toAccount?.accountnumber || "").toLowerCase(),
        ];
        return candidates.some((v) => v === acc);
      })
    : [];
}
 
export async function fetchStatementForChat() {
  const accNo = accountNumber();
  if (!accNo) throw new Error("accountnumber missing in localStorage");
 
  //  same endpoint you use now: /api/transactions/all
  const res = await API.get(ENDPOINTS.TRANSACTIONS.ALL);
  const allTx = Array.isArray(res.data) ? res.data : [];
 
  const myTx = filterTransactionsByAccount(allTx, accNo);
 
  // Extract account + customer details from tx.account.customer like your file
  let accountDetails = null;
  let customerDetails = null;
 
  if (myTx.length > 0) {
    const first = myTx[0];
    if (first?.account) accountDetails = first.account;
    if (first?.account?.customer) customerDetails = first.account.customer;
  }
 
  return { transactions: myTx, accountDetails, customerDetails, accountNumber: accNo };
}