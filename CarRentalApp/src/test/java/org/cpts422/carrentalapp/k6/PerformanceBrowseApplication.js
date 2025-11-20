import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 10,
    duration: '30s',
};

const BASE_URL = 'http://localhost:8080';

export default function () {
    // Home page
    let res = http.get(`${BASE_URL}/`);
    check(res, {
        'GET / status is 200': (r) => r.status === 200,
        'GET / < 500ms': (r) => r.timings.duration < 500,
    });

    // Login page
    res = http.get(`${BASE_URL}/login`);
    check(res, {
        'GET /login status is 200': (r) => r.status === 200,
        'GET /login < 500ms': (r) => r.timings.duration < 500,
    });

    // Register page
    res = http.get(`${BASE_URL}/register`);
    check(res, {
        'GET /register status is 200': (r) => r.status === 200,
        'GET /register < 500ms': (r) => r.timings.duration < 500,
    });

    // Vehicles page
    res = http.get(`${BASE_URL}/vehicles`);
    check(res, {
        'GET /vehicles status is 200': (r) => r.status === 200,
        'GET /vehicles < 500ms': (r) => r.timings.duration < 500,
    });

    // My rentals
    res = http.get(`${BASE_URL}/my-rentals`);
    check(res, {
        'GET /my-rentals is 200 or 302': (r) => r.status === 200 || r.status === 302,
        'GET /my-rentals < 800ms': (r) => r.timings.duration < 800,
    });

    sleep(1);
}