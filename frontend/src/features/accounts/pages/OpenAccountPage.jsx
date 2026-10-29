import React, { useState } from "react";
import axios from "axios";

const OpenAccountPage = () => {
  const [formData, setFormData] = useState({
    customerid: "",
    accounttype: "SAVINGS",
    accountnumber: ""
  });

  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    setMessage("");
    setError("");

    if (!formData.customerid || !formData.accountnumber) {
      setError("Customer ID and Account Number are required");
      return;
    }

    const payload = {
      customer: {
        customerid: Number(formData.customerid)
      },
      accountnumber: formData.accountnumber,
      accounttype: formData.accounttype,
      balance: 0,
      status: "ACTIVE"
    };

    try {
      setLoading(true);

      const res = await axios.post(
        "http://localhost:9192/accounts",
        payload
      );

      setMessage("✅ Account created successfully!");
      console.log(res.data);

      setFormData({
        customerid: "",
        accounttype: "SAVINGS",
        accountnumber: ""
      });

    } catch (err) {
      console.error(err);
      setError(
        err.response?.data?.message ||
        "Failed to create account (check backend logs)"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container mt-5">
      <h2>Open New Account</h2>

      {message && <p style={{ color: "green" }}>{message}</p>}
      {error && <p style={{ color: "red" }}>{error}</p>}

      <form onSubmit={handleSubmit}>

        {/*  Manual Customer ID */}
        <input
          type="number"
          name="customerid"
          placeholder="Customer ID *"
          value={formData.customerid}
          onChange={handleChange}
          className="form-control mb-3"
        />

        {/* Account Number */}
        <input
          type="text"
          name="accountnumber"
          placeholder="Account Number *"
          value={formData.accountnumber}
          onChange={handleChange}
          className="form-control mb-3"
        />

        {/* Account Type */}
        <select
          name="accounttype"
          value={formData.accounttype}
          onChange={handleChange}
          className="form-control mb-3"
        >
          <option value="SAVINGS">Savings</option>
          <option value="CURRENT">Current</option>
        </select>

        <button
          type="submit"
          className="btn btn-primary"
          disabled={loading}
        >
          {loading ? "Creating..." : "Open Account"}
        </button>

      </form>
    </div>
  );
};

export default OpenAccountPage;