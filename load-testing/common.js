import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomString, randomItem } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

export const BASE_URL = 'http://localhost:8080';
export const municipalities = ['Lisboa', 'Porto', 'Braga', 'Faro'];
export const users = ['Maria Silva', 'Carlos Santos'];

export function runTestScenario() {
  // Authenticate
  let payload = JSON.stringify({
    username: randomItem(users),
    password: 'maria123'
  });

  let res = http.post(`${BASE_URL}/users/authenticate`, payload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(res, {
    'POST /users/authenticate status ok': (r) => r.status === 200 || r.status === 401,
    'POST /users/authenticate response time < 500ms': (r) => r.timings.duration < 500,
  });

  sleep(1);

  // Get municipalities
  res = http.get(`${BASE_URL}/requests/municipalities`);
  check(res, {
    'GET /municipalities status 200': (r) => r.status === 200,
    'GET /municipalities response time < 300ms': (r) => r.timings.duration < 300,
    'GET /municipalities returns array': (r) => {
      try {
        const body = JSON.parse(r.body);
        return Array.isArray(body) && body.length > 0;
      } catch (e) {
        return false;
      }
    },
  });

  sleep(1);

  // Create service request
  payload = JSON.stringify({
    user: randomItem(users),
    municipality: randomItem(municipalities),
    requestedDate: '2026-12-25',
    timeSlot: '10:00',
    description: `Load test item ${randomString(8)}`
  });

  res = http.post(`${BASE_URL}/requests`, payload, {
    headers: { 'Content-Type': 'application/json' },
  });

  const createdToken = res.status === 200 ? JSON.parse(res.body).token : null;

  check(res, {
    'POST /requests status ok': (r) => r.status === 200 || r.status === 409 || r.status === 400,
    'POST /requests response time < 800ms': (r) => r.timings.duration < 800,
  });

  sleep(1);

  // Get requests by municipality
  res = http.get(`${BASE_URL}/requests?municipality=${randomItem(municipalities)}`);
  check(res, {
    'GET /requests by municipality status ok': (r) => r.status === 200 || r.status === 404,
    'GET /requests by municipality response time < 500ms': (r) => r.timings.duration < 500,
  });

  sleep(1);

  // Get requests by user
  res = http.get(`${BASE_URL}/requests/user/${encodeURIComponent(randomItem(users))}`);
  check(res, {
    'GET /requests/user status ok': (r) => r.status === 200 || r.status === 404,
    'GET /requests/user response time < 500ms': (r) => r.timings.duration < 500,
  });

  sleep(1);

  // If we created a token, test all token-based endpoints
  if (createdToken) {
    res = http.get(`${BASE_URL}/requests/${createdToken}`);
    check(res, {
      'GET /requests/{token} status 200': (r) => r.status === 200,
      'GET /requests/{token} response time < 400ms': (r) => r.timings.duration < 400,
    });

    sleep(1);

    res = http.get(`${BASE_URL}/requests/${createdToken}/history`);
    check(res, {
      'GET /requests/{token}/history status 200': (r) => r.status === 200,
      'GET /requests/{token}/history has data': (r) => {
        try {
          const body = JSON.parse(r.body);
          return Array.isArray(body);
        } catch (e) {
          return false;
        }
      },
    });

    sleep(1);

    payload = JSON.stringify('IN_PROGRESS');
    res = http.put(`${BASE_URL}/requests/${createdToken}/status`, payload, {
      headers: { 'Content-Type': 'application/json' },
    });
    check(res, {
      'PUT /requests/{token}/status status ok': (r) => r.status === 200 || r.status === 404 || r.status === 400,
    });

    sleep(1);

    res = http.del(`${BASE_URL}/requests/${createdToken}`);
    check(res, {
      'DELETE /requests/{token} status ok': (r) => r.status === 200 || r.status === 404 || r.status === 400,
    });
  }

  sleep(2);
}
