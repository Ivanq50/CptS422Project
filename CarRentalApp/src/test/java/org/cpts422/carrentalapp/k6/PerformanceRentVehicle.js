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
    let res = http.get(`${BASE_URL}/login`);
    check(res, { 'GET /login 200': (r) => r.status === 200 });

    res = http.post(
        `${BASE_URL}/login`,
        {
            username: USERNAME,
            password: PASSWORD,
        },
        {
            redirects: 0
        }
    );

    check(res, {
        'POST /login 302 redirect': (r) => r.status === 302,
    });

    res = http.get(`${BASE_URL}/vehicles`);
    check(res, {
        'GET /vehicles 200': (r) => r.status === 200,
    });

    const rentPayload = {
        vehicleId: 1,
        days: 1
    };

    res = http.post(
        `${BASE_URL}/rent`,
        rentPayload,
        { redirects: 0 }
    );

    check(res, {
        'rent < 1000ms': (r) => r.timings.duration < 1000,
    });

    sleep(1);
}
