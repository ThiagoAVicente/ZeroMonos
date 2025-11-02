import { runTestScenario } from './common.js';

export const options = {
  stages: [
    { duration: '30s', target: 10 },
    { duration: '1m', target: 20 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<1000'],
    'checks': ['rate>0.9'],
  },
};

export default function () {
  runTestScenario();
}
