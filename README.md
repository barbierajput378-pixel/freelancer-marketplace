# Freelancer Marketplace — Assignment 9 (3-Tier Web App)

Browser (index.html) → API (Node serverless functions in /api) → MySQL (Aiven)
Deployed on Vercel.

## STEP 1 — Create the free Aiven MySQL database

1. Go to https://aiven.io and sign up (free, no card needed for the free plan).
2. Click **Create service** → choose **MySQL**.
3. Pick the **free plan** and any cloud region close to you.
4. Name the service (e.g. `freelancer-db`) and click **Create service**.
5. Wait ~2-3 minutes for it to turn green ("Running").
6. Open the service → **Overview** tab → find **Connection Information**:
   - Host
   - Port
   - User (usually `avnadmin`)
   - Password
   - Default database name is `defaultdb` — you'll create your own below.

## STEP 2 — Load your schema, data, procedures & triggers

You need a MySQL client that supports multi-statement scripts with `DELIMITER`
(Aiven's built-in web console does NOT support `DELIMITER` reliably, so use
one of these instead):

**Option A — MySQL Workbench (recommended, has a GUI):**
1. Download MySQL Workbench, create a new connection using the Aiven host/port/user/password (enable SSL under the connection's SSL tab, point it to the downloaded `ca.pem` from Aiven's Overview page).
2. Open `setup.sql` (included in this project) in Workbench and run it (⚡ Execute).

**Option B — mysql CLI:**
```
mysql --host=<DB_HOST> --port=<DB_PORT> --user=avnadmin --password \
  --ssl-ca=ca.pem < setup.sql
```

This creates the `FreelancerMarketplace` database with all 5 tables, sample
data, 2 procedures, 2 functions, and 6 triggers — identical to Assignment 7.

## STEP 3 — Push this project to GitHub

```
cd freelancer-web
git init
git add .
git commit -m "Assignment 9: Freelancer Marketplace web app"
git branch -M main
git remote add origin <your-empty-github-repo-url>
git push -u origin main
```

(Create an empty repo on github.com first, then copy its URL into the command above.)

## STEP 4 — Deploy to Vercel

1. Go to https://vercel.com and sign in (use "Continue with GitHub").
2. Click **Add New → Project**.
3. Select the GitHub repo you just pushed.
4. Before clicking Deploy, open **Environment Variables** and add these
   (values from Aiven's Connection Information, Step 1):

   | Name | Value |
   |---|---|
   | `DB_HOST` | your Aiven host |
   | `DB_PORT` | your Aiven port |
   | `DB_USER` | avnadmin |
   | `DB_PASSWORD` | your Aiven password |
   | `DB_NAME` | FreelancerMarketplace |
   | `DB_SSL` | true |

5. Click **Deploy**. Wait ~1 minute.
6. Vercel gives you a live URL like `https://freelancer-marketplace-web.vercel.app`
   — open it. All 5 tabs (Clients, Freelancers, Jobs, Offers, Contracts) plus
   the Dashboard should load real data from your Aiven database.

## STEP 5 — Test it end-to-end before taking screenshots

- Add a record on any tab → click Refresh → confirm it appears.
- Click a row → form auto-fills → change a field → Update → confirm it changed.
- Delete a record → confirm it's gone.
- Open the Dashboard tab and run all 3 report buttons.

## For your Assignment 9 PDF submission

- Include this source code (all files in this project).
- Screenshot each of the 5 table tabs with data loaded, and the Dashboard tab.
- Screenshot an Add/Update/Delete action working (before & after).
- Include the live Vercel URL at the top of the PDF.

## Project structure
```
freelancer-web/
├── api/
│   ├── _db.js          (shared MySQL connection pool)
│   ├── _crud.js         (generic CRUD handler factory)
│   ├── clients.js
│   ├── freelancers.js
│   ├── jobs.js
│   ├── offers.js
│   ├── contracts.js
│   └── reports.js       (dashboard queries)
├── index.html            (frontend — tabs, forms, tables)
├── setup.sql             (schema + data + procedures + triggers)
├── package.json
├── .env.example
└── .gitignore
```
