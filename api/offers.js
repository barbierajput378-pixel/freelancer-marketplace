const { makeCrudHandler } = require("./_crud");

module.exports = makeCrudHandler(
  "Offers",
  ["offer_id", "job_id", "freelancer_id", "offer_amount", "proposed_duration", "status"],
  "offer_id"
);
