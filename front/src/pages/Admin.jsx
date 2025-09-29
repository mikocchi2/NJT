// src/pages/Admin.jsx
import { useEffect, useMemo, useState } from "react";
import api from "../api";

const REGIONS = ["EUNE","EUW","NA1","KR","BR1","JP1","OC1","TR1","LA1","LA2"];

function getAuth() {
  try {
    const raw = localStorage.getItem("user") || sessionStorage.getItem("user") || "{}";
    const u = JSON.parse(raw);
    return { role: u.role, token: u.token };
  } catch {
    return { role: null, token: null };
  }
}

function StatCard({label, value}) {
  return (
    <div style={{border:"1px solid #eee", borderRadius:8, padding:12, minWidth:160}}>
      <div style={{fontSize:12, color:"#666"}}>{label}</div>
      <div style={{fontSize:22, fontWeight:700}}>{value ?? "-"}</div>
    </div>
  );
}

export default function Admin() {
  const { role, token } = getAuth();

  // attach token (ako već nije globalno)
  useEffect(() => {
    if (token) api.defaults.headers.common.Authorization = `Bearer ${token}`;
    else delete api.defaults.headers.common.Authorization;
  }, [token]);

  const [tab, setTab] = useState("users"); // users | logs | favs

  /** ================= Users ================= */
  const [users, setUsers] = useState([]);
  const [uErr, setUErr] = useState("");
  const [usersLoading, setUsersLoading] = useState(false);

  const loadUsers = async () => {
    setUErr(""); setUsersLoading(true);
    try {
      const { data } = await api.get("/admin/users");
      setUsers(data || []);
    } catch {
      setUErr("Ne mogu da učitam korisnike.");
    } finally { setUsersLoading(false); }
  };

  const deleteUser = async (id) => {
    if (!window.confirm("Obrisati korisnika?")) return;
    setUErr("");
    try {
      await api.delete(`/admin/users/${id}`);
      await loadUsers();
      if (String(favUserId) === String(id)) { setFavUserId(""); setFavRows([]); }
    } catch {
      setUErr("Brisanje korisnika nije uspelo.");
    }
  };

  const usersById = useMemo(() => {
    const m = {};
    users.forEach(u => { m[u.id] = u; });
    return m;
  }, [users]);

  /** ================= Logs ================= */
  const [logs, setLogs] = useState([]);
  const [lErr, setLErr] = useState("");
  const [status, setStatus] = useState("");
  const [region, setRegion] = useState("");
  const [from, setFrom] = useState(""); // YYYY-MM-DDTHH:mm
  const [to, setTo] = useState("");

  const logParams = useMemo(() => {
    const p = {};
    if (status) p.status = Number(status);
    if (region) p.region = region;
    if (from)   p.from   = `${from}:00`;
    if (to)     p.to     = `${to}:00`;
    return p;
  }, [status, region, from, to]);

  const loadLogs = async () => {
    setLErr("");
    try {
      const { data } = await api.get("/admin/logs", { params: logParams });
      setLogs(data || []);
    } catch {
      setLErr("Ne mogu da učitam logove.");
    }
  };

  const deleteLog = async (id) => {
    if (!window.confirm("Obrisati log zapis?")) return;
    setLErr("");
    try {
      await api.delete(`/admin/logs/${id}`);
      await loadLogs();
    } catch {
      setLErr("Brisanje loga nije uspelo.");
    }
  };

  const stats = useMemo(() => {
    const total = logs.length;
    const byStatus = {};
    const byRegion = {};
    logs.forEach(l => {
      const st = l.status ?? "NA";
      const rg = l.region ?? "NA";
      byStatus[st] = (byStatus[st] || 0) + 1;
      byRegion[rg] = (byRegion[rg] || 0) + 1;
    });
    return { total, byStatus, byRegion };
  }, [logs]);

  /** ================= Favorites (po korisniku) ================= */
  const [favUserId, setFavUserId] = useState("");
  const [favRows, setFavRows] = useState([]);
  const [favErr, setFavErr] = useState("");
  const [favBusy, setFavBusy] = useState(null);
  const [favLoading, setFavLoading] = useState(false);

  // keš za nazive summonera (ako FavoriteDto ne donese name/region)
  const [summMap, setSummMap] = useState({}); // { [summonerId]: {name, region} }

  const loadFavs = async (userIdArg) => {
    const uid = userIdArg ?? favUserId;
    if (!uid) { setFavRows([]); return; }
    setFavErr(""); setFavLoading(true);
    try {
      const { data } = await api.get(`/favorites/user/${uid}`);
      setFavRows(data || []);
    } catch {
      setFavErr("Ne mogu da učitam favorite za korisnika.");
    } finally { setFavLoading(false); }
  };

  const deleteFavorite = async (id) => {
    if (!window.confirm("Obrisati favorit?")) return;
    setFavBusy(id);
    try {
      await api.delete(`/favorites/${id}`);
      await loadFavs();
    } catch {
      setFavErr("Brisanje favorita nije uspelo.");
    } finally { setFavBusy(null); }
  };

  // kada dođu favoriti, dohvatiti *nedostajuće* summonere iz /summoners/{id}
  useEffect(() => {
    const missing = Array.from(
      new Set(
        favRows
          .filter(f => !f.summonerName && f.summonerId && !summMap[f.summonerId])
          .map(f => f.summonerId)
      )
    );
    if (!missing.length) return;

    (async () => {
      try {
        const results = await Promise.all(
          missing.map(id =>
            api.get(`/summoners/${id}`)
              .then(r => ({ id, name: r.data?.name, region: r.data?.region }))
              .catch(() => ({ id, name: null, region: null }))
          )
        );
        setSummMap(prev => {
          const next = { ...prev };
          results.forEach(({ id, name, region }) => { next[id] = { name, region }; });
          return next;
        });
      } catch {
        // tiho ignorisati – tabela već prikazuje fallback
      }
    })();
  }, [favRows]); // eslint-disable-line

  useEffect(() => {
    if (role === "ADMIN") {
      loadUsers();
      loadLogs();
    }
  }, [role]);

  useEffect(() => {
    if (favUserId) loadFavs(favUserId);
    else setFavRows([]);
  }, [favUserId]); // eslint-disable-line

  if (role !== "ADMIN") {
    return <div style={{maxWidth:900, margin:"24px auto"}}>Samo administratori imaju pristup ovoj stranici.</div>;
  }

  return (
    <div style={{maxWidth: 1100, margin:"24px auto", padding:"0 12px"}}>
      <h2>Admin</h2>

      {/* Tabs */}
      <div style={{display:"flex", gap:8, margin:"12px 0"}}>
        {["users","logs","favs"].map(t => (
          <button
            key={t}
            onClick={()=>setTab(t)}
            style={{
              padding:"6px 10px",
              border: "1px solid #ddd",
              background: tab===t ? "#eef2ff" : "white",
              fontWeight: tab===t ? 700 : 500
            }}
          >
            {t === "users" ? "Korisnici" : t === "logs" ? "API Logovi" : "Favoriti (po korisniku)"}
          </button>
        ))}
      </div>

      {tab === "users" && (
        <section>
          <h3>Korisnici</h3>
          {uErr && <div style={{color:"red", marginBottom:8}}>{uErr}</div>}
          <div style={{overflowX:"auto"}}>
            <table style={{width:"100%", borderCollapse:"collapse"}}>
              <thead>
                <tr style={{textAlign:"left", borderBottom:"1px solid #eee"}}>
                  <th style={{padding:"8px"}}>ID</th>
                  <th style={{padding:"8px"}}>Email</th>
                  <th style={{padding:"8px"}}>Username</th>
                  <th style={{padding:"8px"}}>Rola</th>
                  <th style={{padding:"8px"}}>Akcije</th>
                </tr>
              </thead>
              <tbody>
                {users.map(u => (
                  <tr key={u.id} style={{borderBottom:"1px solid #f3f3f3"}}>
                    <td style={{padding:"8px"}}>{u.id}</td>
                    <td style={{padding:"8px"}}>{u.email}</td>
                    <td style={{padding:"8px"}}>{u.username}</td>
                    <td style={{padding:"8px"}}>{u.role}</td>
                    <td style={{padding:"8px"}}>
                      <button onClick={()=>deleteUser(u.id)} style={{background:"#fee2e2"}}>
                        Obriši
                      </button>
                    </td>
                  </tr>
                ))}
                {!users.length && (
                  <tr><td colSpan={5} style={{padding:"8px"}}>{usersLoading ? "Učitavam…" : "Nema korisnika."}</td></tr>
                )}
              </tbody>
            </table>
          </div>
          <div style={{marginTop:8, display:"flex", gap:8}}>
            <button onClick={loadUsers}>Osveži</button>
          </div>
        </section>
      )}

      {tab === "logs" && (
        <section>
          <h3>API Logovi</h3>

          {/* mini stats */}
          <div style={{display:"flex", gap:12, flexWrap:"wrap", margin:"8px 0 14px"}}>
            <StatCard label="Logova (trenutni prikaz)" value={stats.total} />
            <StatCard label="200 OK" value={stats.byStatus?.[200] || 0} />
            <StatCard label="4xx" value={Object.entries(stats.byStatus||{}).filter(([k])=>String(k).startsWith("4")).reduce((a, [,v])=>a+v,0)} />
            <StatCard label="5xx" value={Object.entries(stats.byStatus||{}).filter(([k])=>String(k).startsWith("5")).reduce((a, [,v])=>a+v,0)} />
          </div>

          {/* filteri */}
          <div style={{display:"flex", gap:8, flexWrap:"wrap", alignItems:"center", marginBottom:10}}>
            <input
              placeholder="Status (npr. 200)"
              value={status}
              onChange={(e)=>setStatus(e.target.value)}
              style={{width:120, padding:6}}
            />
            <select value={region} onChange={(e)=>setRegion(e.target.value)} style={{padding:6}}>
              <option value="">Region (svi)</option>
              {REGIONS.map(r => <option key={r} value={r}>{r}</option>)}
            </select>
            <label>od:&nbsp;
              <input type="datetime-local" value={from} onChange={(e)=>setFrom(e.target.value)} />
            </label>
            <label>do:&nbsp;
              <input type="datetime-local" value={to} onChange={(e)=>setTo(e.target.value)} />
            </label>
            <button onClick={loadLogs}>Primeni</button>
            <button onClick={()=>{ setStatus(""); setRegion(""); setFrom(""); setTo(""); setLogs([]); }} style={{marginLeft:4}}>
              Reset
            </button>
          </div>

          {lErr && <div style={{color:"red", marginBottom:8}}>{lErr}</div>}

          <div style={{overflowX:"auto"}}>
            <table style={{width:"100%", borderCollapse:"collapse"}}>
              <thead>
                <tr style={{textAlign:"left", borderBottom:"1px solid #eee"}}>
                  <th style={{padding:"8px"}}>ID</th>
                  <th style={{padding:"8px"}}>Vreme</th>
                  <th style={{padding:"8px"}}>Endpoint</th>
                  <th style={{padding:"8px"}}>Status</th>
                  <th style={{padding:"8px"}}>Region</th>
                  <th style={{padding:"8px"}}>Actor</th>
                  <th style={{padding:"8px"}}>Akcije</th>
                </tr>
              </thead>
              <tbody>
                {logs.map(l => (
                  <tr key={l.id} style={{borderBottom:"1px solid #f3f3f3"}}>
                    <td style={{padding:"8px"}}>{l.id}</td>
                    <td style={{padding:"8px"}}>{formatDate(l.createdAt)}</td>
                    <td style={{padding:"8px", maxWidth:520, overflow:"hidden", textOverflow:"ellipsis", whiteSpace:"nowrap"}}>
                      {l.endpoint}
                    </td>
                    <td style={{padding:"8px"}}>{l.status}</td>
                    <td style={{padding:"8px"}}>{l.region || "-"}</td>
                    <td style={{padding:"8px"}}>{l.actorUserId ?? "-"}</td>
                    <td style={{padding:"8px"}}>
                      <button onClick={()=>deleteLog(l.id)} style={{background:"#fee2e2"}}>
                        Obriši
                      </button>
                    </td>
                  </tr>
                ))}
                {!logs.length && (
                  <tr><td colSpan={7} style={{padding:"8px"}}>Nema logova za prikaz.</td></tr>
                )}
              </tbody>
            </table>
          </div>

          <div style={{marginTop:8}}>
            <button onClick={loadLogs}>Osveži</button>
          </div>
        </section>
      )}

      {tab === "favs" && (
        <section>
          <h3>Favoriti (po korisniku)</h3>

          {/* Combobox korisnika */}
          <div style={{display:"flex", gap:8, alignItems:"center", marginBottom:8, flexWrap:"wrap"}}>
            <select
              value={favUserId}
              onChange={(e)=>setFavUserId(e.target.value)}
              style={{minWidth:320, padding:6}}
            >
              <option value="">{usersLoading ? "Učitavam korisnike…" : "— Izaberi korisnika —"}</option>
              {users.map(u => (
                <option key={u.id} value={u.id}>
                  {u.id} • {u.email} {u.username ? `(${u.username})` : ""} — {u.role}
                </option>
              ))}
            </select>
            <button onClick={()=>loadUsers()} style={{padding:"6px 10px"}}>Osveži listu korisnika</button>
            <button onClick={()=>loadFavs()} disabled={!favUserId} style={{padding:"6px 10px"}}>Učitaj favorite</button>
            <button onClick={()=>{ setFavUserId(""); setFavRows([]); }} style={{padding:"6px 10px"}}>Reset</button>
          </div>

          {favErr && <div style={{color:"red", marginBottom:8}}>{favErr}</div>}

          {/* Tabela favorita sa imenima korisnika i summoner-a */}
          <div style={{overflowX:"auto"}}>
            <table style={{width:"100%", borderCollapse:"collapse"}}>
              <thead>
                <tr style={{textAlign:"left", borderBottom:"1px solid #eee"}}>
                  <th style={{padding:"8px"}}>FavID</th>
                  <th style={{padding:"8px"}}>Korisnik</th>
                  <th style={{padding:"8px"}}>UserID</th>
                  <th style={{padding:"8px"}}>Summoner</th>
                  <th style={{padding:"8px"}}>SummonerID</th>
                  <th style={{padding:"8px"}}>Region</th>
                  <th style={{padding:"8px"}}>Napomena</th>
                  <th style={{padding:"8px"}}>Akcije</th>
                </tr>
              </thead>
              <tbody>
                {favLoading && (
                  <tr><td colSpan={8} style={{padding:"8px"}}>Učitavam…</td></tr>
                )}
                {!favLoading && favRows.map(f => {
                  const u = usersById[f.userId];
                  const userLabel = u
                    ? `${u.email}${u.username ? ` (${u.username})` : ""}`
                    : `user#${f.userId}`;

                  const sumFromCache = summMap[f.summonerId];
                  const summName = f.summonerName || sumFromCache?.name || "-";
                  const summRegion = f.region || sumFromCache?.region || "-";

                  return (
                    <tr key={f.id} style={{borderBottom:"1px solid #f3f3f3"}}>
                      <td style={{padding:"8px"}}>{f.id}</td>
                      <td style={{padding:"8px"}}>{userLabel}</td>
                      <td style={{padding:"8px"}}>{f.userId}</td>
                      <td style={{padding:"8px"}}>{summName}</td>
                      <td style={{padding:"8px"}}>{f.summonerId}</td>
                      <td style={{padding:"8px"}}>{summRegion}</td>
                      <td style={{padding:"8px"}}>{f.note || ""}</td>
                      <td style={{padding:"8px"}}>
                        <button
                          onClick={()=>deleteFavorite(f.id)}
                          disabled={favBusy===f.id}
                          style={{background:"#fee2e2"}}
                        >
                          {favBusy===f.id ? "Brišem…" : "Obriši"}
                        </button>
                      </td>
                    </tr>
                  );
                })}
                {!favLoading && !favRows.length && (
                  <tr><td colSpan={8} style={{padding:"8px"}}>{favUserId ? "Nema favorita za korisnika." : "Izaberi korisnika."}</td></tr>
                )}
              </tbody>
            </table>
          </div>
        </section>
      )}
    </div>
  );
}

function formatDate(isoLike) {
  if (!isoLike) return "-";
  const d = new Date(isoLike);
  return d.toLocaleString();
}
