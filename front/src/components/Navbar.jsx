import { NavLink, useNavigate } from "react-router-dom";

export default function Navbar() {
  const nav = useNavigate();
  const token = localStorage.getItem("token");
  const user = JSON.parse(localStorage.getItem("user") || "null");
  const isAdmin = user?.role === "ADMIN";

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    nav("/login", { replace: true });
  };

  const linkStyle = ({ isActive }) => ({
    padding: "6px 10px",
    borderRadius: 6,
    textDecoration: "none",
    color: isActive ? "#111" : "#444",
    background: isActive ? "#eaeaea" : "transparent",
  });

  return (
    <nav style={{
      display: "flex",
      alignItems: "center",
      gap: 10,
      padding: 10,
      borderBottom: "1px solid #eee",
      position: "sticky",
      top: 0,
      background: "#fff",
      zIndex: 10
    }}>
      {/* Left: public routes */}
      <NavLink to="/" style={linkStyle}>Home</NavLink>
      <NavLink to="/search" style={linkStyle}>Search</NavLink>
      <NavLink to="/summoners" style={linkStyle}>
        Summoners
      </NavLink>

      {/* Auth-only */}
      {token && <NavLink to="/favorites" style={linkStyle}>Favorites</NavLink>}
      {token && isAdmin && <NavLink to="/admin" style={linkStyle}>Admin</NavLink>}

      {/* Right side */}
      <div style={{ marginLeft: "auto", display: "flex", alignItems: "center", gap: 8 }}>
        {!token ? (
          <>
            <NavLink to="/login" style={linkStyle}>Login</NavLink>
            <NavLink to="/register" style={linkStyle}>Register</NavLink>
          </>
        ) : (
          <>
            <span style={{ fontSize: 13, color: "#666" }}>
              {user?.email} ({user?.role})
            </span>
            <button onClick={logout} style={{ padding: "6px 10px", borderRadius: 6 }}>
              Logout
            </button>
          </>
        )}
      </div>
    </nav>
  );
}
