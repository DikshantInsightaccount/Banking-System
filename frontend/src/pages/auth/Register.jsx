import { useState } from "react";
import { useNavigate } from "react-router-dom";
import API from "../../api/api";
import "./auth.css";

const initialForm = {
  fullname: "",
  phone: "",
  email: "",
  address: "",
  designation: "",
  branch: "",
  idprooftype: "",
  idproofnumber: "",
  password: "",
  confirmPassword: "",
  role: "",
};

export default function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState(initialForm);
  const [step, setStep] = useState(1);
  const [otp, setOtp] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const validateForm = () => {
    const requiredFields = [
      "fullname",
      "phone",
      "email",
      "address",
      "idprooftype",
      "idproofnumber",
      "password",
      "confirmPassword",
      "role",
    ];
    if (requiredFields.some((field) => !form[field].trim())) {
      return "Please fill all required fields";
    }
    if (form.role === "EMPLOYEE" && (!form.designation.trim() || !form.branch.trim())) {
      return "Employee registration requires designation and branch";
    }
    if (!/^\d{10}$/.test(form.phone)) {
      return "Phone number must be exactly 10 digits";
    }
    if (!/^\S+@\S+\.\S+$/.test(form.email)) {
      return "Enter a valid email";
    }
    if (!/^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[@#$%^&*!]).{8,}$/.test(form.password)) {
      return "Password must be at least 8 characters with uppercase, lowercase, number, and special character";
    }
    if (form.password !== form.confirmPassword) {
      return "Passwords do not match";
    }
    return null;
  };

  const sendOtp = async () => {
    const validationError = validateForm();
    if (validationError) {
      alert(validationError);
      return;
    }

    setIsSubmitting(true);
    try {
      await API.post("/auth/send-otp", null, {
        params: { email: form.email.trim() },
      });
      setStep(2);
      alert("OTP sent to your email");
    } catch (error) {
      alert(error.response?.data?.error || "Failed to send OTP");
    } finally {
      setIsSubmitting(false);
    }
  };

  const verifyOtp = async () => {
    if (!otp.trim()) {
      alert("Enter OTP");
      return;
    }

    setIsSubmitting(true);
    try {
      const email = form.email.trim();
      const otpResponse = await API.post("/auth/verify-otp", {
        email,
        otp: otp.trim(),
      });
      if (!otpResponse.data?.valid) {
        alert("Invalid or expired OTP");
        return;
      }

      const authResponse = await API.post("/auth/register", {
        username: email,
        password: form.password,
        role: form.role,
      });
      const userId = authResponse.data.userid || authResponse.data.id;
      if (!userId) throw new Error("User ID was not returned");

      const profile = {
        fullname: form.fullname.trim(),
        email,
        phone: form.phone,
        address: form.address.trim(),
        idprooftype: form.idprooftype,
        idproofnumber: form.idproofnumber.trim(),
        user: { userid: userId, role: form.role },
      };

      if (form.role === "EMPLOYEE") {
        await API.post("/api/employees/register", {
          ...profile,
          designation: form.designation.trim(),
          branch: form.branch.trim(),
        });
      } else {
        await API.post("/api/customers/register", profile);
      }

      alert("Registration successful");
      navigate("/login", { replace: true });
    } catch (error) {
      console.error(error);
      alert(error.response?.data?.message || error.message || "Registration failed");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-left" />
      <div className="auth-card modern">
        <h2 className="auth-title">Create Account</h2>
        <p className="auth-subtitle">Open your bank account in a few steps</p>

        {step === 1 ? (
          <div className="form-section">
            <div className="form-group">
              <label htmlFor="fullname">Full Name</label>
              <input id="fullname" name="fullname" value={form.fullname} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label htmlFor="phone">Phone Number</label>
              <input id="phone" name="phone" value={form.phone} onChange={handleChange} inputMode="numeric" />
            </div>
            <div className="form-group">
              <label htmlFor="email">Email</label>
              <input id="email" type="email" name="email" value={form.email} onChange={handleChange} />
            </div>
            <div className="form-group full">
              <label htmlFor="address">Address</label>
              <input id="address" name="address" value={form.address} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label htmlFor="idprooftype">ID Proof Type</label>
              <select id="idprooftype" name="idprooftype" value={form.idprooftype} onChange={handleChange}>
                <option value="">Select</option>
                <option value="AADHAR">Aadhar</option>
                <option value="PAN">PAN</option>
                <option value="PASSPORT">Passport</option>
              </select>
            </div>
            <div className="form-group">
              <label htmlFor="idproofnumber">ID Proof Number</label>
              <input id="idproofnumber" name="idproofnumber" value={form.idproofnumber} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label htmlFor="password">Password</label>
              <input id="password" type="password" name="password" value={form.password} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label htmlFor="confirmPassword">Re-enter Password</label>
              <input id="confirmPassword" type="password" name="confirmPassword" value={form.confirmPassword} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label htmlFor="role">Role</label>
              <select id="role" name="role" value={form.role} onChange={handleChange}>
                <option value="">Select Role</option>
                <option value="CUSTOMER">Customer</option>
                <option value="EMPLOYEE">Employee</option>
              </select>
            </div>
            {form.role === "EMPLOYEE" && (
              <>
                <div className="form-group">
                  <label htmlFor="designation">Designation</label>
                  <input id="designation" name="designation" value={form.designation} onChange={handleChange} />
                </div>
                <div className="form-group">
                  <label htmlFor="branch">Branch</label>
                  <input id="branch" name="branch" value={form.branch} onChange={handleChange} />
                </div>
              </>
            )}
            <button className="primary-btn" type="button" onClick={sendOtp} disabled={isSubmitting}>
              {isSubmitting ? "Sending..." : "Send OTP"}
            </button>
          </div>
        ) : (
          <div className="otp-section">
            <p className="otp-text">Enter the OTP sent to your email</p>
            <input className="otp-input" placeholder="Enter OTP" value={otp} onChange={(event) => setOtp(event.target.value)} />
            <button className="primary-btn" type="button" onClick={verifyOtp} disabled={isSubmitting}>
              {isSubmitting ? "Verifying..." : "Verify & Register"}
            </button>
          </div>
        )}

        <p className="register-link">
          Already have an account? <span onClick={() => navigate("/login")}>Login here</span>
        </p>
      </div>
    </div>
  );
}