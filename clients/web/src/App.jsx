import { useEffect, useState } from "react";
import {
  acknowledgeAlert,
  clearAccessToken,
  createSalesImport,
  fetchAlerts,
  fetchCurrentSession,
  fetchDashboardOverview,
  fetchImport,
  fetchImports,
  fetchSalesSummary,
  getAccessToken,
  login,
  switchStore,
  setAccessToken
} from "./api";

const POLLABLE_STATES = new Set(["UPLOADED", "PROCESSING"]);

function formatMoney(value) {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD"
  }).format(Number(value ?? 0));
}

function formatDate(value) {
  return new Intl.DateTimeFormat("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric"
  }).format(new Date(value));
}

export default function App() {
  const [authState, setAuthState] = useState({
    token: getAccessToken(),
    username: "",
    password: "",
    currentStore: null,
    allowedStores: [],
    error: "",
    loading: false
  });
  const [dashboard, setDashboard] = useState({ dailyAggregates: [], openAlerts: [] });
  const [alerts, setAlerts] = useState([]);
  const [imports, setImports] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploadState, setUploadState] = useState({ loading: false, error: "", batch: null, summary: null });

  async function loadDashboard() {
    const [dashboardOverview, alertItems, importItems] = await Promise.all([
      fetchDashboardOverview(),
      fetchAlerts(),
      fetchImports()
    ]);
    setDashboard(dashboardOverview);
    setAlerts(alertItems);
    setImports(importItems);
  }

  useEffect(() => {
    if (!authState.token) {
      return;
    }

    Promise.all([fetchCurrentSession(), loadDashboard()])
      .then(([session]) => {
        setAuthState((current) => ({
          ...current,
          username: session.username,
          currentStore: session.currentStore,
          allowedStores: session.allowedStores,
          error: ""
        }));
      })
      .catch((error) => {
        if (error.status === 401) {
          clearAccessToken();
          setAuthState((current) => ({
            ...current,
            token: "",
            currentStore: null,
            allowedStores: [],
            error: "Session expired. Sign in again."
          }));
          return;
        }
        setUploadState((current) => ({ ...current, error: error.message }));
      });
  }, [authState.token]);

  useEffect(() => {
    if (!uploadState.batch || !POLLABLE_STATES.has(uploadState.batch.status)) {
      return undefined;
    }

    const intervalId = window.setInterval(async () => {
      try {
        const batch = await fetchImport(uploadState.batch.id);
        setUploadState((current) => ({ ...current, batch }));

        if (!POLLABLE_STATES.has(batch.status)) {
          window.clearInterval(intervalId);
          const summary = await fetchSalesSummary(batch.id);
          setUploadState((current) => ({ ...current, batch, summary }));
          await loadDashboard();
        }
      } catch (error) {
        window.clearInterval(intervalId);
        if (error.status === 401) {
          clearAccessToken();
          setAuthState((current) => ({ ...current, token: "", error: "Session expired. Sign in again." }));
          return;
        }
        setUploadState((current) => ({ ...current, error: error.message }));
      }
    }, 1500);

    return () => window.clearInterval(intervalId);
  }, [uploadState.batch]);

  async function handleUpload(event) {
    event.preventDefault();
    if (!selectedFile) {
      setUploadState((current) => ({ ...current, error: "Select a CSV file first." }));
      return;
    }

    setUploadState({ loading: true, error: "", batch: null, summary: null });
    try {
      const batch = await createSalesImport(selectedFile);
      setUploadState({ loading: false, error: "", batch, summary: null });
    } catch (error) {
      setUploadState({ loading: false, error: error.message, batch: null, summary: null });
    }
  }

  async function handleAcknowledge(alertId) {
    try {
      await acknowledgeAlert(alertId);
      await loadDashboard();
    } catch (error) {
      if (error.status === 401) {
        clearAccessToken();
        setAuthState((current) => ({ ...current, token: "", error: "Session expired. Sign in again." }));
        return;
      }
      setUploadState((current) => ({ ...current, error: error.message }));
    }
  }

  async function handleSwitchStore(event) {
    const storeId = event.target.value;
    if (!storeId || storeId === authState.currentStore?.id) {
      return;
    }

    try {
      const session = await switchStore(storeId);
      setAuthState((current) => ({
        ...current,
        currentStore: session.currentStore,
        allowedStores: session.allowedStores,
        error: ""
      }));
      setUploadState({ loading: false, error: "", batch: null, summary: null });
      await loadDashboard();
    } catch (error) {
      if (error.status === 401) {
        clearAccessToken();
        setAuthState((current) => ({ ...current, token: "", error: "Session expired. Sign in again." }));
        return;
      }
      setUploadState((current) => ({ ...current, error: error.message }));
    }
  }

  async function handleLogin(event) {
    event.preventDefault();
    setAuthState((current) => ({ ...current, loading: true, error: "" }));
    try {
      const response = await login(authState.username, authState.password);
      setAccessToken(response.accessToken);
      setAuthState((current) => ({
        ...current,
        token: response.accessToken,
        username: response.username,
        currentStore: response.currentStore,
        allowedStores: response.allowedStores,
        password: "",
        error: "",
        loading: false
      }));
    } catch (error) {
      setAuthState((current) => ({ ...current, loading: false, error: error.message }));
    }
  }

  function handleLogout() {
    clearAccessToken();
    setAuthState({
      token: "",
      username: authState.username,
      password: "",
      currentStore: null,
      allowedStores: [],
      error: "",
      loading: false
    });
    setDashboard({ dailyAggregates: [], openAlerts: [] });
    setAlerts([]);
    setUploadState({ loading: false, error: "", batch: null, summary: null });
  }

  if (!authState.token) {
    return (
      <div className="page-shell auth-shell">
        <section className="auth-panel">
          <p className="eyebrow">StorePulse Access</p>
          <h1>Sign In</h1>
          <p className="hero-copy">Authenticate before accessing imports, dashboards, and alerts.</p>
          <form className="upload-form" onSubmit={handleLogin}>
            <input
              className="auth-input"
              type="text"
              placeholder="Username"
              value={authState.username}
              onChange={(event) => setAuthState((current) => ({ ...current, username: event.target.value }))}
            />
            <input
              className="auth-input"
              type="password"
              placeholder="Password"
              value={authState.password}
              onChange={(event) => setAuthState((current) => ({ ...current, password: event.target.value }))}
            />
            <button className="primary-button" type="submit" disabled={authState.loading}>
              {authState.loading ? "Signing in..." : "Sign In"}
            </button>
          </form>
          {authState.error ? <p className="error-text">{authState.error}</p> : null}
        </section>
      </div>
    );
  }

  return (
    <div className="page-shell">
      <header className="hero">
        <div>
          <p className="eyebrow">Retail Sidecar</p>
          <h1>StorePulse Control Room</h1>
          <p className="hero-copy">
            Upload sales exports, monitor ingestion, and review dashboard signals without leaving the back-office flow.
          </p>
          {authState.currentStore ? (
            <p className="store-chip">
              Scoped to {authState.currentStore.name} ({authState.currentStore.code})
            </p>
          ) : null}
          {authState.allowedStores.length > 1 ? (
            <label className="store-switcher">
              <span>Store</span>
              <select value={authState.currentStore?.id ?? ""} onChange={handleSwitchStore}>
                {authState.allowedStores.map((store) => (
                  <option key={store.id} value={store.id}>
                    {store.name}
                  </option>
                ))}
              </select>
            </label>
          ) : null}
        </div>
        <div className="status-badge">
          <span>API-ready</span>
          <strong>{dashboard.dailyAggregates.length} daily snapshots</strong>
          <small className="status-copy">
            {authState.allowedStores.length} allowed store{authState.allowedStores.length === 1 ? "" : "s"}
          </small>
          <button className="logout-button" onClick={handleLogout}>
            Log Out
          </button>
        </div>
      </header>

      <main className="layout-grid">
        <section className="panel panel-accent">
          <div className="panel-header">
            <div>
              <p className="eyebrow">Imports</p>
              <h2>Upload Sales CSV</h2>
            </div>
          </div>

          <form className="upload-form" onSubmit={handleUpload}>
            <label className="upload-dropzone">
              <input
                type="file"
                accept=".csv"
                onChange={(event) => setSelectedFile(event.target.files?.[0] ?? null)}
              />
              <span>{selectedFile ? selectedFile.name : "Choose a CSV export"}</span>
            </label>
            <button className="primary-button" type="submit" disabled={uploadState.loading}>
              {uploadState.loading ? "Uploading..." : "Start Import"}
            </button>
          </form>

          {uploadState.error ? <p className="error-text">{uploadState.error}</p> : null}

          {uploadState.batch ? (
            <div className="batch-card">
              <div className="batch-row">
                <span>Status</span>
                <strong>{uploadState.batch.status}</strong>
              </div>
              <div className="batch-row">
                <span>Rows</span>
                <strong>{uploadState.batch.processedRows}</strong>
              </div>
              <div className="batch-row">
                <span>Valid</span>
                <strong>{uploadState.batch.successfulRows}</strong>
              </div>
              <div className="batch-row">
                <span>Invalid</span>
                <strong>{uploadState.batch.failedRows}</strong>
              </div>
              {uploadState.batch.errorMessage ? <p className="muted-text">{uploadState.batch.errorMessage}</p> : null}
            </div>
          ) : null}

          {uploadState.summary ? <SalesSummaryCard summary={uploadState.summary} /> : null}
        </section>

        <section className="panel">
          <div className="panel-header">
            <div>
              <p className="eyebrow">Dashboard</p>
              <h2>Daily Aggregates</h2>
            </div>
          </div>

          <div className="aggregate-list">
            {dashboard.dailyAggregates.map((aggregate) => (
              <article className="aggregate-card" key={aggregate.businessDate}>
                <div className="aggregate-topline">
                  <strong>{formatDate(aggregate.businessDate)}</strong>
                  <span>{formatMoney(aggregate.revenue)}</span>
                </div>
                <div className="aggregate-metrics">
                  <span>{aggregate.unitsSold} units</span>
                  <span>{aggregate.receipts} receipts</span>
                  <span>Avg {formatMoney(aggregate.averageBasket)}</span>
                </div>
                <p className="muted-text">
                  Top seller: {aggregate.topProductName ?? "n/a"}
                  {aggregate.topProductUnits ? ` (${aggregate.topProductUnits})` : ""}
                </p>
              </article>
            ))}
            {dashboard.dailyAggregates.length === 0 ? <EmptyState text="No daily aggregates yet." /> : null}
          </div>
        </section>

        <section className="panel">
          <div className="panel-header">
            <div>
              <p className="eyebrow">History</p>
              <h2>Import Batches</h2>
            </div>
          </div>

          <div className="aggregate-list">
            {imports.map((item) => (
              <article className="aggregate-card" key={item.id}>
                <div className="aggregate-topline">
                  <strong>{item.originalFileName}</strong>
                  <span>{item.status}</span>
                </div>
                <div className="aggregate-metrics">
                  <span>{formatDate(item.createdAt)}</span>
                  <span>{item.processedRows} rows</span>
                  <span>{item.successfulRows} valid</span>
                  <span>{item.failedRows} invalid</span>
                </div>
              </article>
            ))}
            {imports.length === 0 ? <EmptyState text="No imports for this store yet." /> : null}
          </div>
        </section>

        <section className="panel panel-wide">
          <div className="panel-header">
            <div>
              <p className="eyebrow">Alerts</p>
              <h2>Operational Signals</h2>
            </div>
          </div>

          <div className="alert-list">
            {alerts.map((alert) => (
              <article className={`alert-card severity-${alert.severity.toLowerCase()}`} key={alert.id}>
                <div>
                  <div className="alert-meta">
                    <span>{alert.type}</span>
                    <span>{alert.status}</span>
                  </div>
                  <p>{alert.message}</p>
                  <small>{formatDate(alert.businessDate)}</small>
                </div>
                {alert.status === "OPEN" ? (
                  <button className="secondary-button" onClick={() => handleAcknowledge(alert.id)}>
                    Acknowledge
                  </button>
                ) : null}
              </article>
            ))}
            {alerts.length === 0 ? <EmptyState text="No alerts yet." /> : null}
          </div>
        </section>
      </main>
    </div>
  );
}

function SalesSummaryCard({ summary }) {
  return (
    <div className="summary-card">
      <div className="summary-grid">
        <Metric label="Revenue" value={formatMoney(summary.revenue)} />
        <Metric label="Units Sold" value={summary.unitsSold} />
        <Metric label="Receipts" value={summary.receipts} />
        <Metric label="Avg Basket" value={formatMoney(summary.averageBasket)} />
      </div>
      <div className="mini-chart">
        {summary.hourlyDistribution.map((hour) => (
          <div className="mini-bar-wrap" key={hour.hour}>
            <div
              className="mini-bar"
              style={{ height: `${Math.max(8, Number(hour.revenue) * 10)}px` }}
              title={`${hour.hour}:00 - ${formatMoney(hour.revenue)}`}
            />
            <span>{hour.hour}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

function Metric({ label, value }) {
  return (
    <div className="metric">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function EmptyState({ text }) {
  return <p className="empty-state">{text}</p>;
}
