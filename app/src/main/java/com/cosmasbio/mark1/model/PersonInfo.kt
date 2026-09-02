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
