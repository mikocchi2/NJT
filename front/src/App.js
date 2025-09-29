
import { BrowserRouter, Link, Routes, Route, Navigate, useNavigate } from "react-router-dom";
import Login from "./pages/Login";
import Register from "./pages/Register";

import Search from "./pages/Search";
import Profile from "./pages/Profile";
import Favorites from "./pages/Favorites";
import Admin from "./pages/Admin";
import "./App.css";
import Navbar from "./components/Navbar";


 
function Home() {
  const userRaw = localStorage.getItem("user");
  const user = userRaw ? JSON.parse(userRaw) : null;
  return (
    <div style={{ maxWidth: 600, margin: "30px auto" }}>
      <h2>Welcome</h2>
      {user ? (
        <>
          <div>Logged in as: <b>{user.email}</b> ({user.role})</div>
          <div style={{ marginTop: 8, fontSize: 12, color: "#666" }}>
            Token saved in localStorage. All axios calls will include Bearer token.
          </div>
        </>
      ) : (
        <div>Please login or register.</div>
      )}
    </div>
  );
}

export default function App() {
  const token = localStorage.getItem("token");

  return (
    <BrowserRouter>
      <Navbar />
      <Routes>
        <Route path="/" element={token ? <Home /> : <Navigate to="/login" replace />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/search" element={<Search/>} />
        <Route path="/profile/:id" element={<Profile/>} />
        <Route path="/favorites" element={token ? <Favorites/> : <Navigate to="/login" replace />} />
        <Route path="/admin" element={
          // jednostavna zaštita na frontu (BE i dalje autoritativno proverava rolu)
          (JSON.parse(localStorage.getItem("user")||"{}")?.role === "ADMIN")
            ? <Admin/>
            : <Navigate to="/" replace />
        } />


        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
