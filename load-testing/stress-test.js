import { runTestScenario } from './common.js';

export const options = {
  stages: [
    { duration: '1m', target: 50 },
    { duration: '2m', target: 100 },
    { duration: '1m', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<1500'],
    'checks': ['rate>0.9'],
  },
};

export default function () {
  runTestScenario();
}
