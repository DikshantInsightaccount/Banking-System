import { registerWithPasskey } from "../api/passkeys";

export default function RegisterPasskeyButton() {
  const token = localStorage.getItem("token");

  const onClick = async () => {
    try {
      if (!window.PublicKeyCredential) {
        alert("Passkeys not supported in this browser.");
        return;
      }
      await registerWithPasskey(token);
      alert("✅ Passkey registered successfully!");
    } catch (e) {
      console.error(e);
      alert("❌ Passkey registration failed: " + e.message);
    }
  };

  return (
    <button onClick={onClick} disabled={!token}>
      Register Passkey
    </button>
  );
}