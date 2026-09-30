import { useState } from 'react'

const initialCourses = [
  { id: 1, code: 'CS301', title: 'Web Information Systems', credits: 3 },
  { id: 2, code: 'CS302', title: 'Enterprise Web Applications', credits: 3 },
  { id: 3, code: 'CS210', title: 'Data Structures and Algorithms', credits: 4 },
]

function Courses() {
  const [courses, setCourses] = useState(initialCourses)
  const [form, setForm] = useState({ code: '', title: '', credits: '' })
  const [editingId, setEditingId] = useState(null)

  function updateField(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  function saveCourse(event) {
    event.preventDefault()
    if (!form.code.trim() || !form.title.trim() || Number(form.credits) < 1) return
    if (editingId) {
      setCourses(courses.map((course) => course.id === editingId ? { ...course, ...form, credits: Number(form.credits) } : course))
    } else {
      setCourses([...courses, { ...form, id: Math.max(0, ...courses.map((course) => course.id)) + 1, credits: Number(form.credits) }])
    }
    setForm({ code: '', title: '', credits: '' })
    setEditingId(null)
  }

  function editCourse(course) {
    setEditingId(course.id)
    setForm({ code: course.code, title: course.title, credits: String(course.credits) })
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  function deleteCourse(id) {
    setCourses(courses.filter((course) => course.id !== id))
    if (editingId === id) {
      setEditingId(null)
      setForm({ code: '', title: '', credits: '' })
    }
  }

  return (
    <div>
      <div className="page-heading"><div><p className="eyebrow">ACADEMIC CATALOG</p><h1>Course Management</h1><p className="text-secondary mb-0">Create and organize courses offered by your university.</p></div><span className="count-pill">{courses.length} courses</span></div>
      <section className="card app-card mb-4">
        <div className="card-header app-card-header"><div><h2 className="h6 mb-1">{editingId ? 'Edit course' : 'Add new course'}</h2><p className="text-secondary small mb-0">Enter the course details below.</p></div><span className="form-step">01</span></div>
        <div className="card-body p-4">
          <form onSubmit={saveCourse}>
            <div className="row g-3 align-items-end">
              <div className="col-md-3"><label className="form-label" htmlFor="course-code">Course code</label><input id="course-code" name="code" className="form-control" placeholder="e.g. CS401" value={form.code} onChange={updateField} required /></div>
              <div className="col-md-5"><label className="form-label" htmlFor="course-title">Course title</label><input id="course-title" name="title" className="form-control" placeholder="Enter course title" value={form.title} onChange={updateField} required /></div>
              <div className="col-md-2"><label className="form-label" htmlFor="course-credits">Credits</label><input id="course-credits" name="credits" type="number" className="form-control" min="1" max="12" placeholder="3" value={form.credits} onChange={updateField} required /></div>
              <div className="col-md-2"><button type="submit" className="btn btn-primary w-100">{editingId ? 'Save changes' : '+ Add course'}</button></div>
            </div>
            {editingId && <button type="button" className="btn btn-link btn-sm px-0 mt-2" onClick={() => { setEditingId(null); setForm({ code: '', title: '', credits: '' }) }}>Cancel editing</button>}
          </form>
        </div>
      </section>
      <section className="card app-card">
        <div className="card-header app-card-header"><div><h2 className="h6 mb-1">Course list</h2><p className="text-secondary small mb-0">A quick overview of all available courses.</p></div><span className="list-mark" aria-hidden="true">▤</span></div>
        <div className="card-body p-0">
          <div className="table-responsive"><table className="table table-hover align-middle mb-0"><thead><tr><th className="ps-4">ID</th><th>Course</th><th>Title</th><th>Credits</th><th className="text-end pe-4">Actions</th></tr></thead><tbody>
            {courses.map((course) => <tr key={course.id}><td className="ps-4 text-secondary">{String(course.id).padStart(2, '0')}</td><td><span className="course-code">{course.code}</span></td><td className="fw-medium">{course.title}</td><td><span className="credit-pill">{course.credits} credits</span></td><td className="text-end pe-4 text-nowrap"><button type="button" className="btn btn-sm btn-edit me-2" onClick={() => editCourse(course)}>Edit</button><button type="button" className="btn btn-sm btn-delete" onClick={() => deleteCourse(course.id)}>Delete</button></td></tr>)}
            {courses.length === 0 && <tr><td colSpan="5" className="text-center text-secondary py-5">No courses yet. Add your first course above.</td></tr>}
          </tbody></table></div>
        </div>
        <div className="card-footer bg-white text-secondary small px-4">Showing {courses.length} {courses.length === 1 ? 'course' : 'courses'}</div>
      </section>
    </div>
  )
}

export default Courses
