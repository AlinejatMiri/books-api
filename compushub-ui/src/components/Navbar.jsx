import { NavLink, Link } from 'react-router-dom'

const links = [
  { to: '/', label: 'Dashboard', end: true },
  { to: '/courses', label: 'Courses' },
  { to: '/students', label: 'Students' },
  { to: '/sections', label: 'Sections' },
  { to: '/registrations', label: 'Registrations' },
]

function Navbar() {
  return (
    <nav className="navbar navbar-expand-lg app-navbar">
      <div className="container">
        <Link className="navbar-brand d-flex align-items-center gap-2" to="/">
          <span className="brand-mark" aria-hidden="true">C</span>
          <span>Compus<span className="brand-accent">Hub</span></span>
        </Link>
        <button className="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav" aria-controls="mainNav" aria-expanded="false" aria-label="Toggle navigation">
          <span className="navbar-toggler-icon" />
        </button>
        <div className="collapse navbar-collapse" id="mainNav">
          <div className="navbar-nav ms-lg-5">
            {links.map(({ to, label, end }) => (
              <NavLink key={to} to={to} end={end} className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>
                {label}
              </NavLink>
            ))}
          </div>
          <div className="ms-lg-auto d-flex align-items-center gap-2 user-chip mt-3 mt-lg-0">
            <span className="avatar">AK</span>
            <span className="small fw-semibold">Admin</span>
          </div>
        </div>
      </div>
    </nav>
  )
}

export default Navbar
