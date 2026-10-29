import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import PageHeader from "../../../components/shared/PageHeader.jsx";
import FilterBar from "../../../components/shared/FilterBar.jsx";
import Loader from "../../../components/shared/Loader.jsx";
import DataTable from "../../../components/shared/DataTable.jsx";

import KycStatusBadge from "../components/KycStatusBadge.jsx";
import { customerService } from "../services/customerService.js";

export default function CustomerListPage() {
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [rows, setRows] = useState([]);
  const [q, setQ] = useState("");
  const [error, setError] = useState("");
  const [currentPage, setCurrentPage] = useState(1);

  const rowsPerPage = 10;

  // Load Customers
  const load = async () => {
    try {
      setLoading(true);
      setError("");

      const data = await customerService.getAll();

      console.log("Customers API Response:", data);

      const formattedRows = Array.isArray(data)
        ? data.map((customer) => ({
            customerid: customer.customerid,
            fullname: customer.fullname,
            email: customer.email,
            phone: customer.phone,
            address: customer.address,
            createdat: customer.createdat,
            kycstatus: customer.kycstatus,
            user: customer.user,
          }))
        : [];

      setRows(formattedRows);
      setCurrentPage(1); // reset to first page on reload
    } catch (err) {
      console.error(err);
      setError(err.message || "Failed to load customers");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  // Search Filter
  const filteredRows = rows.filter((customer) => {
    const searchText = `
      ${customer.customerid ?? ""}
      ${customer.fullname ?? ""}
      ${customer.email ?? ""}
      ${customer.phone ?? ""}
      ${customer.user?.username ?? ""}
      ${customer.kycstatus ?? ""}
    `.toLowerCase();

    return searchText.includes(q.toLowerCase());
  });

  // Pagination logic
  const totalPages = Math.ceil(filteredRows.length / rowsPerPage);
  const startIndex = (currentPage - 1) * rowsPerPage;
  const paginatedRows = filteredRows.slice(startIndex, startIndex + rowsPerPage);

  // Table Columns
  const columns = [
    {
      key: "customerid",
      header: "Customer ID",
      render: (row) => <span className="fw-semibold">{row.customerid}</span>,
    },
    {
      key: "fullname",
      header: "Customer Name",
      render: (row) => row.fullname,
    },
    {
      key: "email",
      header: "Email",
      render: (row) => row.email,
    },
    {
      key: "phone",
      header: "Phone",
      render: (row) => row.phone,
    },
    {
      key: "username",
      header: "Username",
      render: (row) => row.user?.username || "-",
    },
    {
      key: "kycstatus",
      header: "KYC Status",
      render: (row) => (
        <span
          onClick={(e) => {
            e.stopPropagation();
            navigate(`/employee/customers/${row.customerid}/kyc`);
          }}
          style={{ cursor: "pointer", textDecoration: "underline" }}
          title="Manage KYC"
        >
          <KycStatusBadge value={row.kycstatus} />
        </span>
      ),
    },
  ];

  return (
    <>
      <PageHeader
        title="Customers"
        subtitle="View and manage customer profiles."
        right={
          <button className="btn btn-outline-primary btn-sm" onClick={load}>
            Refresh
          </button>
        }
      />

      <FilterBar
        query={q}
        onQueryChange={setQ}
        placeholder="Search by customer id, name, username, email, phone..."
      />

      {loading && <Loader />}

      {error && <div className="alert alert-danger">{error}</div>}

      {!loading && !error && (
        <>
          <DataTable
            columns={columns}
            rows={paginatedRows}
            rowKey={(row) => row.customerid}
            onRowClick={(row) => navigate(`/employee/customers/${row.customerid}`)}
          />

          {/* Pagination Controls */}
          <div className="d-flex justify-content-between align-items-center mt-3">
            <button
              className="btn btn-outline-secondary btn-sm"
              disabled={currentPage === 1}
              onClick={() => setCurrentPage((p) => p - 1)}
            >
              Previous
            </button>

            <span>
              Page {currentPage} of {totalPages}
            </span>

            <button
              className="btn btn-outline-secondary btn-sm"
              disabled={currentPage === totalPages}
              onClick={() => setCurrentPage((p) => p + 1)}
            >
              Next
            </button>
          </div>
        </>
      )}
    </>
  );
}