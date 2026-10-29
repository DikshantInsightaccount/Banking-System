import { useEffect, useRef, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";

import Loader from "../../../components/shared/Loader.jsx";
import PageHeader from "../../../components/shared/PageHeader.jsx";
import CustomerProfileCard from "../components/CustomerProfileCard.jsx";
import KycStatusBadge from "../components/KycStatusBadge.jsx";
import { customerService } from "../services/customerService.js";

const KYC_OPTIONS = ["PENDING", "VERIFIED", "REJECTED"];

export default function CustomerDetailsPage() {
  const { customerId } = useParams();
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [customer, setCustomer] = useState(null);
  const [error, setError] = useState("");
  const [status, setStatus] = useState("PENDING");
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

      const data = await customerService.getById(customerId);
      setCustomer(data);

      const backendStatus = (data.kycstatus || "PENDING")
        .toString()
        .trim()
        .toUpperCase();

      setStatus(KYC_OPTIONS.includes(backendStatus) ? backendStatus : "PENDING");
    } catch (err) {
      setError(getErrMsg(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    return () => {
      if (redirectTimerRef.current) clearTimeout(redirectTimerRef.current);
    };
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

      const name = customer?.fullname || "Customer";
      setSuccessMsg(`KYC status updated to ${finalStatus} for ${name} ✅`);
      setShowToast(true);

      await load();

      redirectTimerRef.current = setTimeout(() => {
        navigate("/employee/customers", { replace: true });
      }, 1500);
    } catch (e) {
      setError(getErrMsg(e) || "Failed to update KYC");
    }
  };

  return (
    <>
      <PageHeader
        title="Customer Details"
        subtitle={
          customer
            ? `Customer ID: ${customer.customerid}`
            : `Customer ID: ${customerId}`
        }
      />

      {loading && <Loader />}

      {error && <div className="alert alert-danger mt-2">{error}</div>}

      {/*  Toast popup */}
      <div className="position-fixed bottom-0 end-0 p-3" style={{ zIndex: 1080 }}>
        <div
          className={`toast align-items-center text-bg-success border-0 ${
            showToast ? "show" : "hide"
          }`}
          role="alert"
          aria-live="assertive"
          aria-atomic="true"
        >
          <div className="d-flex">
            <div className="toast-body">{successMsg || "Updated successfully"}</div>
            <button
              type="button"
              className="btn-close btn-close-white me-2 m-auto"
              aria-label="Close"
              onClick={() => setShowToast(false)}
            />
          </div>
        </div>
      </div>

      {!loading && !error && customer && (
        <>
          <CustomerProfileCard customer={customer} />

          {/* KYC Management Section */}
          <div className="bg-white border rounded p-3 mt-3">
            <div className="d-flex justify-content-end align-items-center">
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
        </>
      )}
    </>
  );
}