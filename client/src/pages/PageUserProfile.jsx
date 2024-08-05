import { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import axiosInstance from '../services';
import { useNotification } from '../components/TheNotificationContext';

const UserProfile = () => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const { showNotification } = useNotification();

  useEffect(() => {
    const getUser = async () => {
      try {
        const response = await axiosInstance.get('/auth/user');
        setUser(response.data);
        setLoading(false);
      } catch (error) {
        console.error('Error:', error);
        showNotification('Failed to fetch user data', 'error');
        setLoading(false);
      }
    };
    getUser();
  }, []);

  const handleResendVerification = async () => {
    try {
      await axiosInstance.get('/auth/resend-verification-email');
      showNotification('Verification email sent successfully', 'success');
    } catch (error) {
      console.error('Error:', error);
      showNotification('Failed to send verification email', 'error');
    }
  };

  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <main>
      <div className="container mt-5">
        <h2>User Details</h2>
        <div className="card">
          <div className="card-body">
            <h5 className="card-title">Username: {user?.username}</h5>
            <p className="card-text">First Name: <span>{user?.firstName}</span></p>
            <p className="card-text">Last Name: <span>{user?.lastName}</span></p>
            <p className="card-text">Email: <span>{user?.email}</span></p>
            <p className="card-text">Enabled: <span>{user?.enabled ? 'Yes' : 'No'}</span></p>
            {!user?.enabled && (
              <button className="btn btn-primary" onClick={handleResendVerification}>
                Resend Verification Email
              </button>
            )}
          </div>
        </div>
      </div>
    </main>
  );
};

UserProfile.propTypes = {
  user: PropTypes.object
};

export default UserProfile;
