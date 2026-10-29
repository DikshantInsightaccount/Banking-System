import { useState } from "react";
import { useNavigate } from "react-router-dom";

import API from "../../api/api";
import "./features.css";

export default function Transfer() {
    const navigate = useNavigate();

    const fromAccountNumber =
        localStorage.getItem("accountnumber");

    const userId =
        localStorage.getItem("userid");

    const [toAccountNumber, setToAccountNumber] =
        useState("");

    const [amount, setAmount] =
        useState("");

    const [password, setPassword] =
        useState("");

    const [message, setMessage] =
        useState("");

    const [messageType, setMessageType] =
        useState("");

    const [loading, setLoading] =
        useState(false);

    const [idempotencyKey, setIdempotencyKey] =
        useState(() => crypto.randomUUID());

    const submit = async (event) => {
        event.preventDefault();

        if (loading) {
            return;
        }

        setMessage("");
        setMessageType("");

        const normalizedRecipient =
            toAccountNumber.trim();

        const numericAmount =
            Number(amount);

        if (!fromAccountNumber || !userId) {
            setMessage(
                "Account information is unavailable. Please log in again."
            );

            setMessageType("error");
            return;
        }

        if (!normalizedRecipient) {
            setMessage(
                "Enter the recipient account number."
            );

            setMessageType("error");
            return;
        }

        if (
            normalizedRecipient ===
            fromAccountNumber.trim()
        ) {
            setMessage(
                "Source and recipient accounts cannot be the same."
            );

            setMessageType("error");
            return;
        }

        if (
            !Number.isFinite(numericAmount) ||
            numericAmount <= 0
        ) {
            setMessage(
                "Enter an amount greater than zero."
            );

            setMessageType("error");
            return;
        }

        if (!password.trim()) {
            setMessage(
                "Enter your password to authorize the transfer."
            );

            setMessageType("error");
            return;
        }

        setLoading(true);

        try {
            const response =
                await API.post(
                    "/api/transactions/transfer",
                    {
                        userId:
                            Number(userId),

                        fromAccountNumber:
                            fromAccountNumber.trim(),

                        toAccountNumber:
                            normalizedRecipient,

                        amount:
                            numericAmount,

                        password,

                        idempotencyKey
                    }
                );

            const transfer =
                response.data;

            if (
                !transfer ||
                transfer.status !== "SUCCESS"
            ) {
                throw new Error(
                    "The transfer could not be confirmed."
                );
            }

            setMessage(
                `Transfer of ₹${numericAmount.toLocaleString(
                    "en-IN",
                    {
                        minimumFractionDigits: 0,
                        maximumFractionDigits: 2
                    }
                )} completed successfully.`
            );

            setMessageType("success");

            setToAccountNumber("");
            setAmount("");
            setPassword("");

            /*
             * Prepare a fresh key for the next independent
             * transfer.
             */
            setIdempotencyKey(
                crypto.randomUUID()
            );

            /*
             * Return to the dashboard after briefly showing
             * the success message.
             *
             * CustomerDashboard listens for
             * location.state.refreshDashboard and refetches
             * the account balance and statement.
             */
            window.setTimeout(() => {
                navigate(
                    "/customer/dashboard",
                    {
                        replace: true,

                        state: {
                            refreshDashboard:
                                Date.now(),

                            transferSuccess:
                                true,

                            transferId:
                                transfer.transferid,

                            transferredAmount:
                                numericAmount
                        }
                    }
                );
            }, 1000);

        } catch (error) {
            console.error(
                "Transfer failed:",
                error
            );

            setMessage(
                error.response?.data?.message ||
                error.response?.data?.error ||
                error.message ||
                "Transfer failed. Please try again."
            );

            setMessageType("error");

            /*
             * Keep the same idempotency key after uncertain
             * request failures. Retrying with the same key
             * prevents the backend from creating a duplicate
             * transfer if the first request was committed but
             * its response was interrupted.
             */
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="customer-page">
            <div className="feature-card">
                <span className="feature-eyebrow">
                    BANKING ACTION
                </span>

                <h2>
                    Transfer Money
                </h2>

                <form
                    onSubmit={submit}
                    className="form-grid"
                >
                    <label>
                        Recipient account

                        <input
                            type="text"
                            value={toAccountNumber}
                            onChange={(event) => {
                                setToAccountNumber(
                                    event.target.value
                                );

                                setMessage("");
                                setMessageType("");
                            }}
                            placeholder="Enter recipient account number"
                            autoComplete="off"
                            disabled={loading}
                            required
                        />
                    </label>

                    <label>
                        Amount

                        <input
                            type="number"
                            min="0.01"
                            step="0.01"
                            value={amount}
                            onChange={(event) => {
                                setAmount(
                                    event.target.value
                                );

                                setMessage("");
                                setMessageType("");
                            }}
                            placeholder="Enter amount"
                            disabled={loading}
                            required
                        />
                    </label>

                    <label>
                        Password

                        <input
                            type="password"
                            value={password}
                            onChange={(event) => {
                                setPassword(
                                    event.target.value
                                );

                                setMessage("");
                                setMessageType("");
                            }}
                            placeholder="Enter your password"
                            autoComplete="current-password"
                            disabled={loading}
                            required
                        />
                    </label>

                    <button
                        type="submit"
                        disabled={loading}
                    >
                        {loading
                            ? "Processing..."
                            : "Transfer"}
                    </button>
                </form>

                {message && (
                    <p
                        className={
                            messageType === "success"
                                ? "feature-message success"
                                : "feature-message error"
                        }
                        role={
                            messageType === "error"
                                ? "alert"
                                : "status"
                        }
                    >
                        {message}
                    </p>
                )}
            </div>
        </div>
    );
}