import styles from './EmptyState.module.css';

export default function EmptyState({ message = 'No data found' }) {
  return (
    <div className={styles.empty}>
      <div className={styles.icon}>📭</div>
      <p className={styles.message}>{message}</p>
    </div>
  );
}
