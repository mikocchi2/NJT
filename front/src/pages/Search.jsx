// src/pages/Search.jsx
import { useMemo, useState } from "react";
import api from "../api";

export default function Search() {
  const [riotId, setRiotId] = useState("");            // npr. "Caps#000"
  const [region, setRegion] = useState("EUNE");        // EUNE, EUW, NA1, KR...
  const [loading, setLoading] = useState(false);
  const [profile, setProfile] = useState(null);
  const [err, setErr] = useState("");

  // FILTERI
  const [queueFilter, setQueueFilter] = useState("ALL");    // ALL|RANKED_SOLO|RANKED_FLEX|ARAM|NORMALS
  const [resultFilter, setResultFilter] = useState("ALL");  // ALL|WIN|LOSS
  const [minKills, setMinKills] = useState(0);

  const isRiotIdValid = (s) => /^.+#.+$/.test(s.trim());

  const submit = async (e) => {
    e.preventDefault();
    setErr("");
    setLoading(true);
    setProfile(null);
    try {
      const { data } = await api.get("/search", {
        params: { riotId: riotId.trim(), region, count: 10 },
      });
      setProfile(data);
    } catch (ex) {
      const msg =
        ex?.response?.data?.message ||
        ex?.response?.data?.error ||
        ex?.message ||
        "Sync nije uspeo.";
      setErr(msg);
    } finally {
      setLoading(false);
    }
  };

  const filteredMatches = useMemo(() => {
    if (!profile) return [];
    let ms = [...(profile.matches || [])];

    if (queueFilter !== "ALL") {
      ms = ms.filter((m) => {
        const q = queueName(m.queueId);
        if (queueFilter === "RANKED_SOLO") return q === "Ranked Solo";
        if (queueFilter === "RANKED_FLEX") return q === "Ranked Flex";
        if (queueFilter === "ARAM") return q === "ARAM";
        if (queueFilter === "NORMALS") return q === "Normal";
        return true;
      });
    }

    if (resultFilter !== "ALL") {
      ms = ms.filter((m) => {
        const win = Boolean(m.win);
        return resultFilter === "WIN" ? win : !win;
      });
    }

    if (minKills > 0) {
      ms = ms.filter((m) => m.kills >= minKills);
    }

    return ms;
  }, [profile, queueFilter, resultFilter, minKills]);

  return (
    <div style={{ maxWidth: 920, margin: "24px auto", padding: "0 12px" }}>
      <h2>Pretraga Summonera</h2>

      <form
        onSubmit={submit}
        style={{ display: "flex", gap: 8, alignItems: "center", flexWrap: "wrap" }}
      >
        <input
          placeholder="Riot ID (npr. Caps#000)"
          value={riotId}
          onChange={(e) => setRiotId(e.target.value)}
          style={{ flex: 1, minWidth: 260, padding: 8 }}
        />
        <select value={region} onChange={(e) => setRegion(e.target.value)} style={{ padding: 8 }}>
          <option value="EUNE">EUNE</option>
          <option value="EUW">EUW</option>
          <option value="NA1">NA1</option>
          <option value="KR">KR</option>
        </select>
        <button disabled={loading || !isRiotIdValid(riotId)} style={{ padding: "8px 12px" }}>
          {loading ? "..." : "Sync"}
        </button>
      </form>

      <div style={{ fontSize: 12, color: "#666", marginTop: 6 }}>
        Unesi <b>Riot ID</b> (format <code>gameName#tagLine</code>, npr. <code>Caps#000</code>).
      </div>

      {err && <div style={{ color: "red", marginTop: 10 }}>{err}</div>}

      {profile && (
        <>
          {/* Profil kartica */}
          <div
            style={{
              marginTop: 16,
              padding: 12,
              border: "1px solid #eee",
              borderRadius: 8,
              display: "flex",
              gap: 12,
            }}
          >
            <img
              alt="icon"
              src={profile.iconId != null ? ddProfileIcon(profile.iconId) : undefined}
              style={{ width: 64, height: 64, borderRadius: 8, background: "#f5f5f5" }}
              onError={(e) => {
                e.target.style.display = "none";
              }}
            />
            <div style={{ flex: 1 }}>
              <h3 style={{ margin: "0 0 4px" }}>
                {profile.name} — lvl {profile.level}
              </h3>
              <div>
                Rang: {profile.rankTier ?? "-"} {profile.rankDivision ?? ""}{" "}
                {profile.leaguePoints != null ? `(${profile.leaguePoints} LP)` : ""}
              </div>
              <div>Region: {profile.region}</div>
              <div>Riot ID: {profile.riotId ?? "-"}</div>
              <div>Fetched: {profile.fetchedAt ? formatDate(profile.fetchedAt) : "-"}</div>
            </div>
          </div>

          {/* Filter traka */}
          <div
            style={{
              marginTop: 16,
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
              <select value={queueFilter} onChange={(e) => setQueueFilter(e.target.value)}>
                <option value="ALL">Svi</option>
                <option value="RANKED_SOLO">Ranked Solo</option>
                <option value="RANKED_FLEX">Ranked Flex</option>
                <option value="ARAM">ARAM</option>
                <option value="NORMALS">Normal</option>
              </select>
            </label>
            <label>
              Rezultat:&nbsp;
              <select value={resultFilter} onChange={(e) => setResultFilter(e.target.value)}>
                <option value="ALL">Svi</option>
                <option value="WIN">Pobeda</option>
                <option value="LOSS">Poraz</option>
              </select>
            </label>
            <label>
              Min. ubistava:&nbsp;
              <input
                type="number"
                min={0}
                value={minKills}
                onChange={(e) => setMinKills(parseInt(e.target.value || "0", 10))}
                style={{ width: 80 }}
              />
            </label>
            <div style={{ marginLeft: "auto", fontSize: 12, color: "#666" }}>
              Prikazano: <b>{filteredMatches.length}</b> mečeva
            </div>
          </div>

          {/* Lista mečeva */}
          <div style={{ marginTop: 12, display: "grid", gap: 8 }}>
            {filteredMatches.map((m) => {
              const kda = calcKDA(m.kills, m.deaths, m.assists);
              const qName = queueName(m.queueId);
              return (
                <div
                  key={m.id}
                  style={{
                    border: "1px solid #eee",
                    borderLeft: `6px solid ${m.win ? "#22c55e" : "#ef4444"}`,
                    borderRadius: 8,
                    padding: 12,
                    display: "grid",
                    gridTemplateColumns: "120px 1fr 220px",
                    gap: 8,
                    alignItems: "center",
                  }}
                >
                  <div style={{ fontWeight: 600 }}>
                    {m.championName}
                    <div style={{ fontSize: 12, color: "#666" }}>{qName}</div>
                  </div>

                  <div style={{ fontSize: 14 }}>
                    K/D/A: <b>{m.kills}/{m.deaths}/{m.assists}</b> &nbsp;|&nbsp; KDA: <b>{kda.toFixed(2)}</b>
                    <div style={{ fontSize: 12, color: "#666" }}>
                      {formatDate(m.gameCreation)} • {formatDuration(m.gameDuration)}
                    </div>
                  </div>

                  <div style={{ textAlign: "right" }}>
                    <span
                      style={{
                        padding: "4px 8px",
                        borderRadius: 6,
                        background: m.win ? "#dcfce7" : "#fee2e2",
                        color: m.win ? "#166534" : "#991b1b",
                        fontWeight: 600,
                      }}
                    >
                      {m.win ? "Pobeda" : "Poraz"}
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        </>
      )}
    </div>
  );
}

/** ===== Helpers ===== */
function formatDuration(sec) {
  const m = Math.floor(sec / 60);
  const s = sec % 60;
  return `${m}m ${s}s`;
}
function formatDate(iso) {
  const d = new Date(iso);
  return d.toLocaleString();
}
function calcKDA(k, d, a) {
  return (k + a) / Math.max(1, d);
}
function queueName(queueId) {
  // najčešći queue-ovi; po želji proširi mapu
  if (queueId === 420) return "Ranked Solo";
  if (queueId === 440) return "Ranked Flex";
  if (queueId === 450) return "ARAM";
  if (queueId === 400 || queueId === 430) return "Normal";
  return "Other";
}
function ddProfileIcon(iconId) {
  // Data Dragon profil ikonice – verziju možeš da učiniš konfigurabilnom ako želiš
  const ver = "14.1.1";
  return `https://ddragon.leagueoflegends.com/cdn/${ver}/img/profileicon/${iconId}.png`;
}
