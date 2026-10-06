const { makeCrudHandler } = require("./_crud");

module.exports = makeCrudHandler(
  "Contracts",
  ["contract_id", "job_id", "freelancer_id", "final_amount", "status"],
  "contract_id"
);
