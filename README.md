# Restaurant Management Game - Stage 1

A turn-based restaurant management game with a command-line interface, written in plain
Java (no external libraries). You are the manager: you decide which customers and orders
get your limited staff's attention across 18 turns.

This directory is the **Stage 1 (basic operation)** version only. It contains no Stage 2
or Stage 3 features: regular customers, the three single menu items, the Waiter/Chef/
Cashier and the full turn sequence with both endings.

## Build and run

Requires a JDK (Java 17 or newer) on the `PATH`.

```bash
./build.sh                 # compiles everything to ./out
./run.sh                   # play interactively (default seed)
./run.sh 12345             # play interactively with a fixed seed (reproducible)
./run.sh --auto            # scripted "sensible manager" run -> VICTORY
./run.sh --auto-wait       # scripted "do nothing" run -> DEFEAT
```

If your JDK is not `javac` on the PATH, point the build at it:

```bash
JAVAC=/path/to/javac ./build.sh
```

Manual equivalent:

```bash
javac -d out $(find src/main/java -name '*.java')
java -cp out restaurant.Main
```

## The base setting

| Setting | Value |
| :-- | :-- |
| Turns | 18 (always all 18) |
| Tables | 2 |
| Staff | Waiter x1, Chef x1, Cashier x1 |
| Arrivals | turns 1, 4, 7, 10, 13, 16 (six regular customers) |
| Objective | >= 4 paid customers **and** revenue >= $45.00 **and** average satisfaction of paid customers >= 60 |

Menu: Salad $9.00 / 1 unit, Burger $15.00 / 1 unit, Pasta $18.00 / 2 units.

## How a turn works

1. Start of turn: the Section 3.1 state display.
2. Arrivals: the scheduled customer joins the queue with satisfaction 100.
3. Manager choices: one Waiter task (seat / take order / serve READY / wait) and one
   Chef task (advance one order / wait). Illegal choices are explained and re-prompted
   without spending the staff task.
4. Kitchen and serving: the Waiter has already acted; the Chef advances its order. An
   order becomes READY on its last preparation unit. A dish that becomes READY this turn
   can only be served from the next turn.
5. Eating and payment: a customer served on an earlier turn finishes eating and becomes
   READY_TO_PAY; the Cashier collects at most one bill (oldest first) from a customer that
   was already READY_TO_PAY before this turn.
6. Waiting and abandonment: every waiting or seated customer loses 8 satisfaction; at 0
   they leave without paying and their order is cancelled and table freed.
7. End of turn: the Section 3.5 summary is shown.

## Design

Three layers (same shape as the later stages):

* `restaurant.boundary` - `GameUI`, `ConsoleUI`, `AutoUI`. Collects decisions and prints
  state only. No game rules here.
* `restaurant.control` - `GameController`, `ArrivalSchedule`, the manager command records
  and the result records. Owns the turn sequencing only.
* `restaurant.domain` - `Customer` (+ `RegularCustomer`), `MenuItem`, `Menu`, `Order`,
  `Table`, `WaitingQueue`, `Staff` (+ `Waiter`/`Chef`/`Cashier`), `Restaurant` and the
  enums. All rules live here.

Seams that Stage 2 and Stage 3 will fill in without touching the controller:

* `Customer.chooseItem` and `Customer.satisfactionLossPerTurn` are polymorphic, so new
  customer types are added as subclasses rather than type checks in the controller.
* The `GameUI` boundary means a different interface or a head-less run changes no rule.

Documented Stage 1 rule: a customer's order is chosen uniformly at random from the menu
using the seeded random source, so a given seed reproduces the same orders.

## Evidence runs

* Victory (scripted sensible policy): `./run.sh --auto` (prints RESULT: VICTORY, exit 0).
* Defeat (scripted idle policy): `./run.sh --auto-wait` (prints RESULT: DEFEAT, exit 1).
