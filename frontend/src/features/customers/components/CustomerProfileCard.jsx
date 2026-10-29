export default function CustomerProfileCard({ customer }) {
  if (!customer) return null;

  return (
    <div className="bg-white border rounded p-3">
      {/* Header */}
      <div className="mb-2">
        <div className="fw-semibold fs-5">
          {customer.fullname || "-"}
        </div>
        <div className="text-muted small">
          Customer ID: {customer.customerid}
        </div>
      </div>

      <hr />

      {/* Details */}
      <div className="row g-3">
        <div className="col-md-6">
          <div className="text-muted small">Email</div>
          <div>{customer.email || "-"}</div>
        </div>

        <div className="col-md-6">
          <div className="text-muted small">Phone</div>
          <div>{customer.phone || "-"}</div>
        </div>

        <div className="col-md-12">
          <div className="text-muted small">Address</div>
          <div>{customer.address || "-"}</div>
        </div>

        <div className="col-md-6">
          <div className="text-muted small">ID Proof Type</div>
          <div>{customer.idprooftype || "-"}</div>
        </div>

        <div className="col-md-6">
          <div className="text-muted small">ID Proof Number</div>
          <div>{customer.idproofnumber || "-"}</div>
        </div>

        <div className="col-md-6">
          <div className="text-muted small">KYC Status</div>
          <div className="fw-semibold">
            {customer.kycstatus || "-"}
          </div>
        </div>

        <div className="col-md-6">
          <div className="text-muted small">Account Status</div>
          <div className="fw-semibold">
            {customer.user?.status || "-"}
          </div>
        </div>

        <div className="col-md-6">
          <div className="text-muted small">Username</div>
          <div>{customer.user?.username || "-"}</div>
        </div>

        <div className="col-md-6">
          <div className="text-muted small">Role</div>
          <div className="fw-semibold">
            {customer.user?.role || "-"}
          </div>
        </div>
      </div>
    </div>
  );
}