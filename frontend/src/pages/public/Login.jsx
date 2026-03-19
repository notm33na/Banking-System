import { useState } from 'react';
import { useNavigate, Link, useSearchParams } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import api from '../../api/axiosInstance';
import FormInput from '../../components/FormInput/FormInput';
import Button from '../../components/Button/Button';
import styles from './Auth.module.css';

const ROLE_REDIRECT = {
  ROLE_ADMIN: '/admin',
  ROLE_CUSTOMER: '/customer',
  ROLE_BUSINESS: '/business',
};

export default function Login() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [searchParams] = useSearchParams();
  const registered = searchParams.get('registered');

  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const { data } = await api.post('/auth/login', form);
      const authData = data.data || data;
      login(authData);
      navigate(ROLE_REDIRECT[authData.role] || '/customer');
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid email or password');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <h1 className={styles.logo}>VIRTBANK</h1>
        <p className={styles.subtitle}>Sign in to your account</p>
        {registered && <div className={styles.success}>Account created! Please sign in.</div>}
        {error && <div className={styles.error}>{error}</div>}
        <form onSubmit={handleSubmit}>
          <FormInput label="Email" name="email" type="email" value={form.email} onChange={onChange} />
          <FormInput label="Password" name="password" type="password" value={form.password} onChange={onChange} />
          <Button type="submit" variant="primary" loading={loading} style={{ width: '100%', marginTop: '0.5rem' }}>
            Sign In
          </Button>
        </form>
        <div className={styles.footer}>
          Don't have an account? <Link to="/register">Register</Link>
        </div>
      </div>
    </div>
  );
}
