import React, { useEffect, useMemo, useRef, useState } from "react";

import { useNavigate } from "react-router-dom";

import "./chat.css";

import StatementTable from "./StatementTable";

import { trainClassifier } from "./intentClassifier";

import { fetchStatementForChat } from "./statementApi";

 

/**

 * ✅ Routes aligned with your customer navigation patterns:

 * - /customer/deposits and /customer/transfer are present in your app. [1](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html)

 * Update WITHDRAW/STATEMENTS if your actual app routes differ.

 */

const ROUTES = {

  TRANSFER: "/customer/transfer",

  DEPOSIT: "/customer/deposits",

  WITHDRAW: "/customer/withdraw",

  STATEMENTS: "/customer/statements",

  DASHBOARD: "/customer/dashboard",

  PROFILE: "/customer/profile",

};

 

function newSessionId() {

  return (crypto?.randomUUID?.() || `${Date.now()}-${Math.random()}`).toString();

}

 

function sessionBasedMessage() {

  return (

    "This chatbot is a session-based chatbot. No information can be stored once the session ends, " +

    "so I can’t retrieve previous responses. Please ask again in the current session."

  );

}

 

function restrictedResponse() {

  return (

    "Sorry, I don’t have access to display sensitive information due to security concerns.\n" +

    "This chatbot is only meant to guide you and help you fetch details."

  );

}

 

function isHowToQuery(text) {

  const t = (text || "").toLowerCase();

  return (

    t.includes("how") ||

    t.includes("procedure") ||

    t.includes("steps") ||

    t.includes("where") ||

    t.includes("navigate") ||

    t.includes("open") ||

    t.includes("view") ||

    t.includes("fetch") ||

    t.includes("check") ||

    t.includes("get details") ||

    t.startsWith("how do i")

  );

}

 

/**

 * Block only when user is asking to DISPLAY sensitive info.

 * If they ask "how to check balance" -> guide; do NOT block.

 */

function isSensitiveDataRequest(text) {

  const t = (text || "").toLowerCase();

  if (isHowToQuery(t)) return false;

 

  const displayVerbs = ["show", "give", "tell", "what is", "display", "get my", "fetch my"];

  const hasDisplayVerb = displayVerbs.some((v) => t.includes(v));

 

  const sensitiveNouns = [

    "balance",

    "my balance",

    "account balance",

    "account number",

    "my account number",

    "account details",

    "my account details",

    "customer details",

    "my details",

    "profile details",

    "kyc status",

  ];

  const hasSensitiveNoun = sensitiveNouns.some((n) => t.includes(n));

 

  return hasDisplayVerb && hasSensitiveNoun;

}

 

/**

 * Session recall: user asks for "previous/earlier/last" content.

 */

function isSessionRecall(text) {

  const t = (text || "").toLowerCase();

  const recallPhrases = [

    "before closing",

    "before i close",

    "previous",

    "earlier",

    "last time",

    "last message",

    "previous message",

    "that transaction",

    "give that transaction",

    "give that again",

    "what you said before",

  ];

  return recallPhrases.some((p) => t.includes(p));

}

 

function buildActions(intent) {

  switch (intent) {

    case "TRANSFER":

      return [{ label: "Open Transfer Page", route: ROUTES.TRANSFER }];

    case "DEPOSIT":

      return [{ label: "Open Deposit Page", route: ROUTES.DEPOSIT }];

    case "WITHDRAW":

      return [{ label: "Open Withdraw Page", route: ROUTES.WITHDRAW }];

    case "STATEMENT":

      return [{ label: "Open Statements Page", route: ROUTES.STATEMENTS }];

    case "ACCOUNT":

      return [{ label: "Open Dashboard", route: ROUTES.DASHBOARD }];

    case "CUSTOMER":

      return [{ label: "Open Profile", route: ROUTES.PROFILE }];

    default:

      return [];

  }

}

 

