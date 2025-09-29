import { useEffect, useState } from "react";
import api from "../api";

export default function Favorites() {
  const [items, setItems] = useState([]);
  const [sortBy, setSortBy] = useState("");
  const [form, setForm] = useState({ summonerId:"", note:"" });
  const [err, setErr] = useState("");

  const load = async () => {
    try {
      const { data } = await api.get("/favorites", { params: { sortBy } });
      setItems(data || []);
    } catch {
      setErr("Ne mogu da učitam favorite.");
    }
  };

  useEffect(() => { load(); }, [sortBy]);

  const add = async (e) => {
    e.preventDefault();
    setErr("");
    try {
      await api.post("/favorites", { summonerId: Number(form.summonerId), note: form.note });
      setForm({summonerId:"", note:""});
      load();
    } catch {
      setErr("Dodavanje nije uspelo.");
    }
  };

  const saveNote = async (id, note) => {
    try {
      await api.put(`/favorites/${id}`, { id, note });
      load();
    } catch { setErr("Ažuriranje nije uspelo."); }
  };

  const removeFav = async (id) => {
    try {
      await api.delete(`/favorites/${id}`);
      load();
    } catch { setErr("Brisanje nije uspelo."); }
  };

  return (
    <div style={{maxWidth:800, margin:"24px auto"}}>
      <h2>Favoriti</h2>

      <div style={{display:"flex", gap:8, marginBottom:10}}>
        <label>Sortiraj po:</label>
        <select value={sortBy} onChange={e=>setSortBy(e.target.value)}>
          <option value="">-</option>
          <option value="rank">Rank</option>
          <option value="level">Level</option>
          <option value="lastSyncedAt">Last synced</option>
        </select>
      </div>

      <form onSubmit={add} style={{display:"flex", gap:8, marginBottom:16}}>
        <input placeholder="summonerId" value={form.summonerId}
               onChange={e=>setForm(f=>({...f, summonerId:e.target.value}))}/>
        <input placeholder="napomena" value={form.note}
               onChange={e=>setForm(f=>({...f, note:e.target.value}))}/>
        <button>Dodaj</button>
      </form>

      {err && <div style={{color:"red"}}>{err}</div>}

      <div style={{display:"grid", gap:8}}>
        {items.map(f => (
          <div key={f.id} style={{border:"1px solid #eee", padding:10}}>
            <div>
              <b>{f.summonerName}</b> — {f.region} · lvl {f.level} · {f.rankTier} {f.rankDivision} ({f.leaguePoints} LP)
            </div>
            <div style={{display:"flex", gap:8, marginTop:6}}>
              <input
                defaultValue={f.note || ""}
                onBlur={(e)=> saveNote(f.id, e.target.value)}
                style={{flex:1}}
              />
              <button onClick={()=> removeFav(f.id)}>Obriši</button>
            </div>
          </div>
        ))}
        {!items.length && <div>Nema favorita.</div>}
      </div>
    </div>
  );
}
