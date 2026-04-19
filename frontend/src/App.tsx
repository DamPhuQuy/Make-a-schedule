import { useState } from "react";
import CalendarView from "./CalendarView";
import AuthView from "./AuthView";

function App() {
  const [token, setToken] = useState<string | null>(localStorage.getItem("jwt"));

  const handleLogin = (newToken: string) => {
    localStorage.setItem("jwt", newToken);
    setToken(newToken);
  };

  const handleLogout = () => {
    localStorage.removeItem("jwt");
    setToken(null);
  };

  if (!token) {
    return <AuthView onLogin={handleLogin} />;
  }

  return (
    <div>
      <CalendarView onLogout={handleLogout} />
    </div>
  );
}

export default App;
