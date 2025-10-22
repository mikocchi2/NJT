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
    console.log("\n\n[Profile] loadProfile() start for", id, "\n\n");
    try {
      const { data } = await api.get(`/summoners/${id}`);
      console.log("\n\n[Profile] loadProfile() fetched:", JSON.stringify(data, null, 2), "\n\n");
      setProfile(data);
    } catch {
      console.log("\n\n[Profile] loadProfile() failed for", id, "\n\n");
      setErr("Ne mogu da učitam profil.");
    }
  }, [id]);

  const loadMatches = useCallback(async () => {
    console.log("\n\n[Profile] loadMatches() start for", id, "filters:", JSON.stringify(filters), "\n\n");
    setLoading(true); setErr(""); setMatchesLoaded(false);
    try {
      const params = {};
      if (filters.gameType) params.gameType = filters.gameType;
      if (filters.win !== "") params.win = filters.win === "true";
      if (filters.minKills) params.minKills = Number(filters.minKills);
      console.log("\n\n[Profile] loadMatches() query params:", JSON.stringify(params), "\n\n");
      const { data } = await api.get(`/matches/by-summoner/${id}`, { params });
      console.log("\n\n[Profile] loadMatches() fetched", Array.isArray(data) ? data.length : "?", "matches", "\n", JSON.stringify(data, null, 2), "\n\n");
      setMatches(data || []);
      setMatchesLoaded(true);
    } catch {
      console.log("\n\n[Profile] loadMatches() failed for", id, "\n\n");
      setErr("Ne mogu da učitam mečeve.");
    } finally { setLoading(false); }
  }, [filters.gameType, filters.minKills, filters.win, id]);

  // ručni "refresh" – pokuša sync opet (povući će lastN=10)
  const refresh = useCallback(async () => {
    if (!profile) return;
    console.log("\n\n[Profile] refresh() start for", id, "profile:", JSON.stringify(profile), "\n\n");
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
      console.log("\n\n[Profile] refresh() params:", JSON.stringify(params), "\n\n");
      await api.post(`/summoners/sync`, null, { params });
      console.log("\n\n[Profile] refresh() sync request done\n\n");
      await Promise.all([loadProfile(), loadMatches()]);
      console.log("\n\n[Profile] refresh() loadProfile/loadMatches finished\n\n");
    } catch (ex) {
      console.log("\n\n[Profile] refresh() failed:", ex);
      setErr(
        ex?.response?.data?.message ||
        ex?.response?.data?.error ||
        ex?.message || "Sync nije uspeo."
      );
    } finally {
      setLoading(false);
    }
  }, [profile, loadMatches, loadProfile]);

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

  if (!profile)
    return (
      <div style={{ maxWidth: 920, margin: "24px auto", padding: "0 12px" }}>
        Učitavanje profila...
      </div>
    );

  const iconId = profile.iconId ?? profile.profileIconId;
  const regionLabel =
    typeof profile.region === "object" && profile.region?.name
      ? profile.region.name
      : profile.region ?? "-";
  const lastSync = profile.lastSyncedAt || profile.fetchedAt || null;

  return (
    <div style={{ maxWidth: 920, margin: "24px auto", padding: "0 12px" }}>
      <h2 style={{ marginBottom: 12 }}>Profil Summonera</h2>

      <div
        style={{
          padding: 12,
          border: "1px solid #eee",
          borderRadius: 8,
          display: "flex",
          gap: 12,
          alignItems: "flex-start",
        }}
      >
        {iconId ? (
          <img
            alt="icon"
            src={ddProfileIcon(iconId)}
            style={{ width: 64, height: 64, borderRadius: 8, background: "#f5f5f5" }}
            onError={(e) => {
              e.target.style.display = "none";
            }}
          />
        ) : (
          <div
            style={{
              width: 64,
              height: 64,
              borderRadius: 8,
              background: "#f5f5f5",
            }}
          />
        )}
        <div style={{ flex: 1 }}>
          <h3 style={{ margin: "0 0 4px" }}>
            {profile.name} — lvl {profile.level ?? "-"}
          </h3>
          <div>
            Rang: {profile.rankTier || "-"} {profile.rankDivision || ""}
            {profile.leaguePoints != null ? ` (${profile.leaguePoints} LP)` : ""}
          </div>
          <div>Region: {regionLabel}</div>
          <div>Riot ID: {profile.riotId ?? profile.name ?? "-"}</div>
          <div>Last sync: {lastSync ? formatDate(lastSync) : "-"}</div>
        </div>
        <button
          onClick={refresh}
          disabled={loading}
          style={{ padding: "8px 12px", cursor: loading ? "wait" : "pointer" }}
        >
          {loading ? "..." : "Osveži podatke"}
        </button>
      </div>

      <h3 style={{ marginTop: 24 }}>Mečevi</h3>
      <div
        style={{
          marginTop: 12,
          padding: 12,
          border: "1px solid #eee",
          borderRadius: 8,
          display: "flex",
          gap: 8,
          alignItems: "center",
          flexWrap: "wrap",
        }}
      >
        <label>
          Tip meča:&nbsp;
          <select
            value={filters.gameType}
            onChange={(e) => setFilters((f) => ({ ...f, gameType: e.target.value }))}
          >
            <option value="">Svi tipovi</option>
            <option value="RANKED">Ranked</option>
            <option value="ARAM">ARAM</option>
            <option value="NORMAL">Normal</option>
          </select>
        </label>
        <label>
          Rezultat:&nbsp;
          <select
            value={filters.win}
            onChange={(e) => setFilters((f) => ({ ...f, win: e.target.value }))}
          >
            <option value="">Svi rezultati</option>
            <option value="true">Pobeda</option>
            <option value="false">Poraz</option>
          </select>
        </label>
        <label>
          Min. ubistava:&nbsp;
          <input
            type="number"
            min="0"
            value={filters.minKills}
            onChange={(e) => setFilters((f) => ({ ...f, minKills: e.target.value }))}
            style={{ width: 80 }}
          />
        </label>
        <div style={{ marginLeft: "auto", fontSize: 12, color: "#666" }}>
          Prikazano: <b>{matches.length}</b> mečeva
        </div>
      </div>

      {err && <div style={{ color: "red", marginTop: 12 }}>{err}</div>}

      {loading && !matchesLoaded ? (
        <div style={{ marginTop: 12 }}>Učitavam...</div>
      ) : (
        <div style={{ marginTop: 12, display: "grid", gap: 8 }}>
          {matches.map((m, idx) => {
            const kills = Number(m.kills ?? 0);
            const deaths = Number(m.deaths ?? 0);
            const assists = Number(m.assists ?? 0);
            const win = normalizeWin(m.win);
            const kda = calcKDA(kills, deaths, assists);
            const queueLabel =
              m.queueId != null
                ? queueName(m.queueId)
                : m.gameType || m.queueType || "Other";
            const duration =
              m.durationSec != null
                ? formatDuration(m.durationSec)
                : m.gameDuration != null
                ? formatDuration(m.gameDuration)
                : "-";
            const playedAt = m.gameCreation || m.gameStartTime || m.playedAt || null;

            const key = m.matchId || m.id || m.gameId || idx;

            return (
              <div
                key={key}
                style={{
                  border: "1px solid #eee",
                  borderLeft: `6px solid ${win ? "#22c55e" : "#ef4444"}`,
                  borderRadius: 8,
                  padding: 12,
                  display: "grid",
                  gridTemplateColumns: "120px 1fr 220px",
                  gap: 8,
                  alignItems: "center",
                }}
              >
                <div style={{ fontWeight: 600 }}>
                  {m.championName || m.champion || "-"}
                  <div style={{ fontSize: 12, color: "#666" }}>{queueLabel}</div>
                </div>

                <div style={{ fontSize: 14 }}>
                  K/D/A: <b>{kills}/{deaths}/{assists}</b> &nbsp;|&nbsp; KDA: <b>{kda.toFixed(2)}</b>
                  <div style={{ fontSize: 12, color: "#666" }}>
                    {playedAt ? formatDate(playedAt) : "-"} • {duration}
                  </div>
                </div>

                <div style={{ textAlign: "right" }}>
                  <span
                    style={{
                      padding: "4px 8px",
                      borderRadius: 6,
                      background: win ? "#dcfce7" : "#fee2e2",
                      color: win ? "#166534" : "#991b1b",
                      fontWeight: 600,
                    }}
                  >
                    {win ? "Pobeda" : "Poraz"}
                  </span>
                </div>
              </div>
            );
          })}
          {matchesLoaded && !matches.length && (
            <div style={{ border: "1px solid #eee", borderRadius: 8, padding: 16 }}>
              Nema mečeva za ovaj nalog (pokušaj Osveži ili drugačije filtere).
            </div>
          )}
        </div>
      )}
    </div>
  );
}