function guidanceText(intent) {

  switch (intent) {

    case "TRANSFER":

      return (

        "Transfer procedure:\n" +

        "1) Open Transfer page\n" +

        "2) Enter To Account and Amount\n" +

        "3) Proceed and confirm using your PIN (if asked)\n" +

        "4) Verify status in Statements"

      );

 

    case "DEPOSIT":

      return (

        "Deposit procedure:\n" +

        "1) Open Deposits page\n" +

        "2) Enter amount and proceed\n" +

        "3) Confirm using PIN (if asked)\n" +

        "4) Verify in Statements"

      );

 

    case "WITHDRAW":

      return (

        "Withdraw procedure:\n" +

        "1) Open Withdraw page\n" +

        "2) Enter amount and proceed\n" +

        "3) Confirm using PIN (if asked)\n" +

        "4) Verify in Statements"

      );

 

    case "ACCOUNT":

      return (

        "To fetch/view your account details (securely):\n" +

        "1) Go to Dashboard / Accounts section\n" +

        "2) Select your account\n" +

        "3) View account info (type/status/balance)\n" +

        "Note: I can guide you, but I won’t display private details in chat."

      );

 

    case "CUSTOMER":

      return (

        "To view/update your profile/KYC:\n" +

        "1) Go to Profile page\n" +

        "2) Update details and upload required KYC documents\n" +

        "3) Save changes"

      );

 

    case "SECURITY":

      return (

        "Security help:\n" +

        "• Never share OTP/PIN/password in chat.\n" +

        "• Use 'Forgot Password' on Login page to reset.\n" +

        "• If PIN verification fails, re-check PIN and try again."

      );

 

    default:

      return (

        "I can help with:\n" +

        "• Transfer / Deposit / Withdraw (steps + navigation)\n" +

        "• Statements (I can fetch statement and show in chat)\n" +

        "• Account/Profile guidance (procedure only)\n\n" +

        "Try: 'show my statement' or 'how to transfer money'."

      );

  }

}

 

