// src/pages/Profile.jsx
import { useCallback, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import api from "../api";

export default function Profile() {
  const { id } = useParams();
  const [profile, setProfile] = useState(null);
  const [matches, setMatches] = useState([]);
  const [filters, setFilters] = useState({ gameType:"", win:"", minKills:"" });
  const [loading, setLoading] = useState(false);
  const [matchesLoaded, setMatchesLoaded] = useState(false);
  const [autoRefreshed, setAutoRefreshed] = useState(false);
  const [err, setErr] = useState("");

  const loadProfile = useCallback(async () => {
    try {
      const { data } = await api.get(`/summoners/${id}`);
      setProfile(data);
    } catch {
      setErr("Ne mogu da učitam profil.");
    }
  }, [id]);

  const loadMatches = useCallback(async () => {
    setLoading(true); setErr(""); setMatchesLoaded(false);
    try {
      const params = {};
      if (filters.gameType) params.gameType = filters.gameType;
      if (filters.win !== "") params.win = filters.win === "true";
      if (filters.minKills) params.minKills = Number(filters.minKills);
      const { data } = await api.get(`/matches/by-summoner/${id}`, { params });
      setMatches(data || []);
      setMatchesLoaded(true);
    } catch {
      setErr("Ne mogu da učitam mečeve.");
    } finally { setLoading(false); }
  }, [filters.gameType, filters.minKills, filters.win, id]);

  // ručni "refresh" – pokuša sync opet (povući će lastN=10)
  const refresh = useCallback(async () => {
    if (!profile) return;
    try {
      setLoading(true); setErr("");
      // backend dozvoljava name ILI riotId parametar; imamo bar display name
      const params = { region: profile.region, lastN: 10 };
      if (profile.puuid) {
        params.puuid = profile.puuid;
      }
      if (profile.name) {
        params.riotId = profile.name;
        if (!profile.puuid) {
          params.name = profile.name;
        }
      }
      await api.post(`/summoners/sync`, null, { params });
      await loadMatches();
    } catch (ex) {
      setErr(
        ex?.response?.data?.message ||
        ex?.response?.data?.error ||
        ex?.message || "Sync nije uspeo."
      );
    } finally {
      setLoading(false);
    }
  }, [profile, loadMatches]);

  useEffect(() => { loadProfile(); }, [loadProfile]);
  useEffect(() => {
    setAutoRefreshed(false);
    setMatches([]);
    setMatchesLoaded(false);
  }, [id]);
  useEffect(() => { loadMatches(); }, [loadMatches]);
  useEffect(() => {
    if (profile && matchesLoaded && matches.length === 0 && !autoRefreshed && !loading) {
      setAutoRefreshed(true);
      refresh();
    }
  }, [profile, matchesLoaded, matches.length, autoRefreshed, refresh, loading]);

  if (!profile) return <div style={{padding:24}}>Učitavanje profila...</div>;

  return (
    <div style={{maxWidth:960, margin:"24px auto"}}>
      <h2>{profile.name} — lvl {profile.level ?? "-"}</h2>
      <div>Rang: {profile.rankTier || "-"} {profile.rankDivision || ""} {profile.leaguePoints != null ? `(${profile.leaguePoints} LP)` : ""}</div>
      <div>Region: {profile.region}</div>
      <div style={{marginTop:10, fontSize:12}}>Last sync: {profile.lastSyncedAt || "-"}</div>

      <div style={{marginTop:10}}>
        <button onClick={refresh} disabled={loading}>Osveži podatke</button>
      </div>

      <h3 style={{marginTop:24}}>Mečevi</h3>
      <div style={{display:"flex", gap:8, marginBottom:10}}>
        <select value={filters.gameType} onChange={e=>setFilters(f=>({...f, gameType:e.target.value}))}>
          <option value="">Svi tipovi</option>
          <option value="Ranked">Ranked</option>
          <option value="ARAM">ARAM</option>
          <option value="Normal">Normal</option>
        </select>
        <select value={filters.win} onChange={e=>setFilters(f=>({...f, win:e.target.value}))}>
          <option value="">Svi rezultati</option>
          <option value="true">Pobeda</option>
          <option value="false">Poraz</option>
        </select>
        <input type="number" min="0" placeholder="Min kills"
               value={filters.minKills}
               onChange={e=>setFilters(f=>({...f, minKills:e.target.value}))}/>
      </div>

      {err && <div style={{color:"red"}}>{err}</div>}
      {loading ? <div>Učitavam...</div> : (
        <div style={{display:"grid", gap:8}}>
          {matches.map(m => (
            <div key={m.matchId} style={{border:"1px solid #eee", padding:10}}>
              <div><b>{m.champion || "-"}</b> — {m.kills ?? 0}/{m.deaths ?? 0}/{m.assists ?? 0} KDA</div>
              <div>{m.gameType || "-"} · trajanje: {m.durationSec != null ? `${m.durationSec}s` : "-"} · {m.win ? "WIN" : "LOSS"}</div>
              <div style={{fontSize:12, color:"#666"}}>matchId: {m.matchId}</div>
            </div>
          ))}
          {matchesLoaded && !matches.length && <div>Nema mečeva za ovaj nalog (pokušaj Osveži ili drugo ime).</div>}
        </div>
      )}
    </div>
  );
}
