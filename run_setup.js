/**
 * One-time setup runner — loads setup.sql into your Aiven MySQL database.
 * Run this locally once. It does NOT need MySQL Workbench at all.
 *
 * HOW TO RUN:
 *   1. Open a terminal in this "freelancer-web" folder.
 *   2. Run:  npm install
 *   3. Set your Aiven credentials as environment variables for just this
 *      one command (so nothing secret ever gets saved into a file that
 *      could be committed to git). In PowerShell, run it all as one line:
 *
 *      $env:DB_HOST="your-host"; $env:DB_PORT="12547"; $env:DB_USER="avnadmin"; $env:DB_PASSWORD="your-password"; node run_setup.js
 *
 *   4. Watch the terminal — it will print each statement as it runs,
 *      and a final success message when done.
 */

const fs = require("fs");
const mysql = require("mysql2/promise");

// ---------------------------------------------------------------------
// Reads connection details from environment variables — nothing secret
// is hardcoded here, so this file is safe to commit to git.
// ---------------------------------------------------------------------
const CONFIG = {
  host: process.env.DB_HOST,
  port: process.env.DB_PORT || 3306,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  // no "database" here on purpose — setup.sql creates it itself
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
  console.log("Reading setup.sql ...");
  let sql = fs.readFileSync("./setup.sql", "utf8");

  // Strip out DELIMITER commands — those are a mysql-CLI-only concept.
  // The Node driver doesn't need them; we just swap the custom "$$"
  // terminator back to a normal ";" for procedures/functions/triggers.
  sql = sql
    .split("\n")
    .filter((line) => !line.trim().toUpperCase().startsWith("DELIMITER"))
    .join("\n");
  sql = sql.replace(/\$\$/g, ";");

  console.log("Connecting to Aiven MySQL ...");
  const conn = await mysql.createConnection(CONFIG);

  console.log("Running setup.sql (this creates the database, tables, data, procedures, functions, and triggers) ...");
  await conn.query(sql);

  console.log("\n✅ Done! Verifying by counting rows in each table:\n");

  await conn.query("USE FreelancerMarketplace");
  const tables = ["Clients", "Freelancers", "Jobs", "Offers", "Contracts"];
  for (const t of tables) {
    const [rows] = await conn.query(`SELECT COUNT(*) AS count FROM ${t}`);
    console.log(`  ${t}: ${rows[0].count} rows`);
  }

  await conn.end();
  console.log("\nAll done. Your Aiven database is ready.");
}

main().catch((err) => {
  console.error("\n❌ Something went wrong:\n", err.message);
  process.exit(1);
});