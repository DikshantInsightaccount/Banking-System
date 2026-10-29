import React, { useMemo, useState } from "react";
import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";
import "bootstrap/dist/css/bootstrap.min.css";
import { formatDate } from "../utils/formatDate";
 
export default function StatementTable({
  transactions = [],
  accountDetails = null,
  customerDetails = null,
  accountNumber = localStorage.getItem("accountnumber") || "",
  enablePdf = true,
  compact = true,
}) {
  const [q, setQ] = useState("");
  const [fromDate, setFromDate] = useState("");
  const [toDate, setToDate] = useState("");
 
  // ---- Same helpers as your Statements.jsx ----
  const getTransactionDateRaw = (tx) =>
    tx?.txntimestamp || tx?.timestamp || tx?.createdAt || tx?.created_at || tx?.date || tx?.createdat;
 
  const getTransactionDate = (tx) => formatDate(getTransactionDateRaw(tx));
 
  const getTransactionAmount = (tx) => {
    if (typeof tx?.amount === "number") return tx.amount;
    if (typeof tx?.amount === "string" && tx.amount.trim() !== "") return Number(tx.amount);
    if (typeof tx?.credit === "number" || typeof tx?.debit === "number") {
      return (tx.credit || 0) - (tx.debit || 0);
    }
    return 0;
  };
 
  const getTransactionDescription = (tx) =>
    tx?.txntype || tx?.description || tx?.type || tx?.transactionType || "Transaction";
 
  const getTransactionRef = (tx) => tx?.txnid || tx?.refId || tx?.refid || tx?.id || "";
 
  // ---- Normalize into rows for table ----
  const normalized = useMemo(() => {
    return (Array.isArray(transactions) ? transactions : []).map((tx) => {
      const amount = getTransactionAmount(tx);
      return {
        _raw: tx,
        rawDate: getTransactionDateRaw(tx),
        date: getTransactionDate(tx),
        type: getTransactionDescription(tx),
        amount,
        txnId: String(getTransactionRef(tx) || ""),
      };
    });
  }, [transactions]);
 
  // ---- Apply search + date filter ----
  const filteredRows = useMemo(() => {
    const query = q.trim().toLowerCase();
 
    const from = fromDate ? new Date(fromDate) : null;
    const to = toDate ? new Date(toDate) : null;
    if (to) to.setHours(23, 59, 59, 999);
 
    return normalized.filter((r) => {
      // date
      if (from || to) {
        const d = r.rawDate ? new Date(r.rawDate) : null;
        if (!d || isNaN(d.getTime())) return false;
        if (from && d < from) return false;
        if (to && d > to) return false;
      }
 
      // search
      if (!query) return true;
      return (
        String(r.date).toLowerCase().includes(query) ||
        String(r.type).toLowerCase().includes(query) ||
        String(r.amount).toLowerCase().includes(query) ||
        String(r.txnId).toLowerCase().includes(query)
      );
    });
  }, [normalized, q, fromDate, toDate]);
 
  // ---- PDF (same pattern as your updated Statements.jsx) ----
  const handleDownloadPDF = () => {
    const doc = new jsPDF();
    let y = 20;
    const margin = 14;
    const line = 7;
 
    doc.setFontSize(20);
    doc.text("IG Bank Statement", margin, y);
    y += 12;
 
    doc.setFontSize(11);
    doc.text("--- Customer Details ---", margin, y);
    y += line + 2;
 
    doc.text(`Name: ${customerDetails?.fullname || "N/A"}`, margin, y);
    y += line;
 
    if (customerDetails?.email) {
      doc.text(`Email: ${customerDetails.email}`, margin, y);
      y += line;
    }
    if (customerDetails?.phone) {
      doc.text(`Phone: ${customerDetails.phone}`, margin, y);
      y += line;
    }
    if (customerDetails?.address) {
      doc.text(`Address: ${customerDetails.address}`, margin, y);
      y += line;
    }
    if (customerDetails?.kycstatus) {
      doc.text(`KYC Status: ${customerDetails.kycstatus}`, margin, y);
      y += line;
    }
 
    doc.text(
      `Account Number: ${accountDetails?.accountnumber || accountNumber || "N/A"}`,
      margin,
      y
    );
    y += line;
 
    doc.text(`Current Balance: Rs. ${accountDetails?.balance ?? "N/A"}`, margin, y);
    y += line + 3;
 
    const periodText =
      fromDate && toDate
        ? `Period: ${fromDate} to ${toDate}`
        : fromDate
        ? `Period: ${fromDate} to Today`
        : toDate
        ? `Period: Beginning to ${toDate}`
        : `Period: Beginning to Today`;
 
    doc.text(periodText, margin, y);
    y += 8;
 
    autoTable(doc, {
      startY: y,
      head: [["Date & Time", "Type", "Amount", "Transaction ID"]],
      body: filteredRows.map((r) => {
        const amt = Number(r.amount || 0);
        const amtStr = `Rs. ${Math.abs(amt).toLocaleString("en-IN")}`;
        return [r.date, r.type, amtStr, r.txnId];
      }),
      theme: "grid",
      styles: { fontSize: 10 },
      headStyles: { fillColor: [22, 160, 133] },
    });
 
    doc.save("bank_statement.pdf");
  };
 
  return (
    <div className={`card shadow-sm border-0 ${compact ? "" : "mt-3"}`}>
      <div className="card-body p-3">
        <div className="d-flex align-items-center justify-content-between mb-2">
          <div className="fw-semibold">Bank Statement</div>
          <input
            className="form-control form-control-sm"
            style={{ width: 160 }}
            placeholder="Search…"
            value={q}
            onChange={(e) => setQ(e.target.value)}
          />
        </div>
 
        {/* Summary (compact-safe) */}
        {(customerDetails || accountDetails || accountNumber) && (
          <div className="p-2 mb-2" style={{ background: "#f8fafc", borderRadius: 10 }}>
            {customerDetails?.fullname && (
              <div className="small">
                <strong>Name:</strong> {customerDetails.fullname}
              </div>
            )}
            {(accountDetails?.accountnumber || accountNumber) && (
              <div className="small">
                <strong>Account:</strong> {accountDetails?.accountnumber || accountNumber}
              </div>
            )}
            {accountDetails?.balance !== undefined && accountDetails?.balance !== null && (
              <div className="small">
                <strong>Balance:</strong> ₹ {accountDetails.balance}
              </div>
            )}
          </div>
        )}
 
        {/* Date Filters + PDF */}
        <div className="d-flex gap-2 mb-2">
          <input
            type="date"
            className="form-control form-control-sm"
            value={fromDate}
            onChange={(e) => setFromDate(e.target.value)}
          />
          <input
            type="date"
            className="form-control form-control-sm"
            value={toDate}
            onChange={(e) => setToDate(e.target.value)}
          />
          {enablePdf && (
            <button className="btn btn-sm btn-outline-success" onClick={handleDownloadPDF}>
              PDF
            </button>
          )}
        </div>
 
        <div className="table-responsive" style={{ maxHeight: 240 }}>
          <table className="table table-sm table-striped align-middle mb-0">
            <thead className="table-light" style={{ position: "sticky", top: 0, zIndex: 1 }}>
              <tr>
                <th style={{ minWidth: 110 }}>Date</th>
                <th style={{ minWidth: 120 }}>Type</th>
                <th className="text-end" style={{ minWidth: 90 }}>Amount</th>
                <th style={{ minWidth: 120 }}>Transaction ID</th>
              </tr>
            </thead>
            <tbody>
              {filteredRows.map((r, idx) => {
                const amt = Number(r.amount || 0);
                return (
                  <tr key={idx}>
                    <td>{r.date}</td>
                    <td>{r.type}</td>
                    <td className={`text-end ${amt >= 0 ? "text-success" : "text-danger"}`}>
                      ₹ {Math.abs(amt).toLocaleString("en-IN")}
                    </td>
                    <td>{r.txnId}</td>
                  </tr>
                );
              })}
 
              {filteredRows.length === 0 && (
                <tr>
                  <td colSpan="4" className="text-center text-muted py-3">
                    No transactions found
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
 
        <div className="small text-muted mt-2">
          This is a compact statement view inside Support Chat.
        </div>
      </div>
    </div>
  );
}
 