const { makeCrudHandler } = require("./_crud");

module.exports = makeCrudHandler(
  "Freelancers",
  ["freelancer_id", "name", "email", "avg_rating"],
  "freelancer_id"
);
