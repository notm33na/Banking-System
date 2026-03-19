import { useState, useEffect, useRef, useCallback } from 'react';
import { Bar, Line, Doughnut } from 'react-chartjs-2';
import {
  Chart as ChartJS, CategoryScale, LinearScale, BarElement, PointElement, LineElement,
  ArcElement, Title, Tooltip, Legend, Filler
} from 'chart.js';
import api from '../../api/axiosInstance';
import useDateRange from '../../hooks/useDateRange';
import Button from '../../components/Button/Button';
import Spinner from '../../components/Spinner/Spinner';
import EmptyState from '../../components/EmptyState/EmptyState';
import styles from '../Page.module.css';

ChartJS.register(CategoryScale, LinearScale, BarElement, PointElement, LineElement, ArcElement, Title, Tooltip, Legend, Filler);

const COLORS = ['#6366f1','#f59e0b','#22c55e','#ef4444','#3b82f6','#8b5cf6','#ec4899','#14b8a6','#f97316','#64748b'];

// Centre-text plugin for doughnut
const centerTextPlugin = {
  id: 'centerText',
  afterDraw(chart) {
    if (chart.config.type !== 'doughnut') return;
    const { ctx, width, height } = chart;
    const total = chart.data.datasets[0].data.reduce((a, b) => a + b, 0);
    ctx.save();
    ctx.font = 'bold 18px Inter, sans-serif';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillStyle = '#1e293b';
    ctx.fillText(`$${total.toLocaleString()}`, width / 2, height / 2);
    ctx.restore();
  },
};
ChartJS.register(centerTextPlugin);

export default function BusinessAnalytics() {
  const { from, to, setFrom, setTo } = useDateRange();
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState(null);

  // Refs for chart cleanup
  const barRef = useRef(null);
  const lineRef = useRef(null);
  const doughnutRef = useRef(null);

  const fetchAnalytics = useCallback(async () => {
    if (!from || !to) return;
    setLoading(true);
    try {
      const startIso = new Date(from).toISOString();
      const endIso = new Date(to).toISOString();
      const [summaryRes, cashflowRes, expensesRes] = await Promise.all([
        api.get('/business/analytics/income-expense', { params: { start: startIso, end: endIso } }),
        api.get('/business/analytics/cashflow', { params: { start: startIso, end: endIso } }),
        api.get('/business/analytics/top-expenses', { params: { start: startIso, end: endIso } }),
      ]);
      setData({
        summary: summaryRes.data.data,
        cashflow: cashflowRes.data.data || [],
        expenses: expensesRes.data.data || [],
      });
    } catch { setData(null); }
    setLoading(false);
  }, [from, to]);

  // Auto-fetch on date change
  useEffect(() => { fetchAnalytics(); }, [fetchAnalytics]);

  // Cleanup charts on unmount
  useEffect(() => {
    return () => {
      barRef.current?.destroy();
      lineRef.current?.destroy();
      doughnutRef.current?.destroy();
    };
  }, []);

  const barData = data?.cashflow?.length ? {
    labels: data.cashflow.map(c => `${c.year}-${String(c.month).padStart(2, '0')}`),
    datasets: [
      { label: 'Income', data: data.cashflow.map(c => c.income), backgroundColor: '#22c55e' },
      { label: 'Expenses', data: data.cashflow.map(c => c.expense), backgroundColor: '#ef4444' },
    ],
  } : null;

  const lineData = data?.cashflow?.length ? {
    labels: data.cashflow.map(c => `${c.year}-${String(c.month).padStart(2, '0')}`),
    datasets: [{
      label: 'Net Cashflow',
      data: data.cashflow.map(c => c.net),
      borderColor: '#6366f1',
      backgroundColor: (ctx) => {
        const values = ctx.dataset.data;
        return values.map(v => v >= 0 ? 'rgba(34,197,94,0.15)' : 'rgba(239,68,68,0.15)');
      },
      fill: true,
      tension: 0.3,
      pointBackgroundColor: data.cashflow.map(c => c.net >= 0 ? '#22c55e' : '#ef4444'),
    }],
  } : null;

  const doughnutData = data?.expenses?.length ? {
    labels: data.expenses.map(e => e.category),
    datasets: [{
      data: data.expenses.map(e => e.totalAmount),
      backgroundColor: COLORS.slice(0, data.expenses.length),
      borderWidth: 2,
      borderColor: '#fff',
    }],
  } : null;

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: true,
    plugins: { legend: { position: 'top', labels: { usePointStyle: true, padding: 12 } } },
  };

  return (
    <div>
      <div className={styles['page-header']}><h1>Analytics</h1></div>
      <div className={styles.filters}>
        <div className={styles['filter-group']}>
          <label>Start Date</label>
          <input type="date" value={from} onChange={e => setFrom(e.target.value)} />
        </div>
        <div className={styles['filter-group']}>
          <label>End Date</label>
          <input type="date" value={to} onChange={e => setTo(e.target.value)} />
        </div>
        <Button variant="primary" onClick={fetchAnalytics} style={{ alignSelf: 'flex-end' }}>Refresh</Button>
      </div>
      {loading && <Spinner />}
      {!loading && !data && <EmptyState message="Select a date range to view analytics" />}
      {!loading && data && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
          <div style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 'var(--radius-md)', padding: '1.25rem' }}>
            <h3 style={{ marginBottom: '1rem' }}>Income vs Expenses</h3>
            {barData ? <Bar ref={barRef} data={barData} options={chartOptions} /> : <EmptyState message="No data" />}
          </div>
          <div style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 'var(--radius-md)', padding: '1.25rem' }}>
            <h3 style={{ marginBottom: '1rem' }}>Cashflow Over Time</h3>
            {lineData ? <Line ref={lineRef} data={lineData} options={chartOptions} /> : <EmptyState message="No data" />}
          </div>
          <div style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: 'var(--radius-md)', padding: '1.25rem', gridColumn: '1 / -1', maxWidth: 480, margin: '0 auto', width: '100%' }}>
            <h3 style={{ marginBottom: '1rem', textAlign: 'center' }}>Expense Breakdown</h3>
            {doughnutData ? <Doughnut ref={doughnutRef} data={doughnutData} options={{ ...chartOptions, plugins: { ...chartOptions.plugins, legend: { position: 'bottom' } } }} />
              : <EmptyState message="No data" />}
          </div>
        </div>
      )}
    </div>
  );
}
