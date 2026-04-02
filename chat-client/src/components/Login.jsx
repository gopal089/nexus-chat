import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { LogIn, UserPlus } from 'lucide-react';
import styles from './Login.module.css';

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8081/api/auth';

export default function Login() {
  const [isLogin, setIsLogin] = useState(true);
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [serverStatus, setServerStatus] = useState('checking'); // 'checking', 'online', 'offline'
  const navigate = useNavigate();

  React.useEffect(() => {
    // Simple health check to verify connectivity
    const healthUrl = API_BASE.replace('/api/auth', '/actuator/health');
    axios.get(healthUrl)
      .then(() => setServerStatus('online'))
      .catch((err) => {
        console.error('Backend unreachable:', err);
        setServerStatus('offline');
      });
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    try {
      const endpoint = isLogin ? '/login' : '/register';
      const payload = isLogin ? { username, password } : { username, password, email };

      const response = await axios.post(`${API_BASE}${endpoint}`, payload);

      if (response.data.token) {
        localStorage.setItem('chat_token', response.data.token);
        localStorage.setItem('chat_username', username);
        navigate('/chat');
      } else if (!isLogin) {
        // Automatically switch to login on successful registration
        setIsLogin(true);
        setError("Registration successful! Please login.");
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Authentication failed. Please try again.');
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.glassCard}>
        <div className={styles.header}>
          <h2>{isLogin ? 'Welcome Back' : 'Create Account'}</h2>
          <p>{isLogin ? 'Enter your details to access the chat.' : 'Sign up to join the real-time chat.'}</p>

          <div className={`${styles.statusIndicator} ${styles[serverStatus]}`}>
            <span className={styles.dot}></span>
            <span>Server: {serverStatus === 'checking' ? 'Checking...' : serverStatus === 'online' ? 'Online' : 'Offline (Check Backend)'}</span>
          </div>
        </div>

        {error && <div className={styles.errorBanner}>{error}</div>}

        <form onSubmit={handleSubmit} className={styles.form}>
          {!isLogin && (
            <div className={styles.inputGroup}>
              <label>Email</label>
              <input
                type="email"
                placeholder="Enter your email address"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
          )}

          <div className={styles.inputGroup}>
            <label>Username</label>
            <input
              type="text"
              placeholder="Enter your username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>

          <div className={styles.inputGroup}>
            <label>Password</label>
            <input
              type="password"
              placeholder="Enter your password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              minLength={8}
              required
            />
          </div>

          <button type="submit" className={styles.submitBtn}>
            {isLogin ? <LogIn size={18} /> : <UserPlus size={18} />}
            <span>{isLogin ? 'Sign In' : 'Sign Up'}</span>
          </button>
        </form>

        <div className={styles.toggleText}>
          {isLogin ? "Don't have an account? " : "Already have an account? "}
          <span onClick={() => setIsLogin(!isLogin)}>
            {isLogin ? 'Register here' : 'Login here'}
          </span>
        </div>
      </div>
    </div>
  );
}
