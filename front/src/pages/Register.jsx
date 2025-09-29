import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api";

export default function Register() {
  const [email, setEmail] = useState("user@example.com");
  const [password, setPassword] = useState("user12345");
  const [role, setRole] = useState("USER"); // ili fiksiraj USER na backendu
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const nav = useNavigate();

  const submit = async (e) => {
    e.preventDefault();
    setLoading(true); setError("");
    try {
      const { data } = await api.post("/auth/register", { email, password, role });
      localStorage.setItem("token", data.token);
      localStorage.setItem("user", JSON.stringify(data));
      nav("/login"); // ili nav("/") za auto-login
    } catch (ex) {
      setError(ex?.response?.status === 409 ? "Email već postoji." : "Registracija nije uspela.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={submit} style={{maxWidth:420, margin:"40px auto", display:"grid", gap:10}}>
      <h2>Registracija</h2>
      <input placeholder="Email" value={email} onChange={e=>setEmail(e.target.value)} />
      <input placeholder="Lozinka (min 6)" type="password" value={password} onChange={e=>setPassword(e.target.value)} />
      <select value={role} onChange={e=>setRole(e.target.value)}>
        <option value="USER">USER</option>
        <option value="ADMIN">ADMIN</option>
      </select>
      {error && <div style={{color:"red"}}>{error}</div>}
      <button disabled={loading}>{loading ? "..." : "Registruj se"}</button>
    </form>
  );
}
