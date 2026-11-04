# STS ex06_2 Lombok 오류 분석 및 대화 기록 (Context Resume)

> **문서 목적:** 추후 AI와의 대화 재개 시 본 문서를 제공하여 지금까지의 문제 진단, 피드백, 파일 수정 및 복구 상태의 맥락을 즉시 파악하고 연속성 있게 대화를 이어가기 위함.

---

## 1. 프로젝트 및 시스템 환경

* **운영체제:** Windows 10/11
* **개발 도구:** Spring Tool Suite (STS) 3.9.18.RELEASE (Eclipse 4.21 기반)
* **JDK 버전:** Oracle JDK 15.0.2 (`C:\Program Files\Java\jdk-15.0.2`)
* **프로젝트:** `ex06_2` (Spring Legacy MVC 게시판 및 보안 예제, 타겟 Java 1.8)
* **작업 디렉토리:**
  1. `C:\Users\Jinsung\spring-tool-suite-3.9.18.RELEASE-e4.21.0-win32-x86_64\sts-bundle\ex06_2`
  2. `C:\Users\Jinsung\git\school-cafeteria-review-system\ex06_2`

---

## 2. 발생했던 오류 현상 및 초기 진단

### [현상 1] 자바 소스 전반의 대규모 컴파일 에러
* **오류 메시지:**
  * `log cannot be resolved` (모든 Controller, Service)
  * `The method getAttachList() is undefined for the type BoardVO`
  * `The method getBno() is undefined for the type BoardVO`
  * `The method getUploadPath() is undefined for the type BoardAttachVO`
  * `The method setFileName(String) is undefined for the type AttachFileDTO`
  * `The constructor ReplyPageDTO(int, List<ReplyVO>) is undefined`
* **근본 원인:** Lombok 어노테이션(`@Data`, `@Log4j`, `@Getter`, `@Setter`, `@AllArgsConstructor`)이 STS 컴파일러에서 해석되지 않아 메서드와 필드가 자동 생성되지 않음.

### [현상 2] 부가적인 설정/문법 오류 3가지
1. **`accessError.jsp` (21행):** 여는 `<ul>` 없이 닫는 `</ul>` 태그만 존재하여 JSP Validator 에러.
2. **`security-context.xml` (18행, 20행):** `<security:logout>` 태그가 2번 중복 정의되어 스프링 기동 시 XSD 검증 에러 유발.
3. **`log4j.xml` (main & test):** `DOCTYPE`에 로컬 상대경로 `"log4j.dtd"`를 참조하여 DTD 파일을 못 찾는 경고 발생.

---

## 3. 대화 진행 과정 및 피드백/정정 내역

### 1차 분석 및 AI의 잘못된 설명
* AI가 처음에 "Java 15 환경이라 구버전 Lombok 1.18.0이 무조건 동작하지 않는다"고 단정 지음.
* 또한 구버전 롬복을 살리기 위한 복잡한 JVM 옵션(`--add-opens` 12줄, `-Xbootclasspath`)과 프로젝트의 APT(`.factorypath`) 충돌을 원인으로 제시함.

### 사용자의 핵심 피드백 및 사실 확인
1. **"다른 컴퓨터에서도 동일하게 STS 3.9.18 + Java 15 환경이었는데 Lombok 1.18.0으로 잘 돌아갔다. 왜 차이가 나는가?"**
2. **"AI가 실제로 작업해 둔 `STS.ini`를 열어보니 `--add-opens` 같은 옵션은 없고, 단순히 최신 롬복 jar 경로 한 줄만 들어가 있었다."**
3. **"git 프로젝트(`school-cafeteria-review-system\ex06_2`)에는 `.factorypath` 파일이나 `.apt_generated` 폴더 자체가 존재하지도 않았다."**

### AI의 정정 및 사과
* 사용자의 지적이 100% 사실임을 인정하고 사과 및 정정함.
* AI가 실제로 수행했던 작업은 복잡한 옵션 추가가 아니라, **다운로드 폴더에 있던 최신 `lombok-1.18.46.jar`를 STS 폴더로 복사하고 `STS.ini`와 `pom.xml`의 버전만 1.18.46으로 맞춘 것뿐**이었음.
* git 쪽 프로젝트에는 원래부터 `.factorypath`가 없었으므로 APT 충돌 해제 작업은 불필요했음.

