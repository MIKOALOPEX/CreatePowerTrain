package com.mikoalopex.createpowertrain.integration.synaxis;

/** Loads the real dependency jar's API without starting Minecraft or a physics world. */
public final class SynaxisApiTest {
    public static void main(String[] args) {
        if (!CouplingPhysics.available()) throw new AssertionError("Welding API unavailable");
        System.out.println("Synaxis welding constructors, limits and constraint methods resolved");
    }
}
