import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 10,
    duration: '30s',
};

const BASE_URL = 'http://localhost:8080';

const USERNAME = 'testuser';
const PASSWORD = 'securePassword123';

export default function () {
    // Simulate loading Login form
    let res = http.get(`${BASE_URL}/login`);
    check(res, {
        'GET /login 200': (r) => r.status === 200,
    });

    // Log user in
    res = http.post(
        `${BASE_URL}/login`,
        {
            username: USERNAME,
            password: PASSWORD,
        },
        {
            redirects: 0,
        }
    );

    check(res, {
        'POST /login 302 redirect': (r) => r.status === 302,
    });

    // Each user can check vehicles after login
    res = http.get(`${BASE_URL}/vehicles`);
    check(res, {
        'GET /vehicles after login 200': (r) => r.status === 200,
        'vehicles < 500ms': (r) => r.timings.duration < 500,
    });

    sleep(1);
}