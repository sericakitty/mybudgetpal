import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import axiosInstance from '../services';

const useQuery = () => {
  return new URLSearchParams(useLocation().search);
};

const ResetPassword = () => {
  const query = useQuery();
  const email = query.get('email');
  const token = query.get('token');
  const [message, setMessage] = useState('');
  const [error, setError] = useState(false);
  const [loading, setLoading] = useState(true);
  const [password, setPassword] = useState('');
  const [passwordCheck, setPasswordCheck] = useState('');
  const navigation = useNavigate();

  useEffect(() => {
    const verifyToken = async () => {
      try {
        const response = await axiosInstance.get(`/reset-password`, {
          params: { email, token }
        });
        setMessage(response.data.message);
        setError(false);
      } catch (error) {
        if (error.response && error.response.data && error.response.data.message) {
          setMessage(error.response.data.message);
        } else {
          setMessage('An error occurred while verifying the token.');
        }
        setError(true);
      } finally {
        setLoading(false);
      }
    };

    verifyToken();
  }, [email, token]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (password !== passwordCheck) {
      setMessage('Passwords do not match.');
      setError(true);
      return;
    }

    try {
     
        const response = await axiosInstance.post(`/reset-password`, {
          email,
          token,
          password,
          passwordCheck
        });

      if (response.status !== 200) {
        throw new Error('Failed to reset password');
      }
      setMessage('Password reset successfully.');
      setError(false);
      setTimeout(() => {
        navigation('/login');
      }, 3000);
    } catch (error) {
      if (error.response && error.response.data && error.response.data.message) {
        setMessage(error.response.data.message);
      } else {
        setMessage('An error occurred while resetting the password.');
      }
      setError(true);
    }
  };

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
        <h1>{message}</h1>
        {!error && message === "Token is valid" && (
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="password">New Password</label>
              <input
                type="password"
                id="password"
                className="form-control"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="passwordCheck">Confirm New Password</label>
              <input
                type="password"
                id="passwordCheck"
                className="form-control"
                value={passwordCheck}
                onChange={(e) => setPasswordCheck(e.target.value)}
                required
              />
            </div>
            <button type="submit" className="btn btn-primary">Reset Password</button>
          </form>
        )}
        {error && <Link className="btn btn-secondary" to="/">Go to Home</Link>}
      </div>
    </div>
  );
};

export default ResetPassword;
