import { loginWithPasskey } from "../api/passkeys";

export default function PasskeyLoginButton({ username, onLoggedIn }) {
  const onClick = async () => {
    try {
      if (!window.PublicKeyCredential) {
        alert("Passkeys not supported in this browser.");
        return;
      }
      const resp = await loginWithPasskey(username);
      // resp has token + user details (your LoginResponse)
      localStorage.setItem("token", resp.token);
      onLoggedIn?.(resp);
      alert("✅ Logged in with passkey!");
    } catch (e) {
      console.error(e);
      alert("❌ Passkey login failed: " + e.message);
    }
  };

  return <button onClick={onClick}>Login with Passkey</button>;
}
