# 가상 폰 미리보기 사용법 (작업자용)

가상 폰(pixel_test, 안드로이드 14)이 이 컴퓨터에서 돌아간다. 화면 크기 1080x2400, 좌표는 전부 이 픽셀 기준이다. (이미지를 900x2000으로 축소해서 보여줄 때는 좌표에 1.2를 곱한다.)

```bash
bash tools/emu.sh boot            # 꺼져 있으면 켠다
./gradlew assembleDebug           # 앱 빌드
bash tools/emu.sh install         # 설치 (데이터 유지)
bash tools/emu.sh fresh           # 지우고 새로 설치 + 샘플 데이터 (위젯도 사라짐)
bash tools/emu.sh start           # 앱 실행
bash tools/emu.sh shot out.png    # 화면 캡처 -> Read 도구로 이미지를 본다 (렌더링에 3~4초 걸리니 start 뒤엔 잠깐 기다릴 것)
bash tools/emu.sh tap X Y         # 터치
bash tools/emu.sh swipe X1 Y1 X2 Y2 [ms]
bash tools/emu.sh pin month       # 위젯 추가: day | week | small | month
bash tools/emu.sh home / back
```

- 하단 탭 좌표(y=2232): 달력 x=126, 근무 종류 x=402, 근무 패턴 x=678, 위젯 안내 x=954
- 샘플 데이터: 근무 종류 4개(주간/오후/야간/휴무), 4조2교대 패턴, 오늘 메모 1개 등 (`seed`, `fresh`가 넣어준다)
- 위젯을 홈화면에 추가한 뒤 앱을 다시 설치하면 위젯이 새 코드로 갱신된다 (몇 초 걸림). 위젯 모양만 볼 때는 `home` 후 `shot`
- 디버그 전용 도우미(`app/src/debug/`)는 정식 앱에 들어가지 않는다
- 앱을 막 켠 직후의 첫 터치는 무시될 수 있고, 화면 캡처는 최대 4초 늦게 반영될 수 있다. 캡처가 이상하면 3~4초 뒤 다시 찍을 것
- 캡처 파일은 프로젝트 안이 아니라 임시 폴더(예: 작업 폴더의 scratchpad)에 저장할 것
