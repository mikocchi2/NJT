import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api";

export default function Login() {
  const [email, setEmail] = useState("user@example.com");
  const [password, setPassword] = useState("user12345");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const nav = useNavigate();

  const submit = async (e) => {
    e.preventDefault();
    setLoading(true); setError("");
    try {
      const { data } = await api.post("/auth/login", { email, password });
      // očekuje: { token, userId, email, role }
      localStorage.setItem("token", data.token);
      localStorage.setItem("user", JSON.stringify(data));
      nav("/"); // ili na stranicu za search
    } catch (ex) {
      setError("Pogrešan email ili lozinka.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={submit} style={{maxWidth:420, margin:"40px auto", display:"grid", gap:10}}>
      <h2>Login</h2>
      <input placeholder="Email" value={email} onChange={e=>setEmail(e.target.value)} />
      <input placeholder="Lozinka" type="password" value={password} onChange={e=>setPassword(e.target.value)} />
      {error && <div style={{color:"red"}}>{error}</div>}
      <button disabled={loading}>{loading ? "..." : "Prijavi se"}</button>
    </form>
  );
}
