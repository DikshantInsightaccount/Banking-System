import { useEffect, useMemo, useState } from "react";
import { useParams } from "react-router-dom";
import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";
import PageHeader from "../../../components/shared/PageHeader.jsx";
import Loader from "../../../components/shared/Loader.jsx";
import AccountCard from "../components/AccountCard.jsx";
import API from "../../../api/api.js";
import { accountService } from "../../customers/services/accountService.js";
import { formatDate } from "../../../utils/formatDate.js";

export default function AccountDetailsPage() {
  const { accountId } = useParams();

  // normalize ID once
  const id = useMemo(() => Number(accountId), [accountId]);

  const [loading, setLoading] = useState(true);
  const [mutating, setMutating] = useState(false);
  const [account, setAccount] = useState(null);
  const [error, setError] = useState("");
  const [downloadingStatement, setDownloadingStatement] = useState(false);

  const getErrMsg = (e) =>
    e?.response?.data?.message ||
    e?.response?.data?.error ||
    e?.message ||
    "Something went wrong";

  //  FETCH ACCOUNT (backend returns DIRECT object)
  const load = async () => {
    if (!id || Number.isNaN(id)) {
      setError("Invalid Account ID");
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError("");

      const data = await accountService.getById(id);

      //  DIRECT assignment (matches backend exactly)
      setAccount(data);
    } catch (e) {
      setError(getErrMsg(e));
      setAccount(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  //  ACTIVATE
  const activate = async () => {
    if (!id) return;

    try {
      setMutating(true);
      setError("");

      const updated = await accountService.activate(id);

      //  backend returns updated Account
      setAccount(updated);
    } catch (e) {
      setError(getErrMsg(e));
    } finally {
      setMutating(false);
    }
  };

  //  DEACTIVATE
  const deactivate = async () => {
    if (!id) return;

    try {
      setMutating(true);
      setError("");

      const updated = await accountService.deactivate(id);

      //  backend returns updated Account
      setAccount(updated);
    } catch (e) {
      setError(getErrMsg(e));
    } finally {
      setMutating(false);
    }
  };

  const status = account?.status; // ACTIVE / INACTIVE
  const canActivate = status !== "ACTIVE";
  const canDeactivate = status !== "INACTIVE";

  // Download Statement
  const downloadStatement = async () => {
    try {
      setDownloadingStatement(true);

      const accountNumber = account?.accountnumber;
      
      // Fetch transactions for this account
      const txRes = await API.get("/api/transactions/all");
      const allTransactions = Array.isArray(txRes.data) ? txRes.data : [];
      
      const accountTransactions = allTransactions.filter((tx) => {
        const candidates = [
          String(tx?.account?.accountnumber || "").toLowerCase(),
          String(tx?.accountnumber || "").toLowerCase(),
          String(tx?.fromnumber || "").toLowerCase(),
          String(tx?.tonumber || "").toLowerCase(),
        ];
        return candidates.some(v => v === String(accountNumber || "").toLowerCase());
      });

      // Create PDF
      const doc = new jsPDF();
      let yPosition = 20;
      const margin = 14;
      const lineHeight = 7;

      doc.setFontSize(20);
      doc.text("Account Statement", margin, yPosition);
      yPosition += 12;

      doc.setFontSize(11);
      doc.text(`Account Number: ${account?.accountnumber}`, margin, yPosition);
      yPosition += lineHeight;
      doc.text(`Account Type: ${account?.accounttype}`, margin, yPosition);
      yPosition += lineHeight;
      doc.text(`Status: ${account?.status}`, margin, yPosition);
      yPosition += lineHeight;
      doc.text(`Balance: Rs. ${account?.balance}`, margin, yPosition);
      yPosition += 8;

      // Transactions table
      autoTable(doc, {
        startY: yPosition,
        head: [["Date", "Type", "Amount", "Transaction ID"]],
        body: accountTransactions.map((tx) => {
          const txDate = formatDate(tx?.txntimestamp || tx?.timestamp || tx?.createdAt || tx?.date || "");
          const amount = tx?.amount || 0;
          const amountStr = `Rs. ${Math.abs(amount).toLocaleString("en-IN")}`;
          const txnId = tx?.txnid || tx?.refId || tx?.id || "-";

          return [txDate, tx?.txntype || "Transaction", amountStr, String(txnId)];
        }),
        theme: "grid",
        styles: { fontSize: 10 },
        headStyles: { fillColor: [22, 160, 133] },
      });

      doc.save(`statement_${accountNumber}.pdf`);
    } catch (e) {
      console.error("Statement download error:", e);
      alert("Failed to download statement");
    } finally {
      setDownloadingStatement(false);
    }
  };

  return (
    <>
      <PageHeader
        title="Account Details"
        subtitle={`Account Number: ${account?.accountnumber || accountId}`}
        right={
          <div className="d-flex gap-2">
            <button
              className="btn btn-info btn-sm"
              onClick={downloadStatement}
              disabled={loading || downloadingStatement || !account}
            >
              {downloadingStatement ? "Downloading..." : "📥 Download Statement"}
            </button>

            <button
              className="btn btn-success btn-sm"
              onClick={activate}
              disabled={loading || mutating || !canActivate}
            >
              Activate
            </button>

            <button
              className="btn btn-secondary btn-sm"
              onClick={deactivate}
              disabled={loading || mutating || !canDeactivate}
            >
              Deactivate
            </button>
          </div>
        }
      />

      {loading && <Loader />}

      {error && (
        <div className="alert alert-danger mt-2">
          {error}
        </div>
      )}

      {!loading && !error && account && (
        <AccountCard account={account} />
      )}
    </>
  );
}