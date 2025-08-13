import http from 'k6/http';
import {check, sleep} from 'k6';
import {Counter, Trend} from 'k6/metrics';

// ===== ENV
const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const CATEGORY = __ENV.CATEGORY || 'SHOES';
const USER_ID = __ENV.USER_ID || 'josh';
const DEBUG_FAIL = (__ENV.DEBUG_FAIL || '0') === '1';

// ===== Brand IDs
const BRAND_IDS = (__ENV.BRAND_IDS || '101,202,303,404,505')
    .split(',').map(s => s.trim()).filter(Boolean);

// ===== Common headers
const baseHeaders = { 'X-USER-ID': USER_ID };

// ===== Metrics
const uc1Trend = new Trend('uc1_duration');
const uc2Trend = new Trend('uc2_duration');
const uc3Trend = new Trend('uc3_duration');
const uc4Trend = new Trend('uc4_duration');
const httpStatus = new Counter('http_status'); // status별 집계

// ===== Options
export const options = {
    scenarios: {
        uc1: { executor:'ramping-arrival-rate', startRate:0, timeUnit:'1s',
            preAllocatedVUs:20, maxVUs:200,
            stages:[{duration:'10s',target:50},{duration:'40s',target:50},{duration:'10s',target:0}],
            exec:'runUC1', tags:{scenario:'uc1'}
        },
        uc2: { executor:'ramping-arrival-rate', startRate:0, timeUnit:'1s',
            preAllocatedVUs:20, maxVUs:200,
            stages:[{duration:'10s',target:50},{duration:'40s',target:50},{duration:'10s',target:0}],
            exec:'runUC2', tags:{scenario:'uc2'}, startTime:'1m10s'
        },
        uc3: { executor:'ramping-arrival-rate', startRate:0, timeUnit:'1s',
            preAllocatedVUs:20, maxVUs:200,
            stages:[{duration:'10s',target:50},{duration:'40s',target:50},{duration:'10s',target:0}],
            exec:'runUC3', tags:{scenario:'uc3'}, startTime:'2m20s'
        },
        uc4: { executor:'ramping-arrival-rate', startRate:0, timeUnit:'1s',
            preAllocatedVUs:20, maxVUs:200,
            stages:[{duration:'10s',target:50},{duration:'40s',target:50},{duration:'10s',target:0}],
            exec:'runUC4', tags:{scenario:'uc4'}, startTime:'3m30s'
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        'http_req_duration{scenario:uc1}': ['p(95)<200'],
        'http_req_duration{scenario:uc2}': ['p(95)<200'],
        'http_req_duration{scenario:uc3}': ['p(95)<200'],
        'http_req_duration{scenario:uc4}': ['p(95)<200'],
    },
};

// ===== Helpers
function pickBrandId() {
    const i = Math.floor(Math.random() * BRAND_IDS.length);
    return BRAND_IDS[i];
}

// 실패 로깅 포함한 공통 GET
function getWithLog(url, { name, scenario }) {
    const res = http.get(url, {
        headers: baseHeaders,
        tags: { name, scenario },
        // 디버깅을 위해 본문 일부 확인하고 싶으면 주석 해제
        // responseType: 'text',
    });

    // 상태코드 집계 (태그로 구분 가능)
    httpStatus.add(1, { status: String(res.status), scenario });

    if (DEBUG_FAIL && (res.error || res.status >= 400)) {
        const bodySnippet = (res.body && typeof res.body === 'string')
            ? res.body.substring(0, 200).replace(/\s+/g, ' ')
            : '';
        console.error(`[${scenario}/${name}] ${res.status} ${url} ${res.error ? 'ERR:'+res.error : ''} ${bodySnippet}`);
    }

    return res;
}

// ===== UC funcs
export function runUC1() {
    const brandId = pickBrandId();
    const url = `${BASE}/api/v1/products?brandId=${brandId}&categoryName=${encodeURIComponent(CATEGORY)}&sort=like_desc&page=0&size=20`;
    const res = getWithLog(url, { name: 'UC1_like_with_category', scenario: 'uc1' });
    uc1Trend.add(res.timings.duration);
    check(res, { 'UC1: 200': (r) => r.status === 200 });
    sleep(0.2);
}

export function runUC2() {
    const brandId = pickBrandId();
    const url = `${BASE}/api/v1/products?brandId=${brandId}&categoryName=${encodeURIComponent(CATEGORY)}&sort=price_asc&page=0&size=20`;
    const res = getWithLog(url, { name: 'UC2_price_with_category', scenario: 'uc2' });
    uc2Trend.add(res.timings.duration);
    check(res, { 'UC2: 200': (r) => r.status === 200 });
    sleep(0.2);
}

export function runUC3() {
    const brandId = pickBrandId();
    const url = `${BASE}/api/v1/products?brandId=${brandId}&sort=like_desc&page=0&size=20`;
    const res = getWithLog(url, { name: 'UC3_like_no_category', scenario: 'uc3' });
    uc3Trend.add(res.timings.duration);
    check(res, { 'UC3: 200': (r) => r.status === 200 });
    sleep(0.2);
}

export function runUC4() {
    const brandId = pickBrandId();
    const url = `${BASE}/api/v1/products?brandId=${brandId}&sort=price_asc&page=0&size=20`;
    const res = getWithLog(url, { name: 'UC4_price_no_category', scenario: 'uc4' });
    uc4Trend.add(res.timings.duration);
    check(res, { 'UC4: 200': (r) => r.status === 200 });
    sleep(0.2);
}

// ===== Summary
export function handleSummary(data) {
    return { 'k6-summary.json': JSON.stringify(data, null, 2) };
}

// ==== 실행 명령어
/*
k6 run \
  -e BASE_URL=http://localhost:8080 \
  -e USER_ID=josh \
  -e CATEGORY=SHOES \
  -e BRAND_IDS=21,34,55,89,144 \
  -e DEBUG_FAIL=1 \
  product-list-bench.k6.js
 */