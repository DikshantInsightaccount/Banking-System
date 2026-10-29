import { formatMoney } from "../../../utils/formatMoney.js";
import AccountStatusBadge from "./AccountStatusBadge.jsx";

export default function AccountCard({ account }) {
  if (!account) return null;

  return (
    <div className="bg-white border rounded p-3">
      <div className="d-flex justify-content-between align-items-start">
        <div>
          <div className="fw-semibold">
            Account #{account.accountnumber || "-"}
          </div>
          <div className="text-muted small">
            Account ID: {account.accountid ?? "-"}
          </div>
        </div>

        <AccountStatusBadge value={account.status} />
      </div>

      <hr />

      <div className="row g-2">
        <div className="col-md-4">
          <div className="text-muted small">Type</div>
          <div>{account.accounttype || "-"}</div>
        </div>

        <div className="col-md-4">
          <div className="text-muted small">Customer ID</div>
          <div>{account.customer?.customerid ?? "-"}</div>
        </div>

        <div className="col-md-4">
          <div className="text-muted small">Balance</div>
          <div className="fw-semibold">
            {formatMoney(account.balance)}
          </div>
        </div>
      </div>
    </div>
  );
}