import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import PageHeader from "../../../components/shared/PageHeader.jsx";
import Loader from "../../../components/shared/Loader.jsx";
import { customerService } from "../services/customerService.js";

export default function CustomerEditPage() {
  const { customerId } = useParams();
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [form, setForm] = useState({
    full_name: "",
    email: "",
    phone: "",
    address: "",
  });

  //  FIXED helper for controlled inputs
  const onChange = (key, value) => {
    setForm((prev) => ({
      ...prev,
      [key]: value,
    }));
  };

  //  Load customer details
  useEffect(() => {
    let alive = true;

    (async () => {
      try {
        setLoading(true);
        setError("");

        const c = await customerService.getById(customerId);
        if (!alive) return;

        setForm({
          full_name: c.full_name || c.fullName || "",
          email: c.email || "",
          phone: c.phone || "",
          address: c.address || "",
        });
      } catch (err) {
        if (!alive) return;
        setError(err?.message || "Failed to load customer");
      } finally {
        if (alive) setLoading(false);
      }
    })();

    return () => {
      alive = false;
    };
  }, [customerId]);

  //  Save updated customer
  const onSave = async (e) => {
    e.preventDefault();

    try {
      setSaving(true);
      setError("");

      await customerService.update(customerId, form);

      navigate(`/employee/customers/${customerId}`, { replace: true });
    } catch (err) {
      setError(err?.message || "Failed to update customer");
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <PageHeader
        title="Edit Customer"
        subtitle={`Customer ID: ${customerId}`}
      />

      {loading && <Loader />}

      {error && (
        <div className="alert alert-danger mt-2">
          {error}
        </div>
      )}

      {!loading && (
        <form
          className="bg-white border rounded p-3"
          onSubmit={onSave}
        >
          <div className="row g-3">
            <div className="col-md-6">
              <label className="form-label">Full Name</label>
              <input
                className="form-control"
                value={form.full_name}
                onChange={(e) =>
                  onChange("full_name", e.target.value)
                }
              />
            </div>

            <div className="col-md-6">
              <label className="form-label">Email</label>
              <input
                className="form-control"
                value={form.email}
                onChange={(e) =>
                  onChange("email", e.target.value)
                }
              />
            </div>

            <div className="col-md-6">
              <label className="form-label">Phone</label>
              <input
                className="form-control"
                value={form.phone}
                onChange={(e) =>
                  onChange("phone", e.target.value)
                }
              />
            </div>

            <div className="col-12">
              <label className="form-label">Address</label>
              <textarea
                className="form-control"
                rows="3"
                value={form.address}
                onChange={(e) =>
                  onChange("address", e.target.value)
                }
              />
            </div>
          </div>

          <div className="d-flex gap-2 mt-3">
            <button
              type="submit"
              className="btn btn-primary"
              disabled={saving}
            >
              {saving ? "Saving..." : "Save"}
            </button>

            <button
              type="button"
              className="btn btn-light"
              onClick={() => navigate(-1)}
            >
              Cancel
            </button>
          </div>
        </form>
      )}
    </>
  );
}