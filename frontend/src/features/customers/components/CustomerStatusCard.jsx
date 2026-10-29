import StatusBadge from "../../../components/shared/StatusBadge.jsx";

export default function CustomerStatusCard({ status }) {
  if (!status) return null;

  return (
    <div className="bg-white border rounded p-3">
      <div className="d-flex justify-content-between align-items-center">
        <div className="fw-semibold">Customer Status</div>
        <StatusBadge value={status.account_active ? "ACTIVE" : "INACTIVE"} />
      </div>
      <hr />
      <div className="text-muted small">Remarks</div>
      <div>{status.remarks || "-"}</div>
      <div className="text-muted small mt-2">Updated</div>
      <div>{status.updated_at || status.updatedAt || "-"}</div>
    </div>
  );
}
