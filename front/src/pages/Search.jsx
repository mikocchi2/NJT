// src/pages/Search.jsx
import { useCallback, useEffect, useMemo, useState } from "react";
import api from "../api";

function getSessionUser() {
  const pick = (k) => sessionStorage.getItem(k) ?? localStorage.getItem(k);
  try {
    const rawUser = pick("user");
    const rawMe = pick("me");
    const tokenOnly = pick("token");
    const parsed = rawUser ? JSON.parse(rawUser) : rawMe ? JSON.parse(rawMe) : {};
    return {
      token: parsed.token || tokenOnly || parsed?.user?.token || null,
      userId: parsed.userId ?? parsed.id ?? parsed?.user?.id ?? null,
      email: parsed.email ?? parsed?.user?.email ?? null,
    };
  } catch {
    return { token: null, userId: null, email: null };
  }
}

export default function Search() {
  const [riotId, setRiotId] = useState("");            // npr. "Caps#000"
  const [region, setRegion] = useState("EUNE");        // EUNE, EUW, NA1, KR...
  const [loading, setLoading] = useState(false);
  const [profile, setProfile] = useState(null);
  const [err, setErr] = useState("");
  const [favoriteErr, setFavoriteErr] = useState("");

  const [auth, setAuth] = useState(getSessionUser());
  const [favoritesMap, setFavoritesMap] = useState(new Map()); // summonerId -> favoriteId
  const [syncedSummoner, setSyncedSummoner] = useState(null);
  const [favoriteBusy, setFavoriteBusy] = useState(false);

  // FILTERI
  const [queueFilter, setQueueFilter] = useState("ALL");    // ALL|RANKED_SOLO|RANKED_FLEX|ARAM|NORMALS
  const [resultFilter, setResultFilter] = useState("ALL");  // ALL|WIN|LOSS
  const [minKills, setMinKills] = useState(0);

  const isRiotIdValid = (s) => /^.+#.+$/.test(s.trim());

  useEffect(() => {
    const a = getSessionUser();
    setAuth(a);
  }, []);

  useEffect(() => {
    if (auth.token) {
      api.defaults.headers.common.Authorization = `Bearer ${auth.token}`;
    } else {
      delete api.defaults.headers.common.Authorization;
    }
  }, [auth.token]);

  const loadFavorites = useCallback(async (userId) => {
    if (!userId) {
      setFavoritesMap(new Map());
      return;
    }
    try {
      const { data } = await api.get(`/favorites/user/${userId}`);
      const map = new Map();
      (data || []).forEach((fav) => {
        if (fav?.summonerId != null) {
          map.set(Number(fav.summonerId), fav.id);
        }
      });
      setFavoritesMap(map);
    } catch {
      setFavoritesMap(new Map());
    }
  }, []);

  useEffect(() => {
    loadFavorites(auth.userId);
  }, [auth.userId, loadFavorites]);

  const submit = async (e) => {
    e.preventDefault();
    setErr("");
    setFavoriteErr("");
    setLoading(true);
    setProfile(null);
    setSyncedSummoner(null);
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

  const regionParam = useMemo(() => {
    if (!profile?.region) return region;
    if (typeof profile.region === "string") return profile.region;
    if (typeof profile.region === "object" && profile.region?.name) {
      return profile.region.name;
    }
    return region;
  }, [profile, region]);

  useEffect(() => {
    setSyncedSummoner(null);
    setFavoriteErr("");
    if (!profile) {
      return;
    }

    let ignore = false;
    const fetchExisting = async () => {
      const candidates = [];
      if (profile.name) candidates.push(profile.name);
      if (profile.riotId && profile.riotId !== profile.name) {
        candidates.push(profile.riotId);
      }
      for (const candidate of candidates) {
        if (!candidate) continue;
        try {
          const { data } = await api.get("/summoners/search", {
            params: { name: candidate, region: regionParam },
          });
          if (!ignore && data) {
            setSyncedSummoner(data);
          }
          return;
        } catch (ex) {
          const status = ex?.response?.status;
          if (status && status !== 404) {
            return;
          }
        }
      }
    };

    fetchExisting();
    return () => {
      ignore = true;
    };
  }, [profile, regionParam]);

  const ensureSummonerProfile = useCallback(async () => {
    if (!profile) {
      throw new Error("Nema učitanog summoner-a.");
    }
    if (syncedSummoner?.id != null) {
      return syncedSummoner;
    }

    const candidates = [];
    if (profile.name) candidates.push(profile.name);
    if (profile.riotId && profile.riotId !== profile.name) {
      candidates.push(profile.riotId);
    }

    for (const candidate of candidates) {
      try {
        const { data } = await api.get("/summoners/search", {
          params: { name: candidate, region: regionParam },
        });
        if (data) {
          setSyncedSummoner(data);
          return data;
        }
      } catch (ex) {
        const status = ex?.response?.status;
        if (status && status !== 404) {
          throw ex;
        }
      }
    }

    const params = { region: regionParam, lastN: 10 };
    if (profile.puuid) params.puuid = profile.puuid;
    else if (profile.riotId) params.riotId = profile.riotId;
    else if (profile.name) params.name = profile.name;

    const { data } = await api.post("/summoners/sync", null, { params });
    setSyncedSummoner(data);
    return data;
  }, [profile, regionParam, syncedSummoner]);

  const toggleFavorite = useCallback(async () => {
    if (!profile) return;
    if (!auth.userId) {
      setFavoriteErr("Moraš biti ulogovan da bi menjao favorite.");
      return;
    }
    setFavoriteErr("");
    setFavoriteBusy(true);
    try {
      const summoner = await ensureSummonerProfile();
      if (!summoner?.id) {
        throw new Error("Summoner nema ID u bazi.");
      }
      const sid = Number(summoner.id);
      const existingFavoriteId = favoritesMap.get(sid);
      if (existingFavoriteId) {
        await api.delete(`/favorites/${existingFavoriteId}`);
      } else {
        await api.post("/favorites", {
          userId: auth.userId,
          summonerId: sid,
          note: "",
        });
      }
      await loadFavorites(auth.userId);
    } catch (ex) {
      const msg =
        ex?.response?.data?.message ||
        ex?.response?.data?.error ||
        ex?.message ||
        "Promena favorita nije uspela.";
      setFavoriteErr(msg);
    } finally {
      setFavoriteBusy(false);
    }
  }, [auth.userId, ensureSummonerProfile, favoritesMap, loadFavorites, profile]);

  const currentSummonerId = syncedSummoner?.id != null ? Number(syncedSummoner.id) : null;
  const currentFavoriteId = currentSummonerId != null ? favoritesMap.get(currentSummonerId) : null;
  const isFavorite = Boolean(currentFavoriteId);

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
              alignItems: "flex-start",
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
            <button
              type="button"
              onClick={toggleFavorite}
              disabled={favoriteBusy || !auth.userId}
              aria-label={
                auth.userId
                  ? isFavorite
                    ? "Ukloni iz favorita"
                    : "Sačuvaj u favorite"
                  : "Uloguj se da sačuvaš u favorite"
              }
              aria-pressed={isFavorite}
              title={
                auth.userId
                  ? isFavorite
                    ? "Ukloni iz favorita"
                    : "Sačuvaj u favorite"
                  : "Uloguj se da sačuvaš u favorite"
              }
              style={{
                background: "transparent",
                border: "none",
                fontSize: 28,
                lineHeight: 1,
                cursor: auth.userId ? "pointer" : "not-allowed",
                color: isFavorite ? "#facc15" : "#d1d5db",
                padding: 4,
              }}
            >
              {favoriteBusy ? "…" : isFavorite ? "★" : "☆"}
            </button>
          </div>

          {favoriteErr && (
            <div style={{ color: "red", marginTop: 8 }}>{favoriteErr}</div>
          )}

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
