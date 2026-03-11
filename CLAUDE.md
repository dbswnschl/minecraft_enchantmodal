# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

**Minecraft 1.21.11** 기반, **Forge 61.1.3** 버전을 사용하는 Java 모드 프로젝트. ForgeGradle 빌드 시스템 사용.

## Forge 문서 참조

최신 Forge 개발 문서는 `forge_docs/Documentation-1.21.x/docs/`에 위치. 모드 기능 구현 시 반드시 참조할 것.

주요 문서 경로:
- 시작 가이드 및 프로젝트 설정: `forge_docs/Documentation-1.21.x/docs/gettingstarted/`
- 등록 시스템 (DeferredRegister): `forge_docs/Documentation-1.21.x/docs/concepts/registries.md`
- 이벤트 시스템 및 라이프사이클: `forge_docs/Documentation-1.21.x/docs/concepts/events.md`, `lifecycle.md`
- 클라이언트/서버 사이드 로직: `forge_docs/Documentation-1.21.x/docs/concepts/sides.md`
- 데이터 제너레이션: `forge_docs/Documentation-1.21.x/docs/datagen/`

## 빌드 명령어

**주의**: Java 21 필수. 시스템 기본이 Java 25인 경우 JAVA_HOME 지정 필요:
```bash
JAVA_HOME="/c/Program Files/Java/jdk-21.0.10" PATH="/c/Program Files/Java/jdk-21.0.10/bin:$PATH" ./gradlew build
```

```bash
./gradlew build              # 모드 JAR 빌드 (출력: build/libs/)
./gradlew runClient          # 모드가 적용된 마인크래프트 클라이언트 실행
./gradlew runServer          # 모드가 적용된 데디케이티드 서버 실행
./gradlew runData            # 데이터 제너레이터 실행 (모델, 레시피, 태그 등)
./gradlew runGameTestServer  # 게임 테스트 실행
```

IDE 실행 구성 생성:
```bash
./gradlew genIntellijRuns    # IntelliJ IDEA
./gradlew genEclipseRuns     # Eclipse
./gradlew genVSCodeRuns      # VSCode
```

## 아키텍처 규칙

- **Java 21** 필수 (Eclipse Temurin 권장). Gradle 8.8 사용 (9.x는 ForgeGradle 미지원)
- **진입점**: `@Mod("modid")` 어노테이션이 붙은 메인 모드 클래스
- **모드 메타데이터**: `src/main/resources/META-INF/mods.toml` — 모드 ID, 의존성, 로더 버전 정의
- **로더**: `javafml`, loaderVersion은 `[61,)` (Forge 61.x 대응)
- **패키지 구조**: 역도메인 네이밍 사용 (예: `com.example.modid`), 기능별(`blocks/`, `items/`, `entities/`) 또는 피처별로 분류
- **클라이언트 전용 코드**: 반드시 별도의 `client` 패키지에 분리 (서버 크래시 방지)
- **등록**: `DeferredRegister<T>` 패턴 사용 — 정적 레지스터 생성, `.register()`로 항목 추가, 생성자에서 `BusGroup`에 연결
- **클래스 네이밍**: 타입을 접미사로 사용 — `PowerRingItem`, `OvenBlock`, `OvenMenu`, `OvenBlockEntity`

## MC 1.21.11 / Forge 61 API 주요 변경사항

문서(1.21.x 기준)와 실제 API(1.21.11)에 차이가 있으므로 주의:
- `ResourceLocation` → `Identifier` (`net.minecraft.resources.Identifier`)
- `ResourceKey.location()` → `ResourceKey.identifier()`
- `IEventBus` → `BusGroup` (`net.minecraftforge.eventbus.api.bus.BusGroup`)
- `FMLJavaModLoadingContext.getModEventBus()` → `.getModBusGroup()`
- `BusGroup.register(MethodHandles.lookup(), this)` 패턴으로 이벤트 리스너 등록
- `@SubscribeEvent` → `net.minecraftforge.eventbus.api.listener.SubscribeEvent`
- `NetworkRegistry.newSimpleChannel()` → `ChannelBuilder.named().simpleChannel()`
- `channel.registerMessage()` → `channel.messageBuilder(Class).encoder().decoder().consumerMainThread().add()`
- `NetworkEvent.Context` → `CustomPayloadEvent.Context`
- `PacketDistributor.PLAYER.with(() -> player)` → `.with(player)` (Supplier 제거)
- `channel.sendToServer()` → `channel.send(packet, PacketDistributor.SERVER.noArg())`
- `CommandSourceStack.hasPermission(int)` → `source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)` 등
- `FriendlyByteBuf.readResourceLocation()` → `.readIdentifier()` / `.writeIdentifier()`
- `player.server` (필드 접근 불가) → `((ServerLevel) player.level()).getServer()`
- `registryAccess().registryOrThrow()` → `.lookupOrThrow()`
- `Registry.getHolder(ResourceKey)` → `Registry.get(Identifier)` (Optional<Holder.Reference> 반환)
- `Registry.holders()` → `Registry.entrySet()` (Set<Map.Entry<ResourceKey, T>> 반환)
- GUI: `mouseClicked(double, double, int)` → `mouseClicked(MouseButtonEvent, boolean)`
- GUI Entry: `render(...)` → `renderContent(GuiGraphics, int mouseX, int mouseY, boolean isHovered, float partialTick)`
- Entry 위치: `getContentX()`, `getContentY()`, `getContentWidth()` 등 사용

## 버전 형식

`MCVERSION-MAJORMOD.MAJORAPI.MINOR.PATCH` 형식 준수 (예: `1.21.11-1.0.0.0`)

## 핵심 패턴

- 게임 오브젝트(블록, 아이템 등)는 반드시 `DeferredRegister`와 `RegistryObject`를 통해 등록 — 레지스트리 항목을 직접 인스턴스화하지 말 것
- **모드 이벤트 버스** (`BusGroup`): 라이프사이클 이벤트(`FMLCommonSetupEvent`, `FMLClientSetupEvent`) 및 등록에 사용
- **Forge 이벤트 버스** (`MinecraftForge.EVENT_BUS`): 인게임 이벤트에 사용
- 데이터 제너레이션 프로바이더는 `DataProvider`를 상속하며 `GatherDataEvent`에서 등록
- 네트워크 패킷은 `ChannelBuilder` + `SimpleChannel`을 사용하여 클라이언트-서버 통신 처리
