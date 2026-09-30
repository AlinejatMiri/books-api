import Navbar from './Navbar.jsx'

function Layout({ children }) {
  return (
    <>
      <Navbar />
      <main className="container page-container">{children}</main>
      <footer className="container app-footer">CompusHub <span>·</span> Academic management made simple</footer>
    </>
  )
}

export default Layout
