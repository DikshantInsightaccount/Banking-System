import { useState } from "react";
import { transactionService } from "../../../features/customers/services/transactionService.js";

export default function TransferForm() {
  const [fromAccountId, setFromAccountId] = useState("");
  const [toAccountId, setToAccountId] = useState("");
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
      await transactionService.transfer({
        fromAccountNumber: fromAccountId,
        toAccountNumber: toAccountId,
        amount: Number(amount)
      });
      setSuccess(true);
      setFromAccountId("");
      setToAccountId("");
      setAmount("");
    } catch (err) {
      setError(err.response?.data?.message || "Transfer failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="card p-4" onSubmit={handleSubmit}>
      <h5 className="mb-3">Transfer Funds</h5>

      {error && <div className="alert alert-danger">{error}</div>}
      {success && <div className="alert alert-success">Transfer successful!</div>}

      <div className="mb-3">
        <label className="form-label">From Account ID</label>
        <input
          type="number"
          className="form-control"
          value={fromAccountId}
          onChange={(e) => setFromAccountId(e.target.value)}
          required
          disabled={loading}
        />
      </div>

      <div className="mb-3">
        <label className="form-label">To Account ID</label>
        <input
          type="number"
          className="form-control"
          value={toAccountId}
          onChange={(e) => setToAccountId(e.target.value)}
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

      <button className="btn btn-primary w-100" type="submit" disabled={loading}>
        {loading ? "Processing..." : "Transfer"}
      </button>
    </form>
  );
          onChange={(e) => setAmount(e.target.value)}
          required
        />
      </div>

      <button className="btn btn-primary w-100" type="submit">
        Transfer
      </button>
    </form>
  );
}
