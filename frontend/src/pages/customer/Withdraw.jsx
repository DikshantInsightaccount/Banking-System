import { useState } from "react";
import API from "../../api/api";
import "./features.css";

export default function Withdraw() {
  const [amount, setAmount] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const accountNumber = localStorage.getItem("accountnumber");

  const submit = async (event) => {
    event.preventDefault();
    if (!accountNumber || Number(amount) <= 0) {
      setMessage("Enter a valid amount.");
      return;
    }
    setLoading(true);
    try {
      await API.post("/api/transactions/withdraw", { accountNumber, amount: Number(amount) });
      setMessage("Withdrawal completed successfully.");
      setAmount("");
    } catch (error) {
      setMessage(error.response?.data?.message || "Withdrawal failed.");
    } finally {
      setLoading(false);
    }
  };

  return <div className="customer-page"><div className="feature-card"><h2>Withdraw Money</h2><form onSubmit={submit} className="form-grid"><label>Amount<input type="number" min="0.01" step="0.01" value={amount} onChange={(event) => setAmount(event.target.value)} required /></label><button type="submit" disabled={loading}>{loading ? "Processing..." : "Withdraw"}</button></form>{message && <p>{message}</p>}</div></div>;
}
