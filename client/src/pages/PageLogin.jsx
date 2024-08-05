import { useState } from 'react';
import { useAuth } from '../components/TheAuthContext';
import { useNavigate } from 'react-router-dom';
import { makeStyles } from '@material-ui/core';
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
  mt3: {
    marginTop: '15px',
  },
  linkContainer: {
    display: 'block',
    marginTop: '10px',
    textAlign: 'center',
    '& a': {
      marginRight: '10px',
    },
  },
  alert: {
    marginBottom: '20px',
    marginTop: '10px',
    color: 'red',
  },
});

const PageLogin = () => {
  const classes = useStyles();
  const [form, setForm] = useState({ email: '', password: '' });
  const { login } = useAuth();
  const navigate = useNavigate();
  const { showNotification } = useNotification();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm({ ...form, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const userData = { email: form.email, password: form.password };
    try {
      const response = await login(userData);
      if (response === false) {
        showNotification('Login failed. Please try again.', 'error');
      } else {
        showNotification('Login successful!', 'success');
        navigate('/');
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
          <h3>Login</h3>
          <form onSubmit={handleSubmit}>
            <div className={classes.formGroup}>
              <label className={classes.formGroupLabel}>Email:
                <input
                  type="email"
                  name="email"
                  value={form.email}
                  onChange={handleChange}
                  className="form-control"
                  required
                />
              </label>
            </div>
            <div className={classes.formGroup}>
              <label className={classes.formGroupLabel}>Password:
                <input
                  type="password"
                  name="password"
                  value={form.password}
                  onChange={handleChange}
                  className="form-control"
                  required
                />
              </label>
            </div>
            <input type="submit" className={`btn btn-primary ${classes.btnBlock}`} value="Login" />
          </form>
          <div className={classes.linkContainer}>
            <a href="/signup">Signup</a>
            <a href="/forgot-password">Forgot your password?</a>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PageLogin;
