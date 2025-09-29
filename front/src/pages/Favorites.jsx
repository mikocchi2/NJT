// src/pages/Summoners.jsx
import { useEffect, useMemo, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import api from "../api";

function getAuth() {
  const pick = (k) => sessionStorage.getItem(k) ?? localStorage.getItem(k);
  try {
    const rawUser = pick("user");
    const rawMe   = pick("me");
    const tokenOnly = pick("token");
    const u = rawUser ? JSON.parse(rawUser) : rawMe ? JSON.parse(rawMe) : {};
    return {
      token:  u.token || tokenOnly || u?.user?.token || null,
      userId: u.userId ?? u.id ?? u?.user?.id ?? null,
      email:  u.email  ?? u?.user?.email ?? null,
    };
  } catch {
    return { token:null, userId:null, email:null };
  }
}

export default function Summoners() {
  const nav = useNavigate();
  const [{ userId, token }] = useState(getAuth());
  const [rows, setRows] = useState([]);
  const [q, setQ] = useState("");
  const [sortBy, setSortBy] = useState("");
  const [err, setErr] = useState("");
  const [busy, setBusy] = useState(null);

  const [favIds, setFavIds] = useState(new Set());
  const [favMap, setFavMap] = useState({});

  useEffect(() => {
    if (token) api.defaults.headers.common.Authorization = `Bearer ${token}`;
    else delete api.defaults.headers.common.Authorization;
  }, [token]);

  const load = async () => {
    setErr("");
    try {
      const [summRes, favRes] = await Promise.all([
        api.get("/summoners"),
        userId ? api.get(`/favorites/user/${userId}`) : Promise.resolve({ data: [] }),
      ]);
      setRows(summRes.data || []);

      const favs = favRes.data || [];
      setFavIds(new Set(favs.map(f => f.summonerId)));
      const map = {};
      favs.forEach(f => { map[f.summonerId] = f.id; });
      setFavMap(map);
    } catch {
      setErr("Ne mogu da učitam podatke.");
    }
  };
  useEffect(() => { load(); }, [userId]);

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

  const addToFav = async (summonerId) => {
    if (!userId) { setErr("Moraš biti ulogovan da bi dodao u favorite."); return; }
    if (favIds.has(summonerId)) return;
    setBusy(summonerId);
    try {
      await api.post("/favorites", { userId, summonerId, note: "" });
      await load();
    } catch (ex) {
      const msg = ex?.response?.data?.message || "Dodavanje u favorite nije uspelo.";
      setErr(msg);
    } finally { setBusy(null); }
  };

  const removeFromFav = async (summonerId) => {
    const favoriteId = favMap[summonerId];
    if (!favoriteId) return;
    setBusy(summonerId);
    try {
      await api.delete(`/favorites/${favoriteId}`);
      await load();
    } catch {
      setErr("Brisanje iz favorita nije uspelo.");
    } finally { setBusy(null); }
  };

  const openProfile = (id) => nav(`/profile/${id}`);

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
        {data.map(p => {
          const inFav = favIds.has(p.id);
          return (
            <div
              key={p.id}
              onClick={() => openProfile(p.id)}
              style={{
                border:"1px solid #eee",
                borderRadius:8,
                padding:12,
                display:"flex",
                gap:12,
                alignItems:"center",
                cursor:"pointer"
              }}
              role="button"
              tabIndex={0}
              onKeyDown={(e)=>{ if (e.key === 'Enter') openProfile(p.id); }}
            >
              <div style={{flex:1}}>
                {/* ime je i Link, radi11 i bez njega jer je cela kartica klikabilna */}
                <div style={{fontWeight:700, fontSize:16}}>
                  <Link
                    to={`/profile/${p.id}`}
                    onClick={(e)=>e.stopPropagation()}
                    style={{textDecoration:"none", color:"inherit"}}
                  >
                    {p.name || "-"}
                  </Link>
                </div>
                <div style={{fontSize:13, color:"#555"}}>
                  lvl {p.level ?? "-"} · {p.region} · {p.rankTier ?? "-"} {p.rankDivision ?? ""} {p.leaguePoints!=null?`(${p.leaguePoints} LP)`:""}
                </div>
                <div style={{fontSize:12, color:"#888"}}>Last sync: {p.lastSyncedAt || "-"}</div>
              </div>

              {inFav ? (
                <button
                  onClick={(e)=>{ e.stopPropagation(); removeFromFav(p.id); }}
                  disabled={busy===p.id}
                  style={{background:"#fee2e2"}}
                  title="Ukloni iz favorita"
                >
                  {busy===p.id ? "Uklanjam…" : "Ukloni iz favorita"}
                </button>
              ) : (
                <button
                  onClick={(e)=>{ e.stopPropagation(); addToFav(p.id); }}
                  disabled={busy===p.id || !userId}
                  title={!userId ? "Uloguj se da dodaš u favorite" : ""}
                >
                  {busy===p.id ? "Dodajem…" : "Dodaj u favorite"}
                </button>
              )}
            </div>
          );
        })}
        {!data.length && <div>Nema zapisa u bazi. Uradi sync na “Search” i vrati se ovde.</div>}
      </div>
    </div>
  );
}
