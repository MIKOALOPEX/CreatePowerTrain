# Legacy integrated Firefighting coexistence

Powertrain permits the legacy integrated Firefighting mod and the independent Firefighting mod to load alongside it. No incompatible-version declaration or startup refusal is introduced. Each mod keeps its own blocks, registrations and gameplay logic.

## Isolation

- Powertrain coupling SavedData uses createpowertrain_couplings and createpowertrain_fluid_coupling_couplings. It never casts a Firefighting SavedData object to its own class or takes over the other namespace.
- Synaxis mechanical/fluid features and constraint instance IDs belong to createpowertrain. Rope solver accessors and mixin-added symbols use distinct Powertrain names.
- Optional @Pseudo mixins target the legacy integrated classes only when present. Missing methods in older builds are optional (require = 0); no Firefighting compile/runtime dependency is introduced.
- A rope captured by either mod's hook cannot be captured again by the other. Both hooks continue to function independently on available ropes.
- During native Create network reads, both mods suppress their bridged contributions. Normal reads still receive both contributions. Scope nesting, exceptions and thread isolation are covered by standalone tests.

## Validation and limits

The 0.2.7-beta, 0.2.8-beta and 0.2.9-beta integrated JARs were checked for the extra/update/writeLocalNetwork/canCapture signatures. The Java build and standalone regression suite passed, including separate SavedData names and foreign-namespace rejection.

No Minecraft client/server or real world was opened. Mixin application and mixed-mod physics still require an in-game test with compatible Create/Sable/Simulated/Synaxis dependencies. Earlier integrated builds without these classes skip the optional paths; they are not all runtime-tested. This does not guarantee arbitrary older dependency combinations or repair existing damaged physics data.

No source world is scanned or rewritten by this change. Existing legacy SavedData is not renamed or claimed by Powertrain. No artifacts have been uploaded to GitHub.
