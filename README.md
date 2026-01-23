# Tany Rest API

## Frontend Integration Guide: Passwordless OAuth2 with PKCE

This API supports a passwordless authentication flow using "Magic Links" combined with the standard OAuth2 Authorization Code flow with PKCE (Proof Key for Code Exchange). This ensures secure authentication for public clients (like Single Page Applications) without requiring users to remember passwords.

### Overview

1.  **Request Magic Link**: User enters email. Backend sends an email with a unique link.
2.  **Handle Link**: User clicks the link, which opens the Frontend.
3.  **Initiate OAuth2**: Frontend generates PKCE challenges and redirects to the Backend's Authorization Endpoint, attaching the token from the link.
4.  **Exchange Code**: Frontend receives an Authorization Code and exchanges it for an Access Token (JWT).

### Step-by-Step Implementation

#### 1. Request Magic Link

**Endpoint:** `POST /auth/magic-link/request`

**Parameters:**
*   `email`: The user's email address.

**React Logic:**
```javascript
const requestMagicLink = async (email) => {
  await fetch(`${API_BASE_URL}/auth/magic-link/request?email=${email}`, {
    method: 'POST'
  });
  alert("Check your email for the magic link!");
};
```

#### 2. Handle Magic Link (Frontend Route)

The email will contain a link like:
`https://your-frontend.com/magic-link?token=UNIQUE_TOKEN_UUID`

Create a route in your React app (e.g., `/magic-link`) to handle this.

#### 3. Initiate PKCE Flow

When the user lands on `/magic-link`, extract the `token` and initiate the OAuth2 flow.

**Requirements:**
*   Generate `code_verifier`: A high-entropy cryptographic random string (43-128 chars).
*   Generate `code_challenge`: SHA-256 hash of the verifier, Base64URL encoded.

**React Logic:**
```javascript
// Helper to generate PKCE values (use a library like 'oauth4webapi' or 'crypto-js' in production)
async function generatePKCE() {
  const array = new Uint32Array(56/2);
  window.crypto.getRandomValues(array);
  const verifier = Array.from(array, dec => ('0' + dec.toString(16)).substr(-2)).join('');

  const encoder = new TextEncoder();
  const data = encoder.encode(verifier);
  const hash = await window.crypto.subtle.digest('SHA-256', data);
  const challenge = btoa(String.fromCharCode(...new Uint8Array(hash)))
    .replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');

  return { verifier, challenge };
}

const handleMagicLink = async () => {
  const urlParams = new URLSearchParams(window.location.search);
  const magicToken = urlParams.get('token');

  if (!magicToken) return;

  const { verifier, challenge } = await generatePKCE();

  // Store verifier for the final step (localStorage/sessionStorage)
  localStorage.setItem('pkce_verifier', verifier);

  // Construct Authorization URL
  const authUrl = new URL(`${API_BASE_URL}/oauth2/authorize`);
  authUrl.searchParams.append('response_type', 'code');
  authUrl.searchParams.append('client_id', 'public-client');
  authUrl.searchParams.append('redirect_uri', 'http://localhost:3000'); // Your Callback URL
  authUrl.searchParams.append('scope', 'openid profile');
  authUrl.searchParams.append('code_challenge', challenge);
  authUrl.searchParams.append('code_challenge_method', 'S256');

  // CRITICAL: Attach the magic link token
  authUrl.searchParams.append('token', magicToken);

  // Redirect
  window.location.href = authUrl.toString();
};
```

#### 4. Handle Callback & Exchange Code

The Backend will authenticate the user using the `token` and redirect back to your `redirect_uri` with an authorization `code`.

`http://localhost:3000?code=AUTHORIZATION_CODE`

**React Logic:**
```javascript
const handleCallback = async () => {
  const urlParams = new URLSearchParams(window.location.search);
  const code = urlParams.get('code');
  const verifier = localStorage.getItem('pkce_verifier');

  if (code && verifier) {
    const response = await fetch(`${API_BASE_URL}/oauth2/token`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({
        grant_type: 'authorization_code',
        client_id: 'public-client',
        redirect_uri: 'http://localhost:3000',
        code: code,
        code_verifier: verifier
      })
    });

    const data = await response.json();
    const accessToken = data.access_token;

    // Store token securely
    localStorage.setItem('access_token', accessToken);

    // Clean up
    localStorage.removeItem('pkce_verifier');

    // Redirect to dashboard/home
    window.location.href = '/dashboard';
  }
};
```

### 5. Authenticated Requests

Include the token in headers:
`Authorization: Bearer <access_token>`
