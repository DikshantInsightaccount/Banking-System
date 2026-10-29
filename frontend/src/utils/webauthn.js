export function bufferToBase64url(buffer) {
  const bytes = new Uint8Array(buffer);
  let str = "";
  for (const b of bytes) str += String.fromCharCode(b);
  const base64 = btoa(str);
  return base64.replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/g, "");
}

export function base64urlToBuffer(base64url) {
  const padding = "=".repeat((4 - (base64url.length % 4)) % 4);
  const base64 = (base64url + padding).replace(/-/g, "+").replace(/_/g, "/");
  const str = atob(base64);
  const bytes = new Uint8Array(str.length);
  for (let i = 0; i < str.length; i++) bytes[i] = str.charCodeAt(i);
  return bytes.buffer;
}

// ---------- Convert server options (JSON) to WebAuthn options (ArrayBuffers) ----------
export function prepareRegistrationOptions(publicKey) {
  const pk = structuredClone(publicKey);

  pk.challenge = base64urlToBuffer(pk.challenge);
  pk.user.id = base64urlToBuffer(pk.user.id);

  if (pk.excludeCredentials) {
    pk.excludeCredentials = pk.excludeCredentials.map((c) => ({
      ...c,
      id: base64urlToBuffer(c.id),
    }));
  }

  return pk;
}

export function prepareAuthenticationOptions(publicKey) {
  const pk = structuredClone(publicKey);

  pk.challenge = base64urlToBuffer(pk.challenge);

  if (pk.allowCredentials) {
    pk.allowCredentials = pk.allowCredentials.map((c) => ({
      ...c,
      id: base64urlToBuffer(c.id),
    }));
  }

  return pk;
}

// ---------- Serialize credential/assertion to JSON (backend-friendly) ----------
export function serializeRegistrationCredential(cred) {
  // cred: PublicKeyCredential (Attestation)
  const response = cred.response;

  return {
    id: cred.id,
    rawId: bufferToBase64url(cred.rawId),
    type: cred.type,
    clientExtensionResults: cred.getClientExtensionResults?.() ?? {},
    response: {
      clientDataJSON: bufferToBase64url(response.clientDataJSON),
      attestationObject: bufferToBase64url(response.attestationObject),
      // transports (optional)
      transports: response.getTransports?.() ?? undefined,
    },
  };
}

export function serializeAuthenticationCredential(assertion) {
  // assertion: PublicKeyCredential (Assertion)
  const response = assertion.response;

  return {
    id: assertion.id,
    rawId: bufferToBase64url(assertion.rawId),
    type: assertion.type,
    clientExtensionResults: assertion.getClientExtensionResults?.() ?? {},
    response: {
      clientDataJSON: bufferToBase64url(response.clientDataJSON),
      authenticatorData: bufferToBase64url(response.authenticatorData),
      signature: bufferToBase64url(response.signature),
      userHandle: response.userHandle ? bufferToBase64url(response.userHandle) : null,
    },
  };
}

// Prefer native toJSON if present (some browsers have it), else manual
export function safeToJSON(obj, kind) {
  if (obj?.toJSON) return obj.toJSON();
  return kind === "registration"
    ? serializeRegistrationCredential(obj)
    : serializeAuthenticationCredential(obj);
}