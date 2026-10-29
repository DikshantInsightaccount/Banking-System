import React, { useState } from "react";
import { transactionService } from "../../customers/services/transactionService.js";

const WithdrawPage = () => {
  const [accountId, setAccountId] = useState("");
  const [amount, setAmount] = useState("");
  const [transaction, setTransaction] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleWithdraw = async (e) => {
    e.preventDefault();

    setError("");
    setTransaction(null);

    // Validation
    if (!accountId || accountId.trim() === "") {
      setError("Invalid Account ID");
      return;
    }

    if (!amount || Number(amount) <= 0) {
      setError("Invalid Amount");
      return;
    }

    try {
      setLoading(true);

      const payload = {
        accountNumber: accountId,
        amount: Number(amount),
      };

      console.log("📤 Sending withdraw request:", payload);
      console.log("🔑 Token:", localStorage.getItem("token") ? "Present" : "Missing");

      const response = await transactionService.withdraw(payload);

      console.log("Response:", response);
      setTransaction(response);
      setAccountId("");
      setAmount("");
    } catch (err) {
      console.error("❌ Withdraw Error:", err);
      console.error("📊 Full Error Response:", JSON.stringify(err.response?.data, null, 2));
      console.error("📊 Error Details:", {
        status: err.response?.status,
        message: err.response?.data?.message,
        error: err.response?.data?.error,
        data: err.response?.data,
        errorMessage: err.message
      });

      //  Handle insufficient balance or backend errors
      setError(
        err.response?.data?.message || 
        err.response?.data?.error ||
        "Withdrawal failed"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <h2>Withdraw Money</h2>

      <form onSubmit={handleWithdraw} style={styles.form}>
        {/* Account Number */}
        <input
          type="text"
          placeholder="Enter Account Number (e.g., SB100001)"
          value={accountId}
          onChange={(e) => {
            setAccountId(e.target.value.toUpperCase());
          }}
          style={styles.input}
        />

        {/* Amount */}
        <input
          type="text"
          placeholder="Enter Amount"
          value={amount}
          onChange={(e) => {
            const value = e.target.value;
            if (/^\d*\.?\d*$/.test(value)) {
              setAmount(value);
            }
          }}
          style={styles.input}
        />

        <button type="submit" disabled={loading} style={styles.button}>
          {loading ? "Processing..." : "Withdraw"}
        </button>
      </form>

      {error && <p style={styles.error}>{error}</p>}

      {transaction && (
        <div style={styles.card}>
          <h3>Transaction Successful </h3>
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

export default WithdrawPage;

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
    backgroundColor: "#dc3545",
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