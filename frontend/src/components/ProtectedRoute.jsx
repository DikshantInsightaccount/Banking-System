import { Navigate } from "react-router-dom";
 
export default function ProtectedRoute({ children }) {
  const isLoggedIn = localStorage.getItem("isLoggedIn");
 
  if (isLoggedIn !== "true") {
    return <Navigate to="/login" />;
  }
 
  return children;
}