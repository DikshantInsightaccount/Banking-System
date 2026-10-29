import { useState } from "react";
import PageHeader from "../../../components/shared/PageHeader.jsx";
import Loader from "../../../components/shared/Loader.jsx";
import DataTable from "../../../components/shared/DataTable.jsx";
import { transactionService } from "../../customers/services/transactionService.js";
import { formatMoney } from "../../../utils/formatMoney.js";
import { formatDate } from "../../../utils/formatDate.js";

export default function StatementPage() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [rows, setRows] = useState([]);

  const [form, setForm] = useState({
    accountId: "",
    from: "",
    to: "",
  });

  const onChange = (k, v) => setForm((p) => ({ ...p, [k]: v }));

  const submit = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      setError("");
      const data = await transactionService.statement({
        accountId: Number(form.accountId),
        from: form.from,
        to: form.to,
      });
      setRows(Array.isArray(data) ? data : []);
    } catch (e2) {
      setError(e2.message);
    } finally {
      setLoading(false);
    }
  };

  const columns = [
    { key: "date", header: "Date", render: (r) => formatDate(r.date || r.createdAt || r.timestamp) },
    { key: "type", header: "Type", render: (r) => r.type || r.transactionType || "-" },
    { key: "amount", header: "Amount", render: (r) => formatMoney(r.amount) },
    { key: "balance", header: "Balance", render: (r) => formatMoney(r.balance) },
    { key: "note", header: "Note", render: (r) => r.note || r.remarks || "-" },
  ];

  return (
    <>
      <PageHeader title="Account Statement" subtitle="Fetch statement for a period." />

      {error ? <div className="alert alert-danger">{error}</div> : null}

      <form className="bg-white border rounded p-3 mb-3" onSubmit={submit}>
        <div className="row g-3">
          <div className="col-md-4">
            <label className="form-label">Account ID</label>
            <input className="form-control" value={form.accountId} onChange={(e) => onChange("accountId", e.target.value)} required />
          </div>
          <div className="col-md-4">
            <label className="form-label">From (ISO DateTime)</label>
            <input className="form-control" value={form.from} onChange={(e) => onChange("from", e.target.value)} placeholder="2026-05-01T00:00:00" required />
          </div>
          <div className="col-md-4">
            <label className="form-label">To (ISO DateTime)</label>
            <input className="form-control" value={form.to} onChange={(e) => onChange("to", e.target.value)} placeholder="2026-05-02T23:59:59" required />
          </div>
        </div>
        <button className="btn btn-dark mt-3" disabled={loading}>
          {loading ? "Fetching..." : "Get Statement"}
        </button>
      </form>

      {loading ? <Loader /> : null}
      {!loading ? (
        <DataTable
          columns={columns}
          rows={rows}
          rowKey={(r, idx) => r.id ?? `${idx}`}
        />
      ) : null}
    </>
  );
}