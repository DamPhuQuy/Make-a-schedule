import React, { useState } from "react";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

type LoginRequest = {
  email: string;
  password: string;
};

type RegisterRequest = {
  email: string;
  password: string;
  confirmPassword: string;
};

type StatusType = "idle" | "error" | "success";

type AuthViewProps = Readonly<{
  onLogin: (token: string) => void;
}>;

export default function AuthView({ onLogin }: AuthViewProps) {
  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [status, setStatus] = useState<StatusType>("idle");
  const [statusMessage, setStatusMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [confirmPassword, setConfirmPassword] = useState("");

  const isRegister = !isLogin;

  const submitAuth = async () => {
    if (!email || !password || (isRegister && password !== confirmPassword)) {
      setStatus("error");
      setStatusMessage("Please fill in all fields correctly.");
      return;
    }

    setLoading(true);
    setStatus("idle");
    setStatusMessage("");

    try {
      const endpoint = isLogin ? "/api/auth/login" : "/api/auth/register";
      const body: LoginRequest | RegisterRequest = isLogin
        ? { email, password }
        : { email, password, confirmPassword };

      const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });

      const contentType = response.headers.get("content-type") ?? "";
      const data = contentType.includes("application/json")
        ? await response.json()
        : { message: await response.text() };

      if (!response.ok) {
        throw new Error(data.message || "Authentication failed");
      }

      if (isLogin) {
        onLogin(data.accessToken);
      } else {
        // Automatically swap to login after registering
        setIsLogin(true);
        setStatus("success");
        setStatusMessage("Registration successful! Please sign in.");
        setPassword("");
        setConfirmPassword("");
      }
    } catch (err: any) {
      setStatus("error");
      setStatusMessage(err.message ?? "Authentication failed");
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = (event: React.SyntheticEvent<HTMLFormElement>) => {
    event.preventDefault();
    void submitAuth();
  };

  const handleToggleMode = () => {
    setIsLogin((prev) => !prev);
    setStatus("idle");
    setStatusMessage("");
  };

  let submitLabel = "Register";
  if (loading) {
    submitLabel = "Processing...";
  } else if (isLogin) {
    submitLabel = "Sign in";
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
          {isLogin ? "Sign in to Schedule" : "Create a new account"}
        </h2>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow rounded-lg sm:px-10">
          {statusMessage && (
            <div
              className={`mb-4 p-2 text-sm rounded ${status === "success" ? "bg-green-100 text-green-700" : "bg-red-100 text-red-700"}`}
            >
              {statusMessage}
            </div>
          )}

          <form className="space-y-6" onSubmit={handleSubmit}>
            <div>
              <label
                htmlFor="auth-email"
                className="block text-sm font-medium text-gray-700"
              >
                Email
              </label>
              <div className="mt-1">
                <input
                  id="auth-email"
                  type="email"
                  required
                  className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>
            </div>

            <div>
              <label
                htmlFor="auth-password"
                className="block text-sm font-medium text-gray-700"
              >
                Password
              </label>
              <div className="mt-1">
                <input
                  id="auth-password"
                  type={showPassword ? "text" : "password"}
                  required
                  className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                />
              </div>
            </div>

            {isRegister && (
              <div>
                <label
                  htmlFor="auth-confirm-password"
                  className="block text-sm font-medium text-gray-700"
                >
                  Confirm Password
                </label>
                <div className="mt-1">
                  <input
                    id="auth-confirm-password"
                    type={showPassword ? "text" : "password"}
                    required
                    className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                  />
                </div>
              </div>
            )}

            <div>
              <div className="flex items-center">
                <input
                  type="checkbox"
                  id="showPassword"
                  checked={showPassword}
                  onChange={(e) => setShowPassword(e.target.checked)}
                />
                <label
                  htmlFor="showPassword"
                  className="ml-2 text-sm text-gray-600"
                >
                  Show Password
                </label>
              </div>
            </div>

            <div>
              <button
                type="submit"
                disabled={loading}
                className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
              >
                {submitLabel}
              </button>
            </div>
          </form>

          <div className="mt-6">
            <div className="relative">
              <div className="relative flex justify-center text-sm">
                <span className="px-2 bg-white text-gray-500">
                  {isLogin ? "New user?" : "Already registered?"}
                </span>
                <button
                  type="button"
                  onClick={handleToggleMode}
                  className="font-medium text-blue-600 hover:text-blue-500"
                >
                  {isLogin ? "Create an account" : "Sign in instead"}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
