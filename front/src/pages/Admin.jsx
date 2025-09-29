import { useEffect, useState } from "react";
import api from "../api";

export default function Admin() {
  const [users, setUsers] = useState([]);
  const [logs, setLogs] = useState([]);
  const [msg, setMsg] = useState("");

  const loadUsers = async () => {
    try {
      const { data } = await api.get("/admin/users");
      setUsers(data || []);
    } catch {}
  };
  const loadLogs = async () => {
    try {
      const { data } = await api.get("/admin/api-logs");
      setLogs(data || []);
    } catch {}
  };

  useEffect(() => { loadUsers(); loadLogs(); }, []);

  const delUser = async (id) => {
    await api.delete(`/admin/users/${id}`);
    loadUsers();
  };

  const refreshPopular = async () => {
    const { data } = await api.post("/admin/refresh-hot");
    setMsg(String(data));
    loadLogs();
  };

  return (
    <div style={{maxWidth:1000, margin:"24px auto"}}>
      <h2>Admin</h2>

      <section style={{marginBottom:24}}>
        <h3>Korisnici</h3>
        <div style={{display:"grid", gap:6}}>
          {users.map(u => (
            <div key={u.id} style={{border:"1px solid #eee", padding:8}}>
              <b>{u.email}</b> — {u.role}
              <button style={{marginLeft:8}} onClick={()=>delUser(u.id)}>Obriši</button>
            </div>
          ))}
          {!users.length && <div>Nema korisnika.</div>}
        </div>
      </section>

      <section style={{marginBottom:24}}>
        <h3>API Logs</h3>
        <div style={{display:"grid", gap:6, maxHeight:260, overflow:"auto"}}>
          {logs.map(l => (
            <div key={l.id} style={{border:"1px solid #eee", padding:8}}>
              <div>{l.createdAt} · {l.region} · {l.endpoint} · status: {l.status}</div>
              <div style={{fontSize:12, color:"#666"}}>actorUserId: {l.actorUserId}</div>
            </div>
          ))}
          {!logs.length && <div>Nema logova.</div>}
        </div>
        <button onClick={refreshPopular}>Refresh popular summoners cache</button>
        {msg && <div style={{marginTop:8}}>{msg}</div>}
      </section>
    </div>
  );
}
