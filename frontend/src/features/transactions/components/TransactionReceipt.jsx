import { formatMoney } from "../../../utils/formatMoney.js";
import { formatDate } from "../../../utils/formatDate.js";
import TransactionStatusBadge from "./TransactionStatusBadge.jsx";

export default function TransactionReceipt({ tx }) {
  if (!tx) return null;

  const customer = tx.account?.customer;
  const account = tx.account;

  return (
    <div className="bg-white border rounded p-3">
      <div className="d-flex justify-content-between align-items-center">
        <div className="fw-semibold">Transaction Receipt</div>
        <TransactionStatusBadge value={tx.status} />
      </div>
      <hr />

      {/* Customer Section */}
      <div className="mb-3">
        <div className="text-muted small fw-semibold mb-2">Customer Details</div>
        <div className="row g-2">
          <div className="col-md-6">
            <div className="text-muted small">Customer Name</div>
            <div>{customer?.fullname || "-"}</div>
          </div>
          <div className="col-md-6">
            <div className="text-muted small">Email</div>
            <div>{customer?.email || "-"}</div>
          </div>
          <div className="col-md-6">
            <div className="text-muted small">Phone</div>
            <div>{customer?.phone || "-"}</div>
          </div>
          <div className="col-md-6">
            <div className="text-muted small">Customer ID</div>
            <div>{customer?.customerid || "-"}</div>
          </div>
        </div>
      </div>

      <hr />

      {/* Account Section */}
      <div className="mb-3">
        <div className="text-muted small fw-semibold mb-2">Account Details</div>
        <div className="row g-2">
          <div className="col-md-6">
            <div className="text-muted small">Account Number</div>
            <div className="fw-semibold">{account?.accountnumber || "-"}</div>
          </div>
          <div className="col-md-6">
            <div className="text-muted small">Account Type</div>
            <div>{account?.accounttype || "-"}</div>
          </div>
          <div className="col-md-6">
            <div className="text-muted small">Balance</div>
            <div className="fw-semibold">{formatMoney(account?.balance)}</div>
          </div>
          <div className="col-md-6">
            <div className="text-muted small">Account Status</div>
            <div>{account?.status || "-"}</div>
          </div>
        </div>
      </div>

      <hr />

      {/* Transaction Section */}
      <div>
        <div className="text-muted small fw-semibold mb-2">Transaction Details</div>
        <div className="row g-2">
          <div className="col-md-4">
            <div className="text-muted small">Transaction ID</div>
            <div>{tx.txnid ?? "-"}</div>
          </div>
          <div className="col-md-4">
            <div className="text-muted small">Type</div>
            <div className="fw-semibold">{tx.txntype ?? "-"}</div>
          </div>
          <div className="col-md-4">
            <div className="text-muted small">Date & Time</div>
            <div>{formatDate(tx.txntimestamp)}</div>
          </div>

          <div className="col-md-6">
            <div className="text-muted small">Amount</div>
            <div className="fw-semibold text-success">{formatMoney(tx.amount)}</div>
          </div>
          <div className="col-md-6">
            <div className="text-muted small">Status</div>
            <div className="fw-semibold">
              <span className={`badge ${tx.status === 'SUCCESS' ? 'bg-success' : tx.status === 'PENDING' ? 'bg-warning' : 'bg-danger'}`}>
                {tx.status}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}