import { useCallback, useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../api";

function getAuth() {
  const pick = (k) => sessionStorage.getItem(k) ?? localStorage.getItem(k);
  try {
    const rawUser = pick("user");
    const rawMe = pick("me");
    const tokenOnly = pick("token");
    const u = rawUser ? JSON.parse(rawUser) : rawMe ? JSON.parse(rawMe) : {};
    return {
      token: u.token || tokenOnly || u?.user?.token || null,
      userId: u.userId ?? u.id ?? u?.user?.id ?? null,
      email: u.email ?? u?.user?.email ?? null,
    };
  } catch {
    return { token: null, userId: null, email: null };
  }
}

export default function Favorites() {
  const nav = useNavigate();
  const [{ userId, token }] = useState(getAuth());

  const [favorites, setFavorites] = useState([]);
  const [summonerMap, setSummonerMap] = useState(new Map());
  const [err, setErr] = useState("");
  const [busyId, setBusyId] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (token) api.defaults.headers.common.Authorization = `Bearer ${token}`;
    else delete api.defaults.headers.common.Authorization;
  }, [token]);

  const load = useCallback(async () => {
    if (!userId) {
      setFavorites([]);
      setSummonerMap(new Map());
      return;
    }
    setErr("");
    setLoading(true);
    try {
      const [favRes, summRes] = await Promise.all([
        api.get(`/favorites/user/${userId}`),
        api.get("/summoners"),
      ]);

      const map = new Map();
      (summRes.data || []).forEach((s) => {
        if (s?.id != null) {
          map.set(Number(s.id), s);
        }
      });
      setSummonerMap(map);

      const seen = new Set();
      const unique = [];
      (favRes.data || []).forEach((fav) => {
        const sid = Number(fav?.summonerId);
        if (!sid || seen.has(sid)) {
          return;
        }
        seen.add(sid);
        unique.push({ ...fav, summoner: map.get(sid) || null });
      });
      unique.sort((a, b) => {
        const aTime = a?.createdAt ? new Date(a.createdAt).getTime() : 0;
        const bTime = b?.createdAt ? new Date(b.createdAt).getTime() : 0;
        return bTime - aTime;
      });
      setFavorites(unique);
    } catch (ex) {
      setErr(
        ex?.response?.data?.message ||
          ex?.response?.data?.error ||
          "Ne mogu da učitam favorite."
      );
    } finally {
      setLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    load();
  }, [load]);

  const remove = async (favoriteId) => {
    if (!favoriteId) return;
    setErr("");
    setBusyId(favoriteId);
    try {
      await api.delete(`/favorites/${favoriteId}`);
      setFavorites((prev) => prev.filter((f) => f.id !== favoriteId));
    } catch (ex) {
      setErr(
        ex?.response?.data?.message ||
          ex?.response?.data?.error ||
          "Brisanje iz favorita nije uspelo."
      );
    } finally {
      setBusyId(null);
    }
  };

  const rows = useMemo(() => {
    return favorites.map((fav) => {
      const summoner = fav.summoner ?? summonerMap.get(Number(fav.summonerId));
      return { favorite: fav, summoner };
    });
  }, [favorites, summonerMap]);

  const openProfile = (summonerId) => {
    if (!summonerId) return;
    nav(`/profile/${summonerId}`);
  };

  return (
    <div style={{ maxWidth: 960, margin: "24px auto" }}>
      <h2>Favoriti</h2>

      {!userId && (
        <div
          style={{
            background: "#fff3cd",
            border: "1px solid #ffeeba",
            padding: 8,
            borderRadius: 6,
            marginBottom: 8,
            fontSize: 14,
          }}
        >
          Moraš biti ulogovan da bi video favorite.
        </div>
      )}

      {err && <div style={{ color: "red", marginBottom: 8 }}>{err}</div>}

      {userId && (
        <div style={{ marginBottom: 12, fontSize: 14, color: "#555" }}>
          Lista prikazuje samo jedinstvene summoner-e koje si sačuvao u favorite.
        </div>
      )}

      {loading ? (
        <div>Učitavam...</div>
      ) : !rows.length ? (
        <div>
          Nema favorita. Dodaj summonera preko strane <Link to="/search">Search</Link>{" "}
          ili <Link to="/summoners">Summoners</Link>.
        </div>
      ) : (
        <div style={{ display: "grid", gap: 8 }}>
          {rows.map(({ favorite, summoner }) => {
            const sid = Number(favorite.summonerId);
            return (
              <div
                key={favorite.id}
                onClick={() => openProfile(sid)}
                role="button"
                tabIndex={0}
                onKeyDown={(e) => {
                  if (e.key === "Enter") openProfile(sid);
                }}
                style={{
                  border: "1px solid #eee",
                  borderRadius: 8,
                  padding: 12,
                  display: "flex",
                  gap: 12,
                  alignItems: "center",
                  cursor: sid ? "pointer" : "default",
                }}
              >
                <div style={{ flex: 1, display: "flex", gap: 12 }}>
                  {summoner?.iconId != null && (
                    <img
                      alt="icon"
                      src={ddProfileIcon(summoner.iconId)}
                      style={{ width: 48, height: 48, borderRadius: 8, background: "#f5f5f5" }}
                      onError={(e) => {
                        e.currentTarget.style.display = "none";
                      }}
                    />
                  )}
                  <div style={{ flex: 1 }}>
                    <div style={{ fontWeight: 700, fontSize: 16 }}>
                      {summoner ? (
                        <Link
                          to={`/profile/${sid}`}
                          onClick={(e) => e.stopPropagation()}
                          style={{ textDecoration: "none", color: "inherit" }}
                        >
                          {summoner.name || "-"}
                        </Link>
                      ) : (
                        "Nepoznat summoner"
                      )}
                    </div>
                    {summoner ? (
                      <>
                        <div style={{ fontSize: 13, color: "#555" }}>
                          lvl {summoner.level ?? "-"} · {summoner.region} · {summoner.rankTier ?? "-"} {summoner.rankDivision ?? ""}{" "}
                          {summoner.leaguePoints != null ? `(${summoner.leaguePoints} LP)` : ""}
                        </div>
                        <div style={{ fontSize: 12, color: "#888" }}>
                          Last sync: {summoner.lastSyncedAt || "-"}
                        </div>
                      </>
                    ) : (
                      <div style={{ fontSize: 13, color: "#555" }}>
                        Profil više ne postoji u bazi. Pokušaj da ga ponovo sinhronizuješ preko Search stranice.
                      </div>
                    )}
                  </div>
                </div>

                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    remove(favorite.id);
                  }}
                  disabled={busyId === favorite.id}
                  style={{ background: "#fee2e2" }}
                >
                  {busyId === favorite.id ? "Uklanjam…" : "Ukloni"}
                </button>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}

function ddProfileIcon(iconId) {
  const ver = "14.1.1";
  return `https://ddragon.leagueoflegends.com/cdn/${ver}/img/profileicon/${iconId}.png`;
}
