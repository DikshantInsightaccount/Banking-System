import { NavLink, useNavigate } from "react-router-dom";
import API from "../../api/api";
import "./CustomerNavBar.css";
import logo from "../../assets/logo.png";

export default function CustomerNavBar() {
  const navigate = useNavigate();
  const name = localStorage.getItem("name");
  const linkClass = ({ isActive }) => `nav-link${isActive ? " active" : ""}`;

  const handleLogout = () => {
    localStorage.clear();
    delete API.defaults.headers.common.Authorization;
    navigate("/login", { replace: true });
  };

  return <nav className="navbar navbar-expand-lg bg-body-tertiary"><div className="container-fluid">
    <NavLink className="navbar-brand" to="/customer"><img src={logo} alt="IG Bank" width="150" height="70" /></NavLink>
    <button className="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#customerNavbar" aria-controls="customerNavbar" aria-label="Toggle navigation"><span className="navbar-toggler-icon" /></button>
    <div className="collapse navbar-collapse" id="customerNavbar"><ul className="navbar-nav ms-auto">
      <li className="nav-item"><NavLink className={linkClass} to="/customer" end>Dashboard</NavLink></li>
      <li className="nav-item"><NavLink className={linkClass} to="/customer/support">Support</NavLink></li>
      <li className="nav-item dropdown"><button className="nav-link dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">{name || "Account"}</button><ul className="dropdown-menu dropdown-menu-end"><li><NavLink className="dropdown-item" to="/customer/profile">Profile</NavLink></li><li><button className="dropdown-item" type="button" onClick={handleLogout}>Logout</button></li></ul></li>
    </ul></div>
  </div></nav>;
}
