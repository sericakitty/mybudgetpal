import { createContext, useState, useContext, useEffect } from 'react';
import PropTypes from 'prop-types';
import axiosInstance from '../services';
import { useNavigate } from 'react-router-dom';
import { useNotification } from './TheNotificationContext';

const AuthContext = createContext();

export const useAuth = () => useContext(AuthContext);

const validateToken = async () => {
  try {
    const response = await axiosInstance.get('/auth/validate-token');
    if (response.status === 200 && response.data) {
      return response.data;
    } else {
      return null;
    }
  } catch (error) {
    console.error('Error:', error);
    return null;
  }
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const { showNotification } = useNotification();

  const login = async (userData) => {
    try {
      const response = await axiosInstance.post('/login', userData);
      if (response.status === 200) {
        const token = response.data.token;
        localStorage.setItem('token', token);
        setUser({
          id: response.data.id,
          username: response.data.username,
          email: response.data.email,
          role: response.data.role,
          enabled: response.data.enabled
        });
        return true;
      } else {
        console.error('Error:', response.statusText);
      }
    } catch (error) {
      console.error('Error:', error);
      setError('Login failed. Please try again.');
    }
    return false;
  };

  const logout = async () => {
    try {
      const response = await axiosInstance.post('/auth/logout');
      if (response.status === 200) {
        localStorage.removeItem('token');
        setUser(null);
        navigate('/login');
        showNotification('Logout successful!', 'success');
      } else {
        console.error('Error:', response.statusText);
      }
    } catch (error) {
      console.error('Error:', error);
      setError('Logout failed. Please try again.');
    }
  };

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      validateToken().then(user => {
        if (user) {
          setUser(user);
        } else {
          localStorage.removeItem('token');
          setError('Invalid token. Please login again.');
          navigate('/login');
        }
        setLoading(false);
      });
    } else {
      setLoading(false);
    }
  }, [navigate]);

  return (
    <AuthContext.Provider value={{ user, login, logout, loading, error, setError }}>
      {children}
    </AuthContext.Provider>
  );
};

AuthProvider.propTypes = {
  children: PropTypes.node.isRequired
};
