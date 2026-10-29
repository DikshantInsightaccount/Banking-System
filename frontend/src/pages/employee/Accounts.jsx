import { useEffect, useState } from "react";
import axios from "axios";
import AccountCard from "../../components/AccountCard";
import AccountSummary from "../../components/AccountSummary";

export default function Accounts() {
  const [listState, setListState] = useState({ status: "idle", data: null });
  const [selectedAccount, setSelectedAccount] = useState(null);

  useEffect(() => {
    let active = true;

    const fetchAccounts = async () => {
      setListState({ status: "loading", data: null });
      try {
        const res = await axios.get("http://localhost:8901/accounts");

        if (!active) return;

        const normalized = Array.isArray(res.data)
          ? res.data.map((a) => ({
              id: a.accountid ?? a.id,
              accountNumber: a.accountnumber ?? a.accountNumber,
              accountType: a.accounttype ?? a.accountType,
              balance: a.balance,
              status: a.status,
              openedAt: a.createdat ?? a.openedat,
            }))
          : [];

        setListState({ status: "success", data: normalized });
        setSelectedAccount(normalized[0] ?? null);
      } catch {
        if (active) {
          setListState({ status: "error", data: null });
        }
      }
    };

    fetchAccounts();

    return () => {
      active = false;
    };
  }, []);

  return (
    <main className="container-fluid p-4">
      <header className="mb-4">
        <h1 className="h3 fw-semibold mb-1">Accounts</h1>
        <p className="text-body-secondary mb-0">
          View and manage customer bank accounts
        </p>
      </header>

      <section className="row g-4">
        <div className="col-xl-8">
          <div className="card shadow-sm h-100">
            <div className="card-body">
              <h2 className="h5 fw-semibold mb-3">Account List</h2>

              {listState.status === "loading" && (
                <div className="spinner-border spinner-border-sm" />
              )}

              {listState.status === "error" && (
                <div className="text-danger">
                  Unable to load accounts
                </div>
              )}

              {Array.isArray(listState.data) && (
                <div className="row g-3">
                  {listState.data.map((account) => (
                    <div
                      key={account.id}
                      className="col-md-6 col-xl-4"
                      role="button"
                      onClick={() => setSelectedAccount(account)}
                    >
                      <AccountCard account={account} />
                    </div>
                  ))}
                </div>
              )}

              {listState.status === "success" &&
                listState.data?.length === 0 && (
                  <p className="text-body-secondary mb-0">
                    No accounts found
                  </p>
                )}
            </div>
          </div>
        </div>

        <div className="col-xl-4">
          <AccountSummary account={selectedAccount} />
        </div>
      </section>
    </main>
  );
}
