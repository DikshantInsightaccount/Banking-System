import { formatMoney } from "../../../utils/formatMoney.js";

export default function TransactionSummary({ totalCount, totalAmount }) {
  return (
    <div className="bg-white border rounded p-3">
      <div className="fw-semibold mb-2">Summary</div>
      <div className="d-flex justify-content-between">
        <span className="text-muted">Transactions</span>
        <span className="fw-semibold">{totalCount ?? 0}</span>
      </div>
      <div className="d-flex justify-content-between">
        <span className="text-muted">Total Amount (approx.)</span>
        <span className="fw-semibold">{formatMoney(totalAmount ?? 0)}</span>
      </div>
    </div>
  );
}