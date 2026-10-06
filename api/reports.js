const { getPool } = require("./_db");

const REPORTS = {
  "open-high-budget": {
    sql: `SELECT job_id, title, budget_max FROM Jobs WHERE status='Open' AND budget_max >= 70000`,
  },
  "active-freelancers": {
    sql: `SELECT freelancer_id, name FROM Freelancers
          WHERE freelancer_id IN (SELECT freelancer_id FROM Offers)`,
  },
  "completed-summary": {
    sql: `SELECT freelancer_id, COUNT(*) AS completed, SUM(final_amount) AS total_earned
          FROM Contracts WHERE status='Completed' GROUP BY freelancer_id`,
  },
};

module.exports = async function handler(req, res) {
  const type = req.query.type;
  const report = REPORTS[type];
  if (!report) {
    return res.status(400).json({ error: `Unknown report type: ${type}` });
  }
  try {
    const pool = getPool();
    const [rows] = await pool.query(report.sql);
    return res.status(200).json(rows);
  } catch (err) {
    return res.status(500).json({ error: err.message });
  }
};
