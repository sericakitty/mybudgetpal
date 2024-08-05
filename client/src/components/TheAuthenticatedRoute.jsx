import { Navigate } from 'react-router-dom';
import { useAuth } from './TheAuthContext';
import PropTypes from 'prop-types';

const AuthenticatedRoute = ({ component: Component, ...rest }) => {
  const { user, loading, error } = useAuth();

  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <Navigate to="/login" />;
  }

  return user ? <Component {...rest} /> : <Navigate to="/login" />;
};

AuthenticatedRoute.propTypes = {
  component: PropTypes.elementType.isRequired,
};

export default AuthenticatedRoute;
