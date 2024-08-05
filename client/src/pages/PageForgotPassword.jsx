import { useState } from 'react';
import { makeStyles } from '@material-ui/core';
import axiosInstance from '../services';
import { useNotification } from '../components/TheNotificationContext';

const useStyles = makeStyles({
  centerContainer: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    height: '100vh',
    backgroundColor: 'transparent !important',
  },
  container: {
    textAlign: 'center',
  },
  containerBox: {
    background: '#fff',
    padding: '30px',
    boxShadow: '0 0 10px rgba(0, 0, 0, 0.1)',
    borderRadius: '10px',
    width: '100%',
    maxWidth: '400px',
    marginTop: '20px',
  },
  formGroup: {
    marginBottom: '15px',
  },
  formGroupLabel: {
    display: 'block',
    marginBottom: '5px',
  },
  btnBlock: {
    display: 'block',
    width: '100%',
  },
  linkContainer: {
    display: 'block',
    marginTop: '10px',
    textAlign: 'center',
  },
  alert: {
    marginBottom: '20px',
    marginTop: '10px',
    color: 'red',
  },
});

const PageForgotPassword = () => {
  const classes = useStyles();
  const [email, setEmail] = useState('');
  const { showNotification } = useNotification();

  const handleChange = (e) => {
    setEmail(e.target.value);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axiosInstance.get('/request-password-reset', {
        params: { email },
       });
      if (response.status === 200) {
        showNotification('Password reset email sent successfully!', 'success');
      } else {
        showNotification('Failed to send password reset email. Please try again.', 'error');
      }
    } catch (error) {
      console.error('Error:', error);
      showNotification('An unexpected error occurred. Please try again.', 'error');
    }
  };

  return (
    <div className={classes.centerContainer}>
      <div className={classes.container}>
        <h1>My Budget Pal</h1>
        <div className={classes.containerBox}>
          <h3>Forgot Your Password</h3>
          <form onSubmit={handleSubmit}>
            <div className={classes.formGroup}>
              <label className={classes.formGroupLabel}>Email:
                <input
                  type="email"
                  name="email"
                  value={email}
                  onChange={handleChange}
                  className="form-control"
                  required
                />
              </label>
            </div>
            <input type="submit" className={`btn btn-primary ${classes.btnBlock}`} value="Send Reset Email" />
          </form>
          <div className={classes.linkContainer}>
            <a href="/login">Back to Login</a>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PageForgotPassword;
