import React, { useState } from "react";
import { transactionService } from "../../customers/services/transactionService.js";

const DepositPage = () => {
  const [accountId, setAccountId] = useState("");
  const [amount, setAmount] = useState("");
  const [loading, setLoading] = useState(false);
  const [transaction, setTransaction] = useState(null);
  const [error, setError] = useState("");

  const handleDeposit = async (e) => {
    e.preventDefault();

    setError("");
    setTransaction(null);

    // Basic validation
    if (!accountId || accountId.trim() === "") {
      setError("Account ID is required");
      return;
    }

    if (!amount || Number(amount) <= 0) {
      setError("Amount must be greater than 0");
      return;
    }

    try {
      setLoading(true);

      const payload = {
        accountNumber: accountId,
        amount: Number(amount),
      };

      console.log("📤 Sending deposit request:", payload);
      console.log("🔑 Token:", localStorage.getItem("token") ? "✅ Present" : "❌ Missing");

      const response = await transactionService.deposit(payload);

      console.log("✅ Response:", response);
      setTransaction(response);
      setAccountId("");
      setAmount("");
    } catch (err) {
      console.error("❌ Deposit Error:", err);
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
        "Deposit failed. Try again."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <h2>Deposit Money</h2>

      <form onSubmit={handleDeposit} style={styles.form}>
        <input
          type="text"
          placeholder="Enter Account Number (e.g., SB100001)"
          value={accountId}
          onChange={(e) => {
            setAccountId(e.target.value.toUpperCase());
          }}
          style={styles.input}
        />

        <input
          type="text"
          placeholder="Enter Amount"
          value={amount}
          onChange={(e) => {
            const value = e.target.value;

            // Allow numbers + decimals
            if (/^\d*\.?\d*$/.test(value)) {
              setAmount(value);
            }
          }}
          style={styles.input}
        />

        <button type="submit" disabled={loading} style={styles.button}>
          {loading ? "Processing..." : "Deposit"}
        </button>
      </form>

      {error && <p style={styles.error}>{error}</p>}

      {transaction && (
        <div style={styles.card}>
          <h3>Transaction Success </h3>
          <p><strong>Txn ID:</strong> {transaction.txnid}</p>
          <p><strong>Amount:</strong> ₹{transaction.amount}</p>
          <p><strong>Status:</strong> {transaction.status}</p>
          <p><strong>Type:</strong> {transaction.txntype}</p>
          <p><strong>Updated Balance:</strong> ₹{transaction.account.balance}</p>
        </div>
      )}
    </div>
  );
};

export default DepositPage;

const styles = {
  container: {
    maxWidth: "400px",
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
    backgroundColor: "#28a745",
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
    background: "#f9f9f9",
  },
};