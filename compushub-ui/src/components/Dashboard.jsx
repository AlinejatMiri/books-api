import { Link } from 'react-router-dom'

function Dashboard() {
  return (
    <div>
      <div className="page-heading">
        <div><p className="eyebrow">OVERVIEW</p><h1>Welcome to CompusHub</h1><p className="text-secondary mb-0">University course and registration management system.</p></div>
        <Link to="/courses" className="btn btn-primary">Explore courses <span aria-hidden="true">→</span></Link>
      </div>
      <section className="row g-3 mb-4" aria-label="Academic overview">
        {[['Courses', '24', 'Across 6 departments', 'course'], ['Students', '1,248', 'Currently enrolled', 'student'], ['Sections', '38', 'Active this semester', 'section']].map(([label, value, detail, icon]) => (
          <div className="col-md-4" key={label}><div className="stat-card"><div className={`stat-icon ${icon}`} aria-hidden="true">{icon === 'course' ? '▤' : icon === 'student' ? '♙' : '▦'}</div><p>{label}</p><strong>{value}</strong><span>{detail}</span></div></div>
        ))}
      </section>
      <section className="card app-card">
        <div className="card-body p-4"><p className="eyebrow">GETTING STARTED</p><h2 className="h5 fw-bold">Your academic workspace</h2><p className="text-secondary mb-3">Browse the sections from the navigation bar to explore the CompusHub interface.</p><div className="d-flex flex-wrap gap-2"><Link className="btn btn-outline-primary" to="/courses">Manage courses</Link><Link className="btn btn-light" to="/students">View students</Link></div></div>
      </section>
    </div>
  )
}

export default Dashboard
