import { NavLink } from "react-router-dom";

const linkClass = ({ isActive }) =>
  `nav-link px-3 py-2 ${isActive ? "active fw-semibold bg-secondary text-white rounded" : "text-white"}`;

export default function Sidebar() {
  return (
    <aside className="bg-dark text-white" style={{ width: 260 }}>
      <div className="p-3 border-bottom border-secondary">
        <div className="fw-bold">Bank Employee</div>
        <div className="small text-secondary">Back Office Portal</div>
      </div>

      <nav className="nav flex-column p-2 gap-1">
        <NavLink to="/employee/dashboard" className={linkClass}>Dashboard</NavLink>
        <NavLink to="/employee/customers" className={linkClass}>Customers</NavLink>
        <NavLink to="/employee/accounts" className={linkClass}>Accounts</NavLink>
        <NavLink to="/employee/transactions" className={linkClass}>Transactions</NavLink>
        <NavLink to="/employee/transactions/statement" className={linkClass}>Statements</NavLink>
      </nav>
    </aside>
  );
}