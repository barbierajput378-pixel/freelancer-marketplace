const { makeCrudHandler } = require("./_crud");

module.exports = makeCrudHandler(
  "Clients",
  ["client_id", "name", "email", "company_name"],
  "client_id"
);
