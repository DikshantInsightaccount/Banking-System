import { useState } from "react";
import "bootstrap/dist/css/bootstrap.min.css";
import ChatbotPanel from "../../support-chat/ChatbotPanel";

const faqs = [
  ["How do I open a new account?", "Log in, open the Open Account page, choose an account type, and submit the requested details."],
  ["How can I check my account balance?", "Your current balance is shown on the dashboard. Use Statements for transaction history."],
  ["How do I transfer money?", "Open Transfer, enter the recipient and amount, then confirm the transaction."],
  ["How do I view my statements?", "Open Statements from the customer dashboard to view or download your history."],
  ["How do I update my profile?", "Open Profile, update the allowed fields, and save your changes."],
  ["How do I apply for a fixed deposit?", "Open Fixed Deposit, select the term and amount, and submit the application."],
  ["What is a recurring deposit?", "A recurring deposit saves a fixed amount periodically for a selected tenure."],
  ["How can I contact support?", "Use this page to browse FAQs or open the chat support panel."],
];

export default function Support() {
  const [openIndex, setOpenIndex] = useState(null);
  const [chatOpen, setChatOpen] = useState(false);

  return (
    <div className="container mt-5 pt-5 w-75">
      <h2 className="text-center mb-4">Frequently Asked Questions</h2>
      <div className="accordion" id="faqAccordion">
        {faqs.map(([question, answer], index) => {
          const isOpen = openIndex === index;
          return (
            <div className="accordion-item" key={question}>
              <h2 className="accordion-header" id={`heading-${index}`}>
                <button
                  className={`accordion-button${isOpen ? "" : " collapsed"}`}
                  type="button"
                  onClick={() => setOpenIndex(isOpen ? null : index)}
                  aria-expanded={isOpen}
                  aria-controls={`collapse-${index}`}
                >
                  {question}
                </button>
              </h2>
              <div
                id={`collapse-${index}`}
                className={`accordion-collapse collapse${isOpen ? " show" : ""}`}
                aria-labelledby={`heading-${index}`}
              >
                <div className="accordion-body">{answer}</div>
              </div>
            </div>
          );
        })}
      </div>
      <button
        className="btn btn-primary rounded-pill shadow"
        type="button"
        style={{ position: "fixed", bottom: 24, right: 24, zIndex: 9999 }}
        onClick={() => setChatOpen(true)}
      >
        Chat Support
      </button>
      <ChatbotPanel open={chatOpen} onClose={() => setChatOpen(false)} />
    </div>
  );
}