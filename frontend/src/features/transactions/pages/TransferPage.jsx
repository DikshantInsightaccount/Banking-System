import React, { useState } from "react";
import { transactionService } from "../../customers/services/transactionService.js";

const TransferPage = () => {
  const [fromAccountId, setFromAccountId] = useState("");
  const [toAccountId, setToAccountId] = useState("");
  const [amount, setAmount] = useState("");

  const [transfer, setTransfer] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleTransfer = async (e) => {
    e.preventDefault();

    setError("");
    setTransfer(null);

    // Validation
    if (!fromAccountId || fromAccountId.trim() === "") {
      setError("Invalid From Account ID");
      return;
    }

    if (!toAccountId || toAccountId.trim() === "") {
      setError("Invalid To Account ID");
      return;
    }

    if (fromAccountId === toAccountId) {
      setError("Both account IDs cannot be same");
      return;
    }

    if (!amount || Number(amount) <= 0) {
      setError("Invalid Amount");
      return;
    }

    try {
      setLoading(true);

      const payload = {
        fromAccountNumber: fromAccountId,
        toAccountNumber: toAccountId,
        amount: Number(amount),
      };

      console.log("📤 Sending transfer request:", payload);
      console.log("🔑 Token:", localStorage.getItem("token") ? "✅ Present" : "❌ Missing");

      const response = await transactionService.transfer(payload);

      console.log("Response:", response);
      setTransfer(response);

      // reset fields
      setFromAccountId("");
      setToAccountId("");
      setAmount("");

    } catch (err) {
      console.error("❌ Transfer Error:", err);
      console.error("📊 Full Error Response:", JSON.stringify(err.response?.data, null, 2));
      console.error("📊 Error Details:", {
        status: err.response?.status,
        message: err.response?.data?.message,
        error: err.response?.data?.error,
        data: err.response?.data,
        errorMessage: err.message
      });

      setError(
        err.response?.data?.message ||
        err.response?.data?.error ||
        "Transfer failed (check balance or account IDs)"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <h2>Transfer Money</h2>

      <form onSubmit={handleTransfer} style={styles.form}>
        {/* From Account */}
        <input
          type="text"
          placeholder="From Account Number (e.g., SB100001)"
          value={fromAccountId}
          onChange={(e) => {
            setFromAccountId(e.target.value.toUpperCase());
          }}
          style={styles.input}
        />

        {/* To Account */}
        <input
          type="text"
          placeholder="To Account Number (e.g., SB100002)"
          value={toAccountId}
          onChange={(e) => {
            setToAccountId(e.target.value.toUpperCase());
          }}
          style={styles.input}
        />

        {/* Amount */}
        <input
          type="text"
          placeholder="Amount"
          value={amount}
          onChange={(e) => {
            const val = e.target.value;
            if (/^\d*\.?\d*$/.test(val)) setAmount(val);
          }}
          style={styles.input}
        />

        <button type="submit" disabled={loading} style={styles.button}>
          {loading ? "Processing..." : "Transfer"}
        </button>
      </form>

      {error && <p style={styles.error}>{error}</p>}

      {transfer && (
        <div style={styles.card}>
          <h3>Transfer Successful </h3>
          <p><strong>Transfer ID:</strong> {transfer.transferid}</p>
          <p><strong>Amount:</strong> ₹{transfer.amount}</p>
          <p><strong>Status:</strong> {transfer.status}</p>

          <p>
            <strong>From Account:</strong>{" "}
            {transfer.fromAccount.accountid}
          </p>

          <p>
            <strong>To Account:</strong>{" "}
            {transfer.toAccount.accountid}
          </p>
        </div>
      )}
    </div>
  );
};

export default TransferPage;

const styles = {
  container: {
    maxWidth: "420px",
    margin: "50px auto",
    padding: "20px",
    border: "1px solid #ddd",
    borderRadius: "10px",
    textAlign: "center",
  },
  form: {
    display: "flex",
    flexDirection: "column",
    gap: "10px",
  },
  input: {
    padding: "10px",
    fontSize: "16px",
  },
  button: {
    padding: "10px",
    backgroundColor: "#6f42c1",
    color: "white",
    border: "none",
    cursor: "pointer",
  },
  error: {
    color: "red",
    marginTop: "10px",
  },
  card: {
    marginTop: "20px",
    padding: "15px",
    border: "1px solid #ccc",
    borderRadius: "8px",
    background: "#f8f9fa",
  },
};