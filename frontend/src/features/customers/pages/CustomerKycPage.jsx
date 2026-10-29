import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import PageHeader from "../../../components/shared/PageHeader.jsx";
import Loader from "../../../components/shared/Loader.jsx";
import KycStatusBadge from "../components/KycStatusBadge.jsx";
import { customerService } from "../services/customerService.js";

const KYC_OPTIONS = ["PENDING", "VERIFIED", "REJECTED"];

export default function CustomerKycPage() {
  const { customerId } = useParams();
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [customer, setCustomer] = useState(null);
  const [status, setStatus] = useState("PENDING");
  const [error, setError] = useState("");
  const [successMsg, setSuccessMsg] = useState("");
  const [showToast, setShowToast] = useState(false);
  const redirectTimerRef = useRef(null);

  const getErrMsg = (e) =>
    e?.response?.data?.message ||
    e?.response?.data?.error ||
    e?.message ||
    "Something went wrong";

  const load = async () => {
    try {
      setLoading(true);
      setError("");

      const c = await customerService.getById(customerId);
      setCustomer(c);

      const backendStatus = (c.kycstatus || "PENDING")
        .toString()
        .trim()
        .toUpperCase();

      setStatus(KYC_OPTIONS.includes(backendStatus) ? backendStatus : "PENDING");
    } catch (e) {
      setError(getErrMsg(e));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    return () => {
      if (redirectTimerRef.current) clearTimeout(redirectTimerRef.current);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [customerId]);

  const update = async () => {
    try {
      setError("");
      setSuccessMsg("");
      setShowToast(false);

      const finalStatus = status.trim().toUpperCase();
      if (!KYC_OPTIONS.includes(finalStatus)) {
        setError("Invalid KYC status");
        return;
      }

      await customerService.updateKyc(customerId, finalStatus);

      //  Toast popup message
      const name = customer?.fullname || "Customer";
      setSuccessMsg(`KYC status updated to ${finalStatus} for ${name} ✅`);
      setShowToast(true);

      //  Optional: refresh badge instantly
      await load();

      //  Redirect to CustomerListPage after 1.5s
      redirectTimerRef.current = setTimeout(() => {
        navigate("/employee/customers", { replace: true });
      }, 1500);
    } catch (e) {
      setError(getErrMsg(e) || "Failed to update KYC");
    }
  };

  return (
    <>
      <PageHeader title="KYC Management" subtitle={`Customer ID: ${customerId}`} />

      {loading && <Loader />}

      {error && <div className="alert alert-danger mt-2">{error}</div>}

      {/*  Toast popup (Bootstrap-only) */}
      <div
        className="position-fixed bottom-0 end-0 p-3"
        style={{ zIndex: 1080 }}
      >
        <div
          className={`toast align-items-center text-bg-success border-0 ${
            showToast ? "show" : "hide"
          }`}
          role="alert"
          aria-live="assertive"
          aria-atomic="true"
        >
          <div className="d-flex">
            <div className="toast-body">
              {successMsg || "Updated successfully"}
            </div>
            <button
              type="button"
              className="btn-close btn-close-white me-2 m-auto"
              aria-label="Close"
              onClick={() => setShowToast(false)}
            />
          </div>
        </div>
      </div>

      {!loading && customer && (
        <div className="bg-white border rounded p-3 mt-3">
          <div className="d-flex justify-content-between align-items-center">
            <div>
              <div className="fw-semibold fs-5">{customer.fullname || "Customer"}</div>
              <div className="text-muted">ID: {customer.customerid}</div>
            </div>

            <KycStatusBadge value={customer.kycstatus} />
          </div>

          <hr />

          <label className="form-label fw-semibold">Set KYC Status</label>

          <select
            className="form-select"
            value={status}
            onChange={(e) => setStatus(e.target.value.toUpperCase())}
          >
            {KYC_OPTIONS.map((opt) => (
              <option key={opt} value={opt}>
                {opt}
              </option>
            ))}
          </select>

          <button className="btn btn-primary mt-3" onClick={update}>
            Update KYC
          </button>
        </div>
      )}
    </>
  );
}
