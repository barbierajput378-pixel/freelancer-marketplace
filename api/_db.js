// Shared MySQL connection pool, reused across all serverless functions.
// Reads connection details from Vercel Environment Variables.
const mysql = require("mysql2/promise");

let pool;

function getPool() {
  if (!pool) {
    pool = mysql.createPool({
      host: process.env.DB_HOST,
      port: process.env.DB_PORT || 3306,
      user: process.env.DB_USER,
      password: process.env.DB_PASSWORD,
      database: process.env.DB_NAME,
      // rejectUnauthorized: false -> connection is still encrypted (TLS),
      // it just doesn't verify Aiven's certificate chain since we're not
      // using the downloaded ca.pem. Fine for a student project; tighten
      // later by setting ca: fs.readFileSync('ca.pem') if you get the cert.
      ssl: process.env.DB_SSL === "true" ? { rejectUnauthorized: false } : undefined,
      waitForConnections: true,
      connectionLimit: 5,
    });
  }
  return pool;
}

module.exports = { getPool };
