const express = require('express');
const axios   = require('axios');
const cors    = require('cors');

const app  = express();
const PORT = process.env.PORT || 3001;
const SPRING_BASE = process.env.SPRING_BASE || 'https://kokkiligadda.onrender.com/api/family-members';

// ── Middleware ────────────────────────────────────────────────────────────────
app.use(cors({
  origin: '*',
  methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization', 'Accept', 'Origin', 'X-Requested-With'],
  credentials: false
}));
app.use(express.json());

// ── Helper ────────────────────────────────────────────────────────────────────
function handleError(err, res) {
  if (err.response) {
    return res.status(err.response.status).json(err.response.data);
  }
  console.error(err.message);
  return res.status(500).json({ message: 'Internal server error', detail: err.message });
}

// ════════════════════════════════════════════════════════════════════════════
// TREE NAVIGATION  (must be before generic /:id routes)
// ════════════════════════════════════════════════════════════════════════════

/**
 * GET /api/family-members/tree/root
 * Get the root node with immediate children.
 * Returns 204 if no root exists — frontend should prompt user to create one.
 */
app.get('/api/family-members/tree/root', async (req, res) => {
  try {
    const response = await axios.get(`${SPRING_BASE}/tree/root`);
    if (response.status === 204) return res.status(204).send();
    res.json(response.data);
  } catch (err) { handleError(err, res); }
});

/**
 * POST /api/family-members/tree/root
 * Create the root node when none exists.
 * Required: firstName, lastName
 * Optional: name, gender, gotram, type, birthDate, isLate, dod,
 *           placeOfBirth, occupation,
 *           contact: { phoneNumber, email },
 *           documents: { aadhaar, pan },
 *           marriages: [...]
 */
app.post('/api/family-members/tree/root', async (req, res) => {
  try {
    const { data } = await axios.post(`${SPRING_BASE}/tree/root`, req.body);
    res.status(201).json(data);
  } catch (err) { handleError(err, res); }
});

/**
 * GET /api/family-members/tree/full
 * Get the ENTIRE family tree recursively from root.
 * Returns deeply nested tree. Best for small/medium trees.
 */
app.get('/api/family-members/tree/full', async (req, res) => {
  try {
    const { data } = await axios.get(`${SPRING_BASE}/tree/full`);
    res.json(data);
  } catch (err) { handleError(err, res); }
});

/**
 * GET /api/family-members/tree/search?q=Ravi
 * Search members by name (case-insensitive partial match).
 */
app.get('/api/family-members/tree/search', async (req, res) => {
  try {
    const { data } = await axios.get(`${SPRING_BASE}/tree/search`, { params: { q: req.query.q } });
    res.json(data);
  } catch (err) { handleError(err, res); }
});

/**
 * GET /api/family-members/tree/subtree/:id
 * Get the full subtree rooted at a specific node (recursive).
 */
app.get('/api/family-members/tree/subtree/:id', async (req, res) => {
  try {
    const { data } = await axios.get(`${SPRING_BASE}/tree/subtree/${req.params.id}`);
    res.json(data);
  } catch (err) { handleError(err, res); }
});

/**
 * GET /api/family-members/tree/ancestors/:id
 * Get ancestor chain: node → parent → grandparent → ... → root.
 */
app.get('/api/family-members/tree/ancestors/:id', async (req, res) => {
  try {
    const { data } = await axios.get(`${SPRING_BASE}/tree/ancestors/${req.params.id}`);
    res.json(data);
  } catch (err) { handleError(err, res); }
});

/**
 * GET /api/family-members/tree/path/:id
 * Get breadcrumb path from root down to a node.
 * Ordered: root → ... → parent → node.
 */
app.get('/api/family-members/tree/path/:id', async (req, res) => {
  try {
    const { data } = await axios.get(`${SPRING_BASE}/tree/path/${req.params.id}`);
    res.json(data);
  } catch (err) { handleError(err, res); }
});

/**
 * GET /api/family-members/tree/node/:id/children
 * Lazy-load direct children of a node.
 * Each child includes hasChildren flag for expand/collapse UI.
 */
app.get('/api/family-members/tree/node/:id/children', async (req, res) => {
  try {
    const { data } = await axios.get(`${SPRING_BASE}/tree/node/${req.params.id}/children`);
    res.json(data);
  } catch (err) { handleError(err, res); }
});

/**
 * GET /api/family-members/tree/node/:id
 * Get a specific node with its immediate children populated.
 */
