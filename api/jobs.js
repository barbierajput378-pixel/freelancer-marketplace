const { makeCrudHandler } = require("./_crud");

module.exports = makeCrudHandler(
  "Jobs",
  ["job_id", "client_id", "title", "budget_max", "status"],
  "job_id"
);
