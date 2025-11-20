import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 10,
    duration: '30s',
};

const BASE_URL = 'http://localhost:8080';

export default function () {
    const randomSuffix = Math.floor(Math.random() * 1e9);
    const username = `perfuser_${randomSuffix}`;

    const payload = {
        username: username,
        password: 'TestPass123!',
        confirmPassword: 'TestPass123!',
        age: 25,
        driversLicenseNumber: `DL${randomSuffix}`,
        driversLicenseExpiry: '2030-01-01',
        membershipType: 'STANDARD',
        walletBalance: 100.0
    };

    const res = http.post(`${BASE_URL}/register`, payload);

    check(res, {
        'POST /register status 200': (r) => r.status === 200,
        'register response < 800ms': (r) => r.timings.duration < 800,
    });

    sleep(1);
}
