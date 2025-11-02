import { runTestScenario } from './common.js';

export const options = {
  stages: [
    { duration: '10s', target: 100 },
    { duration: '30s', target: 100 },
    { duration: '10s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<1500'],
    'checks': ['rate>0.9'],
  },
};

export default function () {
  runTestScenario();
}
