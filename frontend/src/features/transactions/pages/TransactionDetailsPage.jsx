import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import PageHeader from "../../../components/shared/PageHeader.jsx";
import Loader from "../../../components/shared/Loader.jsx";
import TransactionReceipt from "../components/TransactionReceipt.jsx";
import { transactionService } from "../../customers/services/transactionService.js";

export default function TransactionDetailsPage() {
  const { id } = useParams();
  const [loading, setLoading] = useState(true);
  const [tx, setTx] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let alive = true;
    (async () => {
      try {
        setLoading(true);
        setError("");
        const data = await transactionService.getById(id);
        if (!alive) return;
        setTx(data);
      } catch (e) {
        if (!alive) return;
        setError(e.message);
      } finally {
        if (alive) setLoading(false);
      }
    })();
    return () => { alive = false; };
  }, [id]);

  return (
    <>
      <PageHeader title="Transaction Details" subtitle={`Transaction ID: ${id}`} />
      {loading ? <Loader /> : null}
      {error ? <div className="alert alert-danger">{error}</div> : null}
      {!loading && !error ? <TransactionReceipt tx={tx} /> : null}
    </>
  );
}