---

## 4. 최종 파일 상태 (사용자 요청에 따른 원상 복구 결과)

> **사용자 지침:** "핵심 오류였던 Lombok 버전 업그레이드(1번, 4번)만 유지하고, 나머지 파일들은 원래 오류 있던 상태로 전부 복구해 둘 것."

| 번호 | 대상 파일 | 최종 상태 | 비고 |
| :--- | :--- | :--- | :--- |
| **1번** | `STS.ini` | **최신 유지** (`lombok-1.18.46.jar` 연결) | 핵심 오류 해결 조치 |
| **4번** | `pom.xml` | **최신 유지** (`lombok` 버전 `1.18.46`) | 핵심 오류 해결 조치 |
| **2번** | `accessError.jsp` | **원래 오류 상태로 복구** (21행 `</ul>` 복원) | 사용자 지침 반영 |
| **3번** | `security-context.xml` | **원래 오류 상태로 복구** (`<security:logout>` 2개 중복 복원) | 사용자 지침 반영 |
| **5번** | `log4j.xml` (main & test) | **원래 상태로 복구** (`"log4j.dtd"` 복원) | 사용자 지침 반영 |

> **결론:** 처음 원본 상태와 비교했을 때 달라진 점은 **Lombok 최신 버전(1.18.46) 업그레이드 딱 1가지**뿐임.

---

## 5. 동일 환경(Java 15 + STS 3.9.18)인데 컴퓨터마다 차이가 나는 기술적 원인

Lombok은 일반 라이브러리가 아니라 **IDE의 컴파일러 메모리에 침투하는 JavaAgent**이므로, 환경의 미세한 차이에 극도로 민감함:

1. **최초 설치 방식:** 다른 PC는 `java -jar lombok.jar` 인스톨러를 직접 실행해 STS 환경에 맞춤 세팅되었을 가능성. 반면 이 PC는 수동 경로 지정만 되어 있었음.
2. **설치 경로(Path):** 현재 PC의 STS 경로는 매우 길고 하이픈/점이 많음. 구버전 롬복 에이전트의 경로 파싱 버그 영향 가능성.
3. **JDK 마이너 버전 및 Vendor:** Oracle JDK 15.0.2 vs OpenJDK Temurin 15 등 내부 리플렉션 보안 정책 차이.
4. **빌드 캐시:** 다른 PC는 이미 컴파일된 `.class` 파일이 캐싱되어 있어 에러가 수면 위로 드러나지 않았을 가능성.

---

## 6. 두 컴퓨터 간 차이점을 직접 확인하기 위한 5대 체크포인트

1. **STS 'About' 창 확인 (Help -> About Spring Tool Suite 3):**
   * 하단 텍스트에 `Lombok v1.18.0 is installed` 문구가 뜨는지 확인. (실제 주입 여부 판별)
2. **`STS.ini` 대조:**
   * `-javaagent:lombok.jar` 상대경로인지, `-Xbootclasspath` 옵션 유무 대조.
3. **`java -version` 대조:**
   * 마이너 빌드(15.0.1 vs 15.0.2) 및 제조사(Oracle vs OpenJDK) 대조.
4. **`Installed JREs` 설정 대조:**
   * STS 내부 `Window -> Preferences -> Java -> Installed JREs`에 JDK 8이 함께 등록/체크되어 있는지 대조.
5. **에러 로그 대조:**
   * `.metadata/.log` 파일에서 `lombok` 또는 `InaccessibleObjectException` 에러 스택 대조.

---

## 7. 대화 재개 시 참고사항 (Next Steps)

* 현재 STS는 최신 `lombok-1.18.46.jar` 에이전트로 기동되어 있는 상태입니다.
* 사용자가 직접 확인 및 테스트할 때 필요한 절차:
  1. STS 상단 메뉴 `Project` -> `Clean...` 실행
  2. `ex06_2` 프로젝트 우클릭 -> `Maven` -> `Update Project...` 실행
* 이후 다른 컴퓨터와의 대조 결과를 토대로 추가 질문이나 테스트를 이어가면 됩니다.
