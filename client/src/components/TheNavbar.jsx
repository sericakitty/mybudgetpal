import { useState, useEffect } from 'react';
import { Link } from "react-router-dom";
import PropTypes from 'prop-types';

const TheNavbar = ({ user, logout }) => {
  const [showNav, setShowNav] = useState(false);

  const toggleNav = () => {
    setShowNav(!showNav);
  };

  const closeNav = () => {
    if (window.innerWidth < 992) {
      setShowNav(false);
    }
  };

  const handleResize = () => {
    if (window.innerWidth >= 992 && showNav) {
      setShowNav(false);
    }
  };

  useEffect(() => {
    window.addEventListener('resize', handleResize);
    return () => {
      window.removeEventListener('resize', handleResize);
    };
  }, [showNav]);

  return (
    <nav className="navbar navbar-expand-md navbar-light">
      <div className="container-fluid">
        <div className="d-flex align-items-center order-md-1">
          {user && (
            <div className="d-flex align-items-center order-md-2">
              <span className="nav-text">Logged in as 
              <Link to="/Profile" className="nav-link navtext ms-2" onClick={closeNav}><strong>{user.username}</strong></Link>
              </span>
              <button type="button" className="btn btn-sm btn-danger ms-2" onClick={logout}>
                Sign Out
              </button>
            </div>
          )}
          {/* Toggler */}
          <button
            className="navbar-toggler ms-2 order-md-3"
            type="button"
            onClick={toggleNav}
            aria-controls="navbarNav"
            aria-expanded={showNav ? 'true' : 'false'}
            aria-label="Toggle navigation"
          >
            <span className="navbar-toggler-icon"></span>
          </button>
        </div>
        {/* TheNavbar links */}
        <div className={`collapse navbar-collapse ${showNav ? 'show show-animate' : ''}`} id="navbarNav">
          <ul className="navbar-nav me-auto order-md-1">
            <li className="nav-item">
              <Link to="/" className="nav-link navtext" onClick={closeNav}>
                Home
              </Link>
            </li>
            {user?.enabled && (
              <>
                <li className="nav-item">
                  <Link to="/keywords" className="nav-link navtext" onClick={closeNav}>
                    Keywords
                  </Link>
                </li>
                <li className="nav-item">
                  <Link to="/entries" className="nav-link navtext" onClick={closeNav}>
                    Entries
                  </Link>
                </li>
                <li className="nav-item">
                  <Link to="/statistics" className="nav-link navtext" onClick={closeNav}>
                    Statistics
                  </Link>
                </li>
              </>
            )}
          </ul>
        </div>
      </div>
    </nav>
  );
};

TheNavbar.propTypes = {
  user: PropTypes.shape({
    username: PropTypes.string,
    enabled: PropTypes.bool,
  }),
  logout: PropTypes.func.isRequired,
};

export default TheNavbar;
