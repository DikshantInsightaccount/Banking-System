import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import PageHeader from "../../../components/shared/PageHeader.jsx";
import FilterBar from "../../../components/shared/FilterBar.jsx";
import Loader from "../../../components/shared/Loader.jsx";
import DataTable from "../../../components/shared/DataTable.jsx";
import { transactionService } from "../../customers/services/transactionService.js";
import { formatMoney } from "../../../utils/formatMoney.js";
import { formatDate } from "../../../utils/formatDate.js";
import TransactionStatusBadge from "../components/TransactionStatusBadge.jsx";
import TransactionSummary from "../components/TransactionSummary.jsx";

export default function TransactionsListPage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [rows, setRows] = useState([]);
  const [q, setQ] = useState("");
  const [error, setError] = useState("");

  const load = async () => {
    try {
      setLoading(true);
      setError("");
      const data = await transactionService.getAll();
      setRows(Array.isArray(data) ? data : []);
    } catch (e) {
      setError(e?.message || "Failed to load transactions");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  //  Backend-aligned search text
  const filtered = rows.filter((t) => {
    const text = `
      ${t.txnid ?? ""}
      ${t.txntype ?? ""}
      ${t.status ?? ""}
      ${t.amount ?? ""}
      ${t.txntimestamp ?? ""}
      ${t.account?.accountid ?? ""}
      ${t.account?.accountnumber ?? ""}
      ${t.account?.accounttype ?? ""}
      ${t.account?.status ?? ""}
      ${t.account?.customer?.customerid ?? ""}
      ${t.account?.customer?.fullname ?? ""}
      ${t.account?.customer?.email ?? ""}
      ${t.account?.customer?.phone ?? ""}
      ${t.account?.customer?.kycstatus ?? ""}
      ${t.account?.customer?.user?.username ?? ""}
    `.toLowerCase();

    return text.includes(q.toLowerCase());
  });

  //  Summary should reflect filtered list
  const summary = useMemo(() => {
    const totalAmount = filtered.reduce((sum, t) => sum + Number(t.amount || 0), 0);
    return { count: filtered.length, totalAmount };
  }, [filtered]);

  //  Backend-aligned columns
  const columns = [
    {
      key: "txnid",
      header: "Txn ID",
      render: (r) => r.txnid,
    },
    {
      key: "txntype",
      header: "Type",
      render: (r) => r.txntype,
    },
    {
      key: "accountnumber",
      header: "Account No",
      render: (r) => r.account?.accountnumber ?? "-",
    },
    {
      key: "customername",
      header: "Customer",
      render: (r) => r.account?.customer?.fullname ?? "-",
    },
    {
      key: "amount",
      header: "Amount",
      render: (r) => formatMoney(r.amount),
    },
    {
      key: "txntimestamp",
      header: "Date",
      render: (r) => formatDate(r.txntimestamp),
    },
    {
      key: "status",
      header: "Status",
      render: (r) => <TransactionStatusBadge value={r.status} />,
    },
  ];

  return (
    <>
      <PageHeader
        title="Transactions"
        subtitle="View transaction records and details."
        right={
          <button className="btn btn-outline-secondary btn-sm" onClick={load}>
            Refresh
          </button>
        }
      />

      <FilterBar
        query={q}
        onQueryChange={setQ}
        placeholder="Search txn id, type, account no, customer, status..."
        right={
          <>
            <button
              className="btn btn-outline-success btn-sm"
              onClick={() => navigate("/employee/transactions/deposit")}
            >
              Deposit
            </button>
            <button
              className="btn btn-outline-warning btn-sm"
              onClick={() => navigate("/employee/transactions/withdraw")}
            >
              Withdraw
            </button>
            <button
              className="btn btn-outline-primary btn-sm"
              onClick={() => navigate("/employee/transactions/transfer")}
            >
              Transfer
            </button>
          </>
        }
      />

      <div className="row g-3 mb-3">
        <div className="col-lg-4">
          <TransactionSummary totalCount={summary.count} totalAmount={summary.totalAmount} />
        </div>
      </div>

      {loading && <Loader />}
      {error && <div className="alert alert-danger">{error}</div>}

      {!loading && !error && (
        <DataTable
          columns={columns}
          rows={filtered}
          rowKey={(r) => r.txnid}
          onRowClick={(r) => navigate(`/employee/transactions/${r.txnid}`)}
        />
      )}
    </>
  );
}
