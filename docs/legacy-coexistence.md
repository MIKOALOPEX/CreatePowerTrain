# Legacy integrated Firefighting coexistence

Powertrain permits the legacy integrated Firefighting mod and the independent Firefighting mod to load alongside it. No incompatible-version declaration or startup refusal is introduced. Each mod keeps its own blocks, registrations and gameplay logic.

## Isolation

- Powertrain coupling SavedData uses createpowertrain_couplings and createpowertrain_fluid_coupling_couplings. It never casts a Firefighting SavedData object to its own class or takes over the other namespace.
- Synaxis mechanical/fluid features and constraint instance IDs belong to createpowertrain. Rope solver accessors and mixin-added symbols use distinct Powertrain names.
- Optional @Pseudo mixins target the legacy integrated classes only when present. Missing methods in older builds are optional (require = 0); no Firefighting compile/runtime dependency is introduced.
- A rope captured by either mod's hook cannot be captured again by the other. Both hooks continue to function independently on available ropes.
- During native Create network reads, both mods suppress their bridged contributions. Normal reads still receive both contributions. Scope nesting, exceptions and thread isolation are covered by standalone tests.
