import { useEffect, useState } from "react";
import API from "../../api/api";
import { formatDate } from "../../utils/formatDate";
import "./customer.css";

import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";

export default function Statements() {
  const [statements, setStatements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const accountNumber = localStorage.getItem("accountnumber");

  useEffect(() => {
    const loadStatements = async () => {
      if (!accountNumber) {
        setError("Account number not found. Please log in again.");
        setLoading(false);
        return;
      }
      try {
        const response = await API.get("/api/transactions/statement", {
          params: { accountNumber, from: "2000-01-01T00:00:00", to: new Date().toISOString() },
        });
        setStatements(Array.isArray(response.data) ? response.data : []);
      } catch (requestError) {
        setError(requestError.response?.data?.message || "Unable to load statements.");
      } finally {
        setLoading(false);
      }
    };
    loadStatements();
  }, [accountNumber]);

  const downloadStatement = () => {
    if (statements.length === 0) {
      return;
    }

    const doc = new jsPDF({
      orientation: "portrait",
      unit: "mm",
      format: "a4",
    });

    const pageWidth = doc.internal.pageSize.getWidth();
    const pageHeight = doc.internal.pageSize.getHeight();

    const totalCredits = statements.reduce(
      (sum, item) => sum + Number(item.credit || 0),
      0
    );

    const totalDebits = statements.reduce(
      (sum, item) => sum + Number(item.debit || 0),
      0
    );

    const netFlow = totalCredits - totalDebits;

    /*
     * jsPDF's default Helvetica font does not reliably support ₹.
     * Use "Rs." unless you embed a Unicode font.
     */
    const formatPdfAmount = (amount) =>
      `Rs. ${Number(amount || 0).toLocaleString("en-IN", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
      })}`;

    const generatedDate = new Date().toLocaleString("en-IN", {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });

    /* =========================
       HEADER BACKGROUND
    ========================= */

    doc.setFillColor(15, 23, 42);
    doc.rect(0, 0, pageWidth, 38, "F");

    doc.setFillColor(37, 99, 235);
    doc.rect(0, 35, pageWidth, 3, "F");

    /* =========================
       BANK BRANDING
    ========================= */

    doc.setTextColor(255, 255, 255);
    doc.setFont("helvetica", "bold");
    doc.setFontSize(22);
    doc.text("IG Bank", 14, 17);

    doc.setFont("helvetica", "normal");
    doc.setFontSize(8);
    doc.setTextColor(191, 219, 254);
    doc.text("SMARTER BANKING, BETTER LIVING", 14, 24);

    doc.setFont("helvetica", "bold");
    doc.setFontSize(14);
    doc.setTextColor(255, 255, 255);
    doc.text("ACCOUNT STATEMENT", pageWidth - 14, 17, {
      align: "right",
    });

    doc.setFont("helvetica", "normal");
    doc.setFontSize(8);
    doc.setTextColor(203, 213, 225);
    doc.text(`Generated: ${generatedDate}`, pageWidth - 14, 24, {
      align: "right",
    });

    /* =========================
       ACCOUNT INFORMATION
    ========================= */

    doc.setTextColor(15, 23, 42);
    doc.setFont("helvetica", "bold");
    doc.setFontSize(11);
    doc.text("Account details", 14, 49);

    doc.setDrawColor(226, 232, 240);
    doc.line(14, 53, pageWidth - 14, 53);

    doc.setFont("helvetica", "normal");
    doc.setFontSize(9);
    doc.setTextColor(100, 116, 139);
    doc.text("ACCOUNT NUMBER", 14, 60);

    doc.setFont("helvetica", "bold");
    doc.setFontSize(11);
    doc.setTextColor(15, 23, 42);
    doc.text(accountNumber || "Not available", 14, 67);

    doc.setFont("helvetica", "normal");
    doc.setFontSize(9);
    doc.setTextColor(100, 116, 139);
    doc.text("STATEMENT PERIOD", 105, 60);

    doc.setFont("helvetica", "bold");
    doc.setFontSize(10);
    doc.setTextColor(15, 23, 42);
    doc.text("All available transactions", 105, 67);

    /* =========================
       SUMMARY CARDS
    ========================= */

    const summaryTop = 76;
    const cardGap = 5;
    const sideMargin = 14;
    const usableWidth = pageWidth - sideMargin * 2;
    const cardWidth = (usableWidth - cardGap * 2) / 3;
    const cardHeight = 25;

    const drawSummaryCard = ({
      x,
      label,
      value,
      backgroundColor,
      accentColor,
    }) => {
      doc.setFillColor(...backgroundColor);
      doc.roundedRect(
        x,
        summaryTop,
        cardWidth,
        cardHeight,
        3,
        3,
        "F"
      );

      doc.setFillColor(...accentColor);
      doc.roundedRect(
        x,
        summaryTop,
        2.5,
        cardHeight,
        1.2,
        1.2,
        "F"
      );

      doc.setFont("helvetica", "normal");
      doc.setFontSize(8);
      doc.setTextColor(100, 116, 139);
      doc.text(label.toUpperCase(), x + 7, summaryTop + 8);

      doc.setFont("helvetica", "bold");
      doc.setFontSize(12);
      doc.setTextColor(15, 23, 42);
      doc.text(value, x + 7, summaryTop + 18);
    };

    drawSummaryCard({
      x: sideMargin,
      label: "Total credits",
      value: formatPdfAmount(totalCredits),
      backgroundColor: [236, 253, 245],
      accentColor: [22, 163, 74],
    });

    drawSummaryCard({
      x: sideMargin + cardWidth + cardGap,
      label: "Total debits",
      value: formatPdfAmount(totalDebits),
      backgroundColor: [254, 242, 242],
      accentColor: [220, 38, 38],
    });

    drawSummaryCard({
      x: sideMargin + (cardWidth + cardGap) * 2,
      label: "Net cash flow",
      value: formatPdfAmount(netFlow),
      backgroundColor: [239, 246, 255],
      accentColor: [37, 99, 235],
    });

    /* =========================
       TRANSACTION TABLE
    ========================= */

    autoTable(doc, {
      startY: 110,

      margin: {
        left: 14,
        right: 14,
        bottom: 20,
      },

      head: [
        [
          "Date",
          "Type",
          "Description",
          "Status",
          "Credit",
          "Debit",
        ],
      ],

      body: statements.map((statement) => {
        const credit = Number(statement.credit || 0);
        const debit = Number(statement.debit || 0);

        return [
          formatDate(statement.timestamp),
          statement.type || "-",
          statement.description || "-",
          statement.status || "-",
          credit > 0 ? formatPdfAmount(credit) : "-",
          debit > 0 ? formatPdfAmount(debit) : "-",
        ];
      }),

      theme: "plain",

      styles: {
        font: "helvetica",
        fontSize: 8.5,
        textColor: [51, 65, 85],
        cellPadding: {
          top: 4,
          right: 3,
          bottom: 4,
          left: 3,
        },
        lineColor: [226, 232, 240],
        lineWidth: 0.2,
        valign: "middle",
        overflow: "linebreak",
      },

      headStyles: {
        fillColor: [37, 99, 235],
        textColor: [255, 255, 255],
        fontStyle: "bold",
        fontSize: 8,
        halign: "left",
        lineWidth: 0,
        cellPadding: {
          top: 4.5,
          right: 3,
          bottom: 4.5,
          left: 3,
        },
      },

      alternateRowStyles: {
        fillColor: [248, 250, 252],
      },

      columnStyles: {
        0: {
          cellWidth: 35,
        },
        1: {
          cellWidth: 23,
          halign: "center",
          fontStyle: "bold",
        },
        2: {
          cellWidth: 55,
        },
        3: {
          cellWidth: 22,
          halign: "center",
          fontStyle: "bold",
        },
        4: {
          cellWidth: 26,
          halign: "right",
          textColor: [22, 163, 74],
          fontStyle: "bold",
        },
        5: {
          cellWidth: 26,
          halign: "right",
          textColor: [220, 38, 38],
          fontStyle: "bold",
        },
      },

      didParseCell: (data) => {
        if (data.section !== "body") {
          return;
        }

        if (data.column.index === 3) {
          const status = String(data.cell.raw || "").toUpperCase();

          if (status === "SUCCESS") {
            data.cell.styles.textColor = [22, 101, 52];
          } else {
            data.cell.styles.textColor = [153, 27, 27];
          }
        }
      },

      didDrawPage: () => {
        const currentPage =
          doc.internal.getCurrentPageInfo().pageNumber;

        doc.setDrawColor(226, 232, 240);
        doc.line(
          14,
          pageHeight - 15,
          pageWidth - 14,
          pageHeight - 15
        );

        doc.setFont("helvetica", "normal");
        doc.setFontSize(7.5);
        doc.setTextColor(100, 116, 139);

        doc.text(
          "IG Bank | Confidential account statement",
          14,
          pageHeight - 9
        );

        doc.text(
          `Page ${currentPage}`,
          pageWidth - 14,
          pageHeight - 9,
          {
            align: "right",
          }
        );
      },
    });

    doc.save(`IG-Bank-Statement-${accountNumber}.pdf`);
  };

  return <div className="customer-page"><div className="statements-card"><div className="statement-header">

    <div className="statement-header">

      <div>
        <h1 className="statement-title">
          Account Statement
        </h1>

        <p className="statement-subtitle">
          Complete transaction history
        </p>
      </div>

    </div>

    <div className="statement-actions">

      <button
        className="export-btn"
        onClick={downloadStatement}
      >
        ⬇ Export Statement
      </button>

    </div>



  </div>{loading && <p>Loading statements...</p>}{error && <p className="error">{error}</p>}{!loading && !error && statements.length === 0 && <p>No transactions found.</p>}{statements.length > 0 && <div className="table-responsive"><table><thead><tr><th>Date</th><th>Type</th><th>Description</th><th>Status</th><th>Credit</th><th>Debit</th></tr></thead><tbody>{statements.map((statement) => <tr key={statement.refId}><td>{formatDate(statement.timestamp)}</td><td>
    <span
      className={
        statement.type === "TRANSFER"
          ? "type-transfer"
          : "type-deposit"
      }
    >
      {statement.type === "TRANSFER"
        ? "↔ TRANSFER"
        : "↓ DEPOSIT"}
    </span>
  </td><td>{statement.description}</td><td>
      <span
        className={
          statement.status === "SUCCESS"
            ? "status-success"
            : "status-failed"
        }
      >
        {statement.status}
      </span>
    </td><td className="credit-amount">
      {statement.credit > 0
        ? `+ ₹${statement.credit}`
        : "-"}
    </td><td className="debit-amount">
      {statement.debit > 0
        ? `- ₹${statement.debit}`
        : "-"}
    </td></tr>)}</tbody></table></div>}</div></div>;
}
