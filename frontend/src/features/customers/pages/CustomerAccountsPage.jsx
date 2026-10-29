import { useEffect, useState } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import PageHeader from "../../../components/shared/PageHeader.jsx";
import Loader from "../../../components/shared/Loader.jsx";
import DataTable from "../../../components/shared/DataTable.jsx";
import StatusBadge from "../../../components/shared/StatusBadge.jsx";
import { accountService } from "../services/accountService.js";
import { formatMoney } from "../../../utils/formatMoney.js";

export default function CustomerAccountsPage() {
  const { customerId } = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [rows, setRows] = useState([]);
  const [error, setError] = useState("");

  const load = async () => {
    try {
      setLoading(true);
      setError("");
      const data = await accountService.getByCustomer(customerId);
      setRows(Array.isArray(data) ? data : []);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [customerId]);

  const columns = [
    { key: "account_id", header: "Account ID", render: (r) => r.account_id ?? r.accountId },
    { key: "account_number", header: "Account No", render: (r) => r.account_number ?? r.accountNumber },
    { key: "account_type", header: "Type", render: (r) => r.account_type ?? r.accountType },
    { key: "balance", header: "Balance", render: (r) => formatMoney(r.balance) },
    { key: "status", header: "Status", render: (r) => <StatusBadge value={r.status} /> },
  ];

  return (
    <>
      <PageHeader
        title="Customer Accounts"
        subtitle={`Customer ID: ${customerId}`}
        right={
          <div className="d-flex gap-2">
            <Link className="btn btn-outline-primary btn-sm" to="/employee/accounts/open">
              Open Account
            </Link>
            <button className="btn btn-outline-secondary btn-sm" onClick={load}>Refresh</button>
          </div>
        }
      />

      {loading ? <Loader /> : null}
      {error ? <div className="alert alert-danger">{error}</div> : null}

      {!loading && !error ? (
        <DataTable
          columns={columns}
          rows={rows}
          rowKey={(r) => r.account_id ?? r.accountId}
          onRowClick={(r) => navigate(`/employee/accounts/${r.account_id ?? r.accountId}`)}
        />
      ) : null}
    </>
  );
}