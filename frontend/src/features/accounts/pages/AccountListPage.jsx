import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import PageHeader from "../../../components/shared/PageHeader.jsx";
import FilterBar from "../../../components/shared/FilterBar.jsx";
import Loader from "../../../components/shared/Loader.jsx";
import DataTable from "../../../components/shared/DataTable.jsx";
import StatusBadge from "../../../components/shared/StatusBadge.jsx";

import { accountService } from "../../customers/services/accountService.js";
import { formatMoney } from "../../../utils/formatMoney.js";

export default function AccountListPage() {
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [rows, setRows] = useState([]);
  const [q, setQ] = useState("");
  const [error, setError] = useState("");

  //  Load accounts
  const loadAccounts = async () => {
    try {
      setLoading(true);
      setError("");

      const data = await accountService.getAll();
      console.log("Accounts API:", data); //  keep for debugging

      setRows(Array.isArray(data) ? data : []);
    } catch (e) {
      setError(e?.message || "Failed to load accounts");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAccounts();
  }, []);

  //  Search (FIXED)
  const filtered = rows.filter((a) => {
    const text = `
      ${a.accountid ?? ""}
      ${a.accountnumber ?? ""}
      ${a.accounttype ?? ""}
      ${a.status ?? ""}
      ${a.customerid ?? ""}
    `.toLowerCase();

    return text.includes(q.toLowerCase());
  });

  //  Columns (FIXED)
  const columns = [
    {
      key: "accountid",
      header: "Account ID",
      render: (r) => r.accountid,
    },
    {
      key: "accountnumber",
      header: "Account No",
      render: (r) => r.accountnumber,
    },
    {
      key: "customerid",
      header: "Customer ID",
      render: (r) => r.customerid, //  FIXED
    },
    {
      key: "accounttype",
      header: "Type",
      render: (r) => r.accounttype,
    },
    {
      key: "balance",
      header: "Balance",
      render: (r) => formatMoney(r.balance),
    },
    {
      key: "status",
      header: "Status",
      render: (r) => <StatusBadge value={r.status} />,
    },
  ];

  return (
    <>
      <PageHeader
        title="Accounts"
        subtitle="Manage customer accounts"
        right={
          <div className="d-flex gap-2">
            {/*  FIXED ROUTE (no :customerId static) */}
            <button
              className="btn btn-primary btn-sm"
              onClick={() => navigate("/employee/customers")}
            >
              Create / Open Account
            </button>

            <button
              className="btn btn-outline-secondary btn-sm"
              onClick={loadAccounts}
            >
              Refresh
            </button>
          </div>
        }
      />

      <FilterBar
        query={q}
        onQueryChange={setQ}
        placeholder="Search account no, customer id, status..."
      />

      {loading && <Loader />}
      {error && <div className="alert alert-danger">{error}</div>}

      {!loading && !error && (
        <DataTable
          columns={columns}
          rows={filtered}
          rowKey={(r) => r.accountid}
          onRowClick={(r) =>
            navigate(`/employee/accounts/${r.accountid}`)
          }
        />
      )}
    </>
  );
}