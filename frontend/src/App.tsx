import { useState } from "react";
import AuthView from "./AuthView";
import CalendarView from "./CalendarView";

const TOKEN_STORAGE_KEY = "jwt";

function App() {
  const [token, setToken] = useState<string | null>(() =>
    localStorage.getItem(TOKEN_STORAGE_KEY),
  );

  const handleLogin = (newToken: string) => {
    localStorage.setItem(TOKEN_STORAGE_KEY, newToken);
    setToken(newToken);
  };

  const handleLogout = () => {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    setToken(null);
  };

  return token ? (
    <CalendarView onLogout={handleLogout} />
  ) : (
    <AuthView onLogin={handleLogin} />
  );
}

export default App;
