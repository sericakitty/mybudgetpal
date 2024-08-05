import { useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import axiosInstance from '../services';

const useQuery = () => {
  return new URLSearchParams(useLocation().search);
};

const VerifyEmail = () => {
  const query = useQuery();
  const email = query.get('email');
  const token = query.get('token');
  const [message, setMessage] = useState('');
  const [error, setError] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const verifyToken = async () => {
      try {
        console.log('Verifying email:', email, 'with token:', token);
        const response = await axiosInstance.get(`/verify-email`, {
          params: { email, token }
        });
        console.log('Verification response:', response.data);
        setMessage(response.data.message);
        setError(false);
      } catch (error) {
        console.error('Error during verification:', error.response);
        if (error.response && error.response.data && error.response.data.message) {
          setMessage(error.response.data.message);
        } else {
          setMessage('An error occurred while verifying the email.');
        }
        setError(true);
      } finally {
        setLoading(false);
      }
    };

    verifyToken();
  }, [email, token]);

  if (loading) {
    return (
      <div className="center-container">
        <div className="container-box">
          <h1>Loading...</h1>
        </div>
      </div>
    );
  }

  return (
    <div className="center-container">
      <div className="container-box">
        <div>{message || error}</div>
        <Link className="btn btn-primary" to="/login">Login</Link>
      </div>
    </div>
  );
};

export default VerifyEmail;
