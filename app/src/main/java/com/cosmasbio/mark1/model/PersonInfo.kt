package com.cosmasbio.mark1.model

/**
 * 검사 대상자 정보.
 *
 * 개인정보이므로 파일명/로그에 절대 포함하지 않는다.
 * 로컬 저장은 Room(persons 테이블), 서버 저장 시에는 암호화된다.
 */
data class PersonInfo(
    val name: String = "",
    val dateOfBirth: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val organization: String = "",
) {
    val isEmpty: Boolean
        get() = name.isBlank()
}

/**
 * 대상자 정보 수정 화면(AddDiagnosisDetailsScreen)으로 넘길 때 쓰는 편집 대상.
 * personId가 있으면 그 사람 레코드를 그대로 갱신하고, 없으면(예: DiagnoseScreen 샘플)
 * 이름+생년월일로 기존 인물을 찾거나 새로 만든다.
 *
 * captureId가 있으면(검사 이력에서 열었다면), 저장 후 그 촬영을 저장/매칭된 인물과 다시 연결해서
 * 처음에 personId가 비어 있던 촬영도 다음에 "..."를 열면 방금 저장한 값이 그대로 보이게 한다.
 */
data class PersonEditTarget(
    val personId: String?,
    val captureId: String?,
    val info: PersonInfo,
)
