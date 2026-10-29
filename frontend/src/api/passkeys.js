// src/api/passkeys.js
import {
  prepareRegistrationOptions,
  prepareAuthenticationOptions,
  safeToJSON,
} from "../utils/webauthn";

const API_BASE = import.meta.env.VITE_API_BASE_URL || "https://localhost:9192"; // change if needed

async function jsonFetch(url, { method = "POST", token, body } = {}) {
  const res = await fetch(url, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `HTTP ${res.status}`);
  }
  return res.json();
}

// -------- Registration --------
export async function beginPasskeyRegistration(token) {
  return jsonFetch(`${API_BASE}/auth/passkeys/registration/options`, { token });
}

export async function finishPasskeyRegistration(token, requestId, credential) {
  const payload = safeToJSON(credential, "registration");
  return jsonFetch(`${API_BASE}/auth/passkeys/registration/verify`, {
    token,
    body: {
      requestId,
      credentialJson: JSON.stringify(payload),
    },
  });
}

// -------- Login --------
export async function beginPasskeyLogin(username) {
  return jsonFetch(`${API_BASE}/auth/passkeys/authentication/options`, {
    body: { username },
  });
}

export async function finishPasskeyLogin(username, requestId, assertion) {
  const payload = safeToJSON(assertion, "authentication");
  return jsonFetch(`${API_BASE}/auth/passkeys/authentication/verify`, {
    body: {
      username,
      requestId,
      credentialJson: JSON.stringify(payload),
    },
  });
}

// -------- High level helpers that call navigator.* --------
export async function registerWithPasskey(token) {
  const { requestId, publicKey } = await beginPasskeyRegistration(token);

  const options = prepareRegistrationOptions(publicKey);
  const credential = await navigator.credentials.create({ publicKey: options });

  await finishPasskeyRegistration(token, requestId, credential);
  return true;
}

export async function loginWithPasskey(username) {
  const { requestId, publicKey } = await beginPasskeyLogin(username);

  const options = prepareAuthenticationOptions(publicKey);
  const assertion = await navigator.credentials.get({ publicKey: options });

  const loginResponse = await finishPasskeyLogin(username, requestId, assertion);
  return loginResponse; // should contain token like your LoginResponse
}
