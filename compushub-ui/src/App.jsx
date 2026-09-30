import { Route, Routes } from 'react-router-dom'
import Layout from './components/Layout.jsx'
import Dashboard from './components/Dashboard.jsx'
import Courses from './components/Courses.jsx'
import Students from './components/Students.jsx'
import Sections from './components/Sections.jsx'
import Registrations from './components/Registrations.jsx'

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/courses" element={<Courses />} />
        <Route path="/students" element={<Students />} />
        <Route path="/sections" element={<Sections />} />
        <Route path="/registrations" element={<Registrations />} />
        <Route path="*" element={<Dashboard />} />
      </Routes>
    </Layout>
  )
}

export default App
