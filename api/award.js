const { getPool } = require("./_db");

/**
 * Runs the AwardOffer workflow for one offer.
 *   GET /api/award?offer_id=123  ->  CALL AwardOffer(123)
 *
 * The stored procedure (in a transaction) marks the offer Selected, rejects the
 * other Pending offers on the same job, creates the contract, and awards the
 * job. If it SIGNALs an error (offer not Pending, job not Open, …) the message
 * is returned so the UI can show it.
 */
module.exports = async function handler(req, res) {
  if (req.method !== "GET" && req.method !== "POST") {
    return res.status(405).json({ error: "Method not allowed" });
  }
  const offerId = req.query.offer_id;
  if (!offerId) {
    return res.status(400).json({ error: "Missing ?offer_id= for award." });
  }
  try {
    const pool = getPool();
    // Parameterized CALL — the offer id is never concatenated into SQL.
    await pool.query("CALL AwardOffer(?)", [offerId]);
    return res.status(200).json({ message: `Offer ${offerId} awarded — contract created and job set to Awarded.` });
  } catch (err) {
    return res.status(500).json({ error: err.message });
  }
};