app.get('/api/family-members/tree/node/:id', async (req, res) => {
  try {
    const { data } = await axios.get(`${SPRING_BASE}/tree/node/${req.params.id}`);
    res.json(data);
  } catch (err) { handleError(err, res); }
});

// ════════════════════════════════════════════════════════════════════════════
// CRUD — Family Members
// ════════════════════════════════════════════════════════════════════════════

/**
 * POST /api/family-members
 * Create a new family member.
 * Required: firstName, lastName
 * Schema:
 * {
 *   "firstName": "Ravi",
 *   "lastName": "Kokkiligadda",
 *   "name": "Kokkiligadda Ravi",          // optional, auto-built if omitted
 *   "gender": "MALE",                      // MALE / FEMALE / OTHER
 *   "type": "BLOOD",                       // BLOOD / ADOPTED / STEP / IN_LAW
 *   "gotram": "Kasyapa",
 *   "birthDate": "1970-06-15",             // replaces old dateOfBirth
 *   "isLate": false,
 *   "dod": null,
 *   "placeOfBirth": "Andhra Pradesh",
 *   "occupation": "Farmer",
 *   "contact": { "phoneNumber": "9876543210", "email": "ravi@example.com" },
 *   "documents": { "aadhaar": "1234-5678-9012", "pan": "ABCDE1234F" },
 *   "fatherId": null,
 *   "motherId": null,
 *   "marriages": [],
 *   "childrenIds": []
 * }
 */
app.post('/api/family-members', async (req, res) => {
  try {
    const { data } = await axios.post(SPRING_BASE, req.body);
    res.status(201).json(data);
  } catch (err) { handleError(err, res); }
});

/**
 * GET /api/family-members
 * Get all family members.
 * Optional query: ?lastName=Kokkiligadda
 */
app.get('/api/family-members', async (req, res) => {
  try {
    const params = req.query.lastName ? { lastName: req.query.lastName } : {};
    const { data } = await axios.get(SPRING_BASE, { params });
    res.json(data);
  } catch (err) { handleError(err, res); }
});

/**
 * GET /api/family-members/:id
 * Get a single family member by ID.
 */
app.get('/api/family-members/:id', async (req, res) => {
  try {
    const { data } = await axios.get(`${SPRING_BASE}/${req.params.id}`);
    res.json(data);
  } catch (err) { handleError(err, res); }
});

/**
 * PUT /api/family-members/:id
 * Full replacement update of a family member.
 */
app.put('/api/family-members/:id', async (req, res) => {
  try {
    const { data } = await axios.put(`${SPRING_BASE}/${req.params.id}`, req.body);
    res.json(data);
  } catch (err) { handleError(err, res); }
});

/**
 * PATCH /api/family-members/:id
 * Partial update — only fields in the body are changed.
 * New schema fields:
 *   birthDate (replaces dateOfBirth),
 *   contact: { phoneNumber, email },
 *   documents: { aadhaar, pan },
 *   marriages: [...]
 */
app.patch('/api/family-members/:id', async (req, res) => {
  try {
    const { data } = await axios.patch(`${SPRING_BASE}/${req.params.id}`, req.body);
    res.json(data);
  } catch (err) { handleError(err, res); }
});

/**
 * DELETE /api/family-members/:id
 * Delete a family member.
 */
app.delete('/api/family-members/:id', async (req, res) => {
  try {
    await axios.delete(`${SPRING_BASE}/${req.params.id}`);
    res.sendStatus(204);
  } catch (err) { handleError(err, res); }
});

// ════════════════════════════════════════════════════════════════════════════
// CHILDREN
// ════════════════════════════════════════════════════════════════════════════

/**
 * GET /api/family-members/:id/children
 * Get all children of a member.
 */
app.get('/api/family-members/:id/children', async (req, res) => {
  try {
    const { data } = await axios.get(`${SPRING_BASE}/${req.params.id}/children`);
    res.json(data);
  } catch (err) { handleError(err, res); }
});

/**
 * POST /api/family-members/:parentId/children/:childId
 * Link an existing member as a child of a parent.
 * Auto-sets fatherId/motherId on child and updates parent's childrenIds.
 * No body required — IDs are in the URL.
 */
app.post('/api/family-members/:parentId/children/:childId', async (req, res) => {
  try {
    const { data } = await axios.post(
      `${SPRING_BASE}/${req.params.parentId}/children/${req.params.childId}`
    );
    res.json(data);
  } catch (err) { handleError(err, res); }
});