export default function ChatbotPanel({ open, onClose }) {

  const navigate = useNavigate();

  const listRef = useRef(null);

  const clf = useMemo(() => trainClassifier(), []);

 

  const [sessionId, setSessionId] = useState(newSessionId());

  const [messages, setMessages] = useState([]);

  const [input, setInput] = useState("");

  const [sending, setSending] = useState(false);

 

  //  Reset chat every time opened (no previous chat)

  useEffect(() => {

    if (!open) return;

 

    setSessionId(newSessionId());

    setMessages([

      {

        role: "bot",

        type: "text",

        content:

          "Hi! I’m Support.\n\nExamples:\n• show my statement\n• transfer money\n• how to check balance\n\nNote: Session-based chatbot — no message history is stored after you close it.",

        actions: [],

        ts: Date.now(),

      },

    ]);

    setInput("");

    setSending(false);

  }, [open]);

 

  useEffect(() => {

    if (!open) return;

    const el = listRef.current;

    if (el) el.scrollTop = el.scrollHeight;

  }, [open, messages, sending]);

 

  const handleAction = (route) => {

    onClose?.();

    navigate(route);

  };

 

  const onKeyDown = (e) => {

    if (e.key === "Enter" && !e.shiftKey) {

      e.preventDefault();

      handleSend();

    }

  };

 

  const handleSend = async () => {

    const text = input.trim();

    if (!text || sending) return;

 

    setSending(true);

    setInput("");

 

    setMessages((prev) => [

      ...prev,

      { role: "user", type: "text", content: text, ts: Date.now() },

    ]);

 

    //  Session recall handling (must respond with session-based message)

    if (isSessionRecall(text)) {

      setMessages((prev) => [

        ...prev,

        {

          role: "bot",

          type: "text",

          content: sessionBasedMessage(),

          actions: [],

          ts: Date.now(),

        },

      ]);

      setSending(false);

      return;

    }

 

    //  ML intent

    let { intent, confidence } = clf.predict(text);

 

    //  Keyword override for high precision (prevents wrong routing)

    const lowerText = text.toLowerCase();

    if (/\btransfer\b/.test(lowerText)) intent = "TRANSFER";

    else if (/\bdeposit\b/.test(lowerText)) intent = "DEPOSIT";

    else if (/\bwithdraw\b/.test(lowerText) || /\bwithdrawal\b/.test(lowerText)) intent = "WITHDRAW";

    else if (/\bstatement\b/.test(lowerText) || /\btransactions\b/.test(lowerText)) intent = "STATEMENT";

    else if (lowerText.includes("otp") || lowerText.includes("pin") || lowerText.includes("password") || lowerText.includes("forgot")) intent = "SECURITY";

 

    //  Block only when user asks to display sensitive information

    if (isSensitiveDataRequest(text)) {

      setMessages((prev) => [

        ...prev,

        {

          role: "bot",

          type: "text",

          content: restrictedResponse(),

          actions: [],

          ts: Date.now(),

        },

      ]);

      setSending(false);

      return;

    }

 

    try {

      //  STATEMENT => fetch + show table (NO procedure)

      if (intent === "STATEMENT") {

        const data = await fetchStatementForChat();

        setMessages((prev) => [

          ...prev,

          {

            role: "bot",

            type: "statement",

            content: "Here is your bank statement:",

            statementPayload: data,

            actions: buildActions("STATEMENT"),

            ts: Date.now(),

          },

        ]);

      } else {

        //  Guidance response (includes ACCOUNT/CUSTOMER how-to)

        setMessages((prev) => [

          ...prev,

          {

            role: "bot",

            type: "text",

            content: `${guidanceText(intent)}\n\n(Intent: ${intent}, confidence: ${confidence.toFixed(2)})`,

            actions: buildActions(intent),

            ts: Date.now(),

          },

        ]);

      }

    } catch (err) {

      setMessages((prev) => [

        ...prev,

        {

          role: "bot",

          type: "text",

          content:

            intent === "STATEMENT"

              ? "I couldn’t retrieve your statement. Please open Statements page from the app."

              : "Something went wrong. Please try again.",

          actions: intent === "STATEMENT" ? buildActions("STATEMENT") : [],

          ts: Date.now(),

        },

      ]);

    } finally {

      setSending(false);

    }

  };

 

  if (!open) return null;

 

  return (

    <div className="chat-panel">

      <div className="chat-header">

        <strong>Support Chat</strong>

        <button className="btn btn-sm btn-light" onClick={onClose}>

          ✕

        </button>

      </div>

 

      <div className="chat-body" ref={listRef}>

        {messages.map((m, idx) => (

          <div key={idx} className="mb-2">

            <div className={`bubble ${m.role === "user" ? "user" : "bot"}`}>

              {m.content}

 

              {m.type === "statement" && m.statementPayload && (

                <div className="mt-2">

                  <StatementTable {...m.statementPayload} />

                </div>

              )}

 

              {Array.isArray(m.actions) && m.actions.length > 0 && (

                <div className="action-row">

                  {m.actions.map((a, i) => (

                    <button

                      key={i}

                      className="btn btn-sm btn-outline-primary"

                      onClick={() => handleAction(a.route)}

                    >

                      {a.label}

                    </button>

                  ))}

                </div>

              )}

            </div>

          </div>

        ))}

 

        {sending && <div className="bubble bot">Typing...</div>}

      </div>

 

      <div className="chat-footer">

        <div className="d-flex gap-2">

          <textarea

            className="form-control"

            rows={2}

            value={input}

            onChange={(e) => setInput(e.target.value)}

            onKeyDown={onKeyDown}

            placeholder="Type your message..."

            disabled={sending}

          />

          <button

            className="btn btn-primary"

            onClick={handleSend}

            disabled={sending || !input.trim()}

          >

            Send

          </button>

        </div>

 

        <div className="small text-muted mt-2">

          Session: {sessionId.slice(0, 8)}… (not stored)

        </div>

      </div>

    </div>

  );

}