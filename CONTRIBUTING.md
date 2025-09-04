# Contributing Guide

이 문서는 `Ball-link-be` 저장소의 개발 규칙을 정의합니다.  
모든 팀원은 아래 가이드를 따라주세요.

---

## 1. 브랜치 전략

### 브랜치 유형
- **main**: 운영 배포용 브랜치
- **develop**: 개발 통합 브랜치
- **feature/**: 새로운 기능 개발
- **bugfix/**: 버그 수정
- **hotfix/**: 운영 긴급 수정
- **release/**: 배포 준비

### 브랜치 네이밍 규칙
- **기능 개발**
feature/{도메인}-{작업명}-{이슈번호}

  - 예: `feature/auth-login-5`

- **버그 수정**
bugfix/{도메인}-{버그내용}-{이슈번호}

  - 예: `bugfix/auth-token-refresh-12`

- **핫픽스**
hotfix/{작업명}-{이슈번호}

  - 예: `hotfix/deploy-error-21`

- **릴리즈**
release/v{버전}

  - 예: `release/v1.0.0`

---

## 2. 커밋 컨벤션 (Angular 스타일 권장)

<type>: <subject> (#이슈번호)

### type 종류
- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `docs`: 문서 수정
- `style`: 코드 포맷팅 (로직 변화 없음)
- `refactor`: 리팩토링
- `test`: 테스트 관련
- `chore`: 빌드/배포/기타 작업

### 예시
feat: add OAuth login API (#5)


fix: refresh token validation bug (#12)


docs: update README with setup guide

---

## 3. Pull Request 규칙
- PR 제목: `[FEAT] 로그인 API 추가 (#5)`
- PR 본문에 반드시 이슈 연결 키워드 사용:
Closes #5
- 리뷰어 최소 1명 지정 후 Merge 가능
- CI 통과 필수

---


## 4. 이슈 규칙
- 이슈 생성 시 템플릿 활용 (버그 / 기능 요청 등)
- 반드시 **Labels** 지정
- **Projects**는 수동 선택 ❌, PR에 `Closes #번호`로만 연결.
