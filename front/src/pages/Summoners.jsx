// src/pages/Summoners.jsx
import { useEffect, useMemo, useState } from "react";
import api from "../api";

function getSessionUser() {
  const pick = (k) => sessionStorage.getItem(k) ?? localStorage.getItem(k);
  try {
    const rawUser = pick("user");
    const rawMe   = pick("me");
    const tokenOnly = pick("token");
    const u = rawUser ? JSON.parse(rawUser) : rawMe ? JSON.parse(rawMe) : {};
    const token  = u.token || tokenOnly || u?.user?.token || null;
    const userId = u.userId ?? u.id ?? u?.user?.id ?? null;
    const email  = u.email  ?? u?.user?.email ?? null;
    return { userId, email, token };
  } catch { return { userId:null, email:null, token:null }; }
}

export default function Summoners() {
  const [{ userId, token }, setAuth] = useState(getSessionUser());
  const [rows, setRows] = useState([]);
  const [q, setQ] = useState("");
  const [sortBy, setSortBy] = useState("");
  const [err, setErr] = useState("");
  const [busyId, setBusyId] = useState(null);

  // favorites state: mapiranje summonerId -> favoriteId
  const [favMap, setFavMap] = useState(new Map());

  // Init auth header
  useEffect(() => {
    const a = getSessionUser();
    setAuth(a);
    if (a.token) api.defaults.headers.common.Authorization = `Bearer ${a.token}`;
    else delete api.defaults.headers.common.Authorization;
  }, []);

  // Učitaj sve summoner-e (naše zapise)
  const loadSummoners = async () => {
    setErr("");
    try {
      const { data } = await api.get("/summoners");
      setRows(data || []);
    } catch {
      setErr("Ne mogu da učitam summoner-e iz baze.");
    }
  };

  // Učitaj moje favorite => popuni favMap
  const loadMyFavorites = async (uid) => {
    if (!uid) { setFavMap(new Map()); return; }
    try {
      const { data } = await api.get(`/favorites/user/${uid}`);
      // očekujemo niz { id: favoriteId, summonerId: number, ... }
      const m = new Map();
      (data || []).forEach(f => { if (f.summonerId != null) m.set(Number(f.summonerId), f.id); });
      setFavMap(m);
    } catch {
      // tiho fallback – ne blokira listu
      setFavMap(new Map());
    }
  };

  useEffect(() => { loadSummoners(); }, []);
  useEffect(() => { loadMyFavorites(userId); }, [userId]);

  // Lokalna pretraga/sort
  const data = useMemo(() => {
    let out = [...rows];
    if (q.trim()) {
      const s = q.trim().toLowerCase();
      out = out.filter(r =>
        (r.name || "").toLowerCase().includes(s) ||
        (r.region || "").toLowerCase().includes(s)
      );
    }
    if (sortBy === "rank") {
      out.sort((a,b) =>
        ((a.rankTier||"")+(a.rankDivision||"")).localeCompare((b.rankTier||"")+(b.rankDivision||"")) ||
        (b.leaguePoints||0) - (a.leaguePoints||0)
      );
    } else if (sortBy === "level") {
      out.sort((a,b) => (b.level||0) - (a.level||0));
    } else if (sortBy === "lastSyncedAt") {
      out.sort((a,b) => new Date(b.lastSyncedAt||0) - new Date(a.lastSyncedAt||0));
    }
    return out;
  }, [rows, q, sortBy]);

  // Dodaj
  const addToFav = async (summonerId) => {
    if (!userId) { setErr("Moraš biti ulogovan da bi dodao u favorite."); return; }
    setErr(""); setBusyId(summonerId);
    try {
      const { data } = await api.post("/favorites", { userId, summonerId, note: "" });
      // data.id = favoriteId (pretpostavka po tvom BE)
      const favoriteId = data?.id;
      setFavMap(prev => new Map(prev).set(Number(summonerId), favoriteId));
    } catch (ex) {
      const msg = ex?.response?.data?.message || "Dodavanje u favorite nije uspelo.";
      setErr(msg);
    } finally { setBusyId(null); }
  };

  // Ukloni
  const removeFromFav = async (summonerId) => {
    const favoriteId = favMap.get(Number(summonerId));
    if (!favoriteId) return;
    setErr(""); setBusyId(summonerId);
    try {
      await api.delete(`/favorites/${favoriteId}`);
      setFavMap(prev => { const n = new Map(prev); n.delete(Number(summonerId)); return n; });
    } catch (ex) {
      const msg = ex?.response?.data?.message || "Brisanje iz favorita nije uspelo.";
      setErr(msg);
    } finally { setBusyId(null); }
  };

  // helper za dugme
  const isFav = (summonerId) => favMap.has(Number(summonerId));

  return (
    <div style={{maxWidth: 960, margin:"24px auto"}}>
      <h2>Svi summoner-i (iz baze)</h2>

      {!userId && (
        <div style={{background:"#fff3cd", border:"1px solid #ffeeba", padding:8, borderRadius:6, marginBottom:8, fontSize:14}}>
          Nisi ulogovan — dodavanje u favorite je onemogućeno.
        </div>
      )}

      <div style={{display:"flex", gap:8, marginBottom:12}}>
        <input
          placeholder="Pretraga po imenu ili regionu…"
          value={q}
          onChange={(e)=>setQ(e.target.value)}
          style={{flex:1, padding:8}}
        />
        <select value={sortBy} onChange={e=>setSortBy(e.target.value)}>
          <option value="">Bez sortiranja</option>
          <option value="rank">Rank</option>
          <option value="level">Level</option>
          <option value="lastSyncedAt">Last synced</option>
        </select>
      </div>

      {err && <div style={{color:"red", marginBottom:8}}>{err}</div>}

      <div style={{display:"grid", gap:8}}>
        {data.map(p => (
          <div key={p.id} style={{border:"1px solid #eee", borderRadius:8, padding:12, display:"flex", gap:12, alignItems:"center"}}>
            <div style={{flex:1}}>
              <div style={{fontWeight:700}}>{p.name || "-"}</div>
              <div style={{fontSize:13, color:"#555"}}>
                lvl {p.level ?? "-"} · {p.region} · {p.rankTier ?? "-"} {p.rankDivision ?? ""} {p.leaguePoints!=null?`(${p.leaguePoints} LP)`:""}
              </div>
              <div style={{fontSize:12, color:"#888"}}>Last sync: {p.lastSyncedAt || "-"}</div>
            </div>

            {isFav(p.id) ? (
              <button
                disabled={busyId===p.id}
                onClick={()=>removeFromFav(p.id)}
                style={{background:"#fee2e2", border:"1px solid #fecaca"}}
                title="Ukloni iz favorita"
              >
                {busyId===p.id ? "Uklanjam…" : "Ukloni iz favorita"}
              </button>
            ) : (
              <button
                disabled={busyId===p.id || !userId}
                onClick={()=>addToFav(p.id)}
                title={!userId ? "Uloguj se da dodaš u favorite" : ""}
              >
                {busyId===p.id ? "Dodajem…" : "Dodaj u favorite"}
              </button>
            )}
          </div>
        ))}
        {!data.length && <div>Nema zapisa u bazi. Uradi sync na “Search” pa se vrati ovde.</div>}
      </div>
    </div>
  );
}
