package com.ailingmeng.ultimatepvpboss.entity;

/** A trident is not a piercing arrow: at most one entity query per server tick. */
final class TridentCollisionBudget {
    private boolean queried;

    void beginTick() {
        queried = false;
    }

    boolean tryQuery(boolean clientSide, boolean noPhysics) {
        if (clientSide || noPhysics || queried) return false;
        // Consume before entering modded collision code, including reentrant calls.
        queried = true;
        return true;
    }
}
