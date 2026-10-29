import API from "../../api/api";
import "./Card.css";
import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";

export default function Cards() {
  const navigate = useNavigate();
  const userId = localStorage.getItem("userid");
    const clickTimer = useRef(null);
  const [showCvv, setShowCvv] = useState(false);
 
  const [loading, setLoading] = useState(true);
  const [card, setCard] = useState(null);
 
  const [account, setAccount] = useState(null);
  const [customer, setCustomer] = useState(null);
 
  const [showControls, setShowControls] = useState(false);
  const [saving, setSaving] = useState(false);
 
  // controls state
  const [atmEnabled, setAtmEnabled] = useState(true);
  const [atmLimit, setAtmLimit] = useState(20000);
 
  const [onlineEnabled, setOnlineEnabled] = useState(true);
  const [onlineLimit, setOnlineLimit] = useState(50000);
 
  const [contactlessEnabled, setContactlessEnabled] = useState(true);
  const [contactlessLimit, setContactlessLimit] = useState(5000);
 
  const maskCardNumber = (num) => `**** **** **** ${String(num).slice(-4)}`;
 
  const formatMoney = (v) => {
    const n = Number(v || 0);
    return `₹ ${n.toLocaleString("en-IN")}`;
  };
 
  const expiryMMYY = useMemo(() => {
    if (!card?.expiry) return "";
    const d = new Date(card.expiry);
    const mm = String(d.getMonth() + 1).padStart(2, "0");
    const yy = String(d.getFullYear()).slice(-2);
    return `${mm}/${yy}`;
  }, [card]);
 
  const loadAll = async () => {
    try {
      setLoading(true);
 
      // 1) customer by userid
      const customerRes = await API.get(`/api/customers/by-user/${userId}`);
      const c = customerRes.data;
      setCustomer(c);
 
      // 2) account by customerid (take first)
      const accRes = await API.get(`/accounts/customer/${c.customerid}`);
      const accounts = accRes.data || [];
      const a = accounts[0];
      setAccount(a);
 
      // 3) card by accountId, pass holderName
      const cardRes = await API.get(`/cards/${a.accountid}`, {
        params: { holderName: c.fullname }
      });
 
      const cd = cardRes.data;
      setCard(cd);
 
      // init controls
      setAtmEnabled(!!cd.atmEnabled);
      setAtmLimit(Number(cd.atmLimit ?? 20000));
 
      setOnlineEnabled(!!cd.onlineEnabled);
      setOnlineLimit(Number(cd.onlineLimit ?? 50000));
 
      setContactlessEnabled(!!cd.contactlessEnabled);
      setContactlessLimit(Number(cd.contactlessLimit ?? 5000));
 
      // if card is locked, ensure controls sheet is closed
      if (cd.locked) setShowControls(false);
 
    } catch (e) {
      console.error("Cards page error:", e);
    } finally {
      setLoading(false);
    }
  };
 
  useEffect(() => {
    loadAll();
   
  }, []);
 
 
  const saveControls = async () => {
    if (!card?.cardid) return;
    if (card.locked) return; //  block on locked
 
    try {
      setSaving(true);
 
      const payload = {
        atmEnabled,
        atmLimit,
        onlineEnabled,
        onlineLimit,
        contactlessEnabled,
        contactlessLimit
      };
 
      const res = await API.put(`/cards/${card.cardid}/controls`, payload);
      setCard(res.data);
      setShowControls(false);
 
    } catch (e) {
      console.error("Save controls error:", e);
    } finally {
      setSaving(false);
    }
  };
 
  const toggleLock = async () => {
    if (!card?.cardid) return;
    try {
      const res = await API.put(`/cards/${card.cardid}/lock`, {
        locked: !card.locked
      });
 
      const updated = res.data;
      setCard(updated);
 
      // close controls if locked
      if (updated.locked) setShowControls(false);
 
    } catch (e) {
      console.error("Lock error:", e);
    }
  };
 
  if (loading) {
    return <div className="cards-page"><p className="muted">Loading card...</p></div>;
  }
 
  if (!card || !account || !customer) {
    return <div className="cards-page"><p className="muted">Unable to load card</p></div>;
  }
 
  return (
    <div className="cards-page fade-in">
 
      {/* Back Button */}
      <button onClick={() => navigate('/customer-dashboard')} style={{ marginBottom: '20px', padding: '8px 16px', backgroundColor: '#16a085', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '14px' }}>
        ← Back to Dashboard
      </button>

      <div className="cards-header">
        <h2>My Card</h2>
        {/* <button className="ghost-btn" onClick={loadAll}>Refresh</button> */}
      </div>
 
      {/* Virtual Card (single click: open controls if not locked, double click: flip CVV) */}
      <div
        className={`vcard ${card.locked ? "vcard-locked" : ""} ${showCvv ? "flip" : ""}`}
       onClick={() => {
  if (clickTimer.current) return;
 
  clickTimer.current = setTimeout(() => {
    if (!card.locked) setShowControls(true);
    clickTimer.current = null;
  }, 400);
}}
 
onDoubleClick={() => {
  if (clickTimer.current) {
    clearTimeout(clickTimer.current);
    clickTimer.current = null;
  }
  setShowCvv((p) => !p);
}}
 
        role="button"
        tabIndex={0}
        title="Double click to view CVV"
      >
        {/* FRONT */}
        <div className="vcard-face front">
          <div className="vcard-top">
            <div className="brand">
              <span className="brand-name">{card.bankname}</span>
              <span className="brand-tag">TRUST IS KEY</span>
            </div>
            <div className="chip" />
          </div>
 
          <div className="vcard-number">
            {maskCardNumber(card.cardnumber)}
          </div>
 
          <div className="vcard-bottom">
            <div>
              <div className="small">CARD HOLDER</div>
              <div className="strong">{card.cardholdername}</div>
            </div>
            <div className="right">
              <div className="small">EXP</div>
              <div className="strong">{expiryMMYY}</div>
            </div>
          </div>
 
          <div className="tap-hint">
            {card.locked ? "Card is locked" : "Tap card to manage"} • Double‑click for CVV
          </div>
        </div>
 
        {/* BACK */}
        <div className="vcard-face back">
          <div className="magstripe"></div>
 
          <div className="cvv-box">
            <span className="cvv-label">CVV</span>
            <span className="cvv-value">{card.cvv}</span>
          </div>
 
          <div className="cvv-extra">
            <div className="small">CARD NUMBER</div>
            <div className="strong">{card.cardnumber}</div>
          </div>
 
          <div className="hint">Double‑click card to hide</div>
        </div>
      </div>
 
      {/* Quick Actions */}
      <div className="quick-actions">
        <button className={`pill ${card.locked ? "pill-red" : "pill-blue"}`} onClick={toggleLock}>
          {card.locked ? "Unlock Card" : "Lock Card"}
        </button>
      </div>
 
      {/* Summary */}
      <div className="panel">
        <h3>Card Usage Summary</h3>
 
        <div className="row">
          <span>ATM Withdrawal</span>
          <span className={card.atmEnabled ? "ok" : "bad"}>
            {card.atmEnabled ? formatMoney(card.atmLimit) : "OFF"}
          </span>
        </div>
 
        <div className="row">
          <span>Online / E‑Commerce</span>
          <span className={card.onlineEnabled ? "ok" : "bad"}>
            {card.onlineEnabled ? formatMoney(card.onlineLimit) : "OFF"}
          </span>
        </div>
 
        <div className="row">
          <span>Contactless</span>
          <span className={card.contactlessEnabled ? "ok" : "bad"}>
            {card.contactlessEnabled ? formatMoney(card.contactlessLimit) : "OFF"}
          </span>
        </div>
      </div>
 
      {/* Bottom Sheet Controls */}
      {showControls && !card.locked && (
        <div className="sheet-overlay" onClick={() => setShowControls(false)}>
          <div className="sheet" onClick={(e) => e.stopPropagation()}>
            <div className="sheet-head">
              <div>
                <div className="sheet-title">Card Controls & Limits</div>
                <div className="sheet-sub">Set ON/OFF and limits per channel</div>
              </div>
              <button className="xbtn" onClick={() => setShowControls(false)}>✕</button>
            </div>
 
            {/* ATM */}
            <div className="control-block">
              <div className="control-top">
                <span className="control-name">ATM Withdrawal</span>
                <label className="switch">
                  <input
                    type="checkbox"
                    checked={atmEnabled}
                    onChange={(e) => setAtmEnabled(e.target.checked)}
                  />
                  <span className="slider"></span>
                </label>
              </div>
 
              <div className={`control-limit ${!atmEnabled ? "disabled" : ""}`}>
                <div className="limit-value">{formatMoney(atmLimit)}</div>
                <input
                  type="range"
                  min="0"
                  max="100000"
                  step="500"
                  value={atmLimit}
                  disabled={!atmEnabled}
                  onChange={(e) => setAtmLimit(Number(e.target.value))}
                />
              </div>
            </div>
 
            {/* ONLINE */}
            <div className="control-block">
              <div className="control-top">
                <span className="control-name">Online / E‑Commerce</span>
                <label className="switch">
                  <input
                    type="checkbox"
                    checked={onlineEnabled}
                    onChange={(e) => setOnlineEnabled(e.target.checked)}
                  />
                  <span className="slider"></span>
                </label>
              </div>
 
              <div className={`control-limit ${!onlineEnabled ? "disabled" : ""}`}>
                <div className="limit-value">{formatMoney(onlineLimit)}</div>
                <input
                  type="range"
                  min="0"
                  max="200000"
                  step="500"
                  value={onlineLimit}
                  disabled={!onlineEnabled}
                  onChange={(e) => setOnlineLimit(Number(e.target.value))}
                />
              </div>
            </div>
 
            {/* CONTACTLESS */}
            <div className="control-block">
              <div className="control-top">
                <span className="control-name">Contactless</span>
                <label className="switch">
                  <input
                    type="checkbox"
                    checked={contactlessEnabled}
                    onChange={(e) => setContactlessEnabled(e.target.checked)}
                  />
                  <span className="slider"></span>
                </label>
              </div>
 
              <div className={`control-limit ${!contactlessEnabled ? "disabled" : ""}`}>
                <div className="limit-value">{formatMoney(contactlessLimit)}</div>
                <input
                  type="range"
                  min="0"
                  max="50000"
                  step="100"
                  value={contactlessLimit}
                  disabled={!contactlessEnabled}
                  onChange={(e) => setContactlessLimit(Number(e.target.value))}
                />
              </div>
            </div>
 
            <button className="savebtn" onClick={saveControls} disabled={saving}>
              {saving ? "Saving..." : "Save Changes"}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}