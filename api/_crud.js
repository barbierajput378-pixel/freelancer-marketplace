const { getPool } = require("./_db");

/**
 * Builds a Vercel serverless function handler that does full CRUD
 * for a single table.
 *   GET            -> list all rows
 *   POST           -> insert a row (body = {col: value, ...})
 *   PUT  ?pk=value -> update the row where primaryKey = pk (body = other cols)
 *   DELETE ?pk=value -> delete the row where primaryKey = pk
 */
function makeCrudHandler(tableName, columns, primaryKey) {
  return async function handler(req, res) {
    const pool = getPool();
    try {
      if (req.method === "GET") {
        const [rows] = await pool.query(
          `SELECT ${columns.join(", ")} FROM ${tableName}`
        );
        return res.status(200).json(rows);
      }

      if (req.method === "POST") {
        const body = req.body;
        const values = columns.map((c) => body[c]);
        const placeholders = columns.map(() => "?").join(", ");
        await pool.query(
          `INSERT INTO ${tableName} (${columns.join(", ")}) VALUES (${placeholders})`,
          values
        );
        return res.status(201).json({ message: `${tableName} record added.` });
      }

      if (req.method === "PUT") {
        const pkValue = req.query.pk;
        if (!pkValue) {
          return res.status(400).json({ error: `Missing ?pk= for update.` });
        }
        const body = req.body;
        const updateCols = columns.filter((c) => c !== primaryKey);
        const setClause = updateCols.map((c) => `${c} = ?`).join(", ");
        const values = updateCols.map((c) => body[c]);
        values.push(pkValue);
        const [result] = await pool.query(
          `UPDATE ${tableName} SET ${setClause} WHERE ${primaryKey} = ?`,
          values
        );
        return res.status(200).json({ affectedRows: result.affectedRows });
      }

      if (req.method === "DELETE") {
        const pkValue = req.query.pk;
        if (!pkValue) {
          return res.status(400).json({ error: `Missing ?pk= for delete.` });
        }
        const [result] = await pool.query(
          `DELETE FROM ${tableName} WHERE ${primaryKey} = ?`,
          [pkValue]
        );
        return res.status(200).json({ affectedRows: result.affectedRows });
      }

      return res.status(405).json({ error: "Method not allowed" });
    } catch (err) {
      return res.status(500).json({ error: err.message });
    }
  };
}

module.exports = { makeCrudHandler };
