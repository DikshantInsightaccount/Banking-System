import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import API from "../../api/api";
import "./auth.css";

function decodeBase64Url(value) {
  const padding = "=".repeat((4 - (value.length % 4)) % 4);
  const normalized = (value + padding).replace(/-/g, "+").replace(/_/g, "/");
  const binary = atob(normalized);
  return Uint8Array.from(binary, (character) => character.charCodeAt(0));
}

export default function Login() {
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {

    const token =
      localStorage.getItem("token");

    const userId =
      localStorage.getItem("userid");

    const role =
      localStorage.getItem("role");

    if (!token || !userId)
      return;

    navigate(
      role === "EMPLOYEE"
        ? "/employee"
        : "/customer",
      { replace: true }
    );

  }, [navigate]);

  const completeLogin = async (response, isPasskeyLogin = false) => {
    const { token, userid, role, username: loggedInUsername } = response;
    localStorage.setItem("token", token);
    localStorage.setItem("userid", userid);
    localStorage.setItem("role", role);
    localStorage.setItem("username", loggedInUsername);
    // localStorage.setItem("isLoggedIn", "true");

    if (role === "EMPLOYEE") {
      navigate("/employee", { replace: true });
      return;
    }

    if (role !== "CUSTOMER") {
      localStorage.clear();
      throw new Error("Unsupported account role");
    }

    const customerResponse = await API.get(`/api/customers/by-user/${userid}`);
    const customer = customerResponse.data;
    const kycStatus = (customer.kycstatus || customer.kycStatus || "PENDING").toUpperCase();

    localStorage.setItem("name", customer.fullname);
    localStorage.setItem("customerid", customer.customerid);

    if (!isPasskeyLogin && !localStorage.getItem("passkeyRegistered")) {
      try {
        await registerPasskey();
      } catch (error) {
        console.warn("Passkey registration skipped:", error);
      }
    }

    if (!["VERIFIED", "APPROVED"].includes(kycStatus)) {
      alert("KYC not verified");
      return;
    }

    const accountsResponse = await API.get(`/accounts/customer/${customer.customerid}`);
    const accounts = accountsResponse.data;
    if (!accounts?.length) {
      navigate("/open-account", { replace: true });
      return;
    }

    localStorage.setItem("accountid", accounts[0].accountid);
    localStorage.setItem("accountnumber", accounts[0].accountnumber);
    navigate("/customer", { replace: true });
  };

  const registerPasskey = async () => {
    const token = localStorage.getItem("token");
    const response = await API.post(
      "/auth/passkeys/registration/options",
      {},
      { headers: { Authorization: `Bearer ${token}` } }
    );
    const { requestId, publicKey } = response.data;
    publicKey.challenge = decodeBase64Url(publicKey.challenge);
    publicKey.user.id = decodeBase64Url(publicKey.user.id);
    publicKey.excludeCredentials = publicKey.excludeCredentials?.map((credential) => ({
      ...credential,
      id: decodeBase64Url(credential.id),
    }));

    const credential = await navigator.credentials.create({ publicKey });
    await API.post(
      "/auth/passkeys/registration/verify",
      { requestId, credentialJson: JSON.stringify(credential.toJSON()) },
      { headers: { Authorization: `Bearer ${token}` } }
    );
    localStorage.setItem("passkeyRegistered", "true");
  };

  const handleLogin = async (event) => {
    event.preventDefault();
    if (!username.trim() || !password) {
      alert("Enter your email and password");
      return;
    }

    setIsSubmitting(true);
    try {
      const response = await API.post("/auth/login", {
        username: username.trim(),
        password,
      });
      await completeLogin(response.data);
    } catch (error) {
      console.error(error);
      alert(error.response?.data?.message || "Invalid credentials");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handlePasskeyLogin = async () => {
    if (!username.trim()) {
      alert("Enter your email first");
      return;
    }

    setIsSubmitting(true);
    try {
      const optionsResponse = await API.post(
        "/auth/passkeys/authentication/options",
        { username: username.trim() }
      );
      const { requestId, publicKey } = optionsResponse.data;
      publicKey.challenge = decodeBase64Url(publicKey.challenge);
      publicKey.allowCredentials = publicKey.allowCredentials?.map((credential) => ({
        ...credential,
        id: decodeBase64Url(credential.id),
      }));

      const assertion = await navigator.credentials.get({ publicKey });
      const verifyResponse = await API.post(
        "/auth/passkeys/authentication/verify",
        {
          username: username.trim(),
          requestId,
          credentialJson: JSON.stringify(assertion.toJSON()),
        }
      );
      await completeLogin(verifyResponse.data, true);
    } catch (error) {
      console.error(error);
      alert(error.response?.data?.message || "Passkey login failed");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <h2>Login</h2>
        <form onSubmit={handleLogin}>
          <input
            type="email"
            placeholder="Email"
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            autoComplete="username"
          />
          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            autoComplete="current-password"
          />
          <button type="submit" disabled={isSubmitting} className="login-btn">
            {isSubmitting ? "Signing in..." : "Login"}
          </button>
        </form>
        <hr />
        <button type="button" onClick={handlePasskeyLogin} disabled={isSubmitting} className="primary-btn">
          Login with Passkey
        </button>
        <p className="register-link">
          New user? <span onClick={() => navigate("/register")}>Register here</span>
        </p>
      </div>
    </div>
  );
}