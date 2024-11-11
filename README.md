# MAT.ZIP

- 배포 사이트 >> [http://mat-zip.site/](http://mat-zip.site/) </li>
- PPT >> [https://www.miricanvas.com/v/12yn3gi](https://www.miricanvas.com/v/12yn3gi) </li>
---
# 🍽️ **서비스 소개**
맛집 공유 서비스 MAT.ZIP ([실 사이트](http://mat-zip.site))입니다.
맛집 조회와 리뷰 작성, 어드민 사이트 등을 구현하였습니다.
- 역할
    
    
    | 팀장, 발표 | 관리자 페이지 (맛집, 유저, 카테고리, 해시태그 관리) | 배포 EC2 (ubuntu), RDS, DNS (Route53) | kakao map api |
    | --- | --- | --- | --- |
- 프로젝트
    
    
    | 기간 : 2024. 1. 23 ~ 2024. 3. 11 | 총 4인 (심채원, 유은겸, 김연수, 박선희) |
    | --- | --- |
## 👤 유저
1. 플랫폼이나 카카오를 통해 로그인/회원가입을 할 수 있다.
2. 마이페이지에서 유저 정보를 수정할 수 있다.
3. 사이트에 저장되어 있는 맛집들을 map 위의 마커나 리스트에서 조회한다.
4. 자신이 방문한 맛집의 리뷰를 작성한다.
5. 관심있는 맛집을 찜하여 이후에 해당 맛집 정보를 조회할 수 있다.
6. 사이트에 저장되어 있는 맛집들의 정보 수정 요청을 한다.
## 🧑‍💼 어드민
1. 유저에 어드민 권한을 부여할 수 있다.
2. 사이트에 등록된 맛집들을 등록하거나 정보(상태, 위치, 메뉴, 영업시간)를 수정, 삭제할 수 있다.
3. 사이트에 회원가입한 회원들의 정보를 조회하거나 관련 리뷰, 이미지에 대한 삭제할 수 있다.
4. 유저가 전송한 정보 수정 요청을 처리할 수 있다.
5. matzip 사이트의 홈 화면에 보여질 해시태그들을 설정할 수 있다.
## **🛠 사용기술**
### OS
- MacOS, Oracle XE
### IDE
- Intelli J
### **Frontend**
- Thymeleaf, HTML, CSS, Javascript, Axios
### **Backend**
- Spring boot, Spring security, JPA/Hibernate, Querydsl
### **Infra**
- Server: AWS EC2(ubuntu, Nginx), Route53
- DB: AWS RDS (Oracle)
- Storage: AWS S3
### API
- Kakao map API
- Excel api (Apache POI)
## **✍️ 담당 업무**
1. DB 설계 및 구현
2. AWS 서버 배포: EC2 (Nginx), RDS (Oracle), S3 기반
3. 페이지네이션 Util 구현
4. Spring Boot Framework 기반 웹 어플리케이션 구현 (관리자 페이지)
    1. 맛집, 리뷰, 해시태그, 카테고리 관리, 검색, 페이지네이션
### 🔗 Output
- AWS 구조도
![aws구조도matzip](https://github.com/JAVA-GIRLS/matzip/assets/114975208/0ed2b2c4-e5dd-4555-b38d-e78157aefc5d)
- github : [GitHub - JAVA-GIRLS/matzip](https://github.com/JAVA-GIRLS/matzip)
- 맛집 사이트
    - [사이트 바로가기](http://mat-zip.site)
    - 어드민 계정 이용
        - 아이디 : admin123
        - 비밀번호 : admin123!
- 발표 PPT 보기 : [matzip](https://www.miricanvas.com/v/12yn3gi)
    
---
# **💻 담당 개발 내역 상세**
### **✅ Admin 맛집 관리 기능**
- 맛집 등록 : kakao api 를 통해 해당 위치의 경도와 위도를 가지고 json 파일
로 응답받은 위치 리스트를 목록과 지도 위 마커로 해당 위치를 선택하여 해당 좌표를 저장
- 맛집 관리 리스트
    - thymeleaf를 이용하여 페이지네이션을 구현
    - 필터링, 정렬, 페이지네이션 기능을 넣었습니다.
    - 현재 적용된 필터, 정렬 조건대로 Excel 파일 생성하도록 구현
- 맛집 디테일 페이지
    - 맛집 위치정보, 영업시간, 메뉴 수정
    - 맛집 위치정보 수정의 경우, kakao map을 불러오는 부분을 모듈화하여 맛집 등록 페이지와 코드가 중복되지 않도록 구현
- 맛집 카테고리 관리
    - 카테고리 CRUD 구현
    - 유저가 조회하는 맛집 리스트 페이지에서 노출 순서를 드롭다운으로 변경할 수 있도록 구현
    - 카테고리를 선택하면 해당 카테고리별 레스토랑 리스트로 이동
### **✅ Admin 해시태그 관리 기능**
- 해시태그 리스트에서 검색과 필터 기능을 구현
- 체크박스를 이용하여 복수의 태그에 홈 노출 여부를 설정하거나, 부분삭제 가능
### **✅ Admin 요청사항 관리 기능**
- 사용자로부터 레스토랑에 대한 폐업이나 정보 수정 등의 요청을 받았을 때, 해당 레스토랑의 디테일 페이지로 이동하여 즉각적인 수정이 가능하도록 구현
- 검색과 필터 기능을 추가한 리스트를 구현
### **✅ Admin 멤버 관리 기능**
- 멤버 리스트 : 검색과 필터 기능을 구현
- 현재 적용된 필터, 정렬 조건대로 Excel 파일 다운받을 수 있도록 구현
- 멤버의 권한을 수정(어드민 권한 부여)하거나 탈퇴처리가 가능하도록 구현
- 멤버가 작성한 리뷰를 조회하고(페이지네이션), 리뷰 이미지를 삭제하거나 리뷰를
삭제할 수 있도록 구현
### **✅ FE : List 페이지네이션 util 구현**
- 페이지네이션는 어드민에서 5번 중복으로 사용
- 중복되는 코드를 줄이기 위 해 이를 처리하는 함수들을 추출하여 만들었고, 레스토랑 리스트(Thymeleaf 구현)를 제외한 나머지 페이지네이션들에서 적용
### **✅ AWS 서버 배포**
- Linux Shell Script와 Nginx를 통해 배포
- Linux Shell Script로 EC2 백 그라운드에서 서버를 실행시키고, Nginx로 http의 기본 포트로 80포트로 포트 포워딩할 수 있도록 설정
- 가비아에서 Domain 구입 후, 해당 도메인을 AWS Route53에 연결
- 팀 내에서 AWS IAM 계정을 만들어 권한 부여
- RDS를 Private Subnet으로 구성하여 같은 VPN 내에 있는 EC2에서만 접근할 수 있도록 구현
---
# 프로젝트 후기
- **[재사용성을 고려한 코드 작성]**
    - 프론트나 API 개발 시, 중복 코드를 메서드로 구현하여 압축할 수 있었습니다. 특히 프론트의 경우 페이지네이션을 util 함수로 계산하였는데, 페이지마다 현재 페이지를 저장하는 방식이 상이하여 (객체, 기본 자료형) 이를 분기로 나누어 구현하였습니다. 함수 구현을 통해 이후 해당 개발 과정이 단축될 수 있었습니다.
- **[반복된 수작업 배포를 통해 CI/CD의 중요성을 깨달은 프로젝트]**
    - 배포를 맡아 이를 진행하는 과정에서 다른 팀원들의 반복되는 코드 수정으로 소요되는 시간이 생각보다 길어졌습니다. 지속적 통합과 지속적 배포의 필요성을 몸소 체험했습니다.
- **[처음 구현하는 기능을 분석하고, 실 사용 경험이 있는 팀원과 소통하여 기능을 개선함]**
    - 어드민 페이지를 직접적으로 이용한 적이 없어 처음 작성했던 기능 구현 리스트에 부족함을 많이 느꼈습니다. 실제 CS 업무를 하던 팀원과 개선 사항이나 기존 구현 리스트의 수정 사항에 대해 이야기를 나누며 실질적인 사용자의 관점에서 개발할 수 있었습니다.
- **[프로젝트, 시험이 겹친 상황에서 우선순위를 부여하여 일정관리를 할 수 있던 경험]**
    - 수행해야할 여러가지 목표들이 있어 일정 분배에 어려움을 겪었으나, 우선순위를 정해 구현 일정에 맞추어 프로젝트를 관리할 수 있었습니다. 제게 주어진 일은 **팀장 (프로젝트 관리), 배포, 어드민 페이지 (구현해야할 기능의 수가 전체의 50% 이상), 정보처리기사 필기** 등이 있었습니다. 우선 팀과 함께 진행되는 프로젝트 목표가 우선이라고 생각했고, 팀원 별로 리소스 기준으로 구분한 업무를 분배하였고, 해당 리소스에서 우선순위를 고려하여 필수 구현 사항과 추가 구현 사항을 분류하였습니다. 팀장으로 팀의 진행 과정을 나누는 회의를 매일 리드하여 각자의 문제 상황을 공유했고, 해당 문제들을 포함하여 우선순위를 재배치하며 빠르게 해결될 수 있는 문제를 우선적으로 해결. 이런 식의 프로젝트 관리 방식은 서로에게 책임감을 일깨우며 개발할 수 있는 좋은 도구가 될 수 있었습니다.
- **[AWS 지식 공유를 한 경험]**
    - 하고 싶었던 AWS 배포를 맡게 되면서, 해당 경험을 원하고 있는 다른 팀원들과 과정을 나누는 것을 목표로 잡았습니다. 프로젝트 시작 시, AWS에 대해 작은 스터디를 꾸려 사용할 EC2, RDS, S3의 개념과 전반적인 이해를 할 수 있도록 했습니다. 또한 배포를 진행하며 해당 진행 과정을 따로 정리하고, 스터디를 진행하기 위한 자료를 제작했습니다.
