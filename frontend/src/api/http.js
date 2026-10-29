// src/api/http.js

import axios from "axios";

const baseURL =
  import.meta.env.VITE_API_GATEWAY_URL || "https://localhost:9192";

export const http = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true, // helps with cookies if needed later
});

// REQUEST INTERCEPTOR (adds token)
http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // DEBUG (optional but useful)
    // console.log("➡️ API Request:", config.method?.toUpperCase(), config.url);

    return config;
  },
  (error) => Promise.reject(error)
);

// RESPONSE INTERCEPTOR (centralized error handling)
http.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {

    const status = error.response?.status;
    const url = error.config?.url;

    // DEBUG LOGGING
    console.error("❌ API Error:", {
      url,
      status,
      message: error.response?.data || error.message,
    });

    // HANDLE TOKEN EXPIRED
    if (status === 401) {
      localStorage.removeItem("token");
      localStorage.removeItem("isLoggedIn");

      alert("Session expired. Please login again.");

      window.location.href = "/login";
    }

    // HANDLE FORBIDDEN (ROLE ISSUE)
    if (status === 403) {
      console.warn("🚫 Access denied:", url);
      alert("You don't have permission to access this resource.");
    }

    // HANDLE NOT FOUND (ROUTING ISSUE)
    if (status === 404) {
      console.warn("🔍 API not found:", url);
    }

    // HANDLE SERVER ERROR
    if (status >= 500) {
      alert("Server error. Please try again later.");
    }

    return Promise.reject(error);
  }
);
