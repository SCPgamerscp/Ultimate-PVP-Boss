package com.ailingmeng.ultimatepvpboss.entity;

/** Dependency-free tests for the collision-loop termination policy. */
public final class TridentCollisionBudgetTest {
    public static void main(String[] args) {
        clientsNeverQuery();
        returningTridentsNeverQuery();
        serverQueriesAreBoundedAndReset();
        repeatedHitsCannotSpin();
        budgetsArePerProjectile();
        System.out.println("TridentCollisionBudget: 5 regression tests passed");
    }

    private static void clientsNeverQuery() {
        var budget = new TridentCollisionBudget();
        for (int tick = 0; tick < 200; tick++) {
            budget.beginTick();
            require(!budget.tryQuery(true, false), "Client outbound flight must not query");
            require(!budget.tryQuery(true, true), "Client Loyalty return must not query");
        }
    }

    private static void returningTridentsNeverQuery() {
        var budget = new TridentCollisionBudget();
        budget.beginTick();
        require(!budget.tryQuery(false, true), "No-physics server flight must not query");
    }

    private static void serverQueriesAreBoundedAndReset() {
        var budget = new TridentCollisionBudget();
        for (int tick = 0; tick < 200; tick++) {
            budget.beginTick();
            require(budget.tryQuery(false, false), "Outbound server flight must detect hits");
            require(!budget.tryQuery(false, false), "Repeated/reentrant query must stop");
        }
    }

    private static void repeatedHitsCannotSpin() {
        var budget = new TridentCollisionBudget();
        budget.beginTick();
        int queries = 0;
        // Simulate a hook leaving positive piercing and the same hittable entity.
        // A null second query must break AbstractArrow's repeating-hit loop.
        while (budget.tryQuery(false, false)) {
            require(++queries <= 1, "Collision loop must terminate despite repeated hits");
        }
        require(queries == 1, "Must preserve the first server hit");
    }

    private static void budgetsArePerProjectile() {
        var first = new TridentCollisionBudget();
        var second = new TridentCollisionBudget();
        first.beginTick();
        second.beginTick();
        require(first.tryQuery(false, false), "First projectile must query");
        require(second.tryQuery(false, false), "A volley must not share one global budget");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
