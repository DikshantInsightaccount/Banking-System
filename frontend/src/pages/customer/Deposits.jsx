import { useState } from "react";
import API from "../../api/api";
import "./features.css";

export default function Deposits() {
  const [amount, setAmount] = useState("");
  const [step, setStep] = useState(1);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const accountNumber = localStorage.getItem("accountnumber");

  const handleDetailsSubmit = (event) => {
    event.preventDefault();
    if (!Number(amount) || Number(amount) <= 0) {
      setMessage("Enter an amount greater than zero");
      return;
    }
    setMessage("");
    setStep(2);
  };

  const handleDeposit = async (event) => {
    event.preventDefault();
    if (!accountNumber) {
      setMessage("Account number not found. Please log in again.");
      return;
    }

    setLoading(true);
    try {
      const response = await API.post("/api/transactions/deposit", {
        accountNumber,
        amount: Number(amount),
      });
      setMessage(response.data?.message || `₹ ${amount} deposited successfully.`);
      setAmount("");
      setStep(3);
    } catch (error) {
      setMessage(error.response?.data?.message || "Deposit failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="deposit-page">
      <h2 className="page-title">Deposit Money</h2>
      <div className="deposit-card">
        {step === 1 && (
          <form onSubmit={handleDetailsSubmit} className="form-grid">
            <div className="form-group">
              <label htmlFor="deposit-amount">Amount</label>
              <input
                id="deposit-amount"
                type="number"
                min="0.01"
                step="0.01"
                value={amount}
                onChange={(event) => setAmount(event.target.value)}
                required
              />
            </div>
            <div className="form-actions">
              <button type="submit">Proceed</button>
            </div>
          </form>
        )}

        {step === 2 && (
          <div className="pin-section">
            <div className="summary-block">
              <div>
                <p className="label">Account</p>
                <p className="value">{accountNumber}</p>
              </div>
              <div>
                <p className="label">Amount</p>
                <p className="value">₹ {amount}</p>
              </div>
            </div>
            <form onSubmit={handleDeposit} className="pin-form">
              <button type="submit" disabled={loading}>
                {loading ? "Processing..." : "Confirm Transaction"}
              </button>
            </form>
          </div>
        )}

        {step === 3 && <div className="success-box">{message}</div>}
        {message && step !== 3 && <p className="error">{message}</p>}
      </div>
    </div>
  );
}