import { useCallback, useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import API from "../../api/api";
import { formatDate } from "../../utils/formatDate";
import "./customerDashboard.css";

import {
    ResponsiveContainer,
    AreaChart,
    Area,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip
} from "recharts";

const getTransactionDate = (transaction) =>
    transaction.timestamp ||
    transaction.txntimestamp ||
    transaction.createdat ||
    transaction.transactiondate;

const formatCurrency = (value) =>
    `₹ ${Number(value || 0).toLocaleString("en-IN", {
        minimumFractionDigits: 0,
        maximumFractionDigits: 2
    })}`;

const getTransactionAmount = (transaction) => {
    if (transaction.amount !== undefined) {
        return Number(transaction.amount) || 0;
    }

    return (
        Number(transaction.credit || 0) -
        Number(transaction.debit || 0)
    );
};

function BalanceTooltip({
    active,
    payload,
    label
}) {
    if (
        !active ||
        !payload?.length
    ) {
        return null;
    }

    const item =
        payload[0]?.payload;

    if (!item) {
        return null;
    }

    const isCredit =
        Number(
            item.credit || 0
        ) > 0;

    const amount =
        isCredit
            ? Number(
                item.credit || 0
            )
            : Number(
                item.debit || 0
            );

    return (
        <div className="chart-tooltip">
            <span className="chart-tooltip-date">
                {item.fullDate}
            </span>

            <strong className="chart-tooltip-title">
                {item.description}
            </strong>

            <span
                className={
                    isCredit
                        ? "chart-tooltip-amount credit"
                        : "chart-tooltip-amount debit"
                }
            >
                {isCredit ? "+" : "-"}{" "}
                {formatCurrency(amount)}
            </span>

            <div className="chart-tooltip-divider" />

            <span className="chart-tooltip-label">
                Balance after transaction
            </span>

            <strong className="chart-tooltip-balance">
                {formatCurrency(
                    item.balance
                )}
            </strong>
        </div>
    );
}

const toLocalDateTimeString = (date = new Date()) => {
    const pad = (value, length = 2) =>
        String(value).padStart(length, "0");

    return [
        date.getFullYear(),
        "-",
        pad(date.getMonth() + 1),
        "-",
        pad(date.getDate()),
        "T",
        pad(date.getHours()),
        ":",
        pad(date.getMinutes()),
        ":",
        pad(date.getSeconds()),
        ".",
        pad(date.getMilliseconds(), 3)
    ].join("");
};

export default function CustomerDashboard() {
    const navigate = useNavigate();
    const location = useLocation();
    const userId = localStorage.getItem("userid");

    const [transactions, setTransactions] = useState([]);
    const [accountDetails, setAccountDetails] = useState(null);
    const [allTransactions, setAllTransactions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [dashboardError, setDashboardError] = useState("");

    const fetchDashboardData =
        useCallback(async () => {
            if (!userId) {
                setDashboardError(
                    "User information is unavailable. Please log in again."
                );

                setLoading(false);
                return;
            }

            setLoading(true);
            setDashboardError("");

            try {
                const customerResponse =
                    await API.get(
                        `/api/customers/by-user/${userId}`
                    );

                const customerId =
                    customerResponse.data?.customerid;

                if (!customerId) {
                    throw new Error(
                        "Customer record was not found."
                    );
                }

                const accountsResponse =
                    await API.get(
                        `/accounts/customer/${customerId}`,
                        {
                            params: {
                                refresh: Date.now()
                            }
                        }
                    );

                const accounts =
                    Array.isArray(accountsResponse.data)
                        ? accountsResponse.data
                        : [];

                const account =
                    accounts[0];

                if (!account) {
                    throw new Error(
                        "No account is linked to this customer."
                    );
                }

                /*
                 * The backend accepts LocalDateTime.
                 * Generate the browser's current local time
                 * without converting it to UTC.
                 */
                const statementEndTime =
                    toLocalDateTimeString();

                const statementResponse =
                    await API.get(
                        "/api/transactions/statement",
                        {
                            params: {
                                accountNumber:
                                    account.accountnumber,

                                from:
                                    "2000-01-01T00:00:00",

                                to:
                                    statementEndTime,

                                refresh:
                                    Date.now()
                            }
                        }
                    );

                const statementItems =
                    Array.isArray(statementResponse.data)
                        ? statementResponse.data
                        : [];

                console.log(
                    "Statement request end time:",
                    statementEndTime
                );

                console.log(
                    "Statement API response:",
                    statementItems
                );

                const sortedTransactions =
                    [...statementItems]
                        .filter((transaction) =>
                            Boolean(
                                getTransactionDate(
                                    transaction
                                )
                            )
                        )
                        .sort((first, second) =>
                            new Date(
                                getTransactionDate(second)
                            ).getTime() -
                            new Date(
                                getTransactionDate(first)
                            ).getTime()
                        );

                setAccountDetails(account);

                setAllTransactions(
                    sortedTransactions
                );

                setTransactions(
                    sortedTransactions.slice(0, 5)
                );

                console.log(
                    "Dashboard refreshed:",
                    {
                        balance:
                            account.balance,

                        statementCount:
                            sortedTransactions.length,

                        latestTransaction:
                            sortedTransactions[0]
                    }
                );
            } catch (error) {
                console.error(
                    "Dashboard error:",
                    error
                );

                setDashboardError(
                    error.response?.data?.message ||
                    error.response?.data?.error ||
                    error.message ||
                    "Unable to load dashboard information."
                );
            } finally {
                setLoading(false);
            }
        }, [userId]);

    useEffect(() => {
        fetchDashboardData();
    }, [
        fetchDashboardData,
        location.key,
        location.state?.refreshDashboard
    ]);

    useEffect(() => {
        const handleWindowFocus = () => {
            fetchDashboardData();
        };

        window.addEventListener(
            "focus",
            handleWindowFocus
        );

        return () => {
            window.removeEventListener(
                "focus",
                handleWindowFocus
            );
        };
    }, [fetchDashboardData]);

    const totalCredits = useMemo(
        () =>
            allTransactions.reduce(
                (sum, transaction) =>
                    sum +
                    Number(
                        transaction.credit || 0
                    ),
                0
            ),
        [allTransactions]
    );

    const totalDebits = useMemo(
        () =>
            allTransactions.reduce(
                (sum, transaction) =>
                    sum +
                    Number(
                        transaction.debit || 0
                    ),
                0
            ),
        [allTransactions]
    );

    const netCashFlow =
        totalCredits - totalDebits;

    const savingsRate =
        totalCredits > 0
            ? Math.max(
                0,
                Math.min(
                    100,
                    Math.round(
                        (netCashFlow /
                            totalCredits) *
                        100
                    )
                )
            )
            : 0;

    const financialScore = Math.max(
        0,
        Math.min(
            100,
            50 +
            (netCashFlow > 0 ? 20 : 0) +
            (
                Number(
                    accountDetails?.balance || 0
                ) > 5000
                    ? 15
                    : 0
            ) +
            (savingsRate > 50 ? 15 : 0)
        )
    );

    const scoreLabel =
        financialScore >= 85
            ? "Excellent"
            : financialScore >= 70
                ? "Good"
                : financialScore >= 50
                    ? "Fair"
                    : "Needs attention";

    const savingsLabel =
        savingsRate >= 75
            ? "Excellent saving habit"
            : savingsRate >= 50
                ? "Healthy saving habit"
                : savingsRate > 0
                    ? "Savings can improve"
                    : "No credit activity yet";

    const cashFlowStatus =
        netCashFlow > 0
            ? "Positive"
            : netCashFlow < 0
                ? "Negative"
                : "Balanced";

    const transferCount =
        allTransactions.filter(
            (transaction) =>
                String(
                    transaction.type ||
                    transaction.txntype ||
                    ""
                ).toUpperCase() ===
                "TRANSFER"
        ).length;

    const chartData = useMemo(() => {
        const chronologicalTransactions =
            [...allTransactions]
                .filter((transaction) =>
                    Boolean(
                        getTransactionDate(transaction)
                    )
                )
                .sort((first, second) =>
                    new Date(
                        getTransactionDate(first)
                    ).getTime() -
                    new Date(
                        getTransactionDate(second)
                    ).getTime()
                );

        if (
            chronologicalTransactions.length === 0
        ) {
            return [];
        }

        const currentBalance =
            Number(
                accountDetails?.balance || 0
            );

        const statementNetMovement =
            chronologicalTransactions.reduce(
                (sum, transaction) =>
                    sum +
                    Number(
                        transaction.credit || 0
                    ) -
                    Number(
                        transaction.debit || 0
                    ),
                0
            );

        /*
         * Reconstruct the balance before the first
         * statement item.
         */
        let runningBalance =
            currentBalance -
            statementNetMovement;

        return chronologicalTransactions.map(
            (transaction, index) => {
                const credit =
                    Number(
                        transaction.credit || 0
                    );

                const debit =
                    Number(
                        transaction.debit || 0
                    );

                const rawDate =
                    getTransactionDate(
                        transaction
                    );

                const transactionDate =
                    new Date(rawDate);

                const timestamp =
                    transactionDate.getTime();

                runningBalance +=
                    credit - debit;

                const transactionAmount =
                    credit > 0
                        ? credit
                        : debit;

                return {
                    id:
                        transaction.refId ||
                        `${timestamp}-${index}`,

                    /*
                     * Use the exact timestamp as the X-axis
                     * value. This stays unique even when several
                     * transfers happen on the same date.
                     */
                    timestamp,

                    fullDate:
                        transactionDate.toLocaleString(
                            "en-IN",
                            {
                                day: "numeric",
                                month: "short",
                                year: "numeric",
                                hour: "numeric",
                                minute: "2-digit",
                                second: "2-digit"
                            }
                        ),

                    balance:
                        Number(
                            runningBalance.toFixed(2)
                        ),

                    credit,
                    debit,

                    amount:
                        transactionAmount,

                    transaction:
                        credit > 0
                            ? `+ ${formatCurrency(
                                credit
                            )}`
                            : `- ${formatCurrency(
                                debit
                            )}`,

                    description:
                        transaction.description ||
                        transaction.type ||
                        transaction.txntype ||
                        "Transaction",

                    status:
                        transaction.status,

                    type:
                        transaction.type ||
                        transaction.txntype,

                    refId:
                        transaction.refId
                };
            }
        );
    }, [
        allTransactions,
        accountDetails
    ]);

    const chartDomain = useMemo(() => {
        if (chartData.length === 0) {
            return [0, 100];
        }

        const balances =
            chartData.map((item) =>
                Number(item.balance || 0)
            );

        const minimum =
            Math.min(...balances);

        const maximum =
            Math.max(...balances);

        const range =
            Math.max(maximum - minimum, 1);

        const padding =
            Math.max(
                range * 0.25,
                maximum * 0.05,
                100
            );

        return [
            Math.max(
                0,
                Math.floor(minimum - padding)
            ),
            Math.ceil(maximum + padding)
        ];
    }, [chartData]);

    return (
        <div className="dashboard-container">

            {/* HERO */}

            <section className="hero-section">
                <h1 className="customer-name">
                    Dikshant{" "}
                    <span aria-hidden="true">
                        👋
                    </span>
                </h1>

                <p className="customer-subtitle">
                    Available to spend
                </p>
            </section>

            {dashboardError && (
                <div
                    className="dashboard-error"
                    role="alert"
                >
                    {dashboardError}
                </div>
            )}

            {/* WALLET */}

            <section className="wallet-card">
                <div className="wallet-top">
                    <span className="wallet-chip">
                        SAVINGS ACCOUNT
                    </span>

                    <span className="status-chip">
                        {accountDetails?.status ||
                            "ACTIVE"}
                    </span>
                </div>

                <div className="wallet-main">
                    <p className="wallet-label">
                        Available Balance
                    </p>

                    <h2 className="wallet-balance">
                        {formatCurrency(
                            accountDetails?.balance
                        )}
                    </h2>

                    <p className="wallet-account">
                        {accountDetails?.accountnumber ||
                            "Account unavailable"}
                    </p>
                </div>
            </section>

            {/* QUICK ACTIONS */}

            <section
                className="quick-actions"
                aria-label="Quick banking actions"
            >
                <button
                    type="button"
                    className="action-card transfer-card"
                    onClick={() =>
                        navigate(
                            "/customer/transfer"
                        )
                    }
                >
                    <div className="action-card-top">
                        <div className="action-icon-wrap">
                            <span
                                className="action-icon"
                                aria-hidden="true"
                            >
                                💸
                            </span>
                        </div>

                        <span
                            className="action-arrow"
                            aria-hidden="true"
                        >
                            →
                        </span>
                    </div>

                    <h3>Transfer</h3>

                    <p>
                        {transferCount > 0
                            ? `${transferCount} transfer${transferCount === 1
                                ? ""
                                : "s"
                            } in your activity`
                            : "Send money instantly"}
                    </p>
                </button>

                <button
                    type="button"
                    className="action-card cards-card"
                    onClick={() =>
                        navigate(
                            "/customer/cards"
                        )
                    }
                >
                    <div className="action-card-top">
                        <div className="action-icon-wrap">
                            <span
                                className="action-icon"
                                aria-hidden="true"
                            >
                                💳
                            </span>
                        </div>

                        <span
                            className="action-arrow"
                            aria-hidden="true"
                        >
                            →
                        </span>
                    </div>

                    <h3>Cards</h3>

                    <p>
                        Manage card limits and controls
                    </p>
                </button>

                <button
                    type="button"
                    className="action-card fd-card"
                    onClick={() =>
                        navigate(
                            "/customer/fixed-deposit"
                        )
                    }
                >
                    <div className="action-card-top">
                        <div className="action-icon-wrap">
                            <span
                                className="action-icon"
                                aria-hidden="true"
                            >
                                🏦
                            </span>
                        </div>

                        <span
                            className="action-arrow"
                            aria-hidden="true"
                        >
                            →
                        </span>
                    </div>

                    <h3>Fixed Deposit</h3>

                    <p>
                        Build stable long-term savings
                    </p>
                </button>

                <button
                    type="button"
                    className="action-card rd-card"
                    onClick={() =>
                        navigate(
                            "/customer/recurring-deposit"
                        )
                    }
                >
                    <div className="action-card-top">
                        <div className="action-icon-wrap">
                            <span
                                className="action-icon"
                                aria-hidden="true"
                            >
                                📈
                            </span>
                        </div>

                        <span
                            className="action-arrow"
                            aria-hidden="true"
                        >
                            →
                        </span>
                    </div>

                    <h3>
                        Recurring Deposit
                    </h3>

                    <p>
                        Grow savings through monthly deposits
                    </p>
                </button>
            </section>

            {/* CASH FLOW SUMMARY */}

            <section
                className="cashflow-grid"
                aria-label="Cash flow summary"
            >
                <article className="cashflow-card money-in-card">
                    <span>Money In</span>

                    <h2>
                        {formatCurrency(
                            totalCredits
                        )}
                    </h2>

                    <p>
                        Total credited amount
                    </p>
                </article>

                <article className="cashflow-card money-out-card">
                    <span>Money Out</span>

                    <h2>
                        {formatCurrency(
                            totalDebits
                        )}
                    </h2>

                    <p>
                        Total debited amount
                    </p>
                </article>

                <article className="cashflow-card net-flow-card">
                    <span>Net Flow</span>

                    <h2
                        className={
                            netCashFlow >= 0
                                ? "positive"
                                : "negative"
                        }
                    >
                        {formatCurrency(
                            netCashFlow
                        )}
                    </h2>

                    <p>
                        {netCashFlow > 0
                            ? "More money retained"
                            : netCashFlow < 0
                                ? "Outflow exceeds inflow"
                                : "Inflow and outflow are balanced"}
                    </p>
                </article>
            </section>

            {/* BALANCE JOURNEY CHART */}

            <section className="analytics-card">
                <div className="analytics-header">
                    <div>
                        <span className="section-eyebrow">
                            CASH FLOW ANALYTICS
                        </span>

                        <h2>
                            Account Balance Journey
                        </h2>

                        <p>
                            Balance movement after each
                            recorded transaction
                        </p>
                    </div>

                    <div className="current-balance-summary">
                        <span>
                            Current balance
                        </span>

                        <strong>
                            {formatCurrency(
                                accountDetails?.balance
                            )}
                        </strong>
                    </div>
                </div>

                {chartData.length === 0 ? (
                    <div className="chart-empty-state">
                        No transaction history is
                        available for this chart.
                    </div>
                ) : (
                    <ResponsiveContainer
                        width="100%"
                        height={330}
                    >
                        <AreaChart
                            data={chartData}
                            margin={{
                                top: 30,
                                right: 20,
                                left: 8,
                                bottom: 10
                            }}
                        >
                            <defs>
                                <linearGradient
                                    id="balanceGradient"
                                    x1="0"
                                    y1="0"
                                    x2="0"
                                    y2="1"
                                >
                                    <stop
                                        offset="0%"
                                        stopColor="#2563eb"
                                        stopOpacity={0.38}
                                    />

                                    <stop
                                        offset="75%"
                                        stopColor="#60a5fa"
                                        stopOpacity={0.08}
                                    />

                                    <stop
                                        offset="100%"
                                        stopColor="#ffffff"
                                        stopOpacity={0}
                                    />
                                </linearGradient>
                            </defs>

                            <CartesianGrid
                                vertical={false}
                                stroke="#e2e8f0"
                                strokeDasharray="4 6"
                            />

                            <XAxis
                                dataKey="timestamp"
                                type="number"
                                scale="time"
                                domain={[
                                    "dataMin",
                                    "dataMax"
                                ]}
                                axisLine={false}
                                tickLine={false}
                                tickMargin={14}
                                minTickGap={32}
                                tickFormatter={(timestamp) =>
                                    new Date(
                                        timestamp
                                    ).toLocaleDateString(
                                        "en-IN",
                                        {
                                            day: "numeric",
                                            month: "short"
                                        }
                                    )
                                }
                                tick={{
                                    fill: "#64748b",
                                    fontSize: 12
                                }}
                            />

                            <YAxis
                                domain={chartDomain}
                                hide
                            />

                            <Tooltip
                                content={
                                    <BalanceTooltip />
                                }
                                cursor={{
                                    stroke: "#94a3b8",
                                    strokeWidth: 1,
                                    strokeDasharray:
                                        "4 4"
                                }}
                            />

                            <Area
                                type="monotone"
                                dataKey="balance"
                                stroke="#2563eb"
                                strokeWidth={3}
                                fill="url(#balanceGradient)"
                                animationDuration={800}
                                dot={{
                                    r: 5,
                                    fill: "#2563eb",
                                    strokeWidth: 3,
                                    stroke: "#ffffff"
                                }}
                                activeDot={{
                                    r: 8,
                                    fill: "#2563eb",
                                    strokeWidth: 4,
                                    stroke: "#dbeafe"
                                }}
                            />
                        </AreaChart>
                    </ResponsiveContainer>
                )}
            </section>

            {/* FINANCIAL HEALTH */}

            <section className="financial-health-section">
                <div className="section-header">
                    <div>
                        <span className="section-eyebrow">
                            PERSONAL FINANCE
                        </span>

                        <h2>
                            Financial Health Overview
                        </h2>
                    </div>
                </div>

                <div className="financial-health-grid">

                    <article className="health-card savings-card">
                        <div className="health-icon savings-icon">
                            ✅
                        </div>

                        <div className="health-content">
                            <span className="health-label">
                                Savings Rate
                            </span>

                            <h2>
                                {savingsRate}%
                            </h2>

                            <p>
                                {savingsLabel}
                            </p>

                            <div
                                className="health-progress"
                                role="progressbar"
                                aria-label="Savings rate"
                                aria-valuemin="0"
                                aria-valuemax="100"
                                aria-valuenow={
                                    savingsRate
                                }
                            >
                                <span
                                    style={{
                                        width:
                                            `${savingsRate}%`
                                    }}
                                />
                            </div>
                        </div>
                    </article>

                    <article className="health-card cashflow-card-large">
                        <div className="health-icon cashflow-icon">
                            📈
                        </div>

                        <div className="health-content">
                            <span className="health-label">
                                Cash Flow
                            </span>

                            <h2>
                                {cashFlowStatus}
                            </h2>

                            <p>
                                {formatCurrency(
                                    netCashFlow
                                )}{" "}
                                net movement
                            </p>

                            <span
                                className={
                                    netCashFlow >= 0
                                        ? "health-status positive-status"
                                        : "health-status negative-status"
                                }
                            >
                                {netCashFlow > 0
                                    ? "Healthy position"
                                    : netCashFlow < 0
                                        ? "Review spending"
                                        : "No net movement"}
                            </span>
                        </div>
                    </article>

                    <article className="health-card score-card">
                        <div className="health-icon score-icon">
                            🎯
                        </div>

                        <div className="health-content">
                            <span className="health-label">
                                Financial Score
                            </span>

                            <h2>
                                {financialScore}
                                <small>
                                    /100
                                </small>
                            </h2>

                            <p>
                                {scoreLabel}
                            </p>

                            <div
                                className="health-progress score-progress"
                                role="progressbar"
                                aria-label="Financial score"
                                aria-valuemin="0"
                                aria-valuemax="100"
                                aria-valuenow={
                                    financialScore
                                }
                            >
                                <span
                                    style={{
                                        width:
                                            `${financialScore}%`
                                    }}
                                />
                            </div>
                        </div>
                    </article>

                </div>
            </section>

            {/* RECENT TRANSACTIONS */}

            <section className="transactions-card">
                <div className="transactions-header">
                    <div>
                        <span className="section-eyebrow">
                            LATEST MOVEMENTS
                        </span>

                        <h2>
                            Recent Activity
                        </h2>
                    </div>

                    <button
                        type="button"
                        className="view-all-btn"
                        onClick={() =>
                            navigate(
                                "/customer/statements"
                            )
                        }
                    >
                        View All
                        <span aria-hidden="true">
                            {" "}→
                        </span>
                    </button>
                </div>

                {loading ? (
                    <p className="transactions-loading">
                        Loading transactions...
                    </p>
                ) : transactions.length === 0 ? (
                    <div className="empty-state">
                        <div className="empty-icon">
                            💰
                        </div>

                        <h3>
                            No Transactions Yet
                        </h3>

                        <p>
                            Your account activity will
                            appear here.
                        </p>
                    </div>
                ) : (
                    transactions.map(
                        (transaction, index) => {
                            const status =
                                String(
                                    transaction.status ||
                                    ""
                                ).toUpperCase();

                            const amount =
                                getTransactionAmount(
                                    transaction
                                );

                            const isDebit =
                                Number(
                                    transaction.debit ||
                                    0
                                ) > 0;

                            const transactionKey =
                                transaction.refId ||
                                `${getTransactionDate(
                                    transaction
                                )}-${index}`;

                            return (
                                <div
                                    key={
                                        transactionKey
                                    }
                                    className="transaction-row"
                                >
                                    <div className="transaction-left">
                                        <div
                                            className={
                                                isDebit
                                                    ? "transaction-icon transaction-icon-debit"
                                                    : "transaction-icon transaction-icon-credit"
                                            }
                                        >
                                            {isDebit
                                                ? "↗"
                                                : "↙"}
                                        </div>

                                        <div>
                                            <div className="transaction-title">
                                                {transaction.description ||
                                                    transaction.type ||
                                                    transaction.txntype ||
                                                    "Transaction"}
                                            </div>

                                            <div className="transaction-date">
                                                {formatDate(
                                                    getTransactionDate(
                                                        transaction
                                                    )
                                                )}
                                            </div>
                                        </div>
                                    </div>

                                    <div
                                        className={
                                            status ===
                                                "FAILED"
                                                ? "failed"
                                                : isDebit
                                                    ? "debit"
                                                    : "credit"
                                        }
                                    >
                                        {status ===
                                            "FAILED"
                                            ? `FAILED ${formatCurrency(
                                                Math.abs(
                                                    amount
                                                )
                                            )}`
                                            : isDebit
                                                ? `- ${formatCurrency(
                                                    Math.abs(
                                                        amount
                                                    )
                                                )}`
                                                : `+ ${formatCurrency(
                                                    Math.abs(
                                                        amount
                                                    )
                                                )}`}
                                    </div>
                                </div>
                            );
                        }
                    )
                )}
            </section>
        </div>
    );
}