import { useCallback, useEffect, useState } from "react";
import API from "../../api/api";
import "./features.css";

export default function RecurringDeposit() {
  const [monthlyAmount, setMonthlyAmount] = useState("");
  const [tenureMonths, setTenureMonths] = useState("");
  const [createPassword, setCreatePassword] = useState("");

  const [idempotencyKey, setIdempotencyKey] =
    useState(() => crypto.randomUUID());

  const [recurringDeposits, setRecurringDeposits] =
    useState([]);

  const [paymentPasswords, setPaymentPasswords] =
    useState({});

  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [loadingDeposits, setLoadingDeposits] =
    useState(true);

  const [payingRdId, setPayingRdId] =
    useState(null);

  const accountId =
    localStorage.getItem("accountid");

  const userId =
    localStorage.getItem("userid");

  const formatCurrency = (value) =>
    `₹${Number(value || 0).toLocaleString(
      "en-IN",
      {
        minimumFractionDigits: 0,
        maximumFractionDigits: 2
      }
    )}`;

  const formatDate = (value) => {
    if (!value) {
      return "Not available";
    }

    return new Date(
      `${value}T00:00:00`
    ).toLocaleDateString("en-IN", {
      day: "2-digit",
      month: "short",
      year: "numeric"
    });
  };

  const loadRecurringDeposits =
    useCallback(async () => {
      if (!accountId) {
        setLoadingDeposits(false);
        return;
      }

      setLoadingDeposits(true);

      try {
        const response = await API.get(
          `/deposits/recurring/account/${accountId}`
        );

        setRecurringDeposits(
          Array.isArray(response.data)
            ? response.data
            : []
        );
      } catch (error) {
        console.error(
          "Failed to load recurring deposits:",
          error
        );

        setMessage(
          error.response?.data?.message ||
          error.response?.data?.error ||
          "Unable to load recurring deposits."
        );
      } finally {
        setLoadingDeposits(false);
      }
    }, [accountId]);

  useEffect(() => {
    loadRecurringDeposits();
  }, [loadRecurringDeposits]);

  const submit = async (event) => {
    event.preventDefault();

    setMessage("");

    if (
      !accountId ||
      !userId ||
      Number(monthlyAmount) <= 0 ||
      Number(tenureMonths) <= 0 ||
      !createPassword.trim()
    ) {
      setMessage(
        "Enter a valid monthly amount, tenure and password."
      );

      return;
    }

    setLoading(true);

    try {
      const payload = {
        userId: Number(userId),
        password: createPassword,
        idempotencyKey,

        rd: {
          monthlyamount:
            Number(monthlyAmount),

          tenuremonths:
            Number(tenureMonths),

          interestrate: 7.5
        }
      };

      await API.post(
        `/deposits/recurring/${accountId}`,
        payload
      );

      setMessage(
        "Recurring Deposit created successfully."
      );

      setMonthlyAmount("");
      setTenureMonths("");
      setCreatePassword("");

      /*
       * Generate a new key only after successful creation.
       * A failed retry keeps using the same key.
       */
      setIdempotencyKey(
        crypto.randomUUID()
      );

      await loadRecurringDeposits();
    } catch (error) {
      console.error(
        "RD creation failed:",
        error
      );

      setMessage(
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Unable to create recurring deposit."
      );
    } finally {
      setLoading(false);
    }
  };

  const updatePaymentPassword = (
    rdId,
    value
  ) => {
    setPaymentPasswords(
      (currentPasswords) => ({
        ...currentPasswords,
        [rdId]: value
  }));
  };

  const payInstallment = async (rd) => {
    const password =
      paymentPasswords[rd.rdid] || "";

    setMessage("");

    if (!userId) {
      setMessage(
        "User session is unavailable. Please log in again."
      );

      return;
    }

    if (!password.trim()) {
      setMessage(
        "Enter your transaction password before paying the installment."
      );

      return;
    }

    if (!rd.nextduedate) {
      setMessage(
        "This recurring deposit does not have an outstanding installment."
      );

      return;
    }

    setPayingRdId(rd.rdid);

    try {
      const payload = {
        userId: Number(userId),
        password,

        /*
         * Send the exact currently expected due date.
         * The backend generates:
         *
         * RD_INSTALLMENT:{rdId}:{dueDate}
         */
        dueDate: rd.nextduedate
      };

      const response = await API.post(
        `/deposits/recurring/${rd.rdid}/pay`,
        payload
      );

      setMessage(
        `Installment paid successfully for RD #${response.data.rdid}.`
      );

      setPaymentPasswords(
        (currentPasswords) => ({
          ...currentPasswords,
          [rd.rdid]: ""
        })
      );

      await loadRecurringDeposits();
    } catch (error) {
      console.error(
        "RD installment payment failed:",
        error
      );

      setMessage(
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Unable to pay the recurring deposit installment."
      );
    } finally {
      setPayingRdId(null);
    }
  };

  return (
    <div className="customer-page">
      <div className="feature-card">
        <div className="feature-header">
          <div>
            <span className="feature-eyebrow">
              SAVINGS PLAN
            </span>

            <h2>Recurring Deposit</h2>

            <p>
              Create and manage your monthly
              savings plans.
            </p>
          </div>
        </div>

        <form
  onSubmit={submit}
  className="form-grid rd-create-form"
>
  <label>
    <span>Monthly Amount</span>

    <input
      type="number"
      min="0.01"
      step="0.01"
      placeholder="Enter monthly amount"
      value={monthlyAmount}
      onChange={(event) =>
        setMonthlyAmount(event.target.value)
      }
      required
    />
  </label>

  <label>
    <span>Tenure</span>

    <input
      type="number"
      min="1"
      placeholder="Number of months"
      value={tenureMonths}
      onChange={(event) =>
        setTenureMonths(event.target.value)
      }
      required
    />
  </label>

  <label>
    <span>Transaction Password</span>

    <input
      type="password"
      placeholder="Enter transaction password"
      value={createPassword}
      onChange={(event) =>
        setCreatePassword(event.target.value)
      }
      autoComplete="current-password"
      required
    />
  </label>

  <div className="rd-create-actions">
    <button
      type="submit"
      disabled={loading}
    >
      {loading
        ? "Creating..."
        : "Create Recurring Deposit"}
    </button>
  </div>
</form>

        {message && (
          <p className="feature-message">
            {message}
          </p>
        )}
      </div>

      <section className="rd-management-section">
        <div className="rd-section-header">
          <div>
            <span className="feature-eyebrow">
              ACTIVE PLANS
            </span>

            <h2>My Recurring Deposits</h2>

            <p>
              Review upcoming installments and
              payment progress.
            </p>
          </div>
        </div>

        {loadingDeposits ? (
          <div className="rd-empty-state">
            Loading recurring deposits...
          </div>
        ) : recurringDeposits.length === 0 ? (
          <div className="rd-empty-state">
            <div className="rd-empty-icon">
              📈
            </div>

            <h3>No recurring deposits yet</h3>

            <p>
              Create your first recurring deposit
              using the form above.
            </p>
          </div>
        ) : (
          <div className="rd-list">
            {recurringDeposits.map((rd) => {
              const paidMonths =
                Number(rd.paidmonths || 0);

              const tenure =
                Number(rd.tenuremonths || 0);

              const progress =
                tenure > 0
                  ? Math.min(
                    100,
                    Math.round(
                      (paidMonths / tenure) *
                      100
                    )
                  )
                  : 0;

              const status =
                String(
                  rd.status || ""
                ).toUpperCase();

              const isActive =
                status === "ACTIVE";

              const isPaying =
                payingRdId === rd.rdid;

              return (
                <article
                  className="rd-plan-card"
                  key={rd.rdid}
                >
                  <div className="rd-plan-top">
                    <div>
                      <span className="rd-plan-id">
                        RD #{rd.rdid}
                      </span>

                      <h3>
                        {formatCurrency(
                          rd.monthlyamount
                        )}
                        <small> / month</small>
                      </h3>
                    </div>

                    <span
                      className={
                        isActive
                          ? "rd-status rd-status-active"
                          : "rd-status rd-status-inactive"
                      }
                    >
                      {status || "UNKNOWN"}
                    </span>
                  </div>

                  <div className="rd-plan-details">
                    <div>
                      <span>Interest Rate</span>
                      <strong>
                        {rd.interestrate}%
                      </strong>
                    </div>

                    <div>
                      <span>Tenure</span>
                      <strong>
                        {tenure} months
                      </strong>
                    </div>

                    <div>
                      <span>Paid Installments</span>
                      <strong>
                        {paidMonths} of {tenure}
                      </strong>
                    </div>

                    <div>
                      <span>Next Due Date</span>
                      <strong>
                        {formatDate(
                          rd.nextduedate
                        )}
                      </strong>
                    </div>
                  </div>

                  <div className="rd-progress-header">
                    <span>Payment progress</span>
                    <strong>{progress}%</strong>
                  </div>

                  <div
                    className="rd-progress"
                    role="progressbar"
                    aria-label={`RD ${rd.rdid} payment progress`}
                    aria-valuemin="0"
                    aria-valuemax="100"
                    aria-valuenow={progress}
                  >
                    <span
                      style={{
                        width: `${progress}%`
                      }}
                    />
                  </div>

                  {isActive &&
                    rd.nextduedate && (
                      <div className="rd-payment-panel">
                        <label>
                          Transaction Password

                          <input
                            type="password"
                            value={
                              paymentPasswords[
                                rd.rdid
                              ] || ""
                            }
                            onChange={(event) =>
                              updatePaymentPassword(
                                rd.rdid,
                                event.target.value
                              )
                            }
                            autoComplete="current-password"
                            placeholder="Enter password"
                          />
                        </label>

                        <button
                          type="button"
                          disabled={isPaying}
                          onClick={() =>
                            payInstallment(rd)
                          }
                        >
                          {isPaying
                            ? "Processing..."
                            : `Pay ${formatCurrency(
                              rd.monthlyamount
                            )}`}
                        </button>
                      </div>
                    )}
                </article>
              );
            })}
          </div>
        )}
      </section>
    </div>
  );
}