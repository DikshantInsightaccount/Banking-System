import {
  useCallback,
  useEffect,
  useState
} from "react";

import API from "../../api/api";
import "./features.css";

export default function FixedDeposit() {
  const [amount, setAmount] =
    useState("");

  const [tenureMonths, setTenureMonths] =
    useState("");

  const [password, setPassword] =
    useState("");

  const [idempotencyKey, setIdempotencyKey] =
    useState(() => crypto.randomUUID());

  const [fixedDeposits, setFixedDeposits] =
    useState([]);

  const [message, setMessage] =
    useState("");

  const [messageType, setMessageType] =
    useState("");

  const [loading, setLoading] =
    useState(false);

  const [
    loadingDeposits,
    setLoadingDeposits
  ] = useState(true);

  const accountId =
    localStorage.getItem("accountid");

  const userId =
    localStorage.getItem("userid");

  const INTEREST_RATE = 7.5;

  // =====================================================
  // FORMATTING HELPERS
  // =====================================================

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

    const normalizedValue =
      String(value).includes("T")
        ? value
        : `${value}T00:00:00`;

    const date =
      new Date(normalizedValue);

    if (Number.isNaN(date.getTime())) {
      return "Not available";
    }

    return date.toLocaleDateString(
      "en-IN",
      {
        day: "2-digit",
        month: "short",
        year: "numeric"
      }
    );
  };

  const calculateEstimatedMaturity = (
    principal,
    months,
    interestRate
  ) => {
    const numericPrincipal =
      Number(principal || 0);

    const numericMonths =
      Number(months || 0);

    const numericRate =
      Number(interestRate || 0);

    if (
      numericPrincipal <= 0 ||
      numericMonths <= 0 ||
      numericRate < 0
    ) {
      return 0;
    }

    /*
     * Simple-interest estimate:
     *
     * Interest =
     * Principal × Rate × Time in years
     */
    const interest =
      numericPrincipal *
      (numericRate / 100) *
      (numericMonths / 12);

    return numericPrincipal + interest;
  };

  const getMaturityAmount = (fd) => {
    /*
     * Prefer the backend-calculated maturity value.
     * Fall back to a frontend estimate when the response
     * does not provide one.
     */
    const backendMaturity =
      fd.maturityamount ??
      fd.maturityAmount;

    if (
      backendMaturity !== null &&
      backendMaturity !== undefined
    ) {
      return Number(backendMaturity);
    }

    return calculateEstimatedMaturity(
      fd.principalamount ??
      fd.principalAmount,

      fd.tenuremonths ??
      fd.tenureMonths,

      fd.interestrate ??
      fd.interestRate
    );
  };

  const getInterestEarned = (fd) => {
    const principal =
      Number(
        fd.principalamount ??
        fd.principalAmount ??
        0
      );

    return Math.max(
      0,
      getMaturityAmount(fd) - principal
    );
  };

  const getRemainingDays = (fd) => {
    const maturityDate =
      fd.maturitydate ??
      fd.maturityDate;

    if (!maturityDate) {
      return null;
    }

    const normalizedValue =
      String(maturityDate).includes("T")
        ? maturityDate
        : `${maturityDate}T00:00:00`;

    const endDate =
      new Date(normalizedValue);

    if (Number.isNaN(endDate.getTime())) {
      return null;
    }

    const today =
      new Date();

    const difference =
      endDate.getTime() -
      today.getTime();

    return Math.max(
      0,
      Math.ceil(
        difference /
        (1000 * 60 * 60 * 24)
      )
    );
  };

  // =====================================================
  // LOAD EXISTING FIXED DEPOSITS
  // =====================================================

  const loadFixedDeposits =
    useCallback(async () => {
      if (!accountId) {
        setLoadingDeposits(false);
        return;
      }

      setLoadingDeposits(true);

      try {
        const response =
          await API.get(
            `/deposits/fixed/account/${accountId}`,
            {
              params: {
                refresh: Date.now()
              }
            }
          );

        const deposits =
          Array.isArray(response.data)
            ? response.data
            : [];

        /*
         * Show the latest FD first.
         */
        const sortedDeposits =
          [...deposits].sort(
            (first, second) => {
              const firstId =
                Number(
                  first.fdid ??
                  first.fdId ??
                  0
                );

              const secondId =
                Number(
                  second.fdid ??
                  second.fdId ??
                  0
                );

              return secondId - firstId;
            }
          );

        setFixedDeposits(
          sortedDeposits
        );
      } catch (error) {
        console.error(
          "Failed to load fixed deposits:",
          error
        );

        setMessage(
          error.response?.data?.message ||
          error.response?.data?.error ||
          "Unable to load fixed deposits."
        );

        setMessageType("error");
      } finally {
        setLoadingDeposits(false);
      }
    }, [accountId]);

  useEffect(() => {
    loadFixedDeposits();
  }, [loadFixedDeposits]);

  // =====================================================
  // CREATE FIXED DEPOSIT
  // =====================================================

  const submit = async (event) => {
    event.preventDefault();

    if (loading) {
      return;
    }

    setMessage("");
    setMessageType("");

    const numericAmount =
      Number(amount);

    const numericTenure =
      Number(tenureMonths);

    if (!accountId || !userId) {
      setMessage(
        "Account information is unavailable. Please log in again."
      );

      setMessageType("error");
      return;
    }

    if (
      !Number.isFinite(numericAmount) ||
      numericAmount <= 0
    ) {
      setMessage(
        "Enter an amount greater than zero."
      );

      setMessageType("error");
      return;
    }

    if (
      !Number.isInteger(numericTenure) ||
      numericTenure <= 0
    ) {
      setMessage(
        "Enter a valid tenure in whole months."
      );

      setMessageType("error");
      return;
    }

    if (!password.trim()) {
      setMessage(
        "Enter your transaction password."
      );

      setMessageType("error");
      return;
    }

    setLoading(true);

    try {
      const payload = {
        userId:
          Number(userId),

        password,

        idempotencyKey,

        fd: {
          principalamount:
            numericAmount,

          tenuremonths:
            numericTenure,

          interestrate:
            INTEREST_RATE
        }
      };

      console.log(
        "FD creation request:",
        payload
      );

      const response =
        await API.post(
          `/deposits/fixed/${accountId}`,
          payload
        );

      console.log(
        "FD creation response:",
        response.data
      );

      setMessage(
        "Fixed Deposit created successfully."
      );

      setMessageType("success");

      setAmount("");
      setTenureMonths("");
      setPassword("");

      /*
       * Generate a new idempotency key only after the
       * backend confirms successful creation.
       *
       * Failed retries continue using the existing key.
       */
      setIdempotencyKey(
        crypto.randomUUID()
      );

      /*
       * Reload the management section immediately so the
       * newly created FD appears without a page refresh.
       */
      await loadFixedDeposits();
    } catch (error) {
      console.error(
        "FD creation failed:",
        error
      );

      setMessage(
        error.response?.data?.message ||
        error.response?.data?.error ||
        "Unable to create the fixed deposit."
      );

      setMessageType("error");
    } finally {
      setLoading(false);
    }
  };

  const estimatedMaturity =
    calculateEstimatedMaturity(
      amount,
      tenureMonths,
      INTEREST_RATE
    );

  const estimatedInterest =
    Math.max(
      0,
      estimatedMaturity -
      Number(amount || 0)
    );

  // =====================================================
  // PAGE
  // =====================================================

  return (
    <div className="customer-page">
      {/* ==============================================
          CREATE FIXED DEPOSIT
      ============================================== */}

      <div className="feature-card">
        <div className="feature-header">
          <div>
            <span className="feature-eyebrow">
              SAVINGS PLAN
            </span>

            <h2>
              Fixed Deposit
            </h2>

            <p>
              Invest once and earn predictable
              returns until maturity.
            </p>
          </div>
        </div>

        <form
          onSubmit={submit}
          className="form-grid fd-create-form"
        >
          <label>
            <span>
              Investment Amount
            </span>

            <input
              type="number"
              min="0.01"
              step="0.01"
              placeholder="Enter investment amount"
              value={amount}
              onChange={(event) => {
                setAmount(
                  event.target.value
                );

                setMessage("");
                setMessageType("");
              }}
              disabled={loading}
              required
            />
          </label>

          <label>
            <span>
              Tenure
            </span>

            <input
              type="number"
              min="1"
              step="1"
              placeholder="Number of months"
              value={tenureMonths}
              onChange={(event) => {
                setTenureMonths(
                  event.target.value
                );

                setMessage("");
                setMessageType("");
              }}
              disabled={loading}
              required
            />
          </label>

          <label>
            <span>
              Transaction Password
            </span>

            <input
              type="password"
              placeholder="Enter transaction password"
              value={password}
              onChange={(event) => {
                setPassword(
                  event.target.value
                );

                setMessage("");
                setMessageType("");
              }}
              autoComplete="current-password"
              disabled={loading}
              required
            />
          </label>

          <div className="fd-create-actions">
            <button
              type="submit"
              disabled={
                loading ||
                !accountId
              }
            >
              {loading
                ? "Creating..."
                : "Create Fixed Deposit"}
            </button>
          </div>
        </form>

        {Number(amount) > 0 &&
          Number(tenureMonths) > 0 && (
            <div className="fd-estimate-panel">
              <div>
                <span>
                  Interest Rate
                </span>

                <strong>
                  {INTEREST_RATE}%
                </strong>
              </div>

              <div>
                <span>
                  Estimated Interest
                </span>

                <strong>
                  {formatCurrency(
                    estimatedInterest
                  )}
                </strong>
              </div>

              <div>
                <span>
                  Estimated Maturity
                </span>

                <strong>
                  {formatCurrency(
                    estimatedMaturity
                  )}
                </strong>
              </div>
            </div>
          )}

        {message && (
          <p
            className={
              messageType === "success"
                ? "feature-message success"
                : "feature-message error"
            }
            role={
              messageType === "error"
                ? "alert"
                : "status"
            }
          >
            {message}
          </p>
        )}
      </div>

      {/* ==============================================
          FIXED DEPOSIT MANAGEMENT
      ============================================== */}

      <section className="fd-management-section">
        <div className="fd-section-header">
          <div>
            <span className="feature-eyebrow">
              INVESTMENT PORTFOLIO
            </span>

            <h2>
              My Fixed Deposits
            </h2>

            <p>
              Review principal amounts, maturity
              values and investment status.
            </p>
          </div>

          {!loadingDeposits &&
            fixedDeposits.length > 0 && (
              <span className="fd-count-chip">
                {fixedDeposits.length}{" "}
                {fixedDeposits.length === 1
                  ? "deposit"
                  : "deposits"}
              </span>
            )}
        </div>

        {loadingDeposits ? (
          <div className="fd-empty-state">
            Loading fixed deposits...
          </div>
        ) : fixedDeposits.length === 0 ? (
          <div className="fd-empty-state">
            <div className="fd-empty-icon">
              🏦
            </div>

            <h3>
              No fixed deposits yet
            </h3>

            <p>
              Create your first fixed deposit
              using the form above.
            </p>
          </div>
        ) : (
          <div className="fd-list">
            {fixedDeposits.map((fd) => {
              const fdId =
                fd.fdid ??
                fd.fdId;

              const principal =
                Number(
                  fd.principalamount ??
                  fd.principalAmount ??
                  0
                );

              const tenure =
                Number(
                  fd.tenuremonths ??
                  fd.tenureMonths ??
                  0
                );

              const interestRate =
                Number(
                  fd.interestrate ??
                  fd.interestRate ??
                  0
                );

              const maturityAmount =
                getMaturityAmount(fd);

              const interestEarned =
                getInterestEarned(fd);

              const maturityDate =
                fd.maturitydate ??
                fd.maturityDate;

              const openedDate =
                fd.startdate ??
                fd.startDate ??
                fd.createdat ??
                fd.createdAt;

              const remainingDays =
                getRemainingDays(fd);

              const status =
                String(
                  fd.status || ""
                ).toUpperCase();

              const isActive =
                status === "ACTIVE";

              const isMatured =
                status === "MATURED" ||
                status === "CLOSED";

              return (
                <article
                  className="fd-plan-card"
                  key={fdId}
                >
                  <div className="fd-plan-top">
                    <div>
                      <span className="fd-plan-id">
                        FD #{fdId}
                      </span>

                      <h3>
                        {formatCurrency(
                          principal
                        )}
                      </h3>

                      <p>
                        Principal investment
                      </p>
                    </div>

                    <span
                      className={
                        isActive
                          ? "fd-status fd-status-active"
                          : isMatured
                            ? "fd-status fd-status-matured"
                            : "fd-status fd-status-inactive"
                      }
                    >
                      {status || "UNKNOWN"}
                    </span>
                  </div>

                  <div className="fd-plan-details">
                    <div>
                      <span>
                        Interest Rate
                      </span>

                      <strong>
                        {interestRate}%
                      </strong>
                    </div>

                    <div>
                      <span>
                        Tenure
                      </span>

                      <strong>
                        {tenure} months
                      </strong>
                    </div>

                    <div>
                      <span>
                        Interest Earned
                      </span>

                      <strong className="fd-positive-value">
                        {formatCurrency(
                          interestEarned
                        )}
                      </strong>
                    </div>

                    <div>
                      <span>
                        Maturity Amount
                      </span>

                      <strong>
                        {formatCurrency(
                          maturityAmount
                        )}
                      </strong>
                    </div>
                  </div>

                  <div className="fd-date-row">
                    <div>
                      <span>
                        Investment Date
                      </span>

                      <strong>
                        {formatDate(
                          openedDate
                        )}
                      </strong>
                    </div>

                    <div>
                      <span>
                        Maturity Date
                      </span>

                      <strong>
                        {formatDate(
                          maturityDate
                        )}
                      </strong>
                    </div>

                    <div>
                      <span>
                        Time Remaining
                      </span>

                      <strong>
                        {remainingDays === null
                          ? "Not available"
                          : remainingDays === 0
                            ? "Maturity reached"
                            : `${remainingDays} days`}
                      </strong>
                    </div>
                  </div>

                  <div className="fd-maturity-summary">
                    <div>
                      <span>
                        At maturity, this deposit
                        is estimated to return
                      </span>

                      <strong>
                        {formatCurrency(
                          maturityAmount
                        )}
                      </strong>
                    </div>

                    <div className="fd-return-indicator">
                      +{" "}
                      {formatCurrency(
                        interestEarned
                      )}{" "}
                      return
                    </div>
                  </div>
                </article>
              );
            })}
          </div>
        )}
      </section>
    </div>
  );
}