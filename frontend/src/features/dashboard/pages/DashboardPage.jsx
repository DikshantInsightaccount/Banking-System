import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import {
  Users,
  Wallet,
  ArrowLeftRight,
  TrendingUp,
  IndianRupee,
  RefreshCcw,
  Eye,
  ShieldCheck,
  Activity,
} from "lucide-react";

import PageHeader from "../../../components/shared/PageHeader.jsx";
import Loader from "../../../components/shared/Loader.jsx";
import { customerService } from "../../customers/services/customerService.js";
import { accountService } from "../../customers/services/accountService.js";
import { transactionService } from "../../customers/services/transactionService.js";

export default function DashboardPage() {
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState("");

  const [customers, setCustomers] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [transactions, setTransactions] = useState([]);

  const [lastUpdated, setLastUpdated] = useState(null);

  const getErrMsg = (e) =>
    e?.response?.data?.message ||
    e?.response?.data?.error ||
    e?.message ||
    "Something went wrong";

  const safeArray = (x) => (Array.isArray(x) ? x : []);

  const load = async (manual = false) => {
    try {
      manual ? setRefreshing(true) : setLoading(true);
      setError("");

      const [c, a, t] = await Promise.all([
        customerService.getAll(),
        accountService.getAll(),
        transactionService.getAll(),
      ]);

      setCustomers(safeArray(c));
      setAccounts(safeArray(a));
      setTransactions(safeArray(t));
      setLastUpdated(new Date());
    } catch (e) {
      setError(getErrMsg(e));
    } finally {
      manual ? setRefreshing(false) : setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const stats = useMemo(() => {
    const totalCustomers = customers.length;
    const totalAccounts = accounts.length;
    const totalTransactions = transactions.length;

    const activeAccounts = accounts.filter(
      (a) => a?.status === "ACTIVE"
    ).length;

    const totalBalance = accounts.reduce(
      (sum, a) => sum + Number(a?.balance || 0),
      0
    );

    const successfulTransactions = transactions.filter(
      (t) => (t?.status || "").toUpperCase() === "SUCCESS"
    ).length;

    return {
      totalCustomers,
      totalAccounts,
      totalTransactions,
      activeAccounts,
      totalBalance,
      successfulTransactions,
    };
  }, [customers, accounts, transactions]);

  const formatMoney = (value) => {
    return Number(value || 0).toLocaleString("en-IN", {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 0,
    });
  };

  const recentTransactions = useMemo(() => {
    return [...transactions]
      .sort(
        (a, b) =>
          new Date(b?.createdat || b?.timestamp || 0) -
          new Date(a?.createdat || a?.timestamp || 0)
      )
      .slice(0, 5);
  }, [transactions]);

  const recentCustomers = useMemo(() => {
    return [...customers]
      .sort(
        (a, b) =>
          new Date(b?.createdat || 0) - new Date(a?.createdat || 0)
      )
      .slice(0, 5);
  }, [customers]);

  const StatCard = ({ icon, title, value, subtitle, gradient, delay }) => (
    <motion.div
      initial={{ opacity: 0, y: 30 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, delay }}
      whileHover={{ y: -8, scale: 1.02 }}
      className="col-lg-3 col-md-6"
    >
      <div
        className="p-4 rounded-4 text-white position-relative overflow-hidden shadow-lg h-100"
        style={{
          background: gradient,
          minHeight: "180px",
        }}
      >
        <div className="position-absolute top-0 end-0 opacity-25">
          <div
            style={{
              width: 120,
              height: 120,
              borderRadius: "50%",
              background: "rgba(255,255,255,0.2)",
              transform: "translate(30px,-30px)",
            }}
          />
        </div>

        <div className="d-flex justify-content-between align-items-start position-relative">
          <div>
            <div className="small fw-semibold text-uppercase mb-2 opacity-75">
              {title}
            </div>

            <h2 className="fw-bold mb-1">{value}</h2>

            <div className="small opacity-75">{subtitle}</div>
          </div>

          <div
            className="d-flex align-items-center justify-content-center rounded-4"
            style={{
              width: 60,
              height: 60,
              background: "rgba(255,255,255,0.2)",
              backdropFilter: "blur(10px)",
            }}
          >
            {icon}
          </div>
        </div>
      </div>
    </motion.div>
  );

  if (loading) return <Loader />;

  return (
    <div
      className="min-vh-100 p-3 p-md-4"
      style={{
        background:
          "linear-gradient(135deg, #eef2ff 0%, #f8fafc 45%, #ecfeff 100%)",
      }}
    >
      <PageHeader
        title="Employee Dashboard"
        subtitle="Real-time banking overview and operational insights"
        right={
          <button
            className="btn btn-dark rounded-pill px-4 d-flex align-items-center gap-2"
            onClick={() => load(true)}
          >
            <RefreshCcw size={16} className={refreshing ? "spin" : ""} />
            {refreshing ? "Refreshing..." : "Refresh"}
          </button>
        }
      />

      {error && (
        <div className="alert alert-danger rounded-4 border-0 shadow-sm">
          {error}
        </div>
      )}

      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ duration: 0.5 }}
      >
        <div className="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
          <div>
            <h3 className="fw-bold mb-1">Welcome Back 👋</h3>
            <div className="text-muted">
              Last updated: {lastUpdated?.toLocaleString()}
            </div>
          </div>

          <div className="d-flex gap-2 flex-wrap">
            <button
              className="btn btn-primary rounded-pill px-4"
              onClick={() => navigate("/employee/customers")}
            >
              Customers
            </button>

            <button
              className="btn btn-success rounded-pill px-4"
              onClick={() => navigate("/employee/accounts")}
            >
              Accounts
            </button>

            <button
              className="btn btn-dark rounded-pill px-4"
              onClick={() => navigate("/employee/transactions")}
            >
              Transactions
            </button>
          </div>
        </div>

        <div className="row g-4 mb-4">
          <StatCard
            title="Customers"
            value={stats.totalCustomers}
            subtitle="Registered users"
            gradient="linear-gradient(135deg,#4f46e5,#7c3aed)"
            icon={<Users size={28} />}
            delay={0.1}
          />

          <StatCard
            title="Accounts"
            value={stats.totalAccounts}
            subtitle={`${stats.activeAccounts} active accounts`}
            gradient="linear-gradient(135deg,#0891b2,#06b6d4)"
            icon={<Wallet size={28} />}
            delay={0.2}
          />

          <StatCard
            title="Transactions"
            value={stats.totalTransactions}
            subtitle={`${stats.successfulTransactions} successful`}
            gradient="linear-gradient(135deg,#16a34a,#22c55e)"
            icon={<ArrowLeftRight size={28} />}
            delay={0.3}
          />

          <StatCard
            title="Total Balance"
            value={formatMoney(stats.totalBalance)}
            subtitle="Across all accounts"
            gradient="linear-gradient(135deg,#ea580c,#f97316)"
            icon={<IndianRupee size={28} />}
            delay={0.4}
          />
        </div>

        <div className="row g-4 mb-4">
          <div className="col-lg-8">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.5 }}
              className="bg-white rounded-4 shadow-lg border-0 p-4 h-100"
            >
              <div className="d-flex justify-content-between align-items-center mb-4">
                <div>
                  <h5 className="fw-bold mb-1">Banking Performance</h5>
                  <div className="text-muted small">
                    Real-time operational metrics
                  </div>
                </div>

                <div className="bg-light rounded-circle p-3">
                  <TrendingUp size={24} className="text-primary" />
                </div>
              </div>

              <div className="mb-4">
                <div className="d-flex justify-content-between mb-2">
                  <span className="fw-semibold">Active Account Ratio</span>
                  <span className="text-success fw-bold">
                    {stats.totalAccounts
                      ? Math.round(
                          (stats.activeAccounts / stats.totalAccounts) * 100
                        )
                      : 0}
                    %
                  </span>
                </div>

                <div
                  className="progress rounded-pill"
                  style={{ height: 14 }}
                >
                  <div
                    className="progress-bar progress-bar-striped progress-bar-animated bg-success"
                    style={{
                      width: `${
                        stats.totalAccounts
                          ? (stats.activeAccounts / stats.totalAccounts) * 100
                          : 0
                      }%`,
                    }}
                  />
                </div>
              </div>

              <div className="row g-3">
                <div className="col-md-4">
                  <div className="bg-primary bg-opacity-10 rounded-4 p-3 text-center">
                    <ShieldCheck className="text-primary mb-2" />
                    <h4 className="fw-bold mb-0">
                      {stats.activeAccounts}
                    </h4>
                    <small className="text-muted">Active Accounts</small>
                  </div>
                </div>

                <div className="col-md-4">
                  <div className="bg-success bg-opacity-10 rounded-4 p-3 text-center">
                    <Activity className="text-success mb-2" />
                    <h4 className="fw-bold mb-0">
                      {stats.successfulTransactions}
                    </h4>
                    <small className="text-muted">Successful Txns</small>
                  </div>
                </div>

                <div className="col-md-4">
                  <div className="bg-warning bg-opacity-10 rounded-4 p-3 text-center">
                    <Eye className="text-warning mb-2" />
                    <h4 className="fw-bold mb-0">
                      {stats.totalCustomers}
                    </h4>
                    <small className="text-muted">Live Customers</small>
                  </div>
                </div>
              </div>
            </motion.div>
          </div>

          <div className="col-lg-4">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.6 }}
              className="bg-dark text-white rounded-4 shadow-lg p-4 h-100"
            >
              <h5 className="fw-bold mb-4">Quick Actions</h5>

              <div className="d-grid gap-3">
                <button
                  className="btn btn-light rounded-4 p-3 text-start"
                  onClick={() => navigate("/employee/customers")}
                >
                  <div className="fw-semibold">Manage Customers</div>
                  <small className="text-muted">
                    View and manage customer records
                  </small>
                </button>

                <button
                  className="btn btn-light rounded-4 p-3 text-start"
                  onClick={() => navigate("/employee/accounts")}
                >
                  <div className="fw-semibold">Manage Accounts</div>
                  <small className="text-muted">
                    Monitor account activities
                  </small>
                </button>

                <button
                  className="btn btn-light rounded-4 p-3 text-start"
                  onClick={() => navigate("/employee/transactions")}
                >
                  <div className="fw-semibold">View Transactions</div>
                  <small className="text-muted">
                    Track transaction history
                  </small>
                </button>
              </div>
            </motion.div>
          </div>
        </div>

        <div className="row g-4">
          <div className="col-lg-6">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.7 }}
              className="bg-white rounded-4 shadow-lg p-4 h-100"
            >
              <div className="d-flex justify-content-between align-items-center mb-4">
                <h5 className="fw-bold mb-0">Recent Customers</h5>

                <button
                  className="btn btn-sm btn-outline-primary rounded-pill"
                  onClick={() => navigate("/employee/customers")}
                >
                  View All
                </button>
              </div>

              <div className="d-flex flex-column gap-3">
                {recentCustomers.map((customer, index) => (
                  <motion.div
                    key={customer.customerid || index}
                    whileHover={{ scale: 1.02 }}
                    className="d-flex justify-content-between align-items-center p-3 rounded-4 border"
                    style={{ cursor: "pointer" }}
                    onClick={() =>
                      navigate(`/employee/customers/${customer.customerid}`)
                    }
                  >
                    <div>
                      <div className="fw-semibold">
                        {customer.fullname || "Unknown User"}
                      </div>
                      <small className="text-muted">
                        {customer.email || customer.phone}
                      </small>
                    </div>

                    <span className="badge text-bg-primary rounded-pill px-3 py-2">
                      #{customer.customerid}
                    </span>
                  </motion.div>
                ))}
              </div>
            </motion.div>
          </div>

          <div className="col-lg-6">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.8 }}
              className="bg-white rounded-4 shadow-lg p-4 h-100"
            >
              <div className="d-flex justify-content-between align-items-center mb-4">
                <h5 className="fw-bold mb-0">Recent Transactions</h5>

                <button
                  className="btn btn-sm btn-outline-success rounded-pill"
                  onClick={() => navigate("/employee/transactions")}
                >
                  View All
                </button>
              </div>

              <div className="d-flex flex-column gap-3">
                {recentTransactions.map((txn, index) => {
                  const id = txn.transactionid || txn.id || index;

                  return (
                    <motion.div
                      key={id}
                      whileHover={{ scale: 1.02 }}
                      className="d-flex justify-content-between align-items-center p-3 rounded-4 border"
                      style={{ cursor: "pointer" }}
                    >
                      <div>
                        <div className="fw-semibold text-uppercase">
                          {txn.txntype || txn.type || "Transaction"}
                        </div>

                        <small className="text-muted">
                          {txn.status || "SUCCESS"}
                        </small>
                      </div>

                      <div className="text-end">
                        <div className="fw-bold text-success">
                          ₹ {txn.amount || 0}
                        </div>

                        <small className="text-muted">#{id}</small>
                      </div>
                    </motion.div>
                  );
                })}
              </div>
            </motion.div>
          </div>
        </div>
      </motion.div>

      <style>{`
        .spin {
          animation: spin 1s linear infinite;
        }

        @keyframes spin {
          from {
            transform: rotate(0deg);
          }
          to {
            transform: rotate(360deg);
          }
        }

        .progress-bar-animated {
          animation-duration: 1.5s;
        }
      `}</style>
    </div>
  );
}



