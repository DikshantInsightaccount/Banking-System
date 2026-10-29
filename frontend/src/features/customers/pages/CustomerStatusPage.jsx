import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import PageHeader from "../../../components/shared/PageHeader.jsx";
import Loader from "../../../components/shared/Loader.jsx";
import CustomerStatusCard from "../components/CustomerStatusCard.jsx";
import { customerService } from "../services/customerService.js";

export default function CustomerStatusPage() {
  const { customerId } = useParams();
  const [loading, setLoading] = useState(true);
  const [status, setStatus] = useState(null);
  const [remarks, setRemarks] = useState("");
  const [error, setError] = useState("");

  const load = async () => {
    try {
      setLoading(true);
      setError("");
      const s = await customerService.getStatus(customerId);
      setStatus(s);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [customerId]);

  const activate = async () => {
    try {
      setError("");
      await customerService.activate(customerId, remarks || undefined);
      await load();
    } catch (e) {
      setError(e.message);
    }
  };

  const deactivate = async () => {
    try {
      setError("");
      await customerService.deactivate(customerId, remarks || undefined);
      await load();
    } catch (e) {
      setError(e.message);
    }
  };

  return (
    <>
      <PageHeader title="Customer Status" subtitle={`Customer ID: ${customerId}`} />
      {loading ? <Loader /> : null}
      {error ? <div className="alert alert-danger">{error}</div> : null}

      {!loading ? (
        <div className="row g-3">
          <div className="col-lg-7">
            <CustomerStatusCard status={status} />
          </div>
          <div className="col-lg-5">
            <div className="bg-white border rounded p-3">
              <div className="fw-semibold mb-2">Actions</div>
              <label className="form-label">Remarks (optional)</label>
              <textarea className="form-control" rows="3" value={remarks} onChange={(e) => setRemarks(e.target.value)} />

              <div className="d-flex gap-2 mt-3">
                <button className="btn btn-success" onClick={activate}>Activate</button>
                <button className="btn btn-secondary" onClick={deactivate}>Deactivate</button>
              </div>
            </div>
          </div>
        </div>
      ) : null}
    </>
  );
}