# RouteX — SE2030 Group Project (Group 13)

Web-based ride-hailing platform. Java 17, Jakarta Servlets 6 + JSP/JSTL,
JDBC, MySQL 8, Maven WAR, deployed to Tomcat 10.1+ — matching the System
Overview Diagram in the Proposal Report.

## Module ownership (matches your Lab04 use cases 1:1)

| # | Module | Owner | Student ID | Main files |
|---|--------|-------|------------|------------|
| 1 | Ride Booking & Fare Estimation | Moses A.C.N. | IT25102334 | `RideBookingServlet`, `strategy/*`, `booking.jsp` |
| 2 | Driver Matching & Dispatch | Abeygunawardhana S.N.J. | IT25100107 | `DriverDispatchServlet`, `DispatchService`, `DriverDao` |
| 3 | Live Trip Management | Halloluwa T.T. | IT25100099 | `LiveTripServlet`, `observer/*`, `trip.jsp`, `driver-trip.jsp` |
| 4 | Ratings, Reviews & Driver Performance | Gopiga A. | IT25102335 | `RatingServlet`, `RatingDao`, `rate.jsp` |
| 5 | Wallet & Rewards Management | Adikarie J.A.D.C. | IT25102349 | `WalletServlet`, `WalletDao`, `wallet.jsp` |
| 6 | Administration & Reporting | Nimnadi R.D.S. | IT25100122 | `AdminReportServlet`, `ReportDao`, `admin-dashboard.jsp` |

Shared infrastructure (not one person's "major function", built once for everyone):
`AuthServlet` / `AuthFilter` (login, register, logout, route protection),
`UserDao`, `UserFactory`, `DatabaseConnection`, `PasswordUtil`.

## Design patterns used (rubric asks for ≥2 — this has 4, each owned by a different module)

1. **Singleton** — `util/DatabaseConnection.java`. One instance manages the JDBC
   connection settings for the whole app; every DAO calls
   `DatabaseConnection.getInstance().getConnection()`.
2. **Factory Method** — `factory/UserFactory.java`. Builds the correct
   `Rider` / `Driver` / `Admin` object from a database row so the rest of
   the code never branches on role manually.
3. **Strategy** — `strategy/FareStrategy.java` + 3 implementations +
   `FareCalculator.java` (Ride Booking module). Each ride type
   (Standard/Premium/Pool) is its own pricing algorithm class.
4. **Observer** — `observer/RideObserver.java` + `RideSubject.java` +
   `NotificationObserver.java` (Live Trip Management module). Ride status
   changes and SOS alerts are broadcast to whoever is listening (currently
   the notifications table) without the trip code knowing who's listening.

When you're asked in the viva "where's your design pattern", point at the
class-level Javadoc comment at the top of each pattern file — it names the
pattern and explains why it's there.

## Database
Every module writes to real MySQL tables via JDBC (see `sql/schema.sql`).
No in-memory mocking — the "Database Connection" rubric line expects a
live, working connection, and this is one (via the Singleton above).

## How to run
1. Install JDK 17, MySQL 8, Maven, Tomcat 10.1+.
2. Run `sql/schema.sql` in MySQL (creates the `routex` database and all tables).
   Already have an older database from before vehicle types were added?
   Run `sql/migration_v2_vehicle_type.sql` instead of dropping your data.
3. (Optional) override DB credentials with environment variables:
   `ROUTEX_DB_URL`, `ROUTEX_DB_USER`, `ROUTEX_DB_PASS`
   — defaults to `jdbc:mysql://localhost:3306/routex`, user `root`, no password.
4. Build: `mvn clean package`
5. Deploy `target/routex.war` to Tomcat (copy into `webapps/`, or use the Tomcat manager).
6. Open `http://localhost:8080/routex/`.

## Creating your first Admin account
1. Register normally as a Rider or Driver.
2. In MySQL: `UPDATE users SET role='ADMIN' WHERE email='your@email.com';`
3. Log out and log back in — you'll land on the Admin dashboard.

## Suggested demo flow (Progress Evaluation, ~75% complete)
1. Register a Rider and a Driver.
2. Promote one account to Admin (see above); verify the Driver from the Admin dashboard.
3. Driver goes Online.
4. Rider books a ride (fare estimate shown instantly — Strategy pattern in action).
5. System auto-dispatches to the driver (Driver Matching & Dispatch); driver Accepts.
6. Driver: Arrived → Start Trip (simulated GPS starts pinging every 5s) → Complete Trip.
7. Rider's wallet is charged the fare and credited 50 reward points automatically.
8. Rider rates the driver; try triggering SOS mid-trip to see the Observer pattern notify the Admin.
9. Rider tops up / redeems wallet points.
10. Admin dashboard: view metrics, suspend/reactivate a user, log an operational report.

## Academic-project notes (match your Proposal's stated System Limitations)
- GPS/location data is simulated (`FareCalculator.simulateDistanceKm`, the
  JS in `driver-trip.jsp`) rather than pulled from a real mapping API.
- No real payment gateway — wallet balances are internal, simulated money.
- Fare estimation is deterministic prototype logic based on a hash of the
  pickup/drop-off text, not real road distance.
- Driver "proximity" matching is simulated as "highest-rated online,
  verified driver" since there's no live GPS feed to measure real distance.

## CRUD coverage
Full Create/Read/Update/Delete is implemented, but Delete is deliberately
restricted to records where deleting them is safe and realistic:
- **Admin Reports** (Nimnadi's module): Create, Read, Update (resolve), **Delete**.
- **Ratings & Reviews** (Gopiga's module): Create, Read (`/rider/reviews`
  shows what you've submitted), Update (recalculated average), **Delete**
  (retract your own review - `RatingDao.delete`, driver's average
  recalculates afterwards).
- **Rides, Users, Wallet Transactions**: Create/Read/Update only, no hard
  delete, on purpose - a ride-hailing/fintech system should never let a
  financial or trip record disappear (it's cancelled/suspended instead,
  preserving the audit trail). If asked in the viva why these aren't
  deletable, that's the answer: it's a design decision, not a gap.

## Known gaps / good talking points for "future improvements"
- `NotificationDao.findForUser()` exists but isn't surfaced in a JSP yet —
  a notifications bell/inbox UI would be a natural next iteration.
- No password-reset flow yet (only initial registration/login).
- No pagination on the admin tables — fine for a class demo, would need it at scale.
