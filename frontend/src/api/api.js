import axios from "axios";

const baseURL =
  import.meta.env.VITE_API_GATEWAY_URL ||
  "https://localhost:9192";

const API = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json",
  },
});

API.interceptors.request.use(
  (config) => {

    const token =
      localStorage.getItem("token");

    const publicEndpoints = [
      "/auth/login",
      "/auth/register",
      "/auth/send-otp",
      "/auth/verify-otp",
      "/auth/passkeys/authentication"
    ];

    const isPublic =
      publicEndpoints.some(url =>
        config.url?.includes(url)
      );

    if (token && !isPublic) {

      config.headers.Authorization =
        `Bearer ${token}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

API.interceptors.response.use(

  (response) => response,

  (error) => {

    const status =
      error.response?.status;

    if (status === 401) {

      console.warn(
        "Session expired. Redirecting..."
      );

      localStorage.clear();

      window.location.href = "/login";
    }

    return Promise.reject(error);
  }
);

export default API;