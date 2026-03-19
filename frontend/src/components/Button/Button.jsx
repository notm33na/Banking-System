import styles from './Button.module.css';

export default function Button({
  variant = 'primary', loading = false, disabled, onClick, children, size, type = 'button', ...rest
}) {
  return (
    <button
      type={type}
      className={`${styles.btn} ${styles[variant]} ${size === 'small' ? styles.small : ''}`}
      disabled={disabled || loading}
      onClick={onClick}
      {...rest}
    >
      {loading && <span className={styles.spinner} />}
      {children}
    </button>
  );
}
