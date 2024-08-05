import { useState } from 'react';
import axiosInstance from '../services';

const Signup = () => {
  const [form, setForm] = useState({
    username: '',
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    passwordCheck: '',
  });

  const [message, setMessage] = useState(null);
  const [error, setError] = useState({});
  const [generalError, setGeneralError] = useState(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm({ ...form, [name]: value });
    setError({ ...error, [name]: '' });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    let formIsValid = true;
    const newErrors = {};

    // Client-side validation
    if (!form.username) {
      formIsValid = false;
      newErrors.username = 'Username is required';
    }
    if (!form.firstName) {
      formIsValid = false;
      newErrors.firstName = 'First name is required';
    } else if (!/^[a-zA-Z]*$/.test(form.firstName)) {
      formIsValid = false;
      newErrors.firstName = 'First name can only contain letters';
    }
    if (!form.lastName) {
      formIsValid = false;
      newErrors.lastName = 'Last name is required';
    } else if (!/^[a-zA-Z]*$/.test(form.lastName)) {
      formIsValid = false;
      newErrors.lastName = 'Last name can only contain letters';
    }
    if (!form.email) {
      formIsValid = false;
      newErrors.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(form.email)) {
      formIsValid = false;
      newErrors.email = 'Invalid email format';
    }
    if (!form.password || form.password.length < 8) {
      formIsValid = false;
      newErrors.password = 'Password must be at least 8 characters';
    }
    if (!form.passwordCheck) {
      formIsValid = false;
      newErrors.passwordCheck = 'Please retype the password';
    } else if (form.password !== form.passwordCheck) {
      formIsValid = false;
      newErrors.passwordCheck = 'Passwords do not match';
    }

    if (!formIsValid) {
      setError(newErrors);
      return;
    }

    setGeneralError(null);

    try {
      const response = await axiosInstance.post('/signup', form);
      if (response.status === 200) {
        setMessage('Signup successful! Please check your email to verify your account.');
        
        setTimeout(() => {
          setMessage(null);
        }, 5000);
      } else {
        setGeneralError('Signup failed. Please try again.');
        setTimeout(() => {
          setGeneralError(null);
        }, 5000);
      }
    } catch (error) {
      if (error.response && error.response.data) {
        const serverErrorMessages = {};

        if (error.response.data === 'Username already exists') {
          serverErrorMessages.username = 'Username already exists';
        }
        if (error.response.data === 'Email already exists') {
          serverErrorMessages.email = 'Email already exists';
        }

        setError(serverErrorMessages);
      } else {
        setGeneralError('Signup failed. Please try again.');
      }
    }
  };

  return (
    <div className="center-container">
      <div className="container-box signup-container">
        <h3>Sign-up</h3>
        {message && <div className="alert alert-success">{message}</div>}
        {generalError && <div className="alert alert-danger">{generalError}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Username:
              <input 
                type="text" 
                name="username" 
                value={form.username} 
                onChange={handleChange} 
                className={`form-control ${error.username ? 'is-invalid' : ''}`} 
              />
              {error.username && <div className="invalid-feedback">{error.username}</div>}
            </label>
          </div>
          <div className="form-group">
            <label>First Name:
              <input 
                type="text" 
                name="firstName" 
                value={form.firstName} 
                onChange={handleChange} 
                className={`form-control ${error.firstName ? 'is-invalid' : ''}`} 
              />
              {error.firstName && <div className="invalid-feedback">{error.firstName}</div>}
            </label>
          </div>
          <div className="form-group">
            <label>Last Name:
              <input 
                type="text" 
                name="lastName" 
                value={form.lastName} 
                onChange={handleChange} 
                className={`form-control ${error.lastName ? 'is-invalid' : ''}`} 
              />
              {error.lastName && <div className="invalid-feedback">{error.lastName}</div>}
            </label>
          </div>
          <div className="form-group">
            <label>Email:
              <input 
                type="email" 
                name="email" 
                value={form.email} 
                onChange={handleChange} 
                className={`form-control ${error.email ? 'is-invalid' : ''}`} 
              />
              {error.email && <div className="invalid-feedback">{error.email}</div>}
            </label>
          </div>
          <div className="form-group">
            <label>Password:
              <input 
                type="password" 
                name="password" 
                value={form.password} 
                onChange={handleChange} 
                className={`form-control ${error.password ? 'is-invalid' : ''}`} 
              />
              {error.password && <div className="invalid-feedback">{error.password}</div>}
            </label>
          </div>
          <div className="form-group">
            <label>Re-type Password:
              <input 
                type="password" 
                name="passwordCheck" 
                value={form.passwordCheck} 
                onChange={handleChange} 
                className={`form-control ${error.passwordCheck ? 'is-invalid' : ''}`} 
              />
              {error.passwordCheck && <div className="invalid-feedback">{error.passwordCheck}</div>}
            </label>
          </div>
          <input type="submit" className="btn btn-primary btn-block" value="Signup" />
        </form>
        <div className="text-center mt-3">
          <a href="/login" className="link-container">Login</a>
          <a href="/forgot-password" className="link-container">Forgot your password?</a>
        </div>
      </div>
    </div>
  );
};

export default Signup;
