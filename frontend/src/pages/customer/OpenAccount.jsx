import { useState } from "react";
import { useNavigate } from "react-router-dom";
import API from "../../api/api";
import "./features.css";

export default function OpenAccount() {
  const navigate = useNavigate();
  const customerId = localStorage.getItem("customerid");

  const [accountType, setAccountType] = useState("SAVINGS");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const accountTypes = {
    SAVINGS: {
      label: "Savings Account",
      description: "Perfect for regular savings and earning interest",
      icon: "🏦",
      benefits: ["No minimum balance", "24/7 access"]
    },
    CURRENT: {
      label: "Current Account",
      description: "Ideal for business and frequent transactions",
      icon: "💼",
      benefits: ["Unlimited transactions", "No balance restriction", "Business focused"]
    }
  };

  const openAccount = async () => {
    try {
      setLoading(true);
      setMessage("");
      setError("");

      const payload = {
        accountnumber: "SB" + Math.floor(100000 + Math.random() * 900000),
        accounttype: accountType,
        balance: 0,
        status: "ACTIVE",
        customerid: parseInt(customerId)
      };

      const res = await API.post("/accounts", payload);

      localStorage.setItem("accountid", res.data.accountid);
      setMessage("Account opened successfully! Redirecting...");

      setTimeout(() => navigate("/customer"), 2000);
    } catch (err) {
      console.error(err);
      setError("Failed to open account. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="deposit-page">
      <button 
        onClick={() => navigate("/customer")}
        style={{
          background: "transparent",
          border: "none",
          color: "#3b82f6",
          fontSize: "18px",
          cursor: "pointer",
          marginBottom: "20px",
          fontWeight: "600"
        }}
      >
        ← Back to Dashboard
      </button>

      <div className="deposit-card">
        <h1 className="page-title">Open New Account</h1>

        {message && <div className="success">{message}</div>}
        {error && <div className="error">{error}</div>}

        <div style={{ marginTop: "40px" }}>
          <label style={{ display: "block", marginBottom: "20px" }}>
            <span style={{ fontSize: "16px", fontWeight: "700", color: "#1f2937", display: "block", marginBottom: "16px" }}>
              Select Account Type
            </span>

            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "20px", marginBottom: "30px" }}>
              {Object.entries(accountTypes).map(([key, details]) => (
                <div
                  key={key}
                  onClick={() => setAccountType(key)}
                  style={{
                    padding: "24px",
                    border: accountType === key ? "3px solid #3b82f6" : "2px solid #e5e7eb",
                    borderRadius: "16px",
                    cursor: "pointer",
                    background: accountType === key ? "#eff6ff" : "#f9fafb",
                    transition: "all 0.3s ease",
                    boxShadow: accountType === key ? "0 8px 20px rgba(59, 130, 246, 0.15)" : "0 2px 8px rgba(0,0,0,0.05)"
                  }}
                >
                  <div style={{ fontSize: "32px", marginBottom: "12px" }}>{details.icon}</div>
                  <h3 style={{ fontWeight: "700", fontSize: "18px", color: "#1f2937", marginBottom: "6px" }}>
                    {details.label}
                  </h3>
                  <p style={{ fontSize: "14px", color: "#6b7280", marginBottom: "12px" }}>
                    {details.description}
                  </p>
                  <div style={{ fontSize: "13px", color: "#4b5563" }}>
                    {details.benefits.map((benefit, idx) => (
                      <div key={idx} style={{ marginBottom: "6px" }}>
                        ✓ {benefit}
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </label>

          <div style={{
            background: "#f0f9ff",
            border: "2px solid #0ea5e9",
            borderRadius: "12px",
            padding: "20px",
            marginBottom: "30px"
          }}>
            <h4 style={{ color: "#0369a1", fontWeight: "700", marginBottom: "10px" }}>
              📋 Account Summary
            </h4>
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "15px", fontSize: "14px" }}>
              <div>
                <span style={{ color: "#6b7280" }}>Account Type:</span>
                <div style={{ color: "#1f2937", fontWeight: "600", marginTop: "4px" }}>
                  {accountTypes[accountType].label}
                </div>
              </div>
              <div>
                <span style={{ color: "#6b7280" }}>Initial Balance:</span>
                <div style={{ color: "#1f2937", fontWeight: "600", marginTop: "4px" }}>
                  ₹0.00
                </div>
              </div>
              <div>
                
                
              </div>
              <div>
                
                
              </div>
            </div>
          </div>

          <button
            onClick={openAccount}
            disabled={loading}
            style={{
              width: "100%",
              padding: "14px 24px",
              background: loading ? "#d1d5db" : "linear-gradient(135deg, #3b82f6, #2563eb)",
              color: "white",
              border: "none",
              borderRadius: "12px",
              fontSize: "16px",
              fontWeight: "700",
              cursor: loading ? "not-allowed" : "pointer",
              transition: "all 0.3s ease",
              boxShadow: "0 4px 15px rgba(59, 130, 246, 0.3)"
            }}
            onMouseEnter={(e) => !loading && (e.target.style.transform = "translateY(-2px)")}
            onMouseLeave={(e) => !loading && (e.target.style.transform = "translateY(0)")}
          >
            {loading ? "⏳ Creating Account..." : "✓ Open Account"}
          </button>
        </div>
      </div>
    </div>
  );
}