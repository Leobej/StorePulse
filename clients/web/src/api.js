const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const TOKEN_STORAGE_KEY = "storepulse.accessToken";

async function parseResponse(response) {
  if (response.status === 204) {
    return null;
  }

  const contentType = response.headers.get("content-type") ?? "";
  const body = contentType.includes("application/json")
    ? await response.json()
    : await response.text();

  if (!response.ok) {
    const message = typeof body === "string" ? body : body.message ?? "Request failed";
    const error = new Error(message);
    error.status = response.status;
    throw error;
  }

  return body;
}

function withAuthHeaders(headers = {}) {
  const token = getAccessToken();
  return token ? { ...headers, Authorization: `Bearer ${token}` } : headers;
}

export function getAccessToken() {
  return window.localStorage.getItem(TOKEN_STORAGE_KEY);
}

export function setAccessToken(token) {
  window.localStorage.setItem(TOKEN_STORAGE_KEY, token);
}

export function clearAccessToken() {
  window.localStorage.removeItem(TOKEN_STORAGE_KEY);
}

export async function login(username, password) {
  const response = await fetch(`${API_BASE_URL}/auth/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ username, password })
  });
  return parseResponse(response);
}

export async function fetchCurrentSession() {
  const response = await fetch(`${API_BASE_URL}/auth/me`, {
    headers: withAuthHeaders()
  });
  return parseResponse(response);
}

export async function switchStore(storeId) {
  const response = await fetch(`${API_BASE_URL}/auth/switch-store`, {
    method: "POST",
    headers: withAuthHeaders({
      "Content-Type": "application/json"
    }),
    body: JSON.stringify({ storeId })
  });
  return parseResponse(response);
}

export async function fetchDashboardOverview() {
  const response = await fetch(`${API_BASE_URL}/dashboard/overview`, {
    headers: withAuthHeaders()
  });
  return parseResponse(response);
}

export async function fetchAlerts() {
  const response = await fetch(`${API_BASE_URL}/alerts`, {
    headers: withAuthHeaders()
  });
  return parseResponse(response);
}

export async function acknowledgeAlert(alertId) {
  const response = await fetch(`${API_BASE_URL}/alerts/${alertId}/acknowledge`, {
    method: "POST",
    headers: withAuthHeaders()
  });
  return parseResponse(response);
}

export async function createSalesImport(file) {
  const formData = new FormData();
  formData.append("file", file);

  const response = await fetch(`${API_BASE_URL}/imports/sales`, {
    method: "POST",
    headers: withAuthHeaders(),
    body: formData
  });
  return parseResponse(response);
}

export async function fetchImport(batchId) {
  const response = await fetch(`${API_BASE_URL}/imports/${batchId}`, {
    headers: withAuthHeaders()
  });
  return parseResponse(response);
}

export async function fetchImports() {
  const response = await fetch(`${API_BASE_URL}/imports`, {
    headers: withAuthHeaders()
  });
  return parseResponse(response);
}

export async function fetchSalesSummary(batchId) {
  const response = await fetch(`${API_BASE_URL}/analytics/sales-summary?batchId=${batchId}`, {
    headers: withAuthHeaders()
  });
  return parseResponse(response);
}
