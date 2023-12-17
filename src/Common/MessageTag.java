package Common;

public enum MessageTag {
    OKAY,
    FAIL,


    ACCESS, //접속
    CROOM,  //방 생성
    VROOM,  //방 목록
    UROOM,  //방 인원
    EROOM,  //방 입장
    CUSER,  //접속 유저
    READY,  //레디
    START,
    CREDY,  //레디 체
    PEXIT,
    REXIT,


    ////////////////////////////////////////////////////////////////////////////
    //게임 관련
    CTURN,  //턴 체인지
    FCARD,  //바닥 카드
    CCRAD,  //바닥 카드 카운트
    PBELL,  //벨 누름
    DCARD,  //카드 놓기
    SBELL,  //벨 누르기 성공
    FBELL,  //벨 누르기 실패
    GWAIT,
    GDEAD,  //게임 탈락
    GEXIT,
    GEND,
    WIN,
    LOSE;
}
