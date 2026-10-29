import { useState } from "react";
import { transactionService } from "../../../features/customers/services/transactionService.js";

export default function DepositForm() {
  const [accountId, setAccountId] = useState("");
  const [amount, setAmount] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess(false);

    try {
      setLoading(true);
      await transactionService.deposit({
        accountNumber: accountId,
        amount: Number(amount)
      });
      setSuccess(true);
      setAccountId("");
      setAmount("");
    } catch (err) {
      setError(err.response?.data?.message || "Deposit failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="card p-4" onSubmit={handleSubmit}>
      <h5 className="mb-3">Deposit Amount</h5>

      {error && <div className="alert alert-danger">{error}</div>}
      {success && <div className="alert alert-success">Deposit successful!</div>}

      <div className="mb-3">
        <label className="form-label">Account ID</label>
        <input
          type="number"
          className="form-control"
          value={accountId}
          onChange={(e) => setAccountId(e.target.value)}
          required
          disabled={loading}
        />
      </div>

      <div className="mb-3">
        <label className="form-label">Amount</label>
        <input
          type="number"
          className="form-control"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          required
          disabled={loading}
        />
      </div>

      <button className="btn btn-success w-100" type="submit" disabled={loading}>
        {loading ? "Processing..." : "Deposit"}
      </button>
    </form>
  );
}