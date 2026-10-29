import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";

import CustomerLayout from "./layouts/CustomerLayout";
import EmployeeLayout from "./layouts/EmployeeLayout";
import Login from "./pages/auth/Login";
import Register from "./pages/auth/Register";
import Cards from "./pages/customer/Cards";
import CustomerDashboard from "./pages/customer/CustomerDashboard";
import Deposits from "./pages/customer/Deposits";
import FixedDeposit from "./pages/customer/FixedDeposit";
import OpenAccount from "./pages/customer/OpenAccount";
import Profile from "./pages/customer/Profile";
import RecurringDeposit from "./pages/customer/RecurringDeposit";
import Statements from "./pages/customer/Statements";
import Support from "./pages/customer/Support";
import Transfer from "./pages/customer/Transfer";
import Withdraw from "./pages/customer/Withdraw";
import AccountDetailsPage from "./features/accounts/pages/AccountDetailsPage";
import AccountListPage from "./features/accounts/pages/AccountListPage";
import OpenAccountPage from "./features/accounts/pages/OpenAccountPage";
import CustomerAccountsPage from "./features/customers/pages/CustomerAccountsPage";
import CustomerDetailsPage from "./features/customers/pages/CustomerDetailsPage";
import CustomerEditPage from "./features/customers/pages/CustomerEditPage";
import CustomerKycPage from "./features/customers/pages/CustomerKycPage";
import CustomerListPage from "./features/customers/pages/CustomerListPage";
import CustomerStatusPage from "./features/customers/pages/CustomerStatusPage";
import DashboardPage from "./features/dashboard/pages/DashboardPage";
import DepositPage from "./features/transactions/pages/DepositPage";
import StatementPage from "./features/transactions/pages/StatementPage";
import TransactionDetailsPage from "./features/transactions/pages/TransactionDetailsPage";
import TransactionsListPage from "./features/transactions/pages/TransactionsListPage";
import TransactionTransferPage from "./features/transactions/pages/TransferPage";
import TransactionWithdrawPage from "./features/transactions/pages/WithdrawPage";

function ProtectedRoute({ children }) {

  const token =
    localStorage.getItem("token");

  const userId =
    localStorage.getItem("userid");

  return (
    token &&
    userId
  )
    ? children
    : <Navigate to="/login" replace />;
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route
          path="/open-account"
          element={
            <ProtectedRoute>
              <OpenAccount />
            </ProtectedRoute>
          }
        />

        <Route
          path="/customer"
          element={
            <ProtectedRoute>
              <CustomerLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<CustomerDashboard />} />
          <Route path="profile" element={<Profile />} />
          <Route path="deposits" element={<Deposits />} />
          <Route path="withdraw" element={<Withdraw />} />
          <Route path="transfer" element={<Transfer />} />
          <Route path="fixed-deposit" element={<FixedDeposit />} />
          <Route path="recurring-deposit" element={<RecurringDeposit />} />
          <Route path="support" element={<Support />} />
          <Route path="statements" element={<Statements />} />
          <Route path="cards" element={<Cards />} />
        </Route>

        <Route
          path="/employee"
          element={
            <ProtectedRoute>
              <EmployeeLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<DashboardPage />} />
          <Route path="customers" element={<CustomerListPage />} />
          <Route path="customers/:customerId" element={<CustomerDetailsPage />} />
          <Route path="customers/:customerId/edit" element={<CustomerEditPage />} />
          <Route path="customers/:customerId/status" element={<CustomerStatusPage />} />
          <Route path="customers/:customerId/kyc" element={<CustomerKycPage />} />
          <Route
            path="customers/:customerId/accounts"
            element={<CustomerAccountsPage />}
          />
          <Route path="customers/:customerId/accounts/open" element={<OpenAccountPage />} />
          <Route path="accounts" element={<AccountListPage />} />
          <Route path="accounts/:accountId" element={<AccountDetailsPage />} />
          <Route path="transactions" element={<TransactionsListPage />} />
          <Route path="transactions/:id" element={<TransactionDetailsPage />} />
          <Route path="transactions/deposit" element={<DepositPage />} />
          <Route path="transactions/withdraw" element={<TransactionWithdrawPage />} />
          <Route path="transactions/transfer" element={<TransactionTransferPage />} />
          <Route path="transactions/statement" element={<StatementPage />} />
        </Route>

        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}