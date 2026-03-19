import styles from './Skeleton.module.css';

export function SkeletonCard() {
  return <div className={styles.skeletonCard} />;
}

export function SkeletonRow() {
  return <div className={styles.skeletonRow} />;
}

export function SkeletonText({ variant = 'full' }) {
  const cls = variant === 'short' ? styles.skeletonTextShort
            : variant === 'medium' ? styles.skeletonTextMedium
            : styles.skeletonText;
  return <div className={cls} />;
}

export function SkeletonChart() {
  return <div className={styles.skeletonChart} />;
}

export function DashboardSkeleton() {
  return (
    <div className={styles.dashboardSkeleton}>
      <div className={styles.skeletonGrid}>
        {[1, 2, 3, 4].map(i => <SkeletonCard key={i} />)}
      </div>
      <SkeletonChart />
    </div>
  );
}
