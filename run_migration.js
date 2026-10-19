/**
 * Migration runner — applies migration.sql to your Aiven MySQL database.
 * Same style as run_setup.js. Run it once (it is safe to re-run).
 *
 * HOW TO RUN (PowerShell, all on one line so nothing secret is saved anywhere):
 *
 *   $env:DB_HOST="your-host"; $env:DB_PORT="12547"; $env:DB_USER="avnadmin"; $env:DB_PASSWORD="your-password"; node run_migration.js
 *
 * Credentials are read from environment variables only — nothing is hardcoded.
 */

const fs = require("fs");
const mysql = require("mysql2/promise");

// ---------------------------------------------------------------------
// Connection details come from environment variables, so this file holds
// no secrets and is safe to commit to git.
// ---------------------------------------------------------------------
const CONFIG = {
  host: process.env.DB_HOST,
  port: process.env.DB_PORT || 3306,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  // No "database" here — migration.sql selects it with USE itself.
  ssl: { rejectUnauthorized: false },
  multipleStatements: true,
};

if (!CONFIG.host || !CONFIG.user || !CONFIG.password) {
  console.error(
    "\n❌ Missing DB_HOST, DB_USER, or DB_PASSWORD environment variables.\n" +
    "See the instructions at the top of this file for how to set them.\n"
  );
  process.exit(1);
}

async function main() {
  console.log("Reading migration.sql ...");
  let sql = fs.readFileSync("./migration.sql", "utf8");

  // Strip DELIMITER lines (a mysql-CLI-only concept) and turn the custom
  // "$$" terminator back into a normal ";" for the Node driver.
  sql = sql
    .split("\n")
    .filter((line) => !line.trim().toUpperCase().startsWith("DELIMITER"))
    .join("\n");
  sql = sql.replace(/\$\$/g, ";");

  console.log("Connecting to Aiven MySQL ...");
  const conn = await mysql.createConnection(CONFIG);

  console.log("Running migration.sql (status defaults + CHECKs, AwardOffer, new triggers) ...");
  await conn.query(sql);

  console.log("\n✅ Migration applied. Verifying ...\n");

  await conn.query("USE FreelancerMarketplace");

  // Show the new column defaults.
  const [defaults] = await conn.query(
    `SELECT TABLE_NAME, COLUMN_DEFAULT
       FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = 'FreelancerMarketplace'
        AND COLUMN_NAME = 'status'
      ORDER BY TABLE_NAME`
  );
  console.log("  Status column defaults:");
  defaults.forEach((r) => console.log(`    ${r.TABLE_NAME}.status DEFAULT ${r.COLUMN_DEFAULT}`));

  // Show the new CHECK constraints.
  const [checks] = await conn.query(
    `SELECT CONSTRAINT_NAME
       FROM information_schema.TABLE_CONSTRAINTS
      WHERE CONSTRAINT_SCHEMA = 'FreelancerMarketplace'
        AND CONSTRAINT_TYPE = 'CHECK'
        AND CONSTRAINT_NAME IN ('chk_jobs_status','chk_offers_status','chk_contracts_status')
      ORDER BY CONSTRAINT_NAME`
  );
  console.log("  CHECK constraints present: " + checks.map((c) => c.CONSTRAINT_NAME).join(", "));

  // Confirm the procedure and triggers exist.
  const [procs] = await conn.query(
    `SELECT ROUTINE_NAME FROM information_schema.ROUTINES
      WHERE ROUTINE_SCHEMA = 'FreelancerMarketplace' AND ROUTINE_NAME = 'AwardOffer'`
  );
  console.log("  AwardOffer procedure present: " + (procs.length ? "yes" : "NO"));

  const [trigs] = await conn.query(
    `SELECT TRIGGER_NAME FROM information_schema.TRIGGERS
      WHERE TRIGGER_SCHEMA = 'FreelancerMarketplace'
        AND TRIGGER_NAME IN ('before_insert_offers','after_update_contracts')
      ORDER BY TRIGGER_NAME`
  );
  console.log("  New triggers present: " + trigs.map((t) => t.TRIGGER_NAME).join(", "));

  await conn.end();
  console.log("\nAll done. Your Aiven database now enforces the status workflow.");
}

main().catch((err) => {
  console.error("\n❌ Something went wrong:\n", err.message);
  process.exit(1);
});
