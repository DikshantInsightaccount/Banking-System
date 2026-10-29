import { useNavigate } from "react-router-dom";

export default function Topbar() {
  const navigate = useNavigate();

  const logout = () => {
    localStorage.removeItem("access_token");
    navigate("/", { replace: true });
  };

  return (
    <header className="bg-white border-bottom">
      <div className="container-fluid d-flex align-items-center justify-content-between py-2">
        <div className="fw-semibold">Employee Operations</div>
        <button className="btn btn-outline-danger btn-sm" onClick={logout}>
          Logout
        </button>
      </div>
    </header>
  );
}
