# iOS App (Phase 1 placeholder)

The `shared` module produces an iOS framework that can be linked from an Xcode project.

## Build the shared framework

```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

The framework will be in `shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework`.

## Next steps (Phase 4+)

Create an Xcode project in this directory that:

1. Links the `shared` framework
2. Embeds the Compose Multiplatform UI (when added in Phase 3)
3. Provides platform-specific dependencies (e.g. StatsStore, onExit)

See the main [CROSS_PLATFORM_PLAN.md](../CROSS_PLATFORM_PLAN.md) for details.
