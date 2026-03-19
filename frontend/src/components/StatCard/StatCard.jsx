import styles from './StatCard.module.css';

export default function StatCard({ title, value, subtitle }) {
  return (
    <div className={styles.card}>
      <span className={styles.title}>{title}</span>
      <span className={styles.value}>{value}</span>
      {subtitle && <span className={styles.subtitle}>{subtitle}</span>}
    </div>
  );
}