function normalizeWin(win) {
  if (typeof win === "string") {
    const normalized = win.trim().toLowerCase();
    return normalized === "win" || normalized === "pobeda" || normalized === "true";
  }
  return Boolean(win);
}

function formatDuration(totalSeconds) {
  const numeric = Number(totalSeconds);
  if (!Number.isFinite(numeric)) return "-";
  const sec = Math.max(0, Math.floor(numeric));
  const minutes = Math.floor(sec / 60);
  const seconds = sec % 60;
  return `${minutes}m ${seconds}s`;
}

function formatDate(iso) {
  try {
    const date = new Date(iso);
    if (Number.isNaN(date.getTime())) return "-";
    return date.toLocaleString();
  } catch {
    return "-";
  }
}

function calcKDA(k, d, a) {
  return (k + a) / Math.max(1, d);
}

function queueName(queueId) {
  if (queueId === 420) return "Ranked Solo";
  if (queueId === 440) return "Ranked Flex";
  if (queueId === 450) return "ARAM";
  if (queueId === 400 || queueId === 430) return "Normal";
  return "Other";
}

function ddProfileIcon(iconId) {
  const ver = "14.1.1";
  return `https://ddragon.leagueoflegends.com/cdn/${ver}/img/profileicon/${iconId}.png`;
}
