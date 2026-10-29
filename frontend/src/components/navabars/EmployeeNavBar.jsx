import { NavLink, useNavigate } from "react-router-dom";

export default function EmployeeNavBar() {
  const navigate = useNavigate();

  const linkClass = ({ isActive }) =>
    `nav-link text-dark px-3${
      isActive ? " fw-bold border-bottom border-2 border-dark" : ""
    }`;

  const handleLogout = () => {
    localStorage.clear();
    navigate("/login", { replace: true });
  };

  return (
    <nav className="navbar navbar-expand-lg bg-white border-bottom">
      <div className="container-fluid">
        <NavLink className="navbar-brand fw-bold text-dark" to="/employee">
          <img
            src="https://cdn.corenexis.com/files/c/5699658720.png"
            alt="IG Bank"
            width="150"
            height="70"
            className="d-inline-block align-text-top"
          />
        </NavLink>

        <button
          className="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#employeeNavbar"
          aria-controls="employeeNavbar"
          aria-expanded="false"
          aria-label="Toggle navigation"
        >
          <span className="navbar-toggler-icon" />
        </button>

        <div className="collapse navbar-collapse" id="employeeNavbar">
          <ul className="navbar-nav ms-auto align-items-center">
            <li className="nav-item">
              <NavLink className={linkClass} to="/employee" end>
                Dashboard
              </NavLink>
            </li>
            <li className="nav-item">
              <NavLink className={linkClass} to="/employee/customers">
                Customers
              </NavLink>
            </li>
            <li className="nav-item">
              <NavLink className={linkClass} to="/employee/accounts">
                Accounts
              </NavLink>
            </li>
            <li className="nav-item">
              <NavLink className={linkClass} to="/employee/transactions">
                Transactions
              </NavLink>
            </li>
            <li className="nav-item dropdown">
              <button
                className="nav-link dropdown-toggle fw-bold text-dark"
                type="button"
                data-bs-toggle="dropdown"
                aria-expanded="false"
              >
                Employee
              </button>
              <ul className="dropdown-menu dropdown-menu-end">
                <li>
                  <button
                    className="dropdown-item text-danger"
                    type="button"
                    onClick={handleLogout}
                  >
                    Logout
                  </button>
                </li>
              </ul>
            </li>
          </ul>
        </div>
      </div>
    </nav>
  );
}