/**
 * DELETE /api/family-members/:parentId/children/:childId
 * Remove the child link from a parent.
 */
app.delete('/api/family-members/:parentId/children/:childId', async (req, res) => {
  try {
    const { data } = await axios.delete(
      `${SPRING_BASE}/${req.params.parentId}/children/${req.params.childId}`
    );
    res.json(data);
  } catch (err) { handleError(err, res); }
});

// ════════════════════════════════════════════════════════════════════════════
// MARRIAGES  (replaces old /spouses routes)
// New Marriage schema:
// {
//   "partnerId": "abc123",           <- optional, if partner is a registered member
//   "partnerName": "Lakshmi Devi",
//   "relationshipType": "MARRIED",   <- MARRIED / PARTNER / DIVORCED / WIDOWED
//   "marriageStartDate": "1968-02-14",
//   "marriageEndDate": null,
//   "divorced": false,
//   "widowed": false,
//   "order": 1,                      <- 1=first, 2=second marriage etc.
//   "isLate": false,
//   "dod": null,
//   "gotram": "Bharadwaja",
//   "documents": { "aadhaar": "...", "pan": "..." }
// }
// NOTE: routes with fixed path (/marriages) must come before (/marriages/:index)
// ════════════════════════════════════════════════════════════════════════════

/**
 * GET /api/family-members/:id/marriages
 * Get all marriages/partnerships of a member.
 */
app.get('/api/family-members/:id/marriages', async (req, res) => {
  try {
    const { data } = await axios.get(`${SPRING_BASE}/${req.params.id}/marriages`);
    res.json(data);
  } catch (err) { handleError(err, res); }
});

/**
 * POST /api/family-members/:id/marriages
 * Add a new marriage/partnership to a member.
 * If partnerId refers to a registered member, reverse link is auto-set.
 */
app.post('/api/family-members/:id/marriages', async (req, res) => {
  try {
    const { data } = await axios.post(
      `${SPRING_BASE}/${req.params.id}/marriages`, req.body
    );
    res.status(201).json(data);
  } catch (err) { handleError(err, res); }
});

/**
 * PUT /api/family-members/:id/marriages
 * Replace the entire marriages list at once.
 * Body: [ { ...marriage }, ... ]
 */
app.put('/api/family-members/:id/marriages', async (req, res) => {
  try {
    const { data } = await axios.put(
      `${SPRING_BASE}/${req.params.id}/marriages`, req.body
    );
    res.json(data);
  } catch (err) { handleError(err, res); }
});

/**
 * DELETE /api/family-members/:id/marriages
 * Remove ALL marriages from a member.
 */
app.delete('/api/family-members/:id/marriages', async (req, res) => {
  try {
    const { data } = await axios.delete(`${SPRING_BASE}/${req.params.id}/marriages`);
    res.json(data);
  } catch (err) { handleError(err, res); }
});

/**
 * PUT /api/family-members/:id/marriages/:index
 * Update a specific marriage by 0-based index.
 */
app.put('/api/family-members/:id/marriages/:index', async (req, res) => {
  try {
    const { data } = await axios.put(
      `${SPRING_BASE}/${req.params.id}/marriages/${req.params.index}`, req.body
    );
    res.json(data);
  } catch (err) { handleError(err, res); }
});

/**
 * DELETE /api/family-members/:id/marriages/:index
 * Remove a specific marriage by 0-based index.
 */
app.delete('/api/family-members/:id/marriages/:index', async (req, res) => {
  try {
    const { data } = await axios.delete(
      `${SPRING_BASE}/${req.params.id}/marriages/${req.params.index}`
    );
    res.json(data);
  } catch (err) { handleError(err, res); }
});

// ════════════════════════════════════════════════════════════════════════════
// PARENTS
// ════════════════════════════════════════════════════════════════════════════

/**
 * PUT /api/family-members/:memberId/father/:fatherId
 * Assign a father to a member. Also adds this member to the father's childrenIds.
 */
app.put('/api/family-members/:memberId/father/:fatherId', async (req, res) => {
  try {
    const { data } = await axios.put(
      `${SPRING_BASE}/${req.params.memberId}/father/${req.params.fatherId}`
    );
    res.json(data);
  } catch (err) { handleError(err, res); }
});

/**
 * PUT /api/family-members/:memberId/mother/:motherId
 * Assign a mother to a member. Also adds this member to the mother's childrenIds.
 */
