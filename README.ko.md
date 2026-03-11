# Enchant Modal

마인크래프트 Forge 모드 — GUI를 통해 아이템에 원하는 인챈트를 자유롭게 구성할 수 있습니다.

## 기능

- **`/enchantmodal` 명령어** — 손에 든 아이템의 인챈트 설정 화면을 엽니다
- **전체 인챈트 목록** — 등록된 모든 인챈트를 ON/OFF 토글로 선택
- **레벨 조절** — 1~255 범위에서 인챈트 레벨 조절 가능
- **다국어 지원** — 마인크래프트 언어 설정에 따라 자동 반영 (한국어, 영어)

## 요구 사항

- Minecraft 1.21.11
- Forge 61.1.3+
- Java 21

## 설치

[Releases](https://github.com/dbswnschl/minecraft_enchantmodal/releases)에서 모드 JAR 파일을 다운로드하여 `mods/` 폴더에 넣으세요.

## 사용법

1. 인챈트할 아이템을 주 손에 들기
2. `/enchantmodal` 명령어 실행 (관리자 권한 필요)
3. 인챈트를 ON/OFF하고 레벨 조절
4. **적용** 버튼으로 저장, **취소** 버튼으로 닫기

## 빌드

```bash
# Java 21 필수
./gradlew build
```

결과물은 `build/libs/`에 생성됩니다.

## 라이선스

MIT