app.put('/api/family-members/:memberId/mother/:motherId', async (req, res) => {
  try {
    const { data } = await axios.put(
      `${SPRING_BASE}/${req.params.memberId}/mother/${req.params.motherId}`
    );
    res.json(data);
  } catch (err) { handleError(err, res); }
});

// ════════════════════════════════════════════════════════════════════════════
// DETAILS — Contact, Documents, Personal
// ════════════════════════════════════════════════════════════════════════════

/**
 * PATCH /api/family-members/:id/contact
 * Update contact details (nested object).
 * Body: { "phoneNumber": "9876543210", "email": "ravi@example.com" }
 */
app.patch('/api/family-members/:id/contact', async (req, res) => {
  try {
    const { data } = await axios.patch(
      `${SPRING_BASE}/${req.params.id}/contact`, req.body
    );
    res.json(data);
  } catch (err) { handleError(err, res); }
});

/**
 * PATCH /api/family-members/:id/documents
 * Update identity documents (nested object).
 * Body: { "aadhaar": "1234-5678-9012", "pan": "ABCDE1234F" }
 */
app.patch('/api/family-members/:id/documents', async (req, res) => {
  try {
    const { data } = await axios.patch(
      `${SPRING_BASE}/${req.params.id}/documents`, req.body
    );
    res.json(data);
  } catch (err) { handleError(err, res); }
});

/**
 * PATCH /api/family-members/:id/personal
 * Update personal details.
 * Body: { gotram, occupation, placeOfBirth, birthDate, isLate, dod, type, name }
 * Note: use "birthDate" (not "dateOfBirth") in new schema.
 */
app.patch('/api/family-members/:id/personal', async (req, res) => {
  try {
    const { data } = await axios.patch(
      `${SPRING_BASE}/${req.params.id}/personal`, req.body
    );
    res.json(data);
  } catch (err) { handleError(err, res); }
});

// ════════════════════════════════════════════════════════════════════════════
// REMOVE FIELDS
// ════════════════════════════════════════════════════════════════════════════

/**
 * DELETE /api/family-members/:id/fields
 * Null out specific fields of a member record.
 * Protected: id, firstName, lastName, isRoot
 *
 * Removable fields (new schema):
 *   name, type, gender, gotram, birthDate, isLate, dod,
 *   placeOfBirth, occupation, contact, documents,
 *   marriages, fatherId, motherId, childrenIds
 *
 * Body: ["gotram", "occupation", "contact", "documents"]
 */
app.delete('/api/family-members/:id/fields', async (req, res) => {
  try {
    const { data } = await axios.delete(
      `${SPRING_BASE}/${req.params.id}/fields`, { data: req.body }
    );
    res.json(data);
  } catch (err) { handleError(err, res); }
});

// ── Start ─────────────────────────────────────────────────────────────────────
app.listen(PORT, () => {
  console.log(`✅ server.js running on http://localhost:${PORT}`);
  console.log(`   Proxying all requests → Spring Boot at ${SPRING_BASE}`);
  console.log('');
  console.log('  Routes:');
  console.log('  Tree:       GET  /api/family-members/tree/root          (204 if empty)');
  console.log('              POST /api/family-members/tree/root          (create root)');
  console.log('              GET  /api/family-members/tree/full          (full recursive tree)');
  console.log('              GET  /api/family-members/tree/node/:id      (node + children)');
  console.log('              GET  /api/family-members/tree/node/:id/children');
  console.log('              GET  /api/family-members/tree/subtree/:id   (recursive subtree)');
  console.log('              GET  /api/family-members/tree/ancestors/:id');
  console.log('              GET  /api/family-members/tree/path/:id      (root → node)');
  console.log('              GET  /api/family-members/tree/search?q=');
  console.log('  CRUD:       POST/GET/PUT/PATCH/DELETE /api/family-members');
  console.log('  Children:   GET/POST/DELETE /api/family-members/:id/children');
  console.log('  Marriages:  GET/POST/PUT/DELETE /api/family-members/:id/marriages');
  console.log('              PUT/DELETE /api/family-members/:id/marriages/:index');
  console.log('  Parents:    PUT /api/family-members/:id/father/:fatherId');
  console.log('              PUT /api/family-members/:id/mother/:motherId');
  console.log('  Details:    PATCH /api/family-members/:id/contact');
  console.log('              PATCH /api/family-members/:id/documents');
  console.log('              PATCH /api/family-members/:id/personal');
  console.log('  Fields:     DELETE /api/family-members/:id/fields');
